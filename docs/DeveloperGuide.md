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

1. Create link boilerplate.
2. Create a main class.
3. Create a root node.
4. Create application nodes.

## Create Link Boilerplate

Copy the dslink-java-template subproject to create a new repository.  It's README provides
further instructions for customization.

## Create a Main Class

All this needs to do is create a link and run it as follows.

```java
    public static void main(String[] args) throws Exception {
        DSLinkConfig cfg = new DSLinkConfig(args);
        DSLink link = new DSLink(cfg);
        link.run();
    }
```

The main class must be specified in build.gradle.  In the template project, the main class is
the root node.

    mainClassName = 'org.iot.dsa.dslink.template.Main'

## Create a Root Node

A root node must be subclass of 
[org.iot.dsa.node.DSNode](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/index.html?org/iot/dsa/node/DSNode.html).

If your link is to be a responder, it must also implement 
[org.iot.dsa.dslink.DSResponder](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/index.html?org/iot/dsa/dslink/DSResponder.html).

If you will be modeling data as a DSNode tree, then all you need to do is subclass 
[org.iot.dsa.dslink.DSRootNode](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/index.html?org/iot/dsa/dslink/DSRootNode.html).

The fully qualified class name must be specified as the config **rootType** in _dslink.json_.

If you want to proxy another model and don't need a persistent node tree, you should subclass 
DSNode and implement DSResponder.

You can also create a hybrid solution by subclassing DSRootNode, but implementing DSResponder in 
nodes lower in the tree.  If a node in a request path implements DSResponder, it is their
responsibility for processing the request.  Paths of the request will have the path leading to the
responder removed.  This approach can be used to get some configuration persistence without 
having to model everything as a DSNode or DSIValue.

## Create Application Nodes

The hook for links' functionality is the node tree.  Links will subclass 
[org.iot.dsa.node.DSNode](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/index.html?org/iot/dsa/node/DSNode.html) 
and do the following:

1. Configure the default children (nodes, values and actions).
2. Use various lifecycle callbacks to trigger custom functionality.

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

During node serialization (configuration database, not DSA interop), children that match their 
declared default are omitted.  This has two benefits:
1. Smaller node database means faster serialization / deserialization.
2. Default values can be modified and all existing database will be automatically upgraded when next
time updated class loaded.

### Node Lifecycle

It is important to know the application lifecycle.  Your nodes should not execute any application
logic unless they are running (started or stable).

**Stopped**

A node is instantiated in the stopped state.  If a node tree has been persisted, will be be fully
restored in the stopped state.  DSNode.onStopped will not be called, it is only called when nodes
transition from running to stopped.

When nodes are removed from a running parent node, they will be stopped.  DSNode.onStopped will be 
called after all child nodes have been stopped.

When a link is stopped, an attempt to stop the tree will be made, but it cannot be guaranteed.

**Started**

After the node tree is fully deserialized it will be started.  A node's onStart method will be 
called after all of its child nodes have been started.  The only guarantee is that all child
nodes have been started.

Nodes will also started when they are added to an already running parent node.

**Stable**

Stable is called all nodes have been started.  The first time the node tree is loaded, there is a 
stable delay of 5 seconds.  This is configurable as **stableDelay** in _dslink.json_.

Nodes added to an already stable parent will have onStart and onStable called immediately.

When in doubt of whether to use onStarted or onStable, use onStable.

**Other Callbacks**

When a node is stable, there are several other callbacks for various state changes.  All callbacks
begin with **on** such as _onChildAdded()_.  See the 
[DSNode Javadoc](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/index.html?org/iot/dsa/node/DSNode.html) 
for a complete list.

### Subscriptions

Nodes should suspend, or minimize activity when nothing is interested in them.  For example, if 
nothing is interested in a point, it is best to not poll the point on the foreign system.  
To do this you use the following APIs:

* DSNode.onSubscribed - Called when the node transitions from unsubscribed to subscribed.  This is
not called for subsequent subscribers once in the subscribed state.
* DSNode.onUnsubscribed - Called when the node transitions from subscribed to unsubscribed.   If
there are multiple subscribers, this is only called when the last one unsubscribes.
* DSNode.isSubscribed - Tells the caller whether or not the node is subscribed.

### Values

Values mostly represent leaf members of the node tree.  There are two types of values:

1. [org.io.dsa.node.DSElement](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/index.html?org/iot/dsa/node/DSElement.html) - 
These map to the JSON type system and represent leaf members of the node tree.
2. [org.io.dsa.node.DSIValue](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/index.html?org/iot/dsa/node/DSIValue.html) - 
These don't map to the JSON type system, and it is possible for nodes to implement this interface.  
This allows for values to have children.

Many values are singleton instances.  This is for efficiency, the same value instance (e.g.
DSBoolean.TRUE) can be stored in many nodes. Singleton values must be immutable.

Whenever possible, values should also have NULL instance.  Rather than storing a generic null, 
this helps the system decode the proper type such as when a requester is attempting to set
a value.
  
### Actions

Add actions to your node to allow requester invocation using 
[org.iot.dsa.node.action.DSAction](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/index.html?org/iot/dsa/node/action/DSAction.html).  

Override DSNode.onInvoke handle invocations.  The reason for this is complicated but it is possible
to subclass DSAction, just carefully read the javadoc if you do.

### DSInfo

All node children have corresponding DSInfo instances.  This types serves serves two purposes:

1. It carries some meta-data about the relationship between the parent node and the child.
2. It tracks whether or not the child matches a declared default.

Important things for developers to know about DSInfo are:

* You can configure several flags such as transient, readonly and hidden.
* You can declare fields in the your Java class for (declared default) info instances to avoid
looking up the child every time it is needed.  This is can be used to create fast getters and 
setters.

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

### Metadata

Metadata can be information such as units for number types and ranges for enums.

When the system collects metadata about an object, it uses these steps:

1. If the target value or node implements 
[org.iot.dsa.node.DSIMetadata](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/index.html?org/iot/dsa/node/DSIMetadata.html).
it will be given the opportunity to provide metadata first.
2. Then getMetadata on the parent node will be called with the DSInfo representing the child.
This will be useful when nodes want to store user editable metadata.

To simplify metadata, use the utility class
[org.iot.dsa.node.DSMetadata](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/index.html?org/iot/dsa/node/DSMetadata.html).

### Timers and Threads

Use 
[org.iot.dsa.DSRuntime](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/index.html?org/iot/dsa/DSRuntime.html), 
and that all you need to know about that.

### Logging

Use Java Util Logging (JUL).  A high performance async logger is automatically installed which also 
manages backups.  Most types subclass 
[org.iot.dsa.logging.DSLogger](https://iot-dsa-v2.github.io/sdk-dslink-java/javadoc/index.html?org/iot/dsa/logging/DSLogger.html) 
as a convenience.

