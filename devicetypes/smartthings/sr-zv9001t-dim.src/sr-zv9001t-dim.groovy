/**
 *  Copyright 2017 AdamV
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
 *
 *  Version 1.0.0
 *  Author: AdamV
 *  Date: 2017-07-28
 *
 */
 
metadata {
	definition (name: "SR-ZV9001T-DIM", namespace: "smartthings", author: "Yu Chang Mang") {
		capability "Actuator"
		capability "Button"
        capability "Battery"
		capability "Configuration" 
       	capability "Refresh"
        capability "Health Check"
		capability "Switch Level"
        capability "Indicator"
		capability "Switch"
		capability "Polling"
        capability "Sensor"
        
		command "resetBatteryRuntime"
		command "describeAttributes"
        
		attribute "numberOfButtons", "number"
        attribute "sceneNumber", "number"
        attribute "button", "enum", ["pushed", "held", "double clicked", "click held"]
        attribute "needUpdate", "string"
		
        fingerprint deviceId: "0x0106", mfr: "0000", prod: "0003", model: "A10A", deviceJoinName: "SR-ZV9001T-DIM"
		fingerprint deviceId: "0x0106", inClusters: "0x5E, 0x85, 0x59, 0x8E, 0x60, 0x86, 0x72, 0x70, 0x5A, 0x73, 0x7A", outClusters: "0x25, 0x26, 0x5B, 0x2B, 0x2C"
		fingerprint deviceId: "0x0106", inClusters: "0x5E, 0x25, 0x20, 0x85, 0x59, 0x8E, 0x5B, 0x2B, 0x2C"																															
   }

	simulator {
		//status "button 1 pushed":  "command: 9881, payload: 00 5B 03 DE 00 01"
        
        status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"		
		status "24%": "command: 2003, payload: 18"
		status "49%": "command: 2003, payload: 31"
		status "74%": "command: 2003, payload: 4A"
		status "99%": "command: 2003, payload: 63"

		// reply messages
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"
		reply "200118,delay 5000,2602": "command: 2603, payload: 18"
		reply "200131,delay 5000,2602": "command: 2603, payload: 31"
		reply "20014A,delay 5000,2602": "command: 2603, payload: 4A"
		reply "200163,delay 5000,2602": "command: 2603, payload: 63"
		
        // need to redo simulator commands

	}
    tiles (scale: 2){
		
        multiAttributeTile(name:"button", type:"generic", width:6, height:4) {
  			tileAttribute("device.button", key: "PRIMARY_CONTROL"){
    		attributeState "default", label:'Controller', backgroundColor:"#44b621", icon:"st.Home.home30"
            attributeState "held", label: "holding", backgroundColor: "#C390D4"
  			}
            tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
			attributeState "batteryLevel", label:'${currentValue} % battery'
            }
            
        }
	/*tiles {
		standardTile("button", "device.button", width: 2, height: 2) {
			state "default", label: "", icon: "st.Home.home30", backgroundColor: "#ffffff"
            state "held", label: "holding", icon: "st.Home.home30", backgroundColor: "#C390D4"
        }
    	 valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
         	tileAttribute ("device.battery", key: "PRIMARY_CONTROL"){
                        state "battery", label:'${currentValue}% battery', unit:""
        	}
        }*/
        standardTile("configure", "device.configure", width: 2, height: 2, decoration: "flat") {
			state "default", label: "", icon:"st.secondary.configure", backgroundColor: "#ffffff", action: "configuration.configure"
        }
      	standardTile(
			"batteryRuntime", "device.batteryRuntime", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "batteryRuntime", label:'Battery: ${currentValue} Double tap to reset counter', unit:"", action:"resetBatteryRuntime"
		}
        standardTile(
			"statusText2", "device.statusText2", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "statusText2", label:'${currentValue}', unit:"", action:"resetBatteryRuntime"
		}
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        main "button"
		details(["button", "battery", "configure", "statusText2", "refresh"])
	}
    
}

def parse(String description) {
	def results = []
     //log.debug("RAW command: $description")
	if (description.startsWith("Err")) {
		log.debug("An error has occurred")
        updateStatus()
		} 
    else {
       
       	def cmd = zwave.parse(description.replace("98C1", "9881"), [0x98: 1, 0x20: 1, 0x84: 1, 0x80: 1, 0x60: 3, 0x2B: 1, 0x26: 1])
        log.debug "Parsed Command: $cmd"
        if (cmd) {
       	results = zwaveEvent(cmd)
        updateStatus()
		}
        
        if ( !state.numberOfButtons ) {        
    	state.numberOfButtons = "6"
        createEvent(name: "numberOfButtons", value: "6", displayed: false)
		updateStatus()
  		}else{
        sendEvent(name: "numberOfButtons", value: "6")
        }
    }
}
  
def describeAttributes(payload) {
    	payload.attributes = [
        [ name: "holdLevel",    type: "number",    range:"1..100", capability: "button" ],
       	[ name: "Button Events",    type: "enum",    options: ["#1 pushed", "#1 held", "#1 double clicked", "#1 click held", "#1 hold released", "#1 click hold released", "#2 pushed", "#2 held", "#2 double clicked", "#2 click held", "#2 hold released", "#2 click hold released", "#3 pushed", "#3 held", "#3 double clicked", "#3 click held", "#3 hold released", "#3 click hold released", "#4 pushed", "#4 held", "#4 double clicked", "#4 click held", "#4 hold released", "#4 click hold released", "#5 pushed", "#5 held", "#5 double clicked", "#5 click held", "#5 hold released", "#5 click hold released", "#6 pushed", "#6 held", "#6 double clicked", "#6 click held", "#6 hold released", "#6 click hold released"], momentary: true ],
    	[ name: "button",    type: "enum",    options: ["pushed", "held", "double clicked", "click held"],  capability: "button", momentary: true ],
        ]
    	return null
		}	
        

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
        def encapsulatedCommand = cmd.encapsulatedCommand([0x98: 1, 0x20: 1])
			//	log.debug("UnsecuredCommand: $encapsulatedCommand")
        // can specify command class versions here like in zwave.parse
        if (encapsulatedCommand) {
       // 	log.debug("UnsecuredCommand: $encapsulatedCommand")
                return zwaveEvent(encapsulatedCommand)
        }
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
		log.debug( "keyAttributes: $cmd.keyAttributes")
        log.debug( "sceneNumber: $cmd.sceneNumber")

       state.buttonnumber=cmd.sceneNumber     
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	  createEvent(descriptionText: "${device.displayName} woke up")
      log.debug("WakeUpNotification ${cmd.toString()}")
	  response(zwave.wakeUpV2.wakeUpNoMoreInformation())
      updateStatus()
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd){
	log.debug("WakeUpIntervalReport ${cmd.toString()}")
    state.wakeInterval = cmd.seconds
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd) {
	log.debug "Multichannel association report: $cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	log.debug "Multilevel report: $cmd"
    dimmerEvents(cmd)    
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
    state.lastLevel = cmd.value
	if (cmd.value && cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")   
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
	log.debug "Level: $cmd.value"
    setLevel(cmd.value)
    //sendEvent(name: "setLevel", value : cmd.value)
    sendEvent(name: "Button", value: "pushed",  data: [buttonNumber: state.buttonnumber ], isStateChange: true)
        sendEvent(name: "Button Events", value: "#$state.buttonnumber pushed" )
    //delayBetween([zwave.basicV1.basicSet(value: cmd.value).format()],5000)
   // dimmerEvents(cmd)
}
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStartLevelChange cmd) {
	log.debug "Multilevel Start CHange: $cmd"
    [createEvent(name:"switch", value:"on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
	log.debug "Multilevel Stop CHange: $cmd"
}
/*
def on() {
	sendEvent(tapUp1Response("digital"))
	delayBetween([
			zwave.basicV1.basicSet(value: 0xFF).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def off() {
	sendEvent(tapDown1Response("digital"))
	delayBetween([
			zwave.basicV1.basicSet(value: 0x00).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}
*/
def setLevel (value) {
	log.debug "setLevel >> value: $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
    /*
	if (level > 0) {
		sendEvent(name: "switch", value: "on")
	} else {
		sendEvent(name: "switch", value: "off")
	}
    */
	sendEvent(name: "level", value: level, unit: "%")
    def result = []
 
    result += response(zwave.basicV1.basicSet(value: level))
    result += response("delay 5000")
    result += response(zwave.switchMultilevelV1.switchMultilevelGet())
    result += response("delay 5000")
    result += response(zwave.switchMultilevelV1.switchMultilevelGet())
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
        log.debug("battery is $cmd.batteryLevel")
        def map = [ name: "battery", unit: "%" ]
        if (cmd.batteryLevel == 0xFF) {  // Special value for low battery alert
                map.value = 1
                map.descriptionText = "${device.displayName} has a low battery"
                map.isStateChange = true
        } else {
                map.value = cmd.batteryLevel
                log.debug ("Battery: $cmd.batteryLevel")
        }
        // Store time of last battery update so we don't ask every wakeup, see WakeUpNotification handler
        state.lastbatt = new Date().time
        sendEvent(map)
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd){
        log.debug "basic event: $cmd.value"
        dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd){
		log.debug "basic event Set: $cmd.value"
		
        sendEvent(name: "Button", value: "hold",  data: [buttonNumber: state.buttonnumber ], isStateChange: true)
        sendEvent(name: "sceneNumber", value: "$state.buttonnumber" , isStateChange: true)
        
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    log.debug("config is is: $cmd")
   
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
    log.debug("association groupings report  is: $cmd")
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
    log.debug("association report  is: $cmd")
}

def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
	//log.debug( "Dimming Duration: $cmd.dimmingDuration")
    //log.debug( "Button code: $cmd.sceneId")
   
    
    
}
 
def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def refresh() {
	configure()
    updateStatus()
}

  def configure() {
    
 
    def commands = [ ]
			log.debug "Resetting Sensor Parameters to SmartThings Compatible Defaults"
	def cmds = []
    cmds << zwave.associationV2.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId).format()
    cmds << zwave.associationV2.associationSet(groupingIdentifier: 2, nodeId: zwaveHubNodeId).format()
    cmds << zwave.associationV2.associationSet(groupingIdentifier: 3, nodeId: zwaveHubNodeId).format()
    cmds << zwave.associationV2.associationSet(groupingIdentifier: 4, nodeId: zwaveHubNodeId).format()
    cmds << zwave.associationV2.associationSet(groupingIdentifier: 5, nodeId: zwaveHubNodeId).format()
    cmds << zwave.wakeUpV2.wakeUpIntervalSet(seconds:86400, nodeid:zwaveHubNodeId).format()
    
    //cmds << zwave.switchmultilevelv1.SwitchMultilevelSet(value: 10).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 0x03, size: 1, configurationValue: [0]).format()
    cmds << zwave.batteryV1.batteryGet().format()
    delayBetween(cmds, 500)
}


       
private getBatteryRuntime() {
   def currentmillis = now() - state.batteryRuntimeStart
   def days=0
   def hours=0
   def mins=0
   def secs=0
   secs = (currentmillis/1000).toInteger() 
   mins=(secs/60).toInteger() 
   hours=(mins/60).toInteger() 
   days=(hours/24).toInteger() 
   secs=(secs-(mins*60)).toString().padLeft(2, '0') 
   mins=(mins-(hours*60)).toString().padLeft(2, '0') 
   hours=(hours-(days*24)).toString().padLeft(2, '0') 
 

  if (days>0) { 
      return "$days days and $hours:$mins:$secs"
  } else {
      return "$hours:$mins:$secs"
  }
}

def resetBatteryRuntime() {
    if (state.lastReset != null && now() - state.lastReset < 5000) {
        log.debug("Battery reset Double Press")
        state.batteryRuntimeStart = now()
        updateStatus()
    }
    state.lastReset = now()
}

private updateStatus(){
   def result = []
   if(state.batteryRuntimeStart != null){
        //sendEvent(name:"batteryRuntime", value:getBatteryRuntime(), displayed:false)
        sendEvent(name:"statusText2", value: "Battery: ${getBatteryRuntime()} Double tap to reset", displayed:true)
        
    } else {
        state.batteryRuntimeStart = now()
    }
}

def ping() {
   refresh()
}


/*
// Correct configure for dim events:

    for (def i = 11; i <= 12; i++) {
      	commands << zwave.associationV1.associationSet(groupingIdentifier: 2, nodeId: zwaveHubNodeId).format()
   // 	commands << zwave.sceneControllerConfV1.sceneControllerConfSet(groupId: 4, sceneId:i).format()
       commands << zwave.configurationV1.configurationSet(parameterNumber:i, size: 1, scaledConfigurationValue:4).format()
	}
        for (def i = 13; i <= 14; i++) {
      	commands << zwave.associationV1.associationSet(groupingIdentifier: 3, nodeId: zwaveHubNodeId).format()
    //	commands << zwave.sceneControllerConfV1.sceneControllerConfSet(groupId: 5, sceneId:i).format()
       commands << zwave.configurationV1.configurationSet(parameterNumber:i, size: 1, scaledConfigurationValue:4).format()
	}
	
    log.debug("Sending configuration")
	
    
    delayBetween(commands, 1250)
*/    
