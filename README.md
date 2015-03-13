[![Build Status](https://travis-ci.org/rodolfodpk/myeslib2.svg?branch=master)](https://travis-ci.org/rodolfodpk/myeslib2)

## Getting Started

This project is a work in progress so until any published release you may use https://jitpack.io

1) Create a maven project for you first API then add this to your pom.xml. You may have myeslib-example-api as template.
```    
<dependency>
    <groupId>com.github.rodolfodpk</groupId>
    <artifactId>myeslib2.myeslib2-core</artifactId>
    <version>${myeslib2.version}</version>
</dependency>
```
2) Then create another project for you API client.  You may have myeslib-example-client as template.
```
<dependency>
    <groupId>com.github.rodolfodpk</groupId>
    <artifactId>myeslib2.myeslib2-stack1</artifactId>
    <version>${myeslib2.version}</version>
</dependency>
```
The example-client project has a Spock test for <a href="myeslib2-example-api/src/main/java/sampledomain/aggregates/inventoryitem/InventoryItem.java">InventoryItem</a> aggregate root. Please note this class is just an example, not a real world model. See: https://github.com/rodolfodpk/myeslib2/issues/10

<a href="myeslib2-example-client/src/test/groovy/org/myeslib/sampledomain/InventoryItemTest.groovy">Here is the InventoryItemTest written in Spock</a>


