package sjtu.apex.q2semantic.search;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.xmedia.oms.model.api.IProperty;

public class KbEdge extends DefaultWeightedEdge implements KbElement{

	private KbVertex vertex1;

	private KbVertex vertex2;

	private IProperty property;
	
	private double cost;
	
	private int multiplicity;
	
	private int type;
	
	public KbEdge(KbVertex vertex1, KbVertex vertex2, IProperty prop, int type, double weight){
		this.vertex1 = vertex1;
		this.vertex2 = vertex2;
		this.property = prop;
		this.type = type;
		this.cost = weight; 
	}

	public int getType(){
		return type;
	}
	
	public void setVertex1(KbVertex vertex1){
		this.vertex1 = vertex1;
	}

	public void setVertex2(KbVertex vertex2){
		this.vertex2 = vertex2;
	}

	public void setProperty(IProperty property){
		this.property = property;
	}


	public KbVertex getVertex1(){
		return vertex1;
	}

	public KbVertex getVertex2(){
		return vertex2;
	}

	public IProperty getProperty(){
		return property;
	}
	
	public void setCost(double weight){
		this.cost = weight;
	}
	
	public double getCost(){
		return cost;
	}
	
	public int getMultiplicity(){
		return multiplicity;
	}
	
	public void setMultiplicity(int multiplicity){
		this.multiplicity = multiplicity;
	}
	
	public void incrementMultiplicity(){
		this.multiplicity++;
	}

	public boolean equals(Object object){
		if(this == object) return true;
		if(object == null) return false;
		if(!(object instanceof KbEdge)) return false;
		KbEdge edge = (KbEdge)object;
		
		if (!property.getUri().equals(edge.getProperty().getUri()))  return false;
		if (!vertex1.equals(edge.getVertex1())) return false;
		if (!vertex2.equals(edge.getVertex2())) return false;
		return true;
	}

	public int hashCode(){
		return 7 * vertex1.hashCode() 
			+ 11 * vertex2.hashCode() 
			+ 13 * property.hashCode();
	}
	
	public String toString(){
		if(vertex1 != null && vertex2 != null && property != null) return vertex1.toString() + " " + property.getUri() + " "  + vertex2.toString();
		else return super.toString();
	}
}