package org.team.xxplore.core.service.search.datastructure;

import org.dom4j.Document;

/**
 * This interface is implemented by all classes that can serialized to XML to be transmitted to the interface
 * @author tpenin
 */
public interface XMLSerializable {
   
   /**
    * Transforms the current object into a DOM4J XML document
    * @return a DOM4J XML document represnting the current object
    */
   public Document toXML();
}
