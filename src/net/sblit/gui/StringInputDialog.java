package net.sblit.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class StringInputDialog{
	private Shell inputDialogShell;
	private Display display = Display.getCurrent();
	private String message = "";
	private String input   = "";

	public StringInputDialog() {
		this.inputDialogShell = new Shell(display);
	}

	public String[] open() {
		drawContent();
		inputDialogShell.open();
		inputDialogShell.pack();
		
		while (!inputDialogShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return input.split(";");
	}
	
	private void drawContent(){
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 5;
		gridLayout.verticalSpacing = 10;
		inputDialogShell.setLayout(gridLayout);
		
		final Label messageLbl = new Label(inputDialogShell, SWT.NONE);
		messageLbl.setText(message);
		GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1);
		messageLbl.setLayoutData(layoutData);
		
		final Text inputField = new Text(inputDialogShell, SWT.BORDER);
		inputField.setText(input);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		inputField.setLayoutData(layoutData);
		inputField.setSelection(0,input.length());
		
		final Button okBtn = new Button(inputDialogShell, SWT.PUSH);
		okBtn.setText("OK");
		okBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input = inputField.getText();
				inputDialogShell.close();
			}
		});
		layoutData = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		okBtn.setLayoutData(layoutData);
		
		final Button cancelBtn = new Button(inputDialogShell, SWT.PUSH);
		cancelBtn.setText("Cancel");
		cancelBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				inputDialogShell.close();
			}
		});
		layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		cancelBtn.setLayoutData(layoutData);
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getInput() {
		return input;
	}

}
