package org.devops

def timedMavenBuild(task) {
    timestamps {
        sh "${tool 'maven-363'}/bin/mvn ${task}"
    }
}