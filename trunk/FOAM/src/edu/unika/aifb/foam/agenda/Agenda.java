/*
 * Created on 26.05.2004
 *
 */
package edu.unika.aifb.foam.agenda;

import java.util.Collection;

import edu.unika.aifb.foam.input.Structure;

/**
 * The agenda classes are used to determine which entities should be compared.
 * The simplest one is the CompleteAgenda, resulting in n x m comparisons.
 * Others are more elaborated and therefore more efficient.
 * 
 * @author Marc Ehrig
 */
public interface Agenda {

	public void iterate();
	public boolean hasNext();
	public AgendaElement next();
	public int size();
	public void create(Structure structure, boolean internaltoo);
	public void parameter(Object object);
	public void add(Agenda agenda);
	public Collection collection();
}
