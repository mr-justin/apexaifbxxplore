package org.aifb.xxplore.core.service;

import org.aifb.xxplore.core.model.definition.EntityDefinition;
import org.aifb.xxplore.core.model.definition.IDefinition;
import org.aifb.xxplore.core.model.definition.IModelDefinition;
import org.aifb.xxplore.core.model.definition.ModelDefinition;
import org.aifb.xxplore.core.model.definition.ModelDefinition.DefinitionTuple;
import org.aifb.xxplore.shared.exception.Emergency;
import org.apache.log4j.Logger;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.owl.api.ICompositeConcept;
import org.xmedia.oms.model.owl.api.IHasPropConcept;
import org.xmedia.oms.model.owl.impl.CompositeConcept;
import org.xmedia.oms.model.owl.impl.HasPropertyConcept;

public class Definition2ConceptService implements IService {

	private final static Logger s_log = Logger.getLogger(Definition2ConceptService.class);
		
	public IConcept computeModelDefinition(IModelDefinition modelDefinition) {
		Emergency.checkPrecondition(modelDefinition instanceof ModelDefinition, "Only ModelDefinition is currently supported!");
		ICompositeConcept composite = new CompositeConcept(ICompositeConcept.INTERSECTION); 
		
		for (DefinitionTuple dt : ((ModelDefinition)modelDefinition).getCompleteDefinitionTuples()) {
			IProperty property = dt.getRelationDefinition().getDefinition();
			IDefinition objectDefinition = dt.getObjectDefinition();
			
			IConcept concept = null;
			if (objectDefinition instanceof ModelDefinition) {
				concept = computeModelDefinition((ModelDefinition)objectDefinition);
			}
			else if (objectDefinition instanceof EntityDefinition) {
				// TODO can be an instance as well -> has value
				concept = (IConcept)((EntityDefinition)objectDefinition).getDefinition();
			}
			
			HasPropertyConcept prop = new HasPropertyConcept(IHasPropConcept.SOME_OF);
			prop.setConcept(concept);
			prop.setProperty(property);
			composite.addConcept(prop);
		}
		
		return composite;
	}

	public void callService(IServiceListener listener, Object... params) {
		// TODO Auto-generated method stub

	}

	public void disposeService() {
		// TODO Auto-generated method stub

	}

	public void init(Object... params) {
		// TODO Auto-generated method stub

	}

}
