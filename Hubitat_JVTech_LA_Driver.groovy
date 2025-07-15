/**
 *  Hubitat - JVTech Sensor Pulsador Driver
 *  
 *  DRIVER para utilizar com o Leitor LA de Pulso JVTECH: https://jvtech.gitbook.io/produtos-la-jvtech/produtos/leitor-la-de-pulso-jvtech
 *  Precisa também do APP.
 *
 *  Features:
 *  - Read contador from pulsos
 *
 *  1.0  - 15/07/2025  VH Beta 1.0
*/

metadata {
    definition(
        name: "JVTech-PulseSensor",
        namespace: "TRATO",
        author: "VH",
        description: "Driver for Pulse Sensors with 4 pulse counters"
    ) {
        capability "Sensor"
        
        attribute "serialNumber", "string"
        attribute "pulse1", "number"
        attribute "pulse2", "number"
        attribute "pulse3", "number"
        attribute "pulse4", "number"
        attribute "lastUpdate", "string"
        
        command "parseData", ["JSON_OBJECT"]
        command "ResetPulsos"
    }
    
    preferences {
        input name: "serialNumber", type: "text", title: "Sensor Serial Number", required: true
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: false
    }
}

def installed() {
    log.info "TRATO Pulse Sensor ${device.deviceNetworkId} installed"
    sendEvent(name: "serialNumber", value: settings.serialNumber)
    initializeAttributes()
}

def updated() {
    log.info "TRATO Pulse Sensor ${device.deviceNetworkId} updated"
    sendEvent(name: "serialNumber", value: settings.serialNumber)
    initializeAttributes()
}

def ResetPulsos() {
    sendEvent(name: "pulse1", value: 0)
    sendEvent(name: "pulse2", value: 0)
    sendEvent(name: "pulse3", value: 0)
    sendEvent(name: "pulse4", value: 0)
}


def initializeAttributes() {
    sendEvent(name: "pulse1", value: 0)
    sendEvent(name: "pulse2", value: 0)
    sendEvent(name: "pulse3", value: 0)
    sendEvent(name: "pulse4", value: 0)
}


def parseData(data) {
    if (logEnable) log.debug "TRATO Pulse Sensor parsing data: ${data}"
    
    
    // Update pulse values if they exist in the data
    if (data.pulse1 != null) {
        sendEvent(name: "pulse1", value: data.pulse1.toInteger())
    }
    
    if (data.pulse2 != null) {
        sendEvent(name: "pulse2", value: data.pulse2.toInteger())
    }
    
    if (data.pulse3 != null) {
        sendEvent(name: "pulse3", value: data.pulse3.toInteger())
    }
    
    if (data.pulse4 != null) {
        sendEvent(name: "pulse4", value: data.pulse4.toInteger())
    }
    
    // Update last communication time
    def now = new Date().format("yyyy-MM-dd HH:mm:ss", location.timeZone)
    sendEvent(name: "lastUpdate", value: now)
    
    if (logEnable) log.debug "Updated pulse values: ${device.currentValue('pulse1')}, ${device.currentValue('pulse2')}, ${device.currentValue('pulse3')}, ${device.currentValue('pulse4')}"
}
