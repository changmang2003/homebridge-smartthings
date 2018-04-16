/**
 *  ZigBee Button
 *
 *  Copyright 2015 Mitch Pond
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

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata {
    definition (name: "SR-ZG9001T-DIM", namespace: "SmartThings", author: "Yu Chang Mang") {
    capability "Actuator"
    capability "Sensor"
    capability "Battery"
    capability "Button"
    capability "Holdable Button"
    capability "Switch"
    capability "Switch Level"
    capability "Momentary"
    capability "Configuration"
    //   Health Check https://github.com/constjs/jcdevhandlers/commit/ea275dcf5b6ddfb617104e1f8950dd9f7916e276#diff-898033a1cdc1ae113328ecaeab60a1d6

    attribute "lastPress", "string"
    attribute "lastCheckin", "string"
	attribute "numberOfButtons", "number"
    attribute "sceneNumber", "number"
    

        fingerprint endpointId: "01", profileId: "0104", manufacturer: "SR", model: "ZGRC-TUS-006", deviceJoinName: "SR-ZG9001T-DIM"
        fingerprint inClusters: "0000, 0001, 0003, 0B05", outClusters: "0003, 0005, 0006, 0008, 0019, 0300"

    }

	simulator {
    status "button 1 pressed": "on/off: 0"
    status "button 1 released": "on/off: 1"
  }

  preferences{
    input ("holdTime", "number", title: "Minimum time in seconds for a press to count as \"held\"", defaultValue: 4, displayDuringSetup: false)
  }

  tiles(scale: 2) {

    multiAttributeTile(name:"button", type: "generic", width: 6, height: 4, canChangeIcon: true) {
      tileAttribute ("device.button", key: "PRIMARY_CONTROL") {
      	attributeState "default", label:'',  icon:"st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
        //attributeState "off", label: 'push', action: "momentary.push", backgroundColor:"#95cee2", nextState: "turningOn"
        //attributeState "turningOn", label: 'pushing', action: "momentary.push", backgroundColor:"#00a0dc"
        //attributeState "on", label: 'push', action: "momentary.push", backgroundColor:"#00a0dc"
      }

      tileAttribute("device.battery", key: "SECONDARY_CONTROL") {
				attributeState "default", label:'${currentValue}%', unit:"%", icon:"https://raw.githubusercontent.com/tommysqueak/xiaomi-button/master/icons/battery.png"
			}
    }

    valueTile("lastCheckin", "device.lastCheckin", decoration: "flat", inactiveLabel: false, width: 4, height: 1) {
      state "lastCheckin", label:'Last check-in:\n ${currentValue}'
    }

    standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
      state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
    }

    standardTile("menuButton", "device.button", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
    		state "default", label:'',  icon:"st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
    }

    main (["menuButton"])
    details(["button", "lastCheckin", "configure"])
  }
}

def parse(String description) {
  log.debug "Parsing '${description}'"

  def results = []
  if (description?.startsWith('on/off: '))
    results = parseButtonActionMessage(description)
  if (description?.startsWith('catchall:'))
    results = parseCatchAllMessage(description)

  def now = new Date().format("EEE, d MMM yyyy HH:mm:ss",location.timeZone)
  results << createEvent(name: "lastCheckin", value: now, displayed: false)

  return results;
}

def configure(){
  // http://www.silabs.com/documents/public/miscellaneous/AF-V2-API.pdf
  [
    "zdo bind 0x${device.deviceNetworkId} 1 2 0 {${device.zigbeeId}} {}", "delay 5000",
    "zcl global send-me-a-report 2 0 0x10 1 0 {01}", "delay 500",
    "send 0x${device.deviceNetworkId} 1 2"
  ]
  initialize()
}

private ArrayList parseCatchAllMessage(String description) {
  def cluster = zigbee.parse(description)
  log.debug cluster
	//log.debug Math.round(cluster.data[0]/2.575)
  if(cluster && cluster.clusterId == 0x0000) {
    return [createBatteryEvent(cluster.data.last())]
  }
  else {
  	switch (cluster.clusterId) {
    	case 0x0006:
        	sendEvent(name: "button", value: "held", data: [ buttonNumber: device.currentValue('sceneNumber') ], isStateChange: true)            
            break
        case 0x0005:
        	sendEvent(name: "sceneNumber", value: cluster.data[2], displayed: false)
            break
        case 0x0008:
        	sendEvent(name: "level", value: Math.round(cluster.data[0]/2.575), unit: "%", displayed: false)
            sendEvent(name: "Button", value: "pushed",  data: [buttonNumber: device.currentValue('sceneNumber') ], isStateChange: true)
        	break
        default: 
    		break
    }
    return []
  }
}


private ArrayList parseButtonActionMessage(String message) {
  if (message == 'on/off: 0')     //button pressed
    return createPressEvent()
  else if (message == 'on/off: 1')   //button released
    return createButtonEvent()
}

//this method determines if a press should count as a push or a hold and returns the relevant event type
private ArrayList createButtonEvent() {
  def currentTime = now()
  def startOfPress = device.latestState("lastPress").date.getTime()
  def timeDif = currentTime - startOfPress
  def holdTimeMillisec = (settings.holdTime?:3).toInteger() * 1000

  if (timeDif < 0) {
    log.debug "Message arrived out of sequence. lastPress: ${startOfPress} and now: ${currentTime}"
    return []  //likely a message sequence issue. Drop this press and wait for another. Probably won't happen...
  }
  else if (timeDif < holdTimeMillisec) {
    log.debug "Button pushed. ${timeDif}"
    return [createEvent(name: "button", value: "pushed", data: [buttonNumber: 1], isStateChange: true)]
  }
  else {
    log.debug "Button held. ${timeDif}"
    return [createEvent(name: "button", value: "held", data: [buttonNumber: 1], isStateChange: true)]
  }
}

private ArrayList createPressEvent() {
  return [createEvent(name: "lastPress", value: now(), data:[buttonNumber: 1], displayed: false)]
}

void push() {
  sendEvent(name: "switch", value: "on", displayed: false)
  sendEvent(name: "switch", value: "off", displayed: false)
  sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], isStateChange: true)
}

void on() {
  push()
}

void off() {
  push()
}

void initialize() {
  //  Configure the initial state.
  sendEvent(name: "numberOfButtons", value: 6, displayed: false)
  sendEvent(name: "sceneNumber", value: 1, displayed: false)
  sendEvent(name: "level", value: 1, unit: "%", displayed: false)
}

void installed() {
	initialize()
}

void updated() {
	initialize()
}

private createBatteryEvent(rawValue) {
  log.debug "Battery '${rawValue}'"

  int batteryLevel = rawValue
  int maxBatteryLevel = 100

  if (batteryLevel > maxBatteryLevel) {
    batteryLevel = maxBatteryLevel
  }

  def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {  // Special value for low battery alert
		map.value = 1
		map.descriptionText = "Low Battery"
	} else {
		map.value = batteryLevel
	}

  return createEvent(map)
}