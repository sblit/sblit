package net.sblit.gui;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class testInputDialog {
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		
		GridLayout layout = new GridLayout(2, false);
		shell.setLayout(layout);
		
		Button openButton = new Button(shell, SWT.PUSH);
		openButton.setText("Open");
		openButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				StringInputDialog dialog = new StringInputDialog();
				dialog.setInput("OPA;asdfasdfasdf");
				dialog.setMessage("Editing the Host: e.g.: Desktop;Key");
				String[] input = dialog.open();
				System.out.println(input[0] + ";" + input[1]);;
			}
		});
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		openButton.setLayoutData(layoutData);

		shell.pack();
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		shell.dispose();
	}
}
