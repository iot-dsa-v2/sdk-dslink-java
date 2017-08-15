# dslink-java-template

* Version: 0.0.0
* JDK 1.6 and up.
* [License](https://en.wikipedia.org/wiki/ISC_license)


## Overview

This project can be used to create a new link.  In a nutshell, copy it, change 
org.iot.dsa.dlink.template to your package structure and globally replace 'template' with your link 
name.

## Files Requiring Modification
1. build.gradle
    - Group - Your organization's identifier.
    - Version - major.minor.patch[-pre-release]
    - mainClassName - You will probably use the main class, but your package will be 
      different.
    - dependencies - Replace the sdk-dslink-java project dependencies with the commented out
      versions on the following line.
2. dslink.json
    - TODO
3. README.md
    - Please maintain a readme with a version history.
4. Main.java
    - Be sure to change the directory structure to match your package.

## Link Development

Please see the [developer guide](https://iot-dsa-v2.github.io/sdk-dslink-java/DeveloperGuide) and 
the [Javadoc](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/) for the core SDK.

## Acknowledgements

Attributions belong here.

## History

* Version 0.0.0
  - Hello World


