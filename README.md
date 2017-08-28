# sdk-dslink-java (PRE-ALPHA)

* Version: 0.0.0
* JDK 1.6 Compatibility is required.
* [Developer Guide](https://iot-dsa-v2.github.io/sdk-dslink-java/DeveloperGuide)
* [Javadoc](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/)
* [License](https://en.wikipedia.org/wiki/ISC_license)


## Overview

This repository contains a Java SDK for creating [DSA](http://iot-dsa.org) links. to learn about 
the DSA architecture, please visit 
[this description of how DSA works](http://iot-dsa.org/get-started/how-dsa-works).

## Sub Projects

  - **/dslink-core** - The plumbing and APIs used to build new links.
  - **/dslink-java-template** - Boiler plate that can be copied to create a new dslink.
  - **/dslink-websocket-standalone** - Used by links that run in their own process rather
    than in a container that provides it's own websocket implementation.
    
## Link Development

Please see the [developer guide](https://iot-dsa-v2.github.io/sdk-dslink-java/DeveloperGuide) and 
the [Javadoc](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/).

## Acknowledgements

_Project Tyrus_

This software contains unmodified binary redistributions for 
[Project Tyrus](https://tyrus-project.github.io/), which is dual licensed 
and available under the CDDL 1.1 and GPL 2 with CPE.  An original copy of the license 
agreement can be found at: https://tyrus-project.github.io/license.html

_Silk Icons_

This software uses icons from Silk Icons 1.3 created by 
[Mark James](http://www.famfamfam.com/lab/icons/silk/) and licensed 
under a [Creative Commons Attribute 2.5 License](http://creativecommons.org/licenses/by/2.5/).

## History

* 0.1.0
  - Hello World.
