package org.aifb.xxplore.task;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.aifb.xxplore.task.DatePickerPanel;
import org.aifb.xxplore.task.DatePickerPanel.DateSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class DateSelectionDialog extends Dialog {

	private Date reminderDate = null;

	private String title = "Date Selection";
	
	private Calendar initialCalendar = GregorianCalendar.getInstance();

	public DateSelectionDialog(Shell parentShell, String title) {
		this(parentShell, GregorianCalendar.getInstance(), title);
	}

	public DateSelectionDialog(Shell parentShell, Calendar initialDate, String title) {
		super(parentShell);
		if(title != null) {
			this.title = title;
		}
		if(initialDate != null) {
			this.initialCalendar.setTime(initialDate.getTime());
		}
		reminderDate = initialCalendar.getTime();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(title);
		DatePickerPanel datePanel = new DatePickerPanel(parent, SWT.NULL, initialCalendar);

		datePanel.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (!event.getSelection().isEmpty()) {
					DateSelection dateSelection = (DateSelection) event.getSelection();
					reminderDate = dateSelection.getDate().getTime();
				}
			}
		});

		return datePanel;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CLIENT_ID + 1, "Clear", false);
		super.createButtonsForButtonBar(parent);
	}
	
	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
		if(buttonId == IDialogConstants.CLIENT_ID + 1) {
			reminderDate = null;
			okPressed();
		}
	}

	public Date getDate() {
		return reminderDate;
	}
}
