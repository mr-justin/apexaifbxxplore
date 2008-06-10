package org.aifb.xxplore.views.definitionviewer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


public class CheckboxDialog extends TitleAreaDialog {
	
	public interface CheckboxValidator {
		public String validate(Collection options);
	}

	private String[] optionIds, labels;
	private Collection initialIds;
	private Map<String, Button> checkboxMap = new HashMap<String, Button>();
	private Set<String> result;
	String title, areaTitle,message;
	private CheckboxValidator validator;
	
	public CheckboxDialog(Shell parentShell, String title, String areaTitle, 
			String message, String[] optionIds, String[] labels, 
			Collection initialIds, CheckboxValidator validator) {
		super(parentShell);
		this.title = title;
		this.areaTitle = areaTitle;
		this.message = message;
		this.optionIds = optionIds;
		this.labels = labels;
		this.initialIds = initialIds;
		this.validator = validator;
	}
	
	protected Control createDialogArea(Composite parent) {
		if(title != null)
			getShell().setText(title);
		if(areaTitle != null)
			setTitle(areaTitle);
		if(message != null)
			setMessage(message);
		Composite comp = (Composite) super.createDialogArea(parent);
		Composite buttonArea = new Composite(comp, SWT.NONE);
		RowLayout layout = new RowLayout(SWT.VERTICAL);
//		GridLayout layout = new GridLayout(2,false);

		layout.marginWidth = 30;
		layout.marginHeight = 15;
		buttonArea.setLayout(layout);
		for (int i = 0; i < optionIds.length; i++){
			String optionId = optionIds[i];
			Button button = new Button(buttonArea, SWT.CHECK);
			
			String[] splitLabels = labels[i].split("\\$");
			button.setText(splitLabels[0]);
			for(int j = 1; j < splitLabels.length; j++){
				if(i < 9)
					new Label(buttonArea,SWT.NONE).setText("       " + splitLabels[j]);
				else 
					new Label(buttonArea,SWT.NONE).setText("         " + splitLabels[j]);
				
//				new Label(buttonArea,SWT.NONE).setText("    " + splitLabels[j]);
			}
			if(initialIds.contains(optionId))
				button.setSelection(true);
			button.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent e){
					setDialogComplete(validate());
				}
			});
			checkboxMap.put(optionId, button);
		}
		return comp;
	}

	protected void setDialogComplete(boolean valid) {
		getButton(OK).setEnabled(valid);
	}

	protected boolean validate() {
		if(validator != null){
			String errormessage = validator.validate(getSelectedOptions());
			setErrorMessage(errormessage);
			return errormessage == null;
		}
		return true;
	}
	
	private Set<String> getSelectedOptions(){
		Set<String> options = new HashSet<String>();
		for (String optionId : checkboxMap.keySet()){
			Button button = (Button) checkboxMap.get(optionId);
			if(button.getSelection())
				options.add(optionId);
		}
		return options;
	}
	
	protected void buttonPressed(int buttonId){
		if(buttonId == OK)
			result = getSelectedOptions();
		super.buttonPressed(buttonId);
	}
	
	public Set<String> getResult(){
		return result;
	}
}