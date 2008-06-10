AKSesame2Adapter - AccessKnow Adapter for Sesame 2	
  * implement OMS - an API that provides an abstraction over datasources like Sesame 
  * this adapter is a particular implementation of OMS for working with SESAME
  * the provenance management component is part of the adapter implementation
  
You can find a sample usages in:
 * examples/org/xmedia/accessknow/sesame/examples/AKSesame2AdapterExample.java.
 * examples/org/xmedia/accessknow/sesame/examples/AKSesame2AdapterRemoteExample.java.
 * examples/org/xmedia/accessknow/sesame/examples/AKSesame2AdapterMetaknowledgeExample.java

besides, an example for querying with provenance is given in:
 * examples/org/xmedia/accessknow/sesame/examples/AKSesame2AdapterQueryingMetaknowExample.java
This example gives a very easy example on how to query a given knowlegde base with and without provenances. 
It makes use of the following example data: 
  * a folder 'ontologies' containing the ontologies.
  * a folder 'repository' representing the store that will be created to persist the ontologies imported in the example	
  * note that the paths to these folders need to be set to point to the folder of your file system. 
  

Please, note that (due to a design choice) it is not possible the usage of:
* PersistenceUtil class;
* IKbConnection.setActiveOntology() method.

In order to use different ontologies within a connection, you must open different session, and then
access the daos from the ISession.getDaoManager().

Dependencies:
	* AccessKnow
		* OMS (tested with version 0.3.5): svn+ssh://x-media@ontoware.org/svnroot/accessapi/trunk/OMS
		* util (tested with version 0.1):  svn+ssh://x-media@ontoware.org/svnroot/accessapi/trunk/util