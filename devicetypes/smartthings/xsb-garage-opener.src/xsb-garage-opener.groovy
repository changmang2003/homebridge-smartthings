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
	definition (name: "XSB Garage Opener", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.switch") {

    		capability "Actuator"
        	capability "Configuration"
        	capability "Refresh"
			capability "Sensor"
        	capability "Switch"
            capability "Switch Level"
			capability "Door Control"
			capability "Garage Door Control"
			capability "Contact Sensor"
            
            fingerprint profileId: "0104", inClusters: "0000 0003 0004 0005 0006 0008 0100", outClusters: "0000", manufacturer: "ClimaxTechnology", model: "SCM_00.00.03.14TC", deviceJoinName: "Clumax Roller Shutter Controls"
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
    	standardTile("status", "device.status", width: 3, height: 2, canChangeIcon: true) { 
			state("closed", label:'${name}', icon:"st.doors.garage.garage-closed", backgroundColor:"#79b821", nextState:"closing")
			state("open", label:'${name}', icon:"st.doors.garage.garage-open", backgroundColor:"#ffa81e", nextState:"opening")
			state("opening", label:'${name}', icon:"st.doors.garage.garage-opening", backgroundColor:"#ffe71e")
			state("closing", label:'${name}', icon:"st.doors.garage.garage-closing", backgroundColor:"#ffe71e")        
		}
         valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
            state "level", label:'${currentValue} sec', unit:"S", backgroundColor:"#ffffff"
        }
        controlTile("levelControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..60)") {
            state "level", action:"switch level.setLevel", backgroundColor: "#1e9cbb"
        }
    
		standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
			state "open", label:'${name}', action:"switch.on", icon:"st.contact.contact.open", backgroundColor:"#00A0DC", nextState:"opening"
			state "closed", label:'${name}', action:"switch.off", icon:"st.contact.contact.closed", backgroundColor:"#ffffff", nextState:"closing"
			state "opening", label:'${name}', action:"switch.on", icon:"st.contact.contact.open", backgroundColor:"#00A0DC", nextState:"opening"
			state "closing", label:'${name}', action:"switch.off", icon:"st.contact.contact.closed", backgroundColor:"#ffffff", nextState:"closing"
		}
        standardTile("acceleration", "device.acceleration", decoration: "flat") {
			state("active", label:'${name}', icon:"st.motion.acceleration.active", backgroundColor:"#53a7c0")
			state("inactive", label:'${name}', icon:"st.motion.acceleration.inactive", backgroundColor:"#ffffff")
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["status"])
		details(["status","level","levelControl","switch","acceleration","refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {

	log.trace description
	if (description?.startsWith("catchall:")) {
    	
		//def msg = zigbee.parse(description)
		//log.trace msg
		//log.trace "data: $msg.data"
        if(description?.endsWith("0100") || description?.endsWith("1001"))
        {
        	def result = [
            createEvent(name: "switch", value: "closed"),
            createEvent(name: "status", value: "open"),
            ]
            log.debug "Parse returned ${result?.descriptionText}"
            return result
       
        } else if(description?.endsWith("0000") || description?.endsWith("1000"))
        {
        	def result = [ 
            createEvent(name: "switch", value: "open"),
            createEvent(name: "status", value: "closed"),
            ]
            log.debug "Parse returned ${result?.descriptionText}"
            return result
            
        }
	} else if (description?.startsWith("read attr")) {
    	def descMap = parseDescriptionAsMap(description)
        if(descMap.cluster == "0008"){
            def dimmerValue = Math.round(convertHexToInt(descMap.value) * 100 / 255)
            log.debug "Garage value is $dimmerValue"
            sendEvent(name: "level", value: dimmerValue)
        }
        /*
    	log.debug description[-2..-1]
        def i = Math.round(convertHexToInt(description[-2..-1]) / 256 * 100 )
		sendEvent( name: "level", value: i )
        */
    }


}

def on() {
	log.debug "on()"
	sendEvent(name: "switch", value: "open")
    sendEvent(name: "status", value: "closed")
    [
   
	"st cmd 0x${device.deviceNetworkId} 1 0x6 1 {}"
    
    ]
}

def off() {
	log.debug "off()"
	sendEvent(name: "switch", value: "closed")
    sendEvent(name: "status", value: "open")
	"st cmd 0x${device.deviceNetworkId} 1 6 0 {}"
}

def refresh() {
	[
	"st rattr 0x${device.deviceNetworkId} 1 6 0", "delay 500",
    "st rattr 0x${device.deviceNetworkId} 1 8 0"
    ]
}

def ping() {

}

def setLevel(value) {
	log.trace "setLevel($value)"
	def cmds = []
/*
	if (value == 0) {
			sendEvent(name: "switch", value: "open")
    		sendEvent(name: "status", value: "closed")
        
		cmds << "st cmd 0x${device.deviceNetworkId} 1 8 0 {0000 0000}"
	}
	else if (device.latestValue("switch") == "open") {
		sendEvent(name: "switch", value: "closed")
    	sendEvent(name: "status", value: "open")
	}

*/	sendEvent(name: "level", value: value)
    def level = hexString(Math.round(value * 255/100))
	cmds << "st cmd 0x${device.deviceNetworkId} 1 8 0x12 {${level} 0000}"

	log.debug cmds
	cmds
}

def configure() {

	log.debug "Configuring Reporting and Bindings."
	def configCmds = [

        //Switch Reporting
        "zcl global send-me-a-report 6 0 0x10 0 3600 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1000",

        //Level Control Reporting
        "zcl global send-me-a-report 8 0 0x20 5 3600 {0010}", "delay 200",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1500",

        "zdo bind 0x${device.deviceNetworkId} 1 1 6 {${device.zigbeeId}} {}", "delay 1000",
		"zdo bind 0x${device.deviceNetworkId} 1 1 8 {${device.zigbeeId}} {}", "delay 500",
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
