/**
 *  Hubitat - JVTech Sensor Pulsador Driver
 *  
 *  DRIVER para utilizar com o Leitor LA de Pulso JVTECH: https://jvtech.gitbook.io/produtos-la-jvtech/produtos/leitor-la-de-pulso-jvtech
 *  Precisa também do APP.
 *
 *  Features:
 *  
 * 
 *  1.0  - 15/07/2025  VH Beta 1.0       - Read contador from pulsos
 *  1.1  - 16/07/2025  VH                - Mantem as contagens dentro da Hubitat. 
 *  									 - Detecta o restart do Leitor com um 0 seguido por 1
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
    state.lastPulse1 = 0
    state.lastPulse2 = 0
    state.lastPulse3 = 0
    state.lastPulse4 = 0
    state.sensorRestarted1 = false
    state.sensorRestarted2 = false
    state.sensorRestarted3 = false
    state.sensorRestarted4 = false
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
    state.lastPulse1 = 0
    state.lastPulse2 = 0
    state.lastPulse3 = 0
    state.lastPulse4 = 0
    state.sensorRestarted1 = false
    state.sensorRestarted2 = false
    state.sensorRestarted3 = false
    state.sensorRestarted4 = false
}

def initializeAttributes() {
    if (device.currentValue("pulse1") == null) sendEvent(name: "pulse1", value: 0)
    if (device.currentValue("pulse2") == null) sendEvent(name: "pulse2", value: 0)
    if (device.currentValue("pulse3") == null) sendEvent(name: "pulse3", value: 0)
    if (device.currentValue("pulse4") == null) sendEvent(name: "pulse4", value: 0)
}

def parseData(data) {
    if (logEnable) log.debug "TRATO Pulse Sensor parsing data: ${data}"
    
    // Initialize state variables if they don't exist
    if (state.lastPulse1 == null) state.lastPulse1 = 0
    if (state.lastPulse2 == null) state.lastPulse2 = 0
    if (state.lastPulse3 == null) state.lastPulse3 = 0
    if (state.lastPulse4 == null) state.lastPulse4 = 0
    if (state.sensorRestarted1 == null) state.sensorRestarted1 = false
    if (state.sensorRestarted2 == null) state.sensorRestarted2 = false
    if (state.sensorRestarted3 == null) state.sensorRestarted3 = false
    if (state.sensorRestarted4 == null) state.sensorRestarted4 = false
    
    // Get current values from device
    def currentPulse1 = device.currentValue("pulse1") ?: 0
    def currentPulse2 = device.currentValue("pulse2") ?: 0
    def currentPulse3 = device.currentValue("pulse3") ?: 0
    def currentPulse4 = device.currentValue("pulse4") ?: 0
    
    // Process each pulse counter
    if (data.pulse1 != null) {
        def newValue = data.pulse1.toInteger()
        def delta = calculateDelta(1, newValue, state.lastPulse1, state.sensorRestarted1)
        
        if (delta > 0) {
            sendEvent(name: "pulse1", value: currentPulse1 + delta)
            if (logEnable) log.debug "Pulse1: Added ${delta} to ${currentPulse1} (new value: ${currentPulse1 + delta})"
        }
        
        state.lastPulse1 = newValue
        state.sensorRestarted1 = (newValue == 0)
    }
    
    if (data.pulse2 != null) {
        def newValue = data.pulse2.toInteger()
        def delta = calculateDelta(2, newValue, state.lastPulse2, state.sensorRestarted2)
        
        if (delta > 0) {
            sendEvent(name: "pulse2", value: currentPulse2 + delta)
            if (logEnable) log.debug "Pulse2: Added ${delta} to ${currentPulse2} (new value: ${currentPulse2 + delta})"
        }
        
        state.lastPulse2 = newValue
        state.sensorRestarted2 = (newValue == 0)
    }
    
    if (data.pulse3 != null) {
        def newValue = data.pulse3.toInteger()
        def delta = calculateDelta(3, newValue, state.lastPulse3, state.sensorRestarted3)
        
        if (delta > 0) {
            sendEvent(name: "pulse3", value: currentPulse3 + delta)
            if (logEnable) log.debug "Pulse3: Added ${delta} to ${currentPulse3} (new value: ${currentPulse3 + delta})"
        }
        
        state.lastPulse3 = newValue
        state.sensorRestarted3 = (newValue == 0)
    }
    
    if (data.pulse4 != null) {
        def newValue = data.pulse4.toInteger()
        def delta = calculateDelta(4, newValue, state.lastPulse4, state.sensorRestarted4)
        
        if (delta > 0) {
            sendEvent(name: "pulse4", value: currentPulse4 + delta)
            if (logEnable) log.debug "Pulse4: Added ${delta} to ${currentPulse4} (new value: ${currentPulse4 + delta})"
        }
        
        state.lastPulse4 = newValue
        state.sensorRestarted4 = (newValue == 0)
    }
    
    // Update last communication time
    def now = new Date().format("yyyy-MM-dd HH:mm:ss", location.timeZone)
    sendEvent(name: "lastUpdate", value: now)
    
    if (logEnable) log.debug "Current pulse values: ${device.currentValue('pulse1')}, ${device.currentValue('pulse2')}, ${device.currentValue('pulse3')}, ${device.currentValue('pulse4')}"
}

private def calculateDelta(pulseNum, newValue, lastValue, sensorRestarted) {
    if (logEnable) log.debug "Calculating delta for pulse${pulseNum}: new=${newValue}, last=${lastValue}, restarted=${sensorRestarted}"
    
    // Special case: if we get 1 after a restart (0), we should count it
    if (sensorRestarted && newValue == 1) {
        if (logEnable) log.debug "Detected first pulse (1) after restart for pulse${pulseNum}"
        return 1
    }
    
    // Normal case: new value is greater than last value
    if (newValue > lastValue) {
        return newValue - lastValue
    }
    
    // If new value is 0, it's either a restart or no pulses yet
    if (newValue == 0) {
        return 0 // We'll handle the restart case when we get the first 1
    }
    
    // If new value is less than last value but not 0, assume counter wrapped around
    // (though this shouldn't happen with your sensor)
    if (newValue < lastValue) {
        return newValue
    }
    
    // No change
    return 0
}
