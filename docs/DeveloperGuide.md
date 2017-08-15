# DSLink Java Developer Guide

[Home](https://github.com/iot-dsa-v2/sdk-dslink-java) - [Javadoc](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/)

## Warning

Only use org.iot.dsa APIs, do not use or depend on anything in the com.* packages.

## Overview

The purpose of this document is to guide the reader through the development of Java DSA links using
this SDK. Developers will build links using the org.iot.dsa.* packages found in 
sdk-dslink-java/dslink-core.

Key objectives of this SDK:

  - Pluggable architecture:
    - Protocol independence.  Primarily to support DSA V1 and V2.
    - Transport independence.  Websockets, plain sockets, http, and whatever else comes along.
  - Support multiple links in the same process.
  - Support JDK 1.6 for Distech Controls.
  - High performance for video streaming.
  - 3rd party library independence.  Some environments such as Niagara provide transport libraries 
    while others do not.  SLF4J and Netty were explicitly bound to the original SDK but can not be 
    used in Niagara because of it's strict Security Manager.

There are three ways to create a link.

1. The easy way.  Use nodes and values defined in org.iot.dsa.node.  You will get
free configuration persistence and DSA protocol translation.

2. The hard way.  Implement your own org.iot.dsa.dslink.DSResponder.

3. A combination of 1 and 2.

## Creating Links the Easy Way

1. Boiler plate.  Copy the dslink-java-template module from this repo.  It has everything needed
to create a standalone link.  It's README talks about what needs to be modified.

2. Create a root node.  This node should subclass org.iot.dsa.dslink.DSRootNode.  The main class
in the template module already does this.

3. Create nodes and values specific to your link's functionality.

### Project Boiler Plate

The dslink-java-template subproject in this repository can be copied to make a standalone link
repository.

### Create a Root Node

The root node type is specified in dslink.json

### Creating Nodes and Values

## Link

This package defines a data model that supports persistent
There are a few key classes / interfaces that define the general structure of a link and the SDK.

    - Link (org.iot.dsa.dslink.DSLink)
      - Connection (org.iot.dsa.dslink.DSLinkConnection)
      - Node Tree (org.iot.dsa.node.DSNode)
        - Responders (org.iot.dsa.dslink.DSResponder)
    

### org.iot.dsa.dslink.DSLink

A DSLink is a concrete class that represents an upstream connection as well a root node.  The 
link is typically bound to a connection implementation and a module's root node through 
configuration.

The main method of a process is responsible for instantiating a DSLink instance with configuration 
and calling DSLink.start.

### org.iot.dsa.dslink.DSLinkConnection

DSLinkConnection is an interface.  The implementing class is specified through configuration and
the DSLink instance is responsible for instantiating it.

The default implementation is com.acuity.iot.dsa.dslink.DSConnection.  It manages a protocol 
implementation and a transport implementation.  The protocol implementation is negotiated when 
making an upstream connection.  The transport is specified through configuration.

### org.iot.dsa.dslink.DSNode

The root node of a DSLink is a DSNode subclass which is specified through configuration.  It is
the root node that represents the unique functionality of a module.

The DSNode tree is automatically persisted.  If a module does not require or want persistence it
can implement the DSResponder interface for processing requests.

### org.iot.dsa.dslink.DSResponder

This interface can be used for processing DSA requests without modeling everything in the DSNode
tree.  The root node does not have to be the implementor of this interface, there can be several
instances lower in the tree.  

Consider a module that needs to persist configuration for connecting to remote devices 
(such as credentials).  The nodes representing devices could have a child node named
"points" that implements DSResponder.  "Points" would then handle all point related requests without 
having to model or persist all points in the configuration database.

## Creating Modules

Modules encapsulate functionality that makes links unique.  It is where most, if not all
software development for a link takes place.

### Pick a Directory

There are three possible directories for your module:

**dslink-java/modules/applications**
  - Other than file IO, applications limit their communication to the DSA graph using the DSA
  protocol.  Examples of applications are horizontal services such as historians and alarms.
  
**dslink-java/modules/core**
  - Core modules provide APIs used by other modules.

**dslink-java/modules/drivers**
  - Drivers communicate with foreign systems using protocols other than DSA.  Examples of drivers
  include BACnet, MODBUS as well as a company's web services.

### Create a Main Class

The main class has the application main method and is responsible for creating a DSLinkConfig 
instance, constructing a DSLink with the config, and starting it.  For example:

```java
package com.acuity.iot.dsa.example;

import org.iot.dsa.dslink.*;

public class Main {

    public static void main(String[] args) throws Exception {
        DSLinkConfig cfg = new DSLinkConfig(args);
        DSLink link = new DSLink(cfg);
        link.start();
    }
    
}
```

**Configuration**

In the example above, the command line arguments will override the configuration as specified
in dslink.json found in the link home directory.  Configuration will also specify:

  - _rootName_ - If the link does not have a root with this name, it will instantiate an instance
  of the _rootType_ and add it to the true root object of the link.
  
  - _rootType_ - This is a org.iot.dsa.node.DSNode subclass, it is the hook for a module's unique 
  functionality.

**Notes**

  - The DSLinkConfig instance can be completely configured programmatically for special
  circumstances such as testing.
  
  - The root node will be persisted, but it is not necessary to have any more nodes in it's
  subtree tree.  The root node can implement the DSResponder interface to process 
  requests.  Likewise, children lower in the tree can implement DSResponder as some 
  configuration persistence is typically needed (device address, credentials, etc).

### Create a Node Tree

The node tree is where a module plugs in its unique functionality.  At the very least a link
will have a root DSNode subclass.  If that node implements responder interface, then no
other nodes are necessary.

Nodes can perform nearly any functionality.  Common scenarios will be modeling foreign
 systems such as BACnet device networks. Other links can be applications such as alarming and 
 trending.

**The node API is still in design.  Until it is complete, simply subclass DSNode and implement 
org.iot.dsa.dslink.DSResponder.**

### Create a Responder

A DSResponder handles DSA requests directly.  If modeling a foreign system, or pieces of it will
be too heavy weight, a DSResponder can act as a lightweight proxy to the system.

DSResponders must be DSNode subclasses and are automatically discovered by the the underlying
system.  When a request passes through a responder, the responder is responsible for processing it,
even if the responder node has child nodes.  This also means responder nodes cannot have
descendant responder nodes unless the parent responder knows how to delegate requests.

The responder API is pretty straight forward, the Javadoc should be enough to understand what is
needed by the implementation.

## Creating Deployables

Deployables bind one or more modules to a specific deployment scenario.  In most cases this will 
only require a build.gradle and dslink.config.

The most common deployable will be with a standalone websocket library, although container
deployments such as Jetty and Tomcat will be common.

build.gradle simply declares the dependencies.  Your module and the transport.

dslink.json is covered in more detail in configuration section.  The most important variables 
regarding deployables are root name and root type.

## Node Data Model

Classes making up the core data model can be found in the package org.iot.dsa.node. They can
 generally be divided into two major categories:
  1. Values
  2. Containers

The object model looks very similar;
  - org.dsa.iot.node.DSObject
    - org.dsa.iot.node.DSValue
    - org.dsa.iot.node.DSContainer

### DSValue

DSValues represent leaf members of the node tree. They can also be sub-divided into two categories:
  1. Elements
  2. Synthetics

Elements map directly to JSON type system.  They do not require the persistence of additional
typing meta-data to decode.  If new value types are needed, they must be synthetic, no new
elements can be added unless JSON evolves or is abandoned.

Synthetics persist themselves using the core JSON types, but require typing meta-data so that
they can be encoded and decoded properly.

### DSContainer

DSContainer contain values and other containers. At this time DSContainer is an abstract class 
and there is only one subclass, DSNode.  In the future we may want to add another type of container 
that isn't dynamic, doesn't allow container children and is more of a DSValue in that when any 
of it's members change, eventing would treat that as the whole container changing.  
It's unclear if this can be supported in the protocol at the moment.

DSContainers leverage two key concepts:
  1. Metadata
  2. Defaults
  
**Metadata**

Every child member of a container has a corresponding DSObjectInfo.  This info stores metadata
about the child.  In fact, child instance cannot be directly stores are fields, but the info
can.  For example, the following is incorrect:

    public class MyNode extends DSNode {
      private MyNode myChild = new MyNode();
    }

Instead, it would be stored as follows:

    public class MyNode extends DSNode {
      private DSObjectInfo myChild = getInfo("myChild");
    }
    
The second example skips over how the myChild instance was instantiated, but to understand that
one need to first understand defaults.

**Defaults**

The node data model uses defaults extensively.  In a nutshell, all DSNode subclasses have a
default instance in memory.  All other instances of the same type reference the default values
in that instance and only store their differences.

This has some benefits:
  - Reduced memory usage.  This will reduce the number of objects in very large graphs.
  - Upgrade-ability.  Default values can easily be changed for an existing instances
  since they are only recording their differences.

Defaults are defined in a single method and it is only called for the default instance.  The
method is cleverly named initializeDefaults().  Here is an example:

    protected void initializeDefaults() {
      addDefault("Can Not Be Removed", DSElement.valueOf(true));
    }

Only DSValue defaults are tracked, it isn't necessary to build a complex DSNode children in a 
default instance.
    
## Creating DSNode Subclasses

Creating a DSNode subclass requires an understanding of these key concepts:
  - Names
  - Defaults
  - Lifecycle

**Names**

The DSNode API does not restrict names in any way.  It will encode names properly when reading
and writing paths.

    Names should only be encoded (escaped) in paths.  The developer will rarely have to do this.

There are two naming issues to be aware of:

  1. DSA Attributes start with @
  2. DSA Configs start with $

Unless the developer understands these concepts and has a specific reason, they should not start 
their names with $ or @.  If user

**Defaults**

Defaults are created in the initializeDefault method.  This method is only called once on  first
creating an instance of a DSNode subclass.  In initializeDefaults, the implementor will add

TODO

**Lifecycle**

TODO

## Configuration

TODO

## Logging

TODO

When possible subclass DSLogger.  DSNode does so it should not be an issue.  This will allow us to 
plug in other loggings packages should it be necessary.

Create multiple loggers, the level of each will be individually controllable in the future.

It's async so it fast.  Take advantage of DSLogger for efficiency.

Level definitions

