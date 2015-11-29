#!/bin/bash

# avoid virtual desktop jumps at mac terminal on test run
export JAVA_TOOL_OPTIONS='-Djava.awt.headless=true'

./gradlew :app:assembleProdDebug :app:testProdDebugUnitTest :app:sonarqube
