[![Build Status](https://travis-ci.org/rodolfodpk/myeslib2.svg?branch=master)](https://travis-ci.org/rodolfodpk/myeslib2)

## Getting Started

This project is a work in progress so until any published release you may use https://jitpack.io

1) Create a maven project for you first API then add this to your pom.xml. You may have myeslib-example-api as template. Mandatory dependency:
```    
<dependency>
    <groupId>com.github.rodolfodpk</groupId>
    <artifactId>myeslib2.myeslib2-core</artifactId>
    <version>${myeslib2.version}</version>
</dependency>
```
On this project you will create your aggregate root and the respectives commands, events and command handlers. Try to minimize the dependencies here. The example uses:

Role       | implementation
---------- | --------------
logging    | sl4j
DI         | javax.inject
validation | guava preconditions  
fixjava    | lombok and autoValue (TODO decide about this)

2) Then create another project for your API client. You may have myeslib-example-client as template. Mandatory dependencies: your api project and also: 
```
<dependency>
    <groupId>com.github.rodolfodpk</groupId>
    <artifactId>myeslib2.myeslib2-stack1</artifactId>
    <version>${myeslib2.version}</version>
</dependency>
```
On this kind of project you will select the stack for things like JSON serialization, the target database and the dependency injection. For example, myeslib-example-client is using:

Role       | implementation
---------- | --------------
database   | h2
json ser/d | gson
DI impl    | guice
crqs/es    | myeslib2-stack1

myeslib-stack1 is currently the only myeslib-core implementation. It has these dependencies:

Role       | implementation
---------- | --------------
database   | h2
DI         | javax.inject
jdbc lib   | jdbi
eventbus   | guava

## Testing 

The example-client project has a Spock test for <a href="myeslib2-example-api/src/main/java/sampledomain/aggregates/inventoryitem/InventoryItem.java">InventoryItem</a> aggregate root. Please note this class is just an example, not a real world model. See: https://github.com/rodolfodpk/myeslib2/issues/10

This test will exercise all features: your domain events will be persisted as JSON into your database of choice. Please note: the database must have a trigger to control concurrency in optimistic mode. If you want try Oracle instead of H2, you may use <a href="https://github.com/rodolfodpk/myeslib/blob/master/inventory-database/src/main/resources/db/oracle/V1__Create_inventory_item_tables.sql">this trigger</a> as template.


<a href="myeslib2-example-client/src/test/groovy/org/myeslib/sampledomain/InventoryItemTest.groovy">Here is the InventoryItemTest written in Spock</a>


