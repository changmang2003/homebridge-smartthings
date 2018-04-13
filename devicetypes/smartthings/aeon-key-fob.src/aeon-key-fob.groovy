metadata {
	// Fixed by Jordan Rejaud
	definition (name: "Aeon Key Fob", namespace: "SmartThings", author: "Yu Chang Mang") {
		capability "Actuator"
		capability "Button"
		capability "Configuration"
		capability "Sensor"
		capability "Battery"

        fingerprint mfr:"0086", prod:"0101", model:"0058",deviceId: "0x0101", deviceJoinName: "Aeon Key Fob"
        fingerprint inClusters: "0x5E,0x86,0x72,0x70,0x80,0x84,0x85,0x59,0x73,0x5A", outClusters: "0x2B,0x26"
	}

	simulator {
		status "pushed":  "command: 2001, payload: 01"
		status "held":  "command: 2001, payload: 15"
		status "button 2 pushed":  "command: 2001, payload: 29"
		status "button 2 held":  "command: 2001, payload: 3D"
		status "button 3 pushed":  "command: 2001, payload: 51"
		status "button 3 held":  "command: 2001, payload: 65"
		status "button 4 pushed":  "command: 2001, payload: 79"
		status "button 4 held":  "command: 2001, payload: 8D"
		status "wakeup":  "command: 8407, payload: "
	}
	tiles {
		standardTile("button", "device.button", width: 2, height: 2) {
			state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
			state "pushed", label: "pushed", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#79b821"
			state "held", label: "held", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffa81e"
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label:'電力 ${currentValue}%', unit:""
		}
		main "button"
		details(["button", "battery"])
	}
}

def parse(String description) {
	def results = []
	if (description.startsWith("Err")) {
	    results = createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description, [0x2B: 1, 0x80: 1, 0x84: 1])
		if(cmd) results += zwaveEvent(cmd)
		if(!results) results = [ descriptionText: cmd, displayed: false ]
	}
	// log.debug("Parsed '$description' to $results")
	return results
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	def results = [createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)]

	def prevBattery = device.currentState("battery")
	if (!prevBattery || (new Date().time - prevBattery.date.time)/60000 >= 60 * 53) {
		results << response(zwave.batteryV1.batteryGet().format())
	}
	results += configurationCmds().collect{ response(it) }
	results << response(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
	return results
}

def buttonEvent(button, held) {
	button = button as Integer
	if (held) {
		createEvent(name: "button", value: "held", data: [buttonNumber: button], descriptionText: "$device.displayName held", isStateChange: true)
	} else {
		createEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName pushed", isStateChange: true)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
	Integer button = ((cmd.sceneId + 1) / 2) as Integer
	Boolean held = !(cmd.sceneId % 2)
	buttonEvent(button, held)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
	} else {
		map.value = cmd.batteryLevel
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	[ descriptionText: "$device.displayName: $cmd", linkText:device.displayName, displayed: false ]
}

def configurationCmds() {
	[ zwave.configurationV1.configurationSet(parameterNumber: 250, scaledConfigurationValue: 1).format(),
	  zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId).format() ]
}

def configure() {
	def cmd = configurationCmds()
	log.debug("Sending configuration: $cmd")
	return cmd
}