package org.aifb.xxplore.views.dnd;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import org.aifb.xxplore.views.graphviewer.GraphViewer;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

import prefuse.Display;
import prefuse.controls.ControlAdapter;
import prefuse.data.Table;
import prefuse.data.event.EventConstants;
import prefuse.data.event.TableListener;
import prefuse.visual.VisualItem;

public class ExploreDragController extends ControlAdapter implements  DragSourceListener,  TableListener {

	/** the active Item for the drag*/
	private VisualItem activeItem;

	private static Logger s_log = Logger.getLogger(ExploreDragController.class);

	protected String action;

	protected Point2D down = new Point2D.Double();

	protected Point2D temp = new Point2D.Double();

	protected boolean dragged, wasFixed, resetItem;

	private boolean fixOnMouseOver = true;

	protected boolean repaint = true;

	protected IStructuredSelection m_selection;

	protected GraphViewer m_viewer;
	/** the control to which this Adapter is used*/

	protected Control m_control;

	/**
	 * Notice that the static variable is a hack. 
	 * Due to swt awt incomptability, it is yet not possible to transfer the data set on the awt drag event to the swt drop event.
	 * Therefore, this variable is used to store the selection. 
	 */
	public static VisualItem s_draggedItem;

	public ExploreDragController(GraphViewer viewer, Control control) {
		super();
		m_viewer= viewer;
		m_control = control;		
	}

	/**
	 * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	public void itemEntered(VisualItem item, MouseEvent e) {
		if (dragged) return;
		Display d = (Display) e.getSource();
		d.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		activeItem = item;
		if (fixOnMouseOver) {
			wasFixed = item.isFixed();
			resetItem = true;
			item.setFixed(true);
			item.getTable().addTableListener(this);
		}
	}

	/**
	 * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	public void itemExited(VisualItem item, MouseEvent e) {
		if (dragged)return;
		if (activeItem == item) {
			activeItem = null;
			item.getTable().removeTableListener(this);
			if (resetItem)
				item.setFixed(wasFixed);
		}
		Display d = (Display) e.getSource();
		d.setCursor(Cursor.getDefaultCursor());
	} 

	/**
	 * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	public void itemPressed(VisualItem item, MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		if (!fixOnMouseOver) {
			wasFixed = item.isFixed();
			resetItem = true;
			item.setFixed(true);
			item.getTable().addTableListener(this);
		}
		dragged = false;
		Display d = (Display) e.getComponent();
		d.getAbsoluteCoordinate(e.getPoint(), down);
	}

	/**
	 * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	public void itemReleased(VisualItem item, MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		if (dragged) {
			activeItem = null;
			item.getTable().removeTableListener(this);
			if (resetItem)
				item.setFixed(wasFixed);
			dragged = false;
		}
	}


	boolean out= false;
	/**
	 * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	public void itemDragged(VisualItem item, MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e)) return;
		
		s_draggedItem = item;

		if (dragged ){			
			sendDragEvent(m_control,SWT.DragDetect, e.getX(), e.getY());
		}else {			

			dragged = true;
			Display d = (Display) e.getComponent();
			d.getAbsoluteCoordinate(e.getPoint(), temp);
			sendDragEvent(m_control,SWT.DragDetect,e.getX(),e.getY());			
		}
		super.itemDragged(item, e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (dragged){

			Display d = (Display) e.getComponent();
			d.getAbsoluteCoordinate(e.getPoint(), temp);
			//sendDragEvent(m_control,SWT.DragDetect, e.getX(), e.getY());
			out = true;
			super.mouseExited(e);
		}else {
			super.mouseExited(e);
		}
	}
	/**
	 * @see prefuse.controls.ControlAdapter#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {    
		if (dragged && out){

			out = false;
			super.mouseEntered(e);
		}else {
			super.mouseEntered(e);
		}
	}



	@Override
	public void mouseMoved(MouseEvent e) {
		if (dragged && out ){
			super.mouseMoved(e);
		}else 
			super.mouseMoved(e);
	}

	protected void sendDragEvent(final Control control, final int type,final  int x,final int y) {	

		final org.eclipse.swt.widgets.Display  display = control.getDisplay();

		new Thread(){
			@Override
			public void run() {
				display.syncExec(new Runnable(){
					public void run() {
						final Event event = new Event ();
						event.x =x;
						event.y =y;
						event.display = display;
						event.type = type;
						control.notifyListeners(type, event);
					}
				});					
			}
		}.start();

	}

	public void dragStart(DragSourceEvent event) {

		// Only start the drag if there is actually something selected in the
		// graph - this element will be what is dropped on the target.
		if (m_viewer.getSelection() == null || m_viewer.getSelection().isEmpty()) {
			event.doit = false;
		}	      
		else{
			event.doit = true;
		}
		
		event.detail = DND.DROP_COPY;
	}

	public void dragSetData(DragSourceEvent event) {
		dragged= false;
		if (s_log.isDebugEnabled()) s_log.debug("Drag with be init with selection as data");
		
		if (ElementTransfer.getInstance().isSupportedType(event.dataType))
			//support dragging of one elements only
			event.data =((IStructuredSelection) m_viewer.getSelection()).getFirstElement();
	}

	public void dragFinished(DragSourceEvent event) {
		dragged = false;
	} 


	/**
	 * @see prefuse.data.event.TableListener#tableChanged(prefuse.data.Table,
	 *      int, int, int, int)
	 */
	public void tableChanged(Table t, int start, int end, int col, int type) {
		if (activeItem == null || type != EventConstants.UPDATE
				|| col != t.getColumnNumber(VisualItem.FIXED))
			return;
		int row = activeItem.getRow();
		if (row >= start && row <= end)
			resetItem = false;
	}
}