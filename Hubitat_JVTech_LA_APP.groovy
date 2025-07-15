/**
 *  Hubitat - JVTech Sensor Pulsador APP
 *  
 *  APP para utilizar com o Leitor LA de Pulso JVTECH: https://jvtech.gitbook.io/produtos-la-jvtech/produtos/leitor-la-de-pulso-jvtech
 *  Precisa também do DRIVER. 
 *  Features:
 *  - Endpoint 
 *  - Allows multiple JVTech Sensors contador from pulsos
 *
 *  1.0  - 15/07/2025  VH Beta 1.0
 *
*/
definition(
    name: "JVTech Pulse Sensor Receiver",
    namespace: "TRATO",
    author: "VH",
    description: "Receives data from JVTECH Pulse Sensors and routes to appropriate devices",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",    
    oauth: true
)

preferences {
    page(name: "mainPage", install: true, uninstall: true) {
        section {
            paragraph "App for receiving pulse sensor data via HTTP POST requests"
            input "pulseDevices", "device.JVTech-PulseSensor", title: "Select Pulse Sensors", multiple: true, required: false
            input "logEnable", "bool", title: "Enable debug logging", defaultValue: true
            paragraph "Endpoint: ${getApiUrl("/pulse/data")}"     
        }
    }
}

mappings {
    path("/pulse/data") { 
        action: [
            POST: "handleData",
            GET: "handleData"
        ] 
    }
}

def installed() { initialize() }
def updated() { initialize() }
def initialize() { 
    if (!state.accessToken) createAccessToken() 
    if (logEnable) log.info "TRATO Pulse Sensor Receiver initialized with ${pulseDevices?.size() ?: 0} pulse sensors"
}


def handlDataLog(){
   
       
        if (logEnable) {
            log.debug "Request details - Method: ${request?.method}, ContentType: ${request?.contentType}, Headers: ${request?.headers}"
            log.debug "Request params: ${params}"
            log.debug "Request JSON: ${request?.JSON}"
            log.debug "Request text: ${request?.reader?.text}"
             log.debug "********************* LOG ************"
        }  
    
}



def handleData() {
    try {
        if (logEnable) {
            log.debug "Request details - Method: ${request?.method}, ContentType: ${request?.contentType}"
            log.debug "Request headers: ${request?.headers}"
            log.debug "Request params: ${params}"
        }
        
        // Try different ways to get the JSON data
        def jsonData = null
        
        // 1. First try to get from json_data parameter
        if (params.json_data) {
            try {
                jsonData = new groovy.json.JsonSlurper().parseText(params.json_data)
                if (logEnable) log.debug "Got JSON from json_data parameter: ${jsonData}"
            } catch (e) {
                log.error "Failed to parse json_data parameter: ${e}"
            }
        }
        
        // 2. Try regular request.JSON
        if (!jsonData && request.JSON) {
            jsonData = request.JSON
            if (logEnable) log.debug "Got JSON from request.JSON: ${jsonData}"
        }
        
        // 3. Try reading raw body
        if (!jsonData) {
            try {
                def bodyText = request.reader.text
                if (bodyText) {
                    jsonData = new groovy.json.JsonSlurper().parseText(bodyText)
                    if (logEnable) log.debug "Got JSON from raw body: ${jsonData}"
                }
            } catch (e) {
                log.error "Failed to parse raw body: ${e}"
            }
        }
        
        if (!jsonData) {
            log.error "No JSON data could be extracted from request"
            log.error "Params: ${params}"
            log.error "Raw body: ${request?.reader?.text}"
            return [status: 400, body: "No valid JSON data could be extracted"]
        }
        
        if (logEnable) log.debug "Processing JSON data: ${jsonData}"
        
        // Get serial number from incoming data
        def serialNumber = jsonData.SERIAL_NUMBER
        if (!serialNumber) {
            log.error "No serial number found in incoming data"
            return [status: 400, body: "No serial number specified"]
        }
        
        // Find device with matching serial number from all selected devices
        def targetDevice = findDeviceBySerialNumber(serialNumber)
        if (!targetDevice) {
            log.warn "No registered device found with serial number: ${serialNumber}"
            return [status: 404, body: "No device registered for this serial number"]
        }
        
        // Update last communication time
        def now = new Date().format("yyyy-MM-dd HH:mm:ss", location.timeZone)
        targetDevice.sendEvent(name: "lastUpdate", value: now)
        
        // Let the device parse its own data
        targetDevice.parseData(jsonData)
        
        return [status: 200, body: "Data routed to device ${targetDevice.label}"]
        
    } catch (Exception e) {
        log.error "Error processing data: ${e}", e
        return [status: 500, body: "Error processing data: ${e.message}"]
    }
}




private def findDeviceBySerialNumber(serialNumber) {
    // Find device where current serial number matches
    return pulseDevices.find { device -> 
        def currentSerial = device.currentValue('serialNumber')
        if (logEnable) log.trace "Checking pulse device ${device.label} (${device.deviceNetworkId}) with serial: ${currentSerial}"
        currentSerial == serialNumber
    }
}

def getApiUrl(path) {
    return "${getFullLocalApiServerUrl()}${path}?access_token=${state.accessToken}"
}
