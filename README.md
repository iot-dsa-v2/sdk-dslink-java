# sdk-dslink-java-v2
[![](https://jitpack.io/v/iot-dsa-v2/sdk-dslink-java-v2.svg)](https://jitpack.io/#iot-dsa-v2/sdk-dslink-java-v2)

* [Documentation](https://github.com/iot-dsa-v2/sdk-dslink-java-v2/wiki)
* [Javadoc](https://jitpack.io/com/github/iot-dsa-v2/sdk-dslink-java-v2/dslink-v2/master-SNAPSHOT/javadoc/)
* Java 8+


## Overview

This repository contains a Java SDK for creating [DSA](http://iot-dsa.org) links. 
To learn about the DSA architecture, please visit 
[this description of how DSA works](http://iot-dsa.org/get-started/how-dsa-works).

## Link Development

Please read the [developer guide](https://github.com/iot-dsa-v2/sdk-dslink-java-v2/wiki/DSLink-Development-Guide).

## Sub Projects

**/dslink-v2**

  - For links that wish to use a custom websocket library, such as in a servlet 
  container that already provides one.
  
**/dslink-v2-api**

  - The APIs to use for link development.
        
**/dslink-v2-websocket**

  - Link base that uses Tyrus, the reference implementation of JSR 356, for 
  websockets.  Most links should declare a dependency on this.
    
## JPMS Modules

This SDK is targeted at Java 8.  However, they can be used in the Java Platform
Module System with the following automatic module names:

  - **/dslink-v2** - org.iot.dsa.dslink.v2
  - **/dslink-v2-api** - org.iot.dsa.dslink.v2.api
  - **/dslink-v2-websocket** - org.iot.dsa.dslink.v2.websocket
  
## Dependency Management

During development JitPack will be used as the public repository.  For more information, 
[visit the JitPack page for this SDK.](https://jitpack.io/#iot-dsa-v2/sdk-dslink-java-v2)

The following examples show how to declare a dependency on a specific module 
which is the most common use case:

**Maven**
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    repository>
</repositories>

<dependency>
    <groupId>com.github.iot-dsa-v2.sdk-dslink-java-v2</groupId>
    <artifactId>dslink-v2-websocket</artifactId>
    <version>n.n.n</version>
    <type>pom</type>
</dependency>
```

**Gradle**
```
repositories {
    maven {
        url 'https://jitpack.io'
    }
}
dependencies {
    implementation 'com.github.iot-dsa-v2.sdk-dslink-java-v2:dslink-v2-websocket:n.n.n'
}
```

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
