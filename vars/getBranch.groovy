#!/usr/bin/env groovy

def call(String stringInput = '') {
    try {
        if (!stringInput) {
            return "master"
        }

        // match origin
        def originPattern = ~/^origin\/(.+)$/
        def originResult = (stringInput =~ originPattern) ? (stringInput =~ originPattern)[0][1] : null

        // match git tag
        def tagPattern = ~/^v?\d+\.\d+\.\d+(-[a-zA-Z0-9]+)?$/
        def tagResult = (stringInput =~ tagPattern) ? stringInput : null

        // return originResult ?: tagResult ?: stringInput

        if (originResult != null) {
            return originResult
        } else if (tagResult != null) {
            return tagResult
        } else {
            return stringInput
        }
        
    } catch (Exception e) {
        return "master"
    }
}
