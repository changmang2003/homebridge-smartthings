/**
 *  GE Link Bulb
 *
 *  Copyright 2016 SmartThings
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
 *  Thanks to Chad Monroe @cmonroe and Patrick Stuart @pstuart, and others
 *
 ******************************************************************************
 *                                Changes
 ******************************************************************************
 *
 *  Change 1:	2014-10-10 (wackford)
 *				Added setLevel event so subscriptions to the event will work
 *  Change 2:	2014-12-10 (jscgs350 using Sticks18's code and effort!)
 *				Modified parse section to properly identify bulb status in the app when manually turned on by a physical switch
 *  Change 3:	2014-12-12 (jscgs350, Sticks18's)
 *				Modified to ensure dimming was smoother, and added fix for dimming below 7
 *	Change 4:	2014-12-14 Part 1 (Sticks18)
 *				Modified to ignore unnecessary level change responses to prevent level skips
 *	Change 5:	2014-12-14 Part 2 (Sticks18, jscgs350)
 *				Modified to clean up trace&debug logging, added new code from @sticks18 for parsing "on/off" to determine if the bulb is manually turned on and immediately update the app
 *	Change 6:	2015-01-02	(Sticks18)
 *				Modified to allow dim rate in Preferences. Added ability to dim during On/Off commands and included this option in Preferences. Defaults are "Normal" and no dim for On/Off.
 *	Change 7:	2015-01-09	(tslagle13)
 *				dimOnOff is was boolean, and switched to enum. Properly update "rampOn" and "rampOff" when refreshed or a polled (dim transition for On/Off commands)
 *	Change 8:	2015-03-06	(Juan Risso)
 *				Slider range from 0..100
 *	Change 9:	2015-03-06	(Juan Risso)
 *				Setlevel -> value to integer (to prevent smartapp calling this function from not working).
 *	Change 10: 	2016-03-06	(Vinay Rao/Tom Manley)
 *				changed 2/3rds of the file to clean up code and add zigbee library improvements
 *
 */

metadata {
    definition (name: "SR-ZG9101FA7-DIM", namespace: "SmartThings", author: "Yu Chang Mang", ocfDeviceType: "oic.d.light") {

        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Sensor"
        capability "Switch"
        capability "Switch Level"
        capability "Polling"

        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0B05", outClusters: "0019", manufacturer: "SR", model: "DIM Lighting", deviceJoinName: "SR-ZG9101FA7-DIM"
       
    }

	// UI tile definitions
    tiles(scale: 2) {
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
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        main "switch"
        details(["switch", "refresh"])
    }

    preferences {
        input("dimRate", "enum", title: "漸暗的速率", options: ["快", "一般", "慢", "很慢"], defaultValue: "一般", required: false, displayDuringSetup: true)
        input("dimOnOff", "enum", title: "開/關命令的轉換漸暗？", options: ["Yes", "No"], defaultValue: "No", required: false, displayDuringSetup: true)
    }
}

// Parse incoming device messages to generate events
def parse(String description) {
    def resultMap = zigbee.getEvent(description)
    if (resultMap) {
        if (resultMap.name != "level" || resultMap.value != 0) {  // Ignore level reports of 0 sent when bulb turns off
            sendEvent(resultMap)
        }
    }
    else {
        log.debug "DID NOT PARSE MESSAGE for description : $description"
        log.debug zigbee.parseDescriptionAsMap(description)
    }
}

def poll() {
	return zigbee.onOffRefresh() + zigbee.levelRefresh()
}

def installed() {
// 設備每1小時喚醒一次，這段時間允許我們在脫機標記之前錯過一次喚醒通知
	log.debug "Configured health checkInterval when installed()"
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def updated() {
// 設備每1小時喚醒一次，這個間隔允許我們在脫機標記之前錯過一個喚醒通知
	log.debug "Configured health checkInterval when updated()"
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	state.dOnOff = "0000"

	if (dimRate) {

		switch (dimRate)
        	{

        		case "快":

            		state.rate = "0000"
                	if (dimOnOff) { state.dOnOff = "0000"}
                    break

            	case "一般":

            		state.rate = "1500"
                    if (dimOnOff) { state.dOnOff = "0015"}
                	break

            	case "慢":

            		state.rate = "2500"
                    if (dimOnOff) { state.dOnOff = "0025"}
               		break

            	case "很慢":

            		state.rate = "3500"
                    if (dimOnOff) { state.dOnOff = "0035"}
                	break

        	}

    }

    else {

    	state.rate = "1500"
        state.dOnOff = "0000"

    }

        if (dimOnOff == "Yes"){
			switch (dimOnOff){
        		case "InstantOnOff":

            		state.rate = "0000"
                	if (state.rate == "0000") { state.dOnOff = "0000"}
                    break

            	case "NormalOnOff":

            		state.rate = "1500"
                    if (state.rate == "1500") { state.dOnOff = "0015"}
                	break

            	case "SlowOnOff":

            		state.rate = "2500"
                    if (state.rate == "2500") { state.dOnOff = "0025"}
               		break

            	case "Very SlowOnOff":

            		state.rate = "3500"
                    if (state.rate == "3500") { state.dOnOff = "0035"}
                	break

        	}

    }
    else{
    	state.dOnOff = "0000"
    }

	sendHubCommand(new physicalgraph.device.HubAction("st wattr 0x${device.deviceNetworkId} 1 8 0x10 0x21 {${state.dOnOff}}"))
}

def on() {
    zigbee.on()
}

def off() {
    zigbee.off()
}

def refresh() {
    def refreshCmds = [
        "st wattr 0x${device.deviceNetworkId} 1 8 0x10 0x21 {${state?.dOnOff ?: '0000'}}", "delay 2000"
    ]

    return refreshCmds + zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.onOffConfig()
}

def setLevel(value) {
    def cmd
    def delayForRefresh = 500
    if (dimRate && (state?.rate != null)) {
        def computedRate = convertRateValue(state.rate)
        cmd = zigbee.setLevel(value, computedRate)
        delayForRefresh += computedRate * 100       //converting tenth of second to milliseconds
    }
    else {
        cmd = zigbee.setLevel(value, 20)
        delayForRefresh += 2000
    }
    cmd + ["delay $delayForRefresh"] + zigbee.levelRefresh()
}

int convertRateValue(rate) {
    int convertedRate = 0
    switch (rate)
    {
        case "0000":
            convertedRate = 0
            break

        case "1500":
            convertedRate = 20      //0015 hex in int is 2.1
            break

        case "2500":
            convertedRate = 35      //0025 hex in int is 3.7
            break

        case "3500":
            convertedRate = 50      //0035 hex in int is 5.1
            break
    }
    convertedRate
}

def configure() {
    log.debug "Configuring Reporting and Bindings."
    return zigbee.onOffConfig() + zigbee.onOffRefresh() + zigbee.levelRefresh()
}

def ping() {
return zigbee.onOffRefresh() + zwave.switchMultilevelV2.switchMultilevelGet().format() // whatever makes sense for the device
}