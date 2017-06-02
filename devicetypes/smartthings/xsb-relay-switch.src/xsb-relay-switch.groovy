/**
 *  WeMo Direct LED Bulbs
 *
 *  Copyright 2014 SmartThings
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
 *  Thanks to Chad Monroe @cmonroe and Patrick Stuart @pstuart
 *
 */
//DEPRECATED - Using the generic DTH for this device. Users need to be moved before deleting this DTH

metadata {
	definition (name: "XSB Relay Switch", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.switch") {

    	capability "Actuator"
        capability "Configuration"
        capability "Refresh"
		capability "Sensor"
        capability "Switch"
		
	fingerprint profileId: "0104", inClusters: "0000 0003 0004 0005 0006", outClusters: "0000", manufacturer: "ClimaxTechnology", model: "PRL_00.00.03.04TC", deviceJoinName: "Clumax Relay Controllers"
    }

	

	// simulator metadata
	simulator {
		// status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"

		// reply messages
		reply "zcl on-off on": "on/off: 1"
		reply "zcl on-off off": "on/off: 0"
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
			state "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}



		main(["switch"])
		details(["switch", "refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.trace description
	if (description?.startsWith("catchall:")) {
		//def msg = zigbee.parse(description)
		//log.trace msg
		//log.trace "data: $msg.data"
        if(description?.endsWith("0100"))
        {
        	def result = createEvent(name: "switch", value: "on")
            log.debug "Parse returned ${result?.descriptionText}"
            return result
        }else if(description?.endsWith("0000"))
        {
        	def result = createEvent(name: "switch", value: "off")
            log.debug "Parse returned ${result?.descriptionText}"
            return result
        }
	}

}

def on() {
	
    log.debug "on()"
	sendEvent(name: "switch", value: "on")
    [
	"st cmd 0x${device.deviceNetworkId} ${device.endpointId} 6 1 {}"
    ]
    
}

def off() {
	log.debug "off()"
	sendEvent(name: "switch", value: "off")
	[
	"st cmd 0x${device.deviceNetworkId} ${device.endpointId} 6 0 {}"
    ]
    
}

def refresh() {
	[
	"st rattr 0x${device.deviceNetworkId} ${device.endpointId} 6 0"
    ]
}

def configure() {

	log.debug "Configuring Reporting and Bindings."
	def configCmds = [

        //Switch Reporting
        "zcl global send-me-a-report 6 0 0x10 0 3600 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} ${device.endpointId} 1", "delay 1000",

        //Level Control Reporting
        "zcl global send-me-a-report 8 0 0x20 5 3600 {0010}", "delay 200",
        "send 0x${device.deviceNetworkId} ${device.endpointId} 1", "delay 1500",

        "zdo bind 0x${device.deviceNetworkId} ${device.endpointId} 1 6 {${device.zigbeeId}} {}", "delay 1000",
		
	]
    return configCmds + refresh() // send refresh cmds as part of config
}



private hex(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String swapEndianHex(String hex) {
    reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
    int i = 0;
    int j = array.length - 1;
    byte tmp;
    while (j > i) {
        tmp = array[j];
        array[j] = array[i];
        array[i] = tmp;
        j--;
        i++;
    }
    return array
}