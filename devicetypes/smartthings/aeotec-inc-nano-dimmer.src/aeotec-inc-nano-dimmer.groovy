/**
 *
 *  Nano Dimmer (Aeotec Inc)
 *   
 *	github: Eric Maycock (erocm123) -> Converted to Nano Dimmer custom device handler
 *	email: erocmail@gmail.com / ccheng@aeon-labs.com (Modified Code)
 *	Date: 2018-01-02 3:07PM
 *	Copyright Eric Maycock
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
metadata {
	definition (name: "Aeotec Inc Nano Dimmer", namespace: "SmartThings", author: "Yu Chang Mang") {
		capability "Actuator"
		capability "Switch"
		capability "Switch Level"
		capability "Refresh"
		capability "Configuration"
		capability "Sensor"
        capability "Polling"
        capability "Energy Meter"
        capability "Power Meter"
        capability "Button"
        capability "Health Check"
        
        attribute   "needUpdate", "string"

        fingerprint mfr: "0086", prod: "0103", model: "006F", deviceJoinName: "Aeon Nano Dimmer"
		fingerprint deviceId: "0x1101", inClusters: "0x5E,0x25,0x27,0x32,0x81,0x71,0x2C,0x2B,0x70,0x86,0x72,0x73,0x85,0x59,0x98,0x7A,0x5A"
        
	}
    
    preferences {
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		generate_preferences(configuration_model())
    }

	simulator {

	}

	tiles(scale: 2){
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
           		attributeState "statusText", label:'${currentValue}'       		
            }
	    }
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset kWh', action:"reset"
		}
        standardTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "NO" , label:'', action:"configuration.configure", icon:"st.secondary.configure"
            state "YES", label:'', action:"configuration.configure", icon:"https://github.com/erocm123/SmartThingsPublic/raw/master/devicetypes/erocm123/qubino-flush-1d-relay.src/configure@2x.png"
        }


		main "switch"
		details (["switch", "power", "energy", "refresh", "configure", "reset"])
	}
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
	    result = createEvent(descriptionText:description, isStateChange:true)
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x84: 1, 0x98: 1, 0x56: 1, 0x60: 3])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
    
    def statusTextmsg = ""
    if (device.currentState('power') && device.currentState('energy')) statusTextmsg = "${device.currentState('power').value} W ${device.currentState('energy').value} kWh"
    sendEvent(name:"statusText", value:statusTextmsg, displayed:false)
    
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    logging("BasicReport: $cmd")
    def events = []
	if (cmd.value == 0) {
		events << createEvent(name: "switch", value: "off")
	} else if (cmd.value == 255) {
		events << createEvent(name: "switch", value: "on")
	} else {
		events << createEvent(name: "switch", value: "on")
        events << createEvent(name: "switchLevel", value: cmd.value)
	}
    
    def request = update_needed_settings()
    
    if(request != []){
        return [response(commands(request)), events]
    } else {
        return events
    }
}

def buttonEvent(button, value) {
    logging("buttonEvent() Button:$button, Value:$value")
	createEvent(name: "button", value: value, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $value", isStateChange: true)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	logging(cmd)
	dimmerEvents(cmd)
}

def dimmerEvents(physicalgraph.zwave.Command cmd) {
	logging(cmd)
	def result = []
	def value = (cmd.value ? "on" : "off")
	def switchEvent = createEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")
	result << switchEvent
	if (cmd.value) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
	if (switchEvent.isStateChange) {
		result << response(["delay 3000", zwave.meterV2.meterGet(scale: 2).format()])
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	logging("AssociationReport $cmd")
    state."association${cmd.groupingIdentifier}" = cmd.nodeId[0]
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x31: 2, 0x32: 3, 0x70: 1])
	if (encapsulatedCommand) {
		state.sec = 1
		def result = zwaveEvent(encapsulatedCommand)
		result = result.collect {
			if (it instanceof physicalgraph.device.HubAction && !it.toString().startsWith("9881")) {
				response(cmd.CMD + "00" + it.toString())
			} else {
				it
			}
		}
		result
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    logging("Unhandled Z-Wave Event: $cmd")
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	logging(cmd)
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
        	//log.debug "kWh Returned"
			return createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
		} else if (cmd.scale == 1) {
			return createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
		} else if (cmd.scale == 2) {
        	//log.debug "Watt Returned"
			return createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
		} else {
			return createEvent(name: "electric", value: cmd.scaledMeterValue, unit: ["pulses", "V", "A", "R/Z", ""][cmd.scale - 3])
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd){
    logging("SensorMultilevelReport: $cmd")
	def map = [:]
	switch (cmd.sensorType) {
		case 4:
			map.name = "power"
            map.value = cmd.scaledSensorValue.toInteger().toString()
            map.unit = cmd.scale == 1 ? "Btu/h" : "W"
            break
		default:
			map.descriptionText = cmd.toString()
	}
	createEvent(map)
}

def on() {
	commands([zwave.basicV1.basicSet(value: 0xFF), zwave.basicV1.basicGet()])
}

def off() {
	commands([zwave.basicV1.basicSet(value: 0x00), zwave.basicV1.basicGet()])
}

def refresh() {
   	logging("$device.displayName refresh()")

    def cmds = []
    if (state.lastRefresh != null && now() - state.lastRefresh < 5000) {
        logging("Refresh Double Press")
        def configuration = parseXml(configuration_model())
        configuration.Value.each
        {
            if ( "${it.@setting_type}" == "zwave" ) {
                cmds << zwave.configurationV1.configurationGet(parameterNumber: "${it.@index}".toInteger())
            }
        } 
        cmds << zwave.firmwareUpdateMdV2.firmwareMdGet()
    } else {
        cmds << zwave.meterV2.meterGet(scale: 0)
        cmds << zwave.meterV2.meterGet(scale: 2)
	    cmds << zwave.basicV1.basicGet()
    }

    state.lastRefresh = now()
    
    commands(cmds)
}

def ping() {
   	logging("$device.displayName ping()")

    def cmds = []

    cmds << zwave.meterV2.meterGet(scale: 0)
    cmds << zwave.meterV2.meterGet(scale: 2)
	cmds << zwave.basicV1.basicGet()

    commands(cmds)
}

def setLevel(value) {
	if (state.debug) log.debug "setting level to ${value} on ${device.displayName}"
	def valueaux = value as Integer
	def level = Math.min(valueaux, 99)
	def request = [
		zwave.basicV1.basicSet(value: level),
		zwave.switchMultilevelV1.switchMultilevelGet()
		]
	commands(request)	
}

def setLevel(value, duration) {
	def valueaux = value as Integer
	def level = Math.min(valueaux, 99)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	def request = [
		zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration)
		]
	commands(request)	
}


def updated()
{
    state.enableDebugging = settings.enableDebugging
    logging("updated() is being called")
    sendEvent(name: "checkInterval", value: 2 * 30 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    state.needfwUpdate = ""
    
    def cmds = update_needed_settings()
    
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    
    response(commands(cmds))
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=1500) {
	delayBetween(commands.collect{ command(it) }, delay)
}

def generate_preferences(configuration_model)
{
    def configuration = parseXml(configuration_model)
   
    configuration.Value.each
    {
        switch(it.@type)
        {   
            case ["byte","short","four"]:
                input "${it.@index}", "number",
                    title:"${it.@label}\n" + "${it.Help}",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "list":
                def items = []
                it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                input "${it.@index}", "enum",
                    title:"${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}",
                    options: items
            break
            case "decimal":
               input "${it.@index}", "decimal",
                    title:"${it.@label}\n" + "${it.Help}",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "boolean":
               input "${it.@index}", "boolean",
                    title:"${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
        }  
    }
}


def update_current_properties(cmd)
{
    def currentProperties = state.currentProperties ?: [:]
    
    currentProperties."${cmd.parameterNumber}" = cmd.configurationValue

    if (settings."${cmd.parameterNumber}" != null)
    {
        if (settings."${cmd.parameterNumber}".toInteger() == convertParam("${cmd.parameterNumber}".toInteger(), cmd2Integer(cmd.configurationValue)))
        {
            sendEvent(name:"needUpdate", value:"NO", displayed:false, isStateChange: true)
        }
        else
        {
            sendEvent(name:"needUpdate", value:"YES", displayed:false, isStateChange: true)
        }
    }

    state.currentProperties = currentProperties
}

def update_needed_settings()
{
    def cmds = []
    def currentProperties = state.currentProperties ?: [:]
     
    def configuration = parseXml(configuration_model())
    def isUpdateNeeded = "NO"
    
    if(!state.needfwUpdate || state.needfwUpdate == ""){
       logging("Requesting device firmware version")
       cmds << zwave.firmwareUpdateMdV2.firmwareMdGet()
    }   
    if(!state.association1 || state.association1 == "" || state.association1 == "1"){
       logging("Setting association group 1")
       cmds << zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)
       cmds << zwave.associationV2.associationGet(groupingIdentifier:1)
    }
    if(!state.association2 || state.association2 == "" || state.association1 == "2"){
       logging("Setting association group 2")
       cmds << zwave.associationV2.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId)
       cmds << zwave.associationV2.associationGet(groupingIdentifier:2)
    }
   
    configuration.Value.each
    {     
        if ("${it.@setting_type}" == "zwave"){
            if (currentProperties."${it.@index}" == null)
            {
                if (device.currentValue("currentFirmware") == null || "${it.@fw}".indexOf(device.currentValue("currentFirmware")) >= 0){
                    isUpdateNeeded = "YES"
                    logging("Current value of parameter ${it.@index} is unknown")
                    cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
                }
            }
            else if (settings."${it.@index}" != null && convertParam(it.@index.toInteger(), cmd2Integer(currentProperties."${it.@index}")) != settings."${it.@index}".toInteger())
            { 
                if (device.currentValue("currentFirmware") == null || "${it.@fw}".indexOf(device.currentValue("currentFirmware")) >= 0){
                    isUpdateNeeded = "YES"

                    logging("Parameter ${it.@index} will be updated to " + settings."${it.@index}")
                    def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}".toInteger())
                    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
                    cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
                }
            } 
        }
    }
    
    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)
    return cmds
}

/**
* Convert 1 and 2 bytes values to integer
*/
def cmd2Integer(array) { 

switch(array.size()) {
	case 1:
		array[0]
    break
	case 2:
    	((array[0] & 0xFF) << 8) | (array[1] & 0xFF)
    break
    case 3:
    	((array[0] & 0xFF) << 16) | ((array[1] & 0xFF) << 8) | (array[2] & 0xFF)
    break
	case 4:
    	((array[0] & 0xFF) << 24) | ((array[1] & 0xFF) << 16) | ((array[2] & 0xFF) << 8) | (array[3] & 0xFF)
	break
    }
}

def integer2Cmd(value, size) {
	switch(size) {
	case 1:
		[value]
    break
	case 2:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        [value2, value1]
    break
    case 3:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        [value3, value2, value1]
    break
	case 4:
    	def short value1 = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        def short value4 = (value >> 24) & 0xFF
		[value4, value3, value2, value1]
	break
	}
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
     update_current_properties(cmd)
     logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'")
}

def zwaveEvent(physicalgraph.zwave.commands.firmwareupdatemdv2.FirmwareMdReport cmd){
    logging("Firmware Report ${cmd.toString()}")
    def firmwareVersion
    switch(cmd.checksum){
       case "3281":
          firmwareVersion = "3.08"
       break;
       default:
          firmwareVersion = cmd.checksum
    }
    state.needfwUpdate = "false"
    updateDataValue("firmware", firmwareVersion.toString())
    createEvent(name: "currentFirmware", value: firmwareVersion)
}

def configure() {
    state.enableDebugging = settings.enableDebugging
    logging("Configuring Device For SmartThings Use")
    def cmds = []

    cmds = update_needed_settings()
    
    if (cmds != []) commands(cmds)
}

def convertParam(number, value) {
	switch (number){
    	case 201:
        	if (value < 0)
            	256 + value
        	else if (value > 100)
            	value - 256
            else
            	value
        break
        case 202:
        	if (value < 0)
            	256 + value
        	else if (value > 100)
            	value - 256
            else
            	value
        break
        case 203:
            if (value < 0)
            	65536 + value
        	else if (value > 1000)
            	value - 65536
            else
            	value
        break
        case 204:
        	if (value < 0)
            	256 + value
        	else if (value > 100)
            	value - 256
            else
            	value
        break
        default:
        	value
        break
    }
}

private def logging(message) {
    if (state.enableDebugging == null || state.enableDebugging == "true") log.debug "$message"
}


def configuration_model()
{
'''
<configuration>
	  <Value type="list" byteSize="1" index="4" label="過熱保護" min="0" max="1" value="0" setting_type="zwave" fw="">
        <Help>
        如果溫度超過100℃，輸出負載將在30秒後自動關閉。
            0 - 關閉
            1 - 開啟
            Range: 0~1
            Default: 0 (Previous State)
        </Help>
            <Item label="關閉" value="0" />
            <Item label="開啟" value="1" />
      </Value>
      <Value type="list" byteSize="1" index="20" label="停電後的Nano" min="0" max="2" value="0" setting_type="zwave" fw="">
        <Help>
        配置停電後重新上電後的輸出負載狀態。
            0 - 停電前的最後狀態。
            1 - Always ON
            2 - Always OFF
            Range: 0~2
            Default: 0 (Previous State)
        </Help>
            <Item label="停電前的最後狀態。" value="0" />
            <Item label="Always On" value="1" />
            <Item label="Always Off" value="2" />
      </Value>
      <Value type="list" byteSize="1" index="80" label="即時通知" min="0" max="4" value="0" setting_type="zwave" fw="">
        <Help>
        當輸出負載狀態發生變化時，發送到組關聯＃1的狀態更改通知報告。 通常用於立即將狀態更新到網關。
            0 - Nothing
            1 - Hail CC (使用更多頻寬)
            2 - Basic Report CC (ON / OFF樣式狀態報告)
            3 - Multilevel Switch Report (用於調光器狀態報告)
            4 - Hail CC 當使用外部開關來改變任一負載的狀態時。
            Range: 0~4
            Default: 0 (Previous State)
        </Help>
            <Item label="None" value="0" />
            <Item label="Hail CC" value="1" />
            <Item label="Basic Report CC" value="2" />
            <Item label="Multilevel Switch Report" value="3" />
            <Item label="Hail when External Switch used" value="4" />
      </Value>
      <Value type="list" byteSize="1" index="81" label="使用S1開關發送通知" min="0" max="1" value="1" setting_type="zwave" fw="">
        <Help>
       設置在使用外部交換機1切換負載時將關聯組3中的關聯節點發送哪個通知。
            0 = Send Nothing
            1 = Basic Set CC.
            Range: 0~1
            Default: 1 (Previous State)
        </Help>
            <Item label="Nothing" value="0" />
            <Item label="Basic Set CC" value="1" />
      </Value>
      <Value type="list" byteSize="1" index="82" label="使用S2開關發送通知" min="0" max="1" value="1" setting_type="zwave" fw="">
        <Help>
        設置在使用外部交換機2切換負載時將關聯組4中的關聯節點發送哪個通知。
            0 = Send Nothing
            1 = Basic Set CC.
            Range: 0~1
            Default: 1 (Previous State)
        </Help>
            <Item label="Nothing" value="0" />
            <Item label="Basic Set CC" value="1" />
      </Value>
      <Value type="list" byteSize="1" index="90" label="門檻 Enable/Disable" min="0" max="1" value="1" setting_type="zwave" fw="">
        <Help>
       		Enables/disables parameter 91 and 92 below:
            0 = 關閉
            1 = 開啟
            Range: 0~1
            Default: 1 (Previous State)
        </Help>
            <Item label="關閉" value="0" />
            <Item label="開啟" value="1" />
      </Value>
      <Value type="byte" byteSize="4" index="91" label="瓦特門檻" min="0" max="60000" value="25" setting_type="zwave" fw="">
        <Help>
       		此處的值代表要發送的REPORT的最小功率變化（以瓦特數表示）（有效值0-60000）。 
            Range: 0~60000
            Default: 25 (Previous State)
        </Help>
      </Value>
      <Value type="byte" byteSize="1" index="92" label="kWh 門檻" min="0" max="100" value="5" setting_type="zwave" fw="">
        <Help>
       		此處的值代表要發送REPORT的瓦數百分比（以百分比％表示）的最小變化。 
            Range: 0~100
            Default: 5 (Previous State)
        </Help>
      </Value>
      <Value type="byte" byteSize="4" index="101" label="(Group 1) 定時自動報告" min="0" max="15" value="12" setting_type="zwave" fw="">
        <Help>
       		將傳感器報告設置為電壓或電流、瓦特、kWh。
            Value Identifiers-
                1 = Voltage
                2 = Current
                4 = Watt
                8 = kWh
            Example: 如果您只需要瓦特和千瓦時報告，請將瓦特和千瓦時的價值標識符相加。 8 + 4 = 15，因此在此設置中輸入12會給您提供瓦特+ kWh報告。
            Range: 0~15
            Default: 12 (Previous State)
        </Help>
      </Value>
      <Value type="byte" byteSize="4" index="102" label="(Group 2) 定時自動報告" min="0" max="12" value="0" setting_type="zwave" fw="">
        <Help>
       		將傳感器報告設置為電壓或電流、瓦特、kWh。
            Value Identifiers-
                1 = Voltage
                2 = Current
                4 = Watt
                8 = kWh
            Example: 如果您只需要電壓和電流報告，請將電壓+電流的值標識符相加。 1 + 2 = 3，因此在此設置中輸入3將給您電壓+電流報告（如果設置）。
            Range: 0~15
            Default: 0 (Previous State)
        </Help>
      </Value>
      <Value type="byte" byteSize="4" index="103" label="(Group 3) 定時自動報告" min="0" max="15" value="0" setting_type="zwave" fw="">
        <Help>
       		將傳感器報告設置為電壓或電流、瓦特、kWh。
            Value Identifiers-
                1 = Voltage
                2 = Current
                4 = Watt
                8 = kWh
            Example: 如果您想要報告所有值，請將電壓+電流+瓦特+ kWh的值標識符相加。 1 + 2 + 4 + 8 = 15，因此在此設置中輸入15會給您設置電壓+電流+瓦特+千瓦時報告。
            Range: 0~15
            Default: 0 (Previous State)
        </Help>
      </Value>
      <Value type="byte" byteSize="4" index="111" label="(Group 1) 以秒為單位設置報告" min="1" max="2147483647" value="240" setting_type="zwave" fw="">
        <Help>
       		設置報告Group 1的自動報告間隔（秒）。 這控制（Group 1）定時自動報告。
            Range: 0~2147483647
            Default: 240 (Previous State)
        </Help>
      </Value>
      <Value type="byte" byteSize="4" index="112" label="(Group 2) 以秒為單位設置報告" min="1" max="2147483647" value="3600" setting_type="zwave" fw="">
        <Help>
       		設置報告Group 2的自動報告間隔（秒）。 這控制（Group 2）定時自動報告。
            Range: 0~2147483647
            Default: 3600 (Previous State)
        </Help>
      </Value>
      <Value type="byte" byteSize="4" index="113" label="(Group 3) 以秒為單位設置報告" min="1" max="2147483647" value="3600" setting_type="zwave" fw="">
        <Help>
       		設置報告Group 3的自動報告間隔（秒）。 這控制（Group 3）定時自動報告。
            Range: 0~2147483647
            Default: 3600 (Previous State)
        </Help>
      </Value>
      <Value type="list" byteSize="1" index="120" label="外部開關S1設置" min="0" max="3" value="0" setting_type="zwave" fw="">
        <Help>
        通過配置集為S1配置外部開關模式。
            0 = 進入自動識別模式。//可以通過在2秒內點擊內部按鈕4次來進入該模式。
            1 = 瞬時按鈕模式。
            2 = 3路開關模式。 
            3 = 2態開關模式。           
            Note: 當模式確定後，該模式值在排除後不會被重置。
            Range: 0~3
            Default: 0 (Previous State)
        </Help>
            <Item label="進入自動識別模式。" value="0" />
            <Item label="瞬時按鈕模式。" value="1" />
            <Item label="3路開關模式。" value="2" />
            <Item label="2態開關模式。" value="3" />            
      </Value>
      <Value type="list" byteSize="1" index="121" label="外部開關S2設置" min="0" max="4" value="0" setting_type="zwave" fw="">
        <Help>
        通過配置集為S2配置外部開關模式。
            0 = 進入自動識別模式。//可以通過在2秒內點擊內部按鈕6次來進入該模式。
            1 = 瞬時按鈕模式。
            2 = 3路開關模式。 
            3 = 2態開關模式。           
            Note: 當模式確定後，該模式值在排除後不會被重置。
            Range: 0~3
            Default: 0 (Previous State)
        </Help>
            <Item label="進入自動識別模式。" value="0" />
            <Item label="瞬時按鈕模式。" value="1" />
            <Item label="3路開關模式。" value="2" />
            <Item label="2態開關模式。" value="3" />            
      </Value>
      <Value type="list" byteSize="1" index="123" label="設置外部開關S1的控制目標" min="1" max="3" value="3" setting_type="zwave" fw="">
        <Help>
        設置外部開關S1的控制目標
            1 = 控制自身的輸出負載。
            2 = 控制其他節點。
            3 = 控制自身和其他節點的輸出負載。 
            Range: 1~3
            Default: 3 (Previous State)
        </Help>
            <Item label="控制自身的輸出負載。" value="1" />
            <Item label="控制其他節點。" value="2" />
            <Item label="控制自身和其他節點的輸出負載。" value="3" />
      </Value>
      <Value type="list" byteSize="1" index="124" label="設置外部開關S2的控制目標" min="1" max="3" value="3" setting_type="zwave" fw="">
        <Help>
        設置外部開關S2的控制目標
            1 = 控制自身的輸出負載。
            2 = 控制其他節點。
            3 = 控制自身和其他節點的輸出負載。 
            Range: 1~3
            Default: 3 (Previous State)
        </Help>
            <Item label="控制自身的輸出負載。" value="1" />
            <Item label="控制其他節點。" value="2" />
            <Item label="控制自身和其他節點的輸出負載。" value="3" />
      </Value>
      <Value type="byte" byteSize="1" index="125" label="調光斜坡速度" min="1" max="255" value="3" setting_type="zwave" fw="">
        <Help>
       		以秒為單位設置默認調光率。
            Range: 1~255
            Default: 3 (Previous State)
        </Help>
      </Value>
      <Value type="byte" byteSize="1" index="131" label="最小暗度設置" min="0" max="99" value="0" setting_type="zwave" fw="">
        <Help>
       		設置負載可達到的最小亮度級別。
            注意：確定級別後，此級別值在排除後不會重置。 
            Range: 0~99
            Default: 0 (Previous State)
        </Help>
      </Value>
      <Value type="byte" byteSize="1" index="132" label="最大暗度設置" min="0" max="99" value="99" setting_type="zwave" fw="">
        <Help>
       		設置負載可達到的最大亮度級別。
            注意：確定級別後，此級別值在排除後不會重置。
            Range: 0~99
            Default: 99 (Previous State)
        </Help>
      </Value>
      <Value type="list" byteSize="1" index="249" label="自動加載檢測" min="0" max="2" value="2" setting_type="zwave" fw="">
        <Help>
            設置納米調光器掉電或重新上電時負載的識別方式。
            0 = 開機時切勿識別負載。
            1 = 首次開機時只能識別一次。
            2 = 開機後識別負載。
            Range: 1~3
            Default: 0 (Previous State)
        </Help>
            <Item label="開機時切勿識別負載。" value="0" />
            <Item label="首次開機時只能識別一次。" value="1" />
            <Item label="開機後識別負載。" value="2" />
      </Value>
	  <Value type="list" byteSize="1" index="252" label="參數設置鎖定" min="0" max="1" value="0" setting_type="zwave" fw="">
        <Help>
            鎖定參數設置。
            0 = 解鎖參數設置
            1 = 鎖定參數設置
            Range: 0~1
            Default: 0 (Previous State)
        </Help>
            <Item label="解鎖參數設置" value="0" />
            <Item label="鎖定參數設置" value="1" />
      </Value>
</configuration>
'''
}

//add in parameter at later time: 
	//85-86
//Left out or unnecessary:
	//122, 129-130, 255