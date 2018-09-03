/**
 *  Virtual Device v1.0
 *     
 *  
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: 
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
	definition (name: "Virtual Device TV", namespace: "SmartThings", author: "Yu Chang Mang") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
		capability "Button"
		capability "Momentary"
		capability "Refresh"
						
		attribute "epNumber", "number"
		attribute "epDetails", "string"
		command "refreshData"		
		
		btns.each { btn ->
			attribute "btn${btn}Status", "string"
			attribute "btn${btn}Label", "string"
			command "pushButton${btn}"
		}
	}

	simulator {

	}
	
	preferences {			
		    
            input "Key", "text", title:"Key :", displayDuringSetup: true, required: false, defaultValue: ""
            input "URL", "text", title:"URL :", displayDuringSetup: true, required: false, defaultValue: ""
			input "btn1Label", "text",	title: "Power :", displayDuringSetup: true, required: false, defaultValue: ""
       		input "btn2Label", "text",	title: "1 :", displayDuringSetup: true, required: false, defaultValue: ""
			input "btn3Label", "text",	title: "2 :", displayDuringSetup: true, required: false, defaultValue: ""
			input "btn4Label", "text",	title: "3 :", displayDuringSetup: true, required: false, defaultValue: ""
            input "btn5Label", "text",	title: "4 :", displayDuringSetup: true, required: false, defaultValue: ""
            input "btn6Label", "text",	title: "5 :", displayDuringSetup: true, required: false, defaultValue: ""
            input "btn7Label", "text",	title: "6 :", displayDuringSetup: true, required: false, defaultValue: ""
            input "btn8Label", "text",	title: "7 :", displayDuringSetup: true, required: false, defaultValue: ""
            input "btn9Label", "text",	title: "8 :", displayDuringSetup: true, required: false, defaultValue: ""
            input "btn10Label", "text",	title: "9 :", displayDuringSetup: true, required: false, defaultValue: ""
            input "btn12Label", "text",	title: "0 :", displayDuringSetup: true, required: false, defaultValue: ""
            input "btn23Label", "text",	title: "Input :", displayDuringSetup: true, required: false, defaultValue: ""
            input "btn11Label", "text",	title: "Back :", displayDuringSetup: true, required: false, defaultValue: ""            
            input "btn13Label", "text",	title: "Home :", displayDuringSetup: true, required: false, defaultValue: ""
            input "btn14Label", "text",	title: "Vol↑ :", displayDuringSetup: true, required: false, defaultValue: ""
            input "btn20Label", "text",	title: "Vol↓ :", displayDuringSetup: true, required: false, defaultValue: ""
            input "btn16Label", "text",	title: "CH↑ :", displayDuringSetup: true, required: false, defaultValue: ""
            input "btn22Label", "text",	title: "CH↓ :", displayDuringSetup: true, required: false, defaultValue: ""
            input "btn18Label", "text",	title: "OK :", displayDuringSetup: true, required: false, defaultValue: ""
            input "btn15Label", "text",	title: "↑ :", displayDuringSetup: true, required: false, defaultValue: ""            
            input "btn17Label", "text",	title: "← :", displayDuringSetup: true, required: false, defaultValue: ""            
            input "btn19Label", "text",	title: "→ :", displayDuringSetup: true, required: false, defaultValue: ""            
            input "btn21Label", "text",	title: "↓ :", displayDuringSetup: true, required: false, defaultValue: ""
            
            
  /*          
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			displayDuringSetup: false, 
			required: false,
			defaultValue: debugOutputSetting
            */
	}

	tiles(scale: 2) {
 
    	standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label: "Refresh", action: "refresh", icon:"st.secondary.refresh-icon", backgroundColor: "#ffffff"
		}
		valueTile("epDetails", "device.epDetails", width: 2, height: 2, decoration: "flat") {
			state "default", label: '${currentValue}', backgroundColor: "#ffffff"			
		}

        standardTile("btn1Status", "device.btn1Status", width: 2, height: 2) {
			state "unassigned", label: 'Power\n(empty)', action: "pushButton1", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: 'Power', action: "pushButton1", nextState: "assigned", backgroundColor: assignedBtnColor        
		}	
		standardTile("btn2Status", "device.btn2Status", width: 2, height: 2) {
			state "unassigned", label: '1\n(empty)', action: "pushButton2", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: '1', action: "pushButton2", nextState: "assigned", backgroundColor: assignedBtnColor
		}
		standardTile("btn3Status", "device.btn3Status", width: 2, height: 2) {
			state "unassigned", label: '2\n(empty)', action: "pushButton3", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: '2', action: "pushButton3", nextState: "assigned", backgroundColor: assignedBtnColor
		}
		standardTile("btn4Status", "device.btn4Status", width: 2, height: 2) {
			state "unassigned", label: '3\n(empty)', action: "pushButton4", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: '3', action: "pushButton4", nextState: "assigned", backgroundColor: assignedBtnColor
		}
		standardTile("btn5Status", "device.btn5Status", width: 2, height: 2) {
			state "unassigned", label: '4\n(empty)', action: "pushButton5", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: '4', action: "pushButton5", nextState: "assigned", backgroundColor: assignedBtnColor
		}
		standardTile("btn6Status", "device.btn6Status", width: 2, height: 2) {
			state "unassigned", label: '5\n(empty)', action: "pushButton6", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: '5', action: "pushButton6", nextState: "assigned", backgroundColor: assignedBtnColor
		}
		standardTile("btn7Status", "device.btn7Status", width: 2, height: 2) {
			state "unassigned", label: '6\n(empty)', action: "pushButton7", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: '6', action: "pushButton7", nextState: "assigned", backgroundColor: assignedBtnColor
		}
		standardTile("btn8Status", "device.btn8Status", width: 2, height: 2) {
			state "unassigned", label: '7\n(empty)', action: "pushButton8", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: '7', action: "pushButton8", nextState: "assigned", backgroundColor: assignedBtnColor
		}
		standardTile("btn9Status", "device.btn9Status", width: 2, height: 2) {
			state "unassigned", label: '8\n(empty)', action: "pushButton9", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: '8', action: "pushButton9", nextState: "assigned", backgroundColor: assignedBtnColor
		}
		standardTile("btn10Status", "device.btn10Status", width: 2, height: 2) {
			state "unassigned", label: '9\n(empty)', action: "pushButton10", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: '9', action: "pushButton10", nextState: "assigned", backgroundColor: assignedBtnColor
		}
       	standardTile("btn11Status", "device.btn11Status", width: 2, height: 2) {
			state "unassigned", label: 'Back\n(empty)', action: "pushButton11", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: 'Back', action: "pushButton11", nextState: "assigned", backgroundColor: assignedBtnColor
		}	
       	standardTile("btn12Status", "device.btn12Status", width: 2, height: 2) {
			state "unassigned", label: '0\n(empty)', action: "pushButton12", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: '0', action: "pushButton12", nextState: "assigned", backgroundColor: assignedBtnColor
		}	
       	standardTile("btn13Status", "device.btn13Status", width: 2, height: 2) {
			state "unassigned", label: 'Home\n(empty)', action: "pushButton13", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: 'Home', action: "pushButton13", nextState: "assigned", backgroundColor: assignedBtnColor
		}	
       	standardTile("btn14Status", "device.btn14Status", width: 2, height: 2) {
			state "unassigned", label: 'Vol↑\n(empty)', action: "pushButton14", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: 'Vol↑', action: "pushButton14", nextState: "assigned", backgroundColor: assignedBtnColor
		}	
       	standardTile("btn15Status", "device.btn15Status", width: 2, height: 2) {
			state "unassigned", label: '↑\n(empty)', action: "pushButton15", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: '↑', action: "pushButton15", nextState: "assigned", backgroundColor: assignedBtnColor
		}	
       	standardTile("btn16Status", "device.btn16Status", width: 2, height: 2) {
			state "unassigned", label: 'CH↑\n(empty)', action: "pushButton16", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: 'CH↑', action: "pushButton16", nextState: "assigned", backgroundColor: assignedBtnColor
		}	
       	standardTile("btn17Status", "device.btn17Status", width: 2, height: 2) {
			state "unassigned", label: '←\n(empty)', action: "pushButton17", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: '←', action: "pushButton17", nextState: "assigned", backgroundColor: assignedBtnColor
		}	
       	standardTile("btn18Status", "device.btn18Status", width: 2, height: 2) {
			state "unassigned", label: 'OK\n(empty)', action: "pushButton18", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: 'OK', action: "pushButton18", nextState: "assigned", backgroundColor: assignedBtnColor
		}	
       	standardTile("btn19Status", "device.btn19Status", width: 2, height: 2) {
			state "unassigned", label: '→\n(empty)', action: "pushButton19", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: '→', action: "pushButton19", nextState: "assigned", backgroundColor: assignedBtnColor
		}	
       	standardTile("btn20Status", "device.btn20Status", width: 2, height: 2) {
			state "unassigned", label: 'Vol↓\n(empty)', action: "pushButton20", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: 'Vol↓', action: "pushButton20", nextState: "assigned", backgroundColor: assignedBtnColor
		}	
       	standardTile("btn21Status", "device.btn21Status", width: 2, height: 2) {
			state "unassigned", label: '↓\n(empty)', action: "pushButton21", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: '↓', action: "pushButton21", nextState: "assigned", backgroundColor: assignedBtnColor
		}	
       	standardTile("btn22Status", "device.btn22Status", width: 2, height: 2) {
			state "unassigned", label: 'CH↓\n(empty)', action: "pushButton22", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: 'CH↓', action: "pushButton22", nextState: "assigned", backgroundColor: assignedBtnColor
		}	
		standardTile("btn23Status", "device.btn23Status", width: 2, height: 2) {
			state "unassigned", label: 'Input\n(empty)', action: "pushButton23", defaultState: true, nextState: "unassigned", backgroundColor: unassignedBtnColor
			state "assigned", label: 'Input', action: "pushButton23", nextState: "assigned", backgroundColor: assignedBtnColor
		}
	
		main ("epDetails")
		details(["refresh", "epDetails", "btn1Status", "btn2Status", "btn3Status", "btn4Status", "btn5Status", "btn6Status", 
        "btn7Status", "btn8Status", "btn9Status", "btn10Status", "btn11Status", "btn12Status", "btn13Status", "btn14Status", "btn15Status", "btn16Status", "btn17Status",
        "btn18Status", "btn19Status", "btn20Status", "btn21Status", "btn22Status", "btn23Status"])    
	}
}

private getUnassignedBtnColor() { return "#ffffff" }
private getAssignedBtnColor() { return "#79b821" }


def updated() {        
            btns.each { btn ->        	
        	if (settings?."btn${btn}Label") {
					sendEventIfNew("btn${btn}Status", "assigned")
        		} else {
                	sendEventIfNew("btn${btn}Status", "unassigned")
                }			
		}    

}

private initialize() {

}

def refresh() {
 
	try {
            btns.each { btn ->
        	
        	if (settings?."btn${btn}Label") {
					sendEventIfNew("btn${btn}Status", "assigned")
        		} else {
                	sendEventIfNew("btn${btn}Status", "unassigned")
                }
		}		
	}
	catch (e) {
		logDebug "Refresh request to parent failed."
	}
}


private sendEventIfNew(name, value) {
	if (value != null && device.currentValue(name) != value) {
		sendEvent(createEventMap(name, value, false))
	}
}

private sendEventIfNull(name, value) {
	if (device.currentValue(name) == null) {
		sendEvent(createEventMap(name, value, false))
	}
}

def pushButton1() {

    try {
		httpPostJson(uri: settings?.URL+settings?.btn1Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}

}
def pushButton2() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn2Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton3() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn3Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton4() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn4Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton5() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn5Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton6() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn6Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton7() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn7Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton8() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn8Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton9() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn9Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton10() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn10Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton11() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn11Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton12() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn12Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton13() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn13Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton14() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn14Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton15() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn15Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton16() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn16Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton17() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn17Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton18() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn18Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton19() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn19Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton20() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn20Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton21() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn21Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton22() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn22Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}
def pushButton23() { 
    try {
		httpPostJson(uri: settings?.URL+settings?.btn23Label+settings?.Key)
	} catch (e) {
    	log.error "http post failed: $e"
	}
}


def parse(String description) {	
	logDebug "Unknown Description: $description"
}

// Settings
private getDebugOutputSetting() {
	return (settings?.debugOutput != false)
}
private getBtnLabelSetting(btn) {
	def settingName = "btn${btn}Label"
	return (settings && settings[settingName]) ? settings[settingName] : ""
}
private getBtnDelaySettingMilliseconds(btn) {
	return ((convertOptionSettingToInt(btnDelayOptions, getBtnDelaySetting(btn)) ?: 0) * 1000)
}
private getBtnDelaySetting(btn) {
	return getOptionSetting("btn${btn}Delay", btnDelayOptions)
}
private getBtnRepeatSetting(btn) {
	return getOptionSetting("btn${btn}Repeat", btnRepeatOptions)
}
private getBtnTriggerSettingEvents(btn) {
	def name = getOptionSetting("btn${btn}Trigger", btnTriggerOptions)
	return btnTriggerOptions.find { it.name == name }?.events ?: []
}
private getBtnTriggerSetting(btn) {
	return getOptionSetting("btn${btn}Trigger", btnTriggerOptions)
}
private getOptionSetting(settingName, options) {
	if (settings && settings["${settingName}"]) {
		return settings["${settingName}"]
	}
	else {
		return findDefaultOptionName(options)
	}
}

private getBtns() {
	def result = []
	(1..23).each { 
		result << it
	}
	return result
}

private getBtnDelayOptions() {
	def result = []
	result << [name: formatDefaultOptionName("No Delay"), value: 0]
	(1..5).each { // 1-5 Seconds
		def suffix = (it == 1 ? "" : "s")
		result << [name: "${it} Second${suffix}", value: it]
	}
	result << [name: "10 Seconds", value: 10]
	(1..4).each {
		result << [name: "${it * 15} Seconds", value: (it * 15)]
	}	
	return result
}

private getBtnRepeatOptions() {
	def result = []
	result << [name: formatDefaultOptionName("No Repeat"), value: 0]
	(1..23).each {
		result << [name: "${it}", value: it]
	}
	return result
}

private getBtnTriggerOptions() {
	return [		
		[name: formatDefaultOptionName("None"), events: [""]],
		[name: "Momentary Switch Push", events: ["push"]],
		[name: "Switch On", events: ["on"]],
		[name: "Switch Off", events: ["off"]],		
		[name: "Switch On/Off", events: ["on", "off"]]
	]
}

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

private createEventMap(name, value, displayed=null) {
	displayed = (displayed == null) ? (device?.currentValue("$name") != value) : displayed	
	return [
		name: name,
		value: value,
		displayed: displayed,
		isStateChange: true
	]
}

private safeToInt(val, defaultVal=-1) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private logDebug(msg) {
	if (debugOutputSetting) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "${msg}"
}