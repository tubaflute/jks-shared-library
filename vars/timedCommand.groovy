#!/usr/bin/env groovy

def call(body) {
    def settings = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = settings

    body()

    timestamps {
        cmdOutput = sh (script: "${settings.cmd}",returnStdout: true).trim()
    }
    echo cmdOutput
    writeFile file: "${settings.logFilePath}", text: "${cmdOutput}"
}