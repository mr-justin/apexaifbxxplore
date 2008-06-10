package org.aifb.xxplore.views.definitionviewer;

import org.aifb.xxplore.views.dnd.DefinitionDropAdapter;
import org.aifb.xxplore.views.dnd.ElementTransfer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.navigator.CommonDragAdapter;
import org.eclipse.ui.navigator.CommonDropAdapter;

public class DefinitionViewer extends TreeViewer{

	public DefinitionViewer(Composite parent, int style) {
		super(parent, style);
		initDrop();
	}
	
	/**
	 * <p>
	 * Adds DND support to the Navigator. Uses hooks into the extensible
	 * framework for DND.
	 * </p>
	 * 
	 * @see CommonDragAdapter
	 * @see CommonDropAdapter
	 */
	protected void initDrop (){
				
		int ops = DND.DROP_COPY ;
		Transfer[] transfers = new Transfer[] { ElementTransfer.getInstance(), TextTransfer.getInstance()};
		addDropSupport(ops, transfers, new DefinitionDropAdapter(this));

	}

}
