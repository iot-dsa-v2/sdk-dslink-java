# dslink-java-template

* Version: 0.0.0
* Java - version 1.6 and up.
* [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)


## Overview

This project can be used to create the boilerplate for a new Java DSLink.  In a nutshell, copy it, 
change org.iot.dsa.dlink.template to your package structure and globally replace 
'dslink-java-template' with your link name.

If you are not familiar with DSA, an overview can be found at
[here](http://iot-dsa.org/get-started/how-dsa-works).

This link was built using the Java DSLink SDK which can be found
[here](https://github.com/iot-dsa-v2/sdk-dslink-java).

## Files Requiring Modification

1. build.gradle
    - Group - Your organization's identifier.
    - Version - major.minor.patch.revision[-pre-release]
    - dependencies - Replace the sdk-dslink-java project dependencies with the commented out
      versions on the following line.
2. dslink.json
    - Change the rootType config to the fully qualified class name of your root node type.
    - Change XXX
3. README.md
    - Please maintain a useful readme.
    - Change the title link name.
    - Maintain a version number.
    - Change the license if necessary.
    - Provide an overview of the links purpose.  Maintain the links to the DSA overview
      and the core SDK.
    - Update the Link Architecture
    - Update the Node Guide
    - Maintain a version history.

## Link Development

Please see the [developer guide](https://iot-dsa-v2.github.io/sdk-dslink-java/DeveloperGuide) and 
the [Javadoc](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/) for the core SDK.

## Link Architecture

The outlines the hierarchy of nodes in this link and gives a brief description of each.

- _TemplateRoot_ - The only node in this link.
  - _TemplateChild_ - There is no child, this is just a documentation example.

## Node Guide

### TemplateRoot

Simple demonstration of a (root) node with a data value and an action.

**Values**
- Incrementing Int - Automatically updates whenever the node is subscribed.

**Actions**
- Reset - Resets the incrementing int to 0.

### TemplateChild

## Acknowledgements

SDK-DSLINK-JAVA

This software contains unmodified binary redistributions for 
[sdk-dslink-java](https://github.com/iot-dsa-v2/sdk-dslink-java), which is licensed 
and available under the Apache License 2.0. An original copy of the license agreement can be found 
at: https://github.com/iot-dsa-v2/sdk-dslink-java/blob/master/LICENSE

## History

* Version 0.0.0
  - Hello World


