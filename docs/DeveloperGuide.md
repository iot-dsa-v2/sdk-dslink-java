# DSLink Java Developer Guide

[Home](https://github.com/iot-dsa-v2/sdk-dslink-java) • [Javadoc](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/)

## Warning

Only use org.iot.dsa APIs, do not use or depend on anything in the com.* packages.

## Overview

The purpose of this document is to guide the reader through the development of Java DSA links using
this SDK. Developers will build links using the org.iot.dsa.* packages found in 
dslink-core sub-project.

Key objectives of this SDK:

  - Pluggable architecture:
    - Protocol independence.  Primarily to support DSA V1 and V2.
    - Transport independence.  Websockets, plain sockets, http, and whatever else comes along.
  - Support multiple links in the same process.
  - Support JDK 1.6 for Distech Controls.
  - High performance for activities such as video streaming.
  - 3rd party library independence.  Some environments such as Niagara provide transport libraries 
    while others do not.  SLF4J and Netty were explicitly bound to the original SDK but can not be 
    used in Niagara because of it's strict Security Manager.

## Creating a Link

These are the major steps you'll take when creating a link:

1. Create link boiler plate.
 
2. Create a root node.

3. Create application nodes.

## Project Boiler Plate

Copy the dslink-java-template subproject to create a new repository.  It's README provides
further instructions.

## Create a Main Class

All this needs to do is create a link and run it as follows.

```java
    public static void main(String[] args) throws Exception {
        DSLinkConfig cfg = new DSLinkConfig(args);
        DSLink link = new DSLink(cfg);
        link.run();
    }
```

The main class must be specified in build.gradle.  It can be the root node of the link.

## Create a Root Node

A root node must be DSNode subclass.

If your link is to be a responder, it must also implement DSResponder. If you will be modeling 
data as a DSNode tree, all you need to do is subclass **org.iot.dsa.dslink.DSRootNode**.

The fully qualified class name must be specified as the **rootType** in _dslink.json_.

If you want to proxy another model and don't need a persistent node tree, you should subclass 
DSNode and implement org.iot.dsa.dslink.DSResponder.

You can also create a hybrid solution by subclassing DSRootNode, but implementing DSResponder in 
nodes lower in the tree.  If a node in a request path implements DSResponder, it will be their
responsibility for processing the request.  Paths of the request will have the path leading to the
responder removed.  This approach can be used to get some configuration persistence without 
having to model everything as a DSNode or DSIValue.

## Creating Application Nodes

The hook for links' functionality is the node tree.  Links will subclass 
org.iot.dsa.node.DSNode, configure the default child values and nodes, and use the various 
lifecycle callbacks to trigger custom functionality.

### Defaults

Nodes use a type of instance based inheritance.  Every subtype of DSNode has a private default 
instance, all other instances of any particular type are copies of the default instance.  This is
why you should never perform application logic unless your node is running (started or stable).

If a DSNode subtype needs to have specific child nodes or values (most will), it should override
the declareDefaults method.  The method should:

1. Call super.declareDefaults();
2. Call DSNode.declareDefault(String name, DSIObject child) for each permanent (non-removable)
child.  Do not add dynamic children in declareDefaults, because if they are removed, they will be
re-added the next time the link is restarted.

### Node Lifecycle

It is important to know the application lifecycle.  Your nodes should not execute any application
logic unless they are running (started or stable).

**Stopped**

A node is instantiated in the stopped state.  If a node tree has been persisted, will be be fully
restored in the stopped state.  DSNode.onStopped will not be called.

When nodes are removed from a stable parent node, they will be stopped.  DSNode.onStopped will be 
called after all of the child nodes have been stopped.

When a link is stopped, an attempt to stop the tree will be made, but it cannot be guaranteed.

**Started**

After the node tree is fully restored it will be started.  DSNode.onStart will be called after all
of it's child nodes have been started.  There is no guarantee anywhere else in the node tree is
started.

Nodes will also started when they are added to a stable parent node.

**Stable**

Stable is called all nodes have been started.  The first time the tree is loaded, there is a 
stable delay of 5 seconds.  This is configurable as **stableDelay** in _dslink.json_.

Nodes added to an already stable parent will have onStart and onStable called immediately.

When in doubt of whether to use onStarted or onStable, use onStable.

**Other Callbacks**

When a node is stable, there are several other callbacks for various state changes.  All callbacks
begin with **on** such as _onChildAdded()_.  See the [DSNode Javadoc]() for a complete list.

### Subscriptions

Nodes should suspend, or minimize activity when nothing is interested in them.  To do this use
the following APIs:

* DSNode.onSubscribed - Called when the node transitions from unsubscribed to subscribed.  This is
not called for subsequent subscribers once already in the subscribed state.
* DSNode.onUnsubscribed - Called when the node transitions from subscribed to unsubscribed.   If
there are multiple subscribers, this is only called when the last one unsubscribes.
* DSNode.isSubscribed - Tell the caller whether or not the node is subscribed.


### Values

DSValues represent leaf members of the node tree. They can also be sub-divided into two categories:
  1. Elements
  2. Synthetics

Elements map directly to JSON type system.  They do not require the persistence of additional
typing meta-data to decode.  If new value types are needed, they must be synthetic, no new
elements can be added unless JSON evolves or is abandoned.

Synthetics persist themselves using the core JSON types, but require typing meta-data so that
they can be encoded and decoded properly.


## Metadata

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

### DSInfo

All node children have a corresponding DSInfo instances.  This class serves serves two purposes:

1. It carries some meta-data defining the relationship between the parent node and the child.
2. It tracks whether or not the child matches a declared default.

Important things for developers to know about DSInfo are:

* You can configure several flags such as transient, readonly and hidden.
* You can declare fields in the your Java class for (declared default) info instances to avoid
looking up the child every time it is needed.  This is can be used to create getters and setters.

Without declaring fields (lookups required):

```java
    public void declareDefaults() {
        super.declareDefaults();
        declareDefault("The Int", DSInt.valueOf(0));
    }
    public int getTheInt() {
        DSInt theInt = (DSInt) get("The Int");
        return theInt.toInt();
    }
    public void setTheInt(int value) {
        put("The Int", DSInt.valueOf(value));
    }
```

With declared fields:

```java
    private DSInfo theInt = getInfo("The Int"); //will be null in the default instance
    public void declareDefaults() {
        super.declareDefaults();
        declareDefault("The Int", DSInt.valueOf(0));
    }
    public int getTheInt() {
        return theInt.toInt();
    }
    public void setTheInt(int value) {
        put(theInt, DSInt.valueOf(value));
    }
```







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

