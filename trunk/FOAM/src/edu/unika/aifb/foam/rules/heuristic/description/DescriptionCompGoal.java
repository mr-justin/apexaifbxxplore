/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.heuristic.description;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.kaon2.api.owl.elements.DataAll;
import org.semanticweb.kaon2.api.owl.elements.DataCardinality;
import org.semanticweb.kaon2.api.owl.elements.DataHasValue;
import org.semanticweb.kaon2.api.owl.elements.DataNot;
import org.semanticweb.kaon2.api.owl.elements.DataOneOf;
import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.DataPropertyExpression;
import org.semanticweb.kaon2.api.owl.elements.DataRange;
import org.semanticweb.kaon2.api.owl.elements.DataSome;
import org.semanticweb.kaon2.api.owl.elements.Datatype;
import org.semanticweb.kaon2.api.owl.elements.Description;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectAll;
import org.semanticweb.kaon2.api.owl.elements.ObjectAnd;
import org.semanticweb.kaon2.api.owl.elements.ObjectCardinality;
import org.semanticweb.kaon2.api.owl.elements.ObjectHasValue;
import org.semanticweb.kaon2.api.owl.elements.ObjectNot;
import org.semanticweb.kaon2.api.owl.elements.ObjectOneOf;
import org.semanticweb.kaon2.api.owl.elements.ObjectOr;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;
import org.semanticweb.kaon2.api.owl.elements.ObjectPropertyExpression;
import org.semanticweb.kaon2.api.owl.elements.ObjectSome;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;
import edu.unika.aifb.foam.rules.heuristic.object.Similar;
import edu.unika.aifb.foam.rules.heuristic.set.SetAvgMaxAvgAvgGoal;
import edu.unika.aifb.foam.rules.heuristic.tuple.Tuple;
import edu.unika.aifb.foam.rules.heuristic.tuple.TupleMinGoal;
import edu.unika.aifb.foam.rules.heuristic.tuple.TupleSetAvgMaxGoal;


/**
 * @author meh
 */
public class DescriptionCompGoal implements Heuristic {

	private static final long serialVersionUID = 1L;
	private Heuristic goal;
	private ResultTable previousResult;
	private Heuristic goalTuple;
	private Heuristic goalTupleSet;
	private Heuristic goalSet;
	
	public double get(Object object1, Object object2) {
		try {
		Description descr1 = (Description) object1;
		Description descr2 = (Description) object2;
//		if ((descr1 != null)&&(descr2 != null)) {
			if (((descr1 instanceof OWLClass)&&(descr2 instanceof OWLClass))		//Concepts, Properties, Instances
					||((descr1 instanceof DataProperty)&&(descr2 instanceof DataProperty))
					||((descr1 instanceof ObjectProperty)&&(descr2 instanceof ObjectProperty))
					||((descr1 instanceof Individual)&&(descr2 instanceof Individual))) {
				return goal.get(object1,object2);
			}
			goal = new Similar();
			goalTuple = new TupleMinGoal();
			goalTupleSet = new TupleSetAvgMaxGoal();
			goalSet = new SetAvgMaxAvgAvgGoal();
			goal.setPreviousResult(previousResult);
			goalTuple.setPreviousResult(previousResult);
			goalTupleSet.setPreviousResult(previousResult);
			goalSet.setPreviousResult(previousResult);
			if ((descr1 instanceof DataAll)&&(descr2 instanceof DataAll)) {	//DataAll
				DataAll comp1 = (DataAll) descr1;
				List propertiesList1 = comp1.getDataProperties();
				DataRange range1 = comp1.getDataRange();
				Set tupleSet1 = new HashSet();
				for (int i = 0; i<propertiesList1.size(); i++) {
					Tuple tuple = new Tuple(propertiesList1.get(i),range1,null);
					tupleSet1.add(tuple);
				}
				DataAll comp2 = (DataAll) descr2;				
				List propertiesList2 = comp2.getDataProperties();
				DataRange range2 = comp2.getDataRange();
				Set tupleSet2 = new HashSet();
				for (int i = 0; i<propertiesList2.size(); i++) {
					Tuple tuple = new Tuple(propertiesList2.get(i),range2,null);
					tupleSet2.add(tuple);
				}		
				return goalTupleSet.get(tupleSet1,tupleSet2);
			}
			if ((descr1 instanceof DataCardinality)&&(descr2 instanceof DataCardinality)) {	//DataCardinality
				DataCardinality comp1 = (DataCardinality) descr1;
				DataPropertyExpression property1 = comp1.getDataProperty();
				Integer min1 = new Integer(0);
				Integer max1 = Integer.MAX_VALUE;
				if(comp1.isMaximumCardinality()){
					min1 = comp1.getCardinality();
				} else if(comp1.isMinimumCardinality()){
					max1 = comp1.getCardinality();
				}
				/*Integer min1 = new Integer(comp1.getMinimumCardinality());
				Integer max1 = new Integer(comp1.getMaximumCardinality());*/
				Tuple tuple1 = new Tuple(property1,min1,max1);
				
				DataCardinality comp2 = (DataCardinality) descr2;
				DataPropertyExpression property2 = comp2.getDataProperty();
				Integer min2 = new Integer(0);
				Integer max2 = Integer.MAX_VALUE;
				if(comp2.isMaximumCardinality()){
					min2 = comp2.getCardinality();
				} else if(comp2.isMinimumCardinality()){
					max2 = comp2.getCardinality();
				}				
				/*Integer min2 = new Integer(comp2.getMinimumCardinality());
				Integer max2 = new Integer(comp2.getMaximumCardinality());*/	
				Tuple tuple2 = new Tuple(property2,min2,max2);				
				return goalTuple.get(tuple1,tuple2);
			}
			if ((descr1 instanceof DataHasValue)&&(descr2 instanceof DataHasValue)) {	//DataHasValue
				DataHasValue comp1 = (DataHasValue) descr1;
				DataPropertyExpression property1 = comp1.getDataProperty();
				Object literal1 = comp1.getLiteralValue();
				Tuple tuple1 = new Tuple(property1,literal1,null);
				DataHasValue comp2 = (DataHasValue) descr2;
				DataPropertyExpression property2 = comp2.getDataProperty();
				Object literal2 = comp2.getLiteralValue();
				Tuple tuple2 = new Tuple(property2,literal2,null);
				return goalTuple.get(tuple1,tuple2);
			}			
			if ((descr1 instanceof DataSome)&&(descr2 instanceof DataSome)) {	//DataSome
				DataSome comp1 = (DataSome) descr1;
				List propertiesList1 = comp1.getDataProperties();
				DataRange range1 = comp1.getDataRange();
				Set tupleSet1 = new HashSet();
				for (int i = 0; i<propertiesList1.size(); i++) {
					Tuple tuple = new Tuple(propertiesList1.get(i),range1,null);
					tupleSet1.add(tuple);
				}
				DataSome comp2 = (DataSome) descr2;
				List propertiesList2 = comp2.getDataProperties();
				DataRange range2 = comp2.getDataRange();
				Set tupleSet2 = new HashSet();
				for (int i = 0; i<propertiesList2.size(); i++) {
					Tuple tuple = new Tuple(propertiesList2.get(i),range2,null);
					tupleSet2.add(tuple);
				}		
				return goalTupleSet.get(tupleSet1,tupleSet2);
			}	
			if ((descr1 instanceof DataAll)&&(descr2 instanceof DataSome)) {	//DataAllSome
				DataAll comp1 = (DataAll) descr1;
				List propertiesList1 = comp1.getDataProperties();
				DataRange range1 = comp1.getDataRange();
				Set tupleSet1 = new HashSet();
				for (int i = 0; i<propertiesList1.size(); i++) {
					Tuple tuple = new Tuple(propertiesList1.get(i),range1,null);
					tupleSet1.add(tuple);
				}
				DataSome comp2 = (DataSome) descr2;
				List propertiesList2 = comp2.getDataProperties();
				DataRange range2 = comp2.getDataRange();
				Set tupleSet2 = new HashSet();
				for (int i = 0; i<propertiesList2.size(); i++) {
					Tuple tuple = new Tuple(propertiesList2.get(i),range2,null);
					tupleSet2.add(tuple);
				}		
				return 0.5*goalTupleSet.get(tupleSet1,tupleSet2);
			}	
			if ((descr1 instanceof DataSome)&&(descr2 instanceof DataAll)) {	//DataSomeAll
				DataSome comp1 = (DataSome) descr1;
				List propertiesList1 = comp1.getDataProperties();
				DataRange range1 = comp1.getDataRange();
				Set tupleSet1 = new HashSet();
				for (int i = 0; i<propertiesList1.size(); i++) {
					Tuple tuple = new Tuple(propertiesList1.get(i),range1,null);
					tupleSet1.add(tuple);
				}
				DataAll comp2 = (DataAll) descr2;
				List propertiesList2 = comp2.getDataProperties();
				DataRange range2 = comp2.getDataRange();
				Set tupleSet2 = new HashSet();
				for (int i = 0; i<propertiesList2.size(); i++) {
					Tuple tuple = new Tuple(propertiesList2.get(i),range2,null);
					tupleSet2.add(tuple);
				}		
				return 0.5*goalTupleSet.get(tupleSet1,tupleSet2);
			}					
			if ((descr1 instanceof ObjectAll)&&(descr2 instanceof ObjectAll)) {	//ObjectAll
				ObjectAll comp1 = (ObjectAll) descr1;
				ObjectPropertyExpression property1 = comp1.getObjectProperty();
				Description description1 = comp1.getDescription();
				Tuple tuple1 = new Tuple(property1,description1,null);
				ObjectAll comp2 = (ObjectAll) descr2;
				ObjectPropertyExpression property2 = comp2.getObjectProperty();
				Description description2 = comp2.getDescription();
				Tuple tuple2 = new Tuple(property2,description2,null);
				return goalTuple.get(tuple1,tuple2);
			}	
			if ((descr1 instanceof ObjectCardinality)&&(descr2 instanceof ObjectCardinality)) {	//ObjectCardinality
				ObjectCardinality comp1 = (ObjectCardinality) descr1;
				ObjectPropertyExpression property1 = comp1.getObjectProperty();
				Integer min1 = new Integer(0);
				Integer max1 = Integer.MAX_VALUE;
				if(comp1.isMaximumCardinality()){
					min1 = comp1.getCardinality();
				} else if(comp1.isMinimumCardinality()){
					max1 = comp1.getCardinality();
				}
				/*Integer min1 = new Integer(comp1.getMinimumCardinality());
				Integer max1 = new Integer(comp1.getMaximumCardinality());*/
				Tuple tuple1 = new Tuple(property1,min1,max1);
				ObjectCardinality comp2 = (ObjectCardinality) descr2;
				ObjectPropertyExpression property2 = comp2.getObjectProperty();
				Integer min2 = new Integer(0);
				Integer max2 = Integer.MAX_VALUE;
				if(comp2.isMaximumCardinality()){
					min2 = comp2.getCardinality();
				} else if(comp2.isMinimumCardinality()){
					max2 = comp2.getCardinality();
				}	
				/*Integer min2 = new Integer(comp2.getMinimumCardinality());
				Integer max2 = new Integer(comp2.getMaximumCardinality());*/	
				Tuple tuple2 = new Tuple(property2,min2,max2);				
				return goalTuple.get(tuple1,tuple2);
			}
			if ((descr1 instanceof ObjectHasValue)&&(descr2 instanceof ObjectHasValue)) {	//ObjectHasValue
				ObjectHasValue comp1 = (ObjectHasValue) descr1;
				ObjectPropertyExpression property1 = comp1.getObjectProperty();
				Individual individual1 = comp1.getIndividual();
				Tuple tuple1 = new Tuple(property1,individual1,null);
				ObjectHasValue comp2 = (ObjectHasValue) descr2;
				ObjectPropertyExpression property2 = comp2.getObjectProperty();
				Individual individual2 = comp2.getIndividual();
				Tuple tuple2 = new Tuple(property2,individual2,null);
				return goalTuple.get(tuple1,tuple2);
			}			
			if ((descr1 instanceof ObjectSome)&&(descr2 instanceof ObjectSome)) {	//ObjectSome
				ObjectSome comp1 = (ObjectSome) descr1;
				ObjectPropertyExpression property1 = comp1.getObjectProperty();
				Description description1 = comp1.getDescription();
				Tuple tuple1 = new Tuple(property1,description1,null);
				ObjectSome comp2 = (ObjectSome) descr2;
				ObjectPropertyExpression property2 = comp2.getObjectProperty();
				Description description2 = comp2.getDescription();
				Tuple tuple2 = new Tuple(property2,description2,null);
				return goalTuple.get(tuple1,tuple2);
			}	
			if ((descr1 instanceof ObjectSome)&&(descr2 instanceof ObjectAll)) {	//ObjectSomeAll
				ObjectSome comp1 = (ObjectSome) descr1;
				ObjectPropertyExpression property1 = comp1.getObjectProperty();
				Description description1 = comp1.getDescription();
				Tuple tuple1 = new Tuple(property1,description1,null);
				ObjectAll comp2 = (ObjectAll) descr2;
				ObjectPropertyExpression property2 = comp2.getObjectProperty();
				Description description2 = comp2.getDescription();
				Tuple tuple2 = new Tuple(property2,description2,null);
				return 0.5*goalTuple.get(tuple1,tuple2);
			}			
			if ((descr1 instanceof ObjectAll)&&(descr2 instanceof ObjectSome)) {	//ObjectAllSome
				ObjectAll comp1 = (ObjectAll) descr1;
				ObjectPropertyExpression property1 = comp1.getObjectProperty();
				Description description1 = comp1.getDescription();
				Tuple tuple1 = new Tuple(property1,description1,null);
				ObjectSome comp2 = (ObjectSome) descr2;
				ObjectPropertyExpression property2 = comp2.getObjectProperty();
				Description description2 = comp2.getDescription();
				Tuple tuple2 = new Tuple(property2,description2,null);
				return 0.5*goalTuple.get(tuple1,tuple2);
			}			
			if ((descr1 instanceof ObjectNot)&&(descr2 instanceof ObjectNot)) {		//ObjectNot
				ObjectNot comp1 = (ObjectNot) descr1;
				Description description1 = comp1.getDescription();
				ObjectNot comp2 = (ObjectNot) descr2;
				Description description2 = comp2.getDescription();
				return goal.get(description1,description2);
			}	
			if ((descr1 instanceof ObjectOneOf)&&(descr2 instanceof ObjectOneOf)) {		//ObjectOneOf
				ObjectOneOf comp1 = (ObjectOneOf) descr1;
				Set set1 = comp1.getIndividuals();
				ObjectOneOf comp2 = (ObjectOneOf) descr2;
				Set set2 = comp2.getIndividuals();
				return goalSet.get(set1,set2);
			}		
			if ((descr1 instanceof ObjectAnd)&&(descr2 instanceof ObjectAnd)) {		//ObjectAnd
				ObjectAnd comp1 = (ObjectAnd) descr1;
				Set set1 = comp1.getDescriptions();
				ObjectAnd comp2 = (ObjectAnd) descr2;
				Set set2 = comp2.getDescriptions();
				return goalSet.get(set1,set2);
			}	
			if ((descr1 instanceof ObjectOr)&&(descr2 instanceof ObjectOr)) {		//ObjectOr
				ObjectOr comp1 = (ObjectOr) descr1;
				Set set1 = comp1.getDescriptions();
				ObjectOr comp2 = (ObjectOr) descr2;
				Set set2 = comp2.getDescriptions();
				return goalSet.get(set1,set2);
			}		
//		}
		} catch (Exception e) {
//			e.printStackTrace();
//		}
		try {
			DataRange data1 = (DataRange) object1;
			DataRange data2 = (DataRange) object2;
			goal = new Similar();
			goalSet = new SetAvgMaxAvgAvgGoal();
			if ((data1 instanceof Datatype)&&(data2 instanceof Datatype))	{	//Datatypes
				return goal.get(object1,object2);
			}
			if ((data1 instanceof DataNot)&&(data2 instanceof DataNot)) {		//DataNot
				DataNot comp1 = (DataNot) data1;
				DataRange range1 = comp1.getDataRange();
				DataNot comp2 = (DataNot) data2;
				DataRange range2 = comp2.getDataRange();
				return goal.get(range1,range2);
			}	
			if ((data1 instanceof DataOneOf)&&(data2 instanceof DataOneOf)) {	//DataOneOf
				DataOneOf comp1 = (DataOneOf) data1;
				Set set1 = comp1.getLiteralValues();
				DataOneOf comp2 = (DataOneOf) data2;
				Set set2 = comp2.getLiteralValues();
				return goalSet.get(set1,set2);
			}	
		} catch (Exception e1) {
			e1.printStackTrace();
		}}
		return 0;
	}

	public void setPreviousResult(ResultTable resultTable) {
		previousResult = resultTable;
	}

}
