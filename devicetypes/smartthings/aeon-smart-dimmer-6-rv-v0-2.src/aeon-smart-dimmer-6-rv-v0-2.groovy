/*
 * V 0.2 of Aeon Smart Dimmer 6 code 9/24/2017
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * some code used from various SmartThings device type and metering code from ElasticDev
 *
*/

 metadata {
	definition (name: "Aeon Smart Dimmer 6 - RV v0.2", namespace: "SmartThings", author: "Yu Chang Mang") {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Actuator"
		capability "Switch"
		capability "Switch Level"
        capability "Color Control"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		command "reset"

		/* Capability notes
		0x26 COMMAND_CLASS_SWITCH_MULTILEVEL 2
        0x27 COMMAND_CLASS_SWITCH_ALL 1
        0x32 COMMAND_CLASS_METER 3
        0x33 COMMAND_CLASS_COLOR_SWITCH 3
		0x59 COMMAND_CLASS_ASSOCIATION_GRP_INFO  1
		0x5A COMMAND_CLASS_DEVICE_RESET_LOCALLY 1
		0x5E COMMAND_CLASS_ZWAVE_PLUS_INFO 2
		0x60 Multi Channel Command Class (V3)
		0x70 COMMAND_CLASS_CONFIGURATION 1
        0x72 COMMAND_CLASS_MANUFACTURER_SPECIFIC 2
        0x73 COMMAND_CLASS_POWERLEVEL 1
		0x7A COMMAND_CLASS_FIRMWARE_UPDATE_MD 2
		0x81 COMMAND_CLASS_CLOCK 1
        0x82 COMMAND_CLASS_HAIL 1
		0x8E COMMAND_CLASS_MULTI_INSTANCE_ASSOCIATION 2
        0x85 COMMAND_CLASS_ASSOCIATION 2
        0x86 COMMAND_CLASS_VERSION 2
		0xEF COMMAND_CLASS_MARK 1
        */

		fingerprint mfr:"0086", prod:"0103", model:"0063",deviceId: "0x1101", deviceJoinName: "Aeon Smart Dimmer"
        fingerprint inClusters: "0x5E,0x26,0x33,0x32,0x81,0x60,0x8E,0x27,0x2C,0x2B,0x70,0x85,0x59,0x72,0x86,0x7A,0x73,0x5A", outClusters: "0x82"
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x26,0x59,0x85,0x98,0x8E,0x32,0x81,0x70,0x86,0x27"
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x26,0x59,0x85,0x98,0x33"
       
	}
	// simulator metadata
	simulator {
		status "on":  "command: 2603, payload: FF"
		status "off": "command: 2603, payload: 00"
		status "09%": "command: 2603, payload: 09"
		status "10%": "command: 2603, payload: 0A"
		status "33%": "command: 2603, payload: 21"
		status "66%": "command: 2603, payload: 42"
		status "99%": "command: 2603, payload: 63"

	for (int i = 0; i <= 10000; i += 1000) {
	    status "power  ${i} W": 
		    new physicalgraph.zwave.Zwave().meterV3.meterReport(scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
	}
	for (int i = 0; i <= 100; i += 10) {
	    status "energy  ${i} kWh":
		    new physicalgraph.zwave.Zwave().meterV3.meterReport(scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
	}
	// reply messages
	["FF", "00", "09", "0A", "21", "42", "63"].each { val -> reply "2001$val,delay 100,2602": "command: 2603, payload: $val" }
}
	// tile definitions
	tiles (scale: 2) {
    	multiAttributeTile(name:"main", type:"lighting", width:6, height:4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
            	attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            }
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
            tileAttribute("device.power", key: "SECONDARY_CONTROL") {
                attributeState "default",label:'${currentValue} W'
            }
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh', action:"poll"
		}
		valueTile("current", "device.current", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} A', action:"poll"
		}
        valueTile("voltage", "device.voltage", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} V', action:"poll"
		}
        controlTile("rgbSelector", "device.color", "color", height: 2, width: 2, inactiveLabel: false) {
			state "color", action:"setColor"
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "main","energy","current","voltage"
		details(["main","energy","current","voltage","rgbSelector","reset","refresh","configure"])
	}
	
    preferences { 
    	/*
		input "ledBehavior", "integer", 
			title: "LED 用途",
            description: "0=energy tracking, 1=momentary status, 2=night light",
			defaultValue: 0, 
			displayDuringSetup: true
     */
     	input "ledBehavior", "enum", 
			title: "LED 用途",
            defaultValue: ledBehaviorSetting,
            required: false,
			displayDuringSetup: true,
     		options: ledBehaviorOptions.collect { it.name }
        /*    
		input "ledlightcolor", "enum", 
			title: "LED 色彩",
            defaultValue: ledlightcolorSetting,
            required: false,
			displayDuringSetup: true,
     		options: ledlightcolorOptions.collect { it.name }
        */    
		input "ledBrightness", "integer",
        	title: "LED 亮度",
            description: "LED能量模式&順時顯示時，設置指示燈LED的亮度百分比",
            defaultValue: 50,
            range: "0..100",
            required: false,
            displayduringSetup: true
            
		input "monitorInterval", "integer", 
			title: "監測間隔", 
			description: "發送設備報告的時間間隔（以秒為單位）", 
			defaultValue: 60,
            range: "1..4294967295",
			required: false, 
			displayDuringSetup: true

		input "debugOutput", "boolean", 
			title: "啟用除錯日誌？",
			defaultValue: false,
			displayDuringSetup: true
	}
}

def updated()
{
	state.debug = ("true" == debugOutput)
	if (state.sec && !isConfigured()) {
		// in case we miss the SCSR
		response(configure())
		
	}
}

def parse(String description)
{
	def result = null
	if (description.startsWith("Err 106")) {
		state.sec = 0
		result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true,
			descriptionText: "該傳感器未能完成網絡安全密鑰交換。 如果您無法通過SmartThings控制它，則必須將其從網絡中刪除並重新添加。")
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x25: 1, 0x26: 1, 0x27: 1, 0x32: 3, 0x33: 3, 0x59: 1, 0x70: 1, 0x72: 2, 0x73: 1, 0x82: 1, 0x85: 2, 0x86: 2])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	// if (state.debug) log.debug "Parsed '${description}' to ${result.inspect()}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x25: 1, 0x26: 1, 0x27: 1, 0x32: 3, 0x33: 3, 0x59: 1, 0x70: 1, 0x72: 2, 0x73: 1, 0x82: 1, 0x85: 2, 0x86: 2])
	state.sec = 1
	if (state.debug) log.debug "encapsulated: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	response(configure())
}

def zwaveEvent(physicalgraph.zwave.commands.powerlevelv1.PowerlevelTestNodeReport cmd) {
	if (state.debug) log.debug "===Power level test node report received=== ${device.displayName}: statusOfOperation: ${cmd.statusOfOperation} testFrameCount: ${cmd.testFrameCount} testNodeid: ${cmd.testNodeid}"
	def request = [
        physicalgraph.zwave.commands.powerlevelv1.PowerlevelGet()
    ]
    response(commands(request))
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    if (state.debug) log.debug "---CONFIGURATION REPORT V2--- ${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    if (state.debug) log.debug "---CONFIGURATION REPORT V1--- ${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
    def meterTypes = ["Unknown", "Electric", "Gas", "Water"]
    def electricNames = ["energy", "energy", "power", "count",  "voltage", "current", "powerFactor",  "unknown"]
    def electricUnits = ["kWh",    "kVAh",   "W",     "pulses", "V",       "A",       "Power Factor", ""]

    //NOTE ScaledPreviousMeterValue does not always contain a value
    def previousValue = cmd.scaledPreviousMeterValue ?: 0

    def map = [ name: electricNames[cmd.scale], unit: electricUnits[cmd.scale], displayed: state.display]
    switch(cmd.scale) {
        case 0: //kWh
	    previousValue = device.currentValue("energy") ?: cmd.scaledPreviousMeterValue ?: 0
            map.value = cmd.scaledMeterValue
            break;
        case 1: //kVAh
            map.value = cmd.scaledMeterValue
            break;
        case 2: //Watts
            previousValue = device.currentValue("power") ?: cmd.scaledPreviousMeterValue ?: 0
            map.value = Math.round(cmd.scaledMeterValue)
            break;
        case 3: //pulses
            map.value = Math.round(cmd.scaledMeterValue)
            break;
        case 4: //Volts
            previousValue = device.currentValue("voltage") ?: cmd.scaledPreviousMeterValue ?: 0
            map.value = cmd.scaledMeterValue
            break;
        case 5: //Amps
            previousValue = device.currentValue("current") ?: cmd.scaledPreviousMeterValue ?: 0
            map.value = cmd.scaledMeterValue
            break;
        case 6: //Power Factor
            previousValue = device.currentValue("powerFactor") ?: cmd.scaledPreviousMeterValue ?: 0
            map.value = cmd.scaledMeterValue
            break;
        case 7: //Unknown
            map.value = cmd.scaledMeterValue
            break;
        default:
            break;
    }
    //Check if the value has changed by more than 5%, if so mark as a stateChange
    //map.isStateChange = ((cmd.scaledMeterValue - previousValue).abs() > (cmd.scaledMeterValue * 0.05))

    createEvent(map)
    return map
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	def result;
	result =createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical", displayed: true, isStateChange: true)
    return result;
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) 
{
   	def result;
	result =createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical", displayed: true, isStateChange: true)
    return result;
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	def result;
	result =createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical", displayed: true, isStateChange: true)
    return result;
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd)
{
	def result;
    result = createEvent(name: "level", value: cmd.value, type: "digital", displayed: true, isStateChange: true);
    return result;
}
	
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinarySet cmd)
{
	def result;
    result = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital", displayed: true, isStateChange: true);
    return result;
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	if (state.debug) log.debug "Unhandled: $cmd sent to ${device.displayName}"
    createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

def on() {
	if (state.debug) log.debug "turning on ${device.displayName}"
	def request = [
		zwave.basicV1.basicSet(value: 0xFF),
	    zwave.basicV1.basicGet(),
		zwave.switchBinaryV1.switchBinaryGet(),
		zwave.switchMultilevelV1.switchMultilevelGet()
	]
    commands(request)
}

def off() {
	if (state.debug) log.debug "turning off ${device.displayName}"
	def request = [
		zwave.basicV1.basicSet(value: 0x00),
        zwave.basicV1.basicGet(),
		zwave.switchBinaryV1.switchBinaryGet(),
		zwave.switchMultilevelV1.switchMultilevelGet()
	]
    commands(request)
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

def setColor(value) {
	def result = []
	if (state.debug) log.debug "setColor: ${value}"
	if (value.hex) {
		def c = value.hex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
        result << zwave.configurationV1.configurationSet(parameterNumber: 83, size: 3, configurationValue: [c[0], c[1], c[2]])
	}
	if(value.hex) sendEvent(name: "color", value: value.hex)
    commands(result)
}

def poll() {
	if (state.debug) log.debug "poll request sent to ${device.displayName}"
	def request = [
		zwave.switchBinaryV1.switchBinaryGet(),
        zwave.basicV1.basicGet(),
		zwave.switchMultilevelV1.switchMultilevelGet(),
		zwave.meterV3.meterGet(scale: 0),	//kWh
        zwave.meterV3.meterGet(scale: 1),	//kVAh
		zwave.meterV3.meterGet(scale: 2),	//Wattage
		zwave.meterV3.meterGet(scale: 4),	//Volts
		zwave.meterV3.meterGet(scale: 5),	//Amps
        zwave.meterV3.meterGet(scale: 6)	//Power Factor
	]
    commands(request)
}

def refresh() {
	if (state.debug) log.debug "refresh request sent to ${device.displayName}"
	def request = [
        zwave.basicV1.basicGet(),
		zwave.switchMultilevelV1.switchMultilevelGet(),
		zwave.switchBinaryV1.switchBinaryGet()
	]
    commands(request)
}

def reset() {
	if (state.debug) log.debug "reset request sent to ${device.displayName}"
	def request = [
		zwave.meterV3.meterReset(),
		zwave.meterV3.meterGet(scale: 0),	//kWh
        zwave.meterV3.meterGet(scale: 1),	//kVAh
		zwave.meterV3.meterGet(scale: 2),	//Wattage
		zwave.meterV3.meterGet(scale: 4),	//Volts
		zwave.meterV3.meterGet(scale: 5),	//Amps
        zwave.meterV3.meterGet(scale: 6)	//Power Factor
	]
    commands(request)
}

def configure() {
	log.debug "led color ${ledlightcolorSettingMinutes}"
	if (state.debug) {
    	if (state.sec) {
        	log.debug "secure configuration being sent to ${device.displayName}"
            }
        else
        	if (state.debug) log.debug "configuration being sent to ${device.displayName}"
		}            
    def monitorInt = 60
		if (monitorInterval) {
			monitorInt=monitorInterval.toInteger()
		}

	def ledBright = 50
    	if (ledBrightness) {
        	ledBright=ledBrightness.toInteger()
		}
       
    if (state.debug) log.debug "Sending configure commands - monitorInterval '${monitorInt}'"
	def request = [
    	// Reset switch configuration to defaults
        // zwave.configurationV1.configurationSet(parameterNumber: 255, size: 1, scaledConfigurationValue: 1),
		// 當微交換機的負載狀態發生變化時，啟用向相關設備（Group 1）發送通知 (0=nothing, 1=hail CC, 2=basic CC report)
        zwave.configurationV1.configurationSet(parameterNumber: 80, size: 1, configurationValue: [2]),
        // 設置LED 用途 0能量模式 1瞬時顯示 2夜間照明        
		zwave.configurationV1.configurationSet(parameterNumber: 81, size: 1, scaledConfigurationValue: ledBehaviorSettingMinutes),
        /*
         // 小夜燈模式下配置RGB值。
        zwave.configurationV1.configurationSet(parameterNumber: 83, size: 3, configurationValue: ledlightcolorSettingMinutes),
        */
		// 設置LED 亮度
        zwave.configurationV1.configurationSet(parameterNumber: 84, size: 3, configurationValue: [ledBright,ledBright,ledBright]),
        // 報告group 1中需要發送哪些報告
		zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 4|2|1),
		// 報告group 2中需要發送哪些報告
		zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 8),
		// 報告group 3中需要發送哪些報告
		zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: 0),
		// 間隔發送報告 group 1
		zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: monitorInt),
		// 間隔發送報告 group 2
		zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 60),
		// 間隔發送報告group 3
		zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: 0),
        
        // get notification behavior
		zwave.configurationV1.configurationGet(parameterNumber: 80),
		// get LED behavior
		zwave.configurationV1.configurationGet(parameterNumber: 81),
    	// get night light RGB value
		zwave.configurationV1.configurationGet(parameterNumber: 83),
    	// get Energy Mode/momentary indicate LED brightness value
		zwave.configurationV1.configurationGet(parameterNumber: 84),
    	// 報告中需要發送哪些報告 group 1
		zwave.configurationV1.configurationGet(parameterNumber: 101),
		// 報告中需要發送哪些報告 group 2
		zwave.configurationV1.configurationGet(parameterNumber: 102),
		// 報告中需要發送哪些報告 group 3
		zwave.configurationV1.configurationGet(parameterNumber: 103),
    	// 間隔發送報告 group 1
		zwave.configurationV1.configurationGet(parameterNumber: 111),
		// 間隔發送報告 group 2
		zwave.configurationV1.configurationGet(parameterNumber: 112),
		// 間隔發送報告 group 3
		zwave.configurationV1.configurationGet(parameterNumber: 113),
		
		// Can use the zwaveHubNodeId variable to add the hub to the device's associations:
		zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId)
    ]
	commands(request)
    
}

private setConfigured() {
	updateDataValue("configured", "true")
}

private isConfigured() {
	getDataValue("configured") == "true"
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=500) {
	delayBetween(commands.collect{ command(it) }, delay)
}

// getledBehaviorSettingMinutes()、getledBehaviorSetting()、getledBehaviorOptions() input選單的時候是一組使用
private getledBehaviorSettingMinutes() {
	return convertOptionSettingToInt(ledBehaviorOptions, ledBehaviorSetting)
}

private getledBehaviorSetting() {
	return settings?.ledBehavior ?: findDefaultOptionName(ledBehaviorOptions)
}

private getledBehaviorOptions() {
	[
		[name: "能量跟蹤", value: 0],
		[name: formatDefaultOptionName("瞬時顯示"), value: 1],
		[name: "小夜燈", value: 2]		
	]
}

/*
// getledBehaviorSettingMinutes()、getledBehaviorSetting()、getledBehaviorOptions() input選單的時候是一組使用
private getledlightcolorSettingMinutes() {
	return convertOptionSetting(ledlightcolorOptions, ledlightcolorSetting)
}

private getledlightcolorSetting() {
	return settings?.ledlightcolor ?: findDefaultOptionName(ledlightcolorOptions)
}

private getledlightcolorOptions() {
	[
    	[name: "紅色", value: [0xDD,0,0]],
		[name: formatDefaultOptionName("綠色"), value: [0,0xA0,0]],
		[name: "藍色", value: [0,0,0xDD]]		
	]
}
*/
// convertOptionSettingToInt()、formatDefaultOptionName()、findDefaultOptionName()、getDefaultOptionSuffix()、safeToInt()  input 選單使用到的函式
private convertOptionSettingToInt(options, settingVal) {
	return safeToInt(options?.find { "${settingVal}" == it.name }?.value, 0)
}

private formatDefaultOptionName(val) {
	return "${val}${defaultOptionSuffix}"
}

private findDefaultOptionName(options) {
	def option = options?.find { it.name?.contains("${defaultOptionSuffix}") }
	return option?.name ?: ""
}

private getDefaultOptionSuffix() {
	return "   (Default)"
}

private safeToInt(val, defaultVal=-1) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}
/*
private convertOptionSetting(options, settingVal) {
	return options?.find { "${settingVal}" == it.name }?.value
}
*/