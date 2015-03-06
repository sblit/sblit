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

public class StringInputDialog extends Dialog {
	private String message;
	private String input;

	public StringInputDialog(Shell parent) {
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	}

	public StringInputDialog(Shell parent, int style) {
		super(parent, style);
		setText("Receiver Edit Dialog");
		setMessage("Please enter a value:");
	}
	
	public String open() {
		Shell inputDialogShell = new Shell(getParent(), getStyle());
		inputDialogShell.setText(getText());
		drawContent(inputDialogShell);
		inputDialogShell.open();
		
		Rectangle parentBounds = getParent().getBounds();
		inputDialogShell.setBounds(parentBounds.x + 10, parentBounds.y + 10, 200, 200);
		
		Display display = getParent().getDisplay();
	    while (!inputDialogShell.isDisposed()) {
	      if (!display.readAndDispatch()) {
	        display.sleep();
	      }
	    }
		return input;
	}
	
	private void drawContent(final Shell parentShell){
		GridLayout gridLayout = new GridLayout(2, false);
		parentShell.setLayout(gridLayout);
		
		final Label messageLbl = new Label(parentShell, SWT.NONE);
		messageLbl.setText(message);
		GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1);
		messageLbl.setLayoutData(layoutData);
		
		final Text inputField = new Text(parentShell, SWT.BORDER);
		inputField.setText(input);
		inputField.setSelection(0);
		layoutData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1);
		inputField.setLayoutData(layoutData);
		
		final Button okBtn = new Button(parentShell, SWT.PUSH);
		okBtn.setText("OK");
		okBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input = inputField.getText();
				parentShell.close();
			}
		});
		layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		okBtn.setLayoutData(layoutData);
		
		final Button cancelBtn = new Button(parentShell, SWT.PUSH);
		cancelBtn.setText("Cancel");
		cancelBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				parentShell.close();
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
