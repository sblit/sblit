package org.sblit.gui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.sblit.configuration.Configuration;

/**
 * Draws the configuration-dialog. At this GUI
 * the user can set all configuration parameters of sblit.
 * @author Andi
 *
 */
public class ConfigurationDialog{

	Shell configShell;;
	Configuration configuration;
	String[] receivers;
	File dataDirectory;
	boolean configSaved = true;

	public void open(Shell parentShell){
		configShell   = new Shell (parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		configuration = new Configuration();
		
		// TODO getReceivers wirft NullPointerException --> momentan wird ein dummyarray benutzt
//		receivers = configuration.getReceivers();

		configShell.setText("Configuration");
		configShell.setLayout (new FillLayout());

		// Adds a prompt for closing configuration-gui without saving
		configShell.addListener (SWT.Close, new Listener () {
			@Override
			public void handleEvent (Event event) {
				if (configSaved == false){
					int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO;
					MessageBox messageBox = new MessageBox (configShell, style);
					messageBox.setText ("Alert");
					messageBox.setMessage ("You have unsaved changes. Do you want to continue anyway?");
					event.doit = messageBox.open () == SWT.YES;
				}
			}
		});

		ScrolledComposite scrolledComposite = new ScrolledComposite(configShell, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite parent = new Composite(scrolledComposite, SWT.NONE);
		scrolledComposite.setContent(parent);
		parent.setLayout(new GridLayout(2, false));

		Group dataDirectoryGroup = new Group(parent, SWT.NONE);
		dataDirectoryGroup.setText("Base Data Directory");
		GridData data = new GridData(400,100);
		dataDirectoryGroup.setData(data);
		dataDirectoryGroup.setLayout(new GridLayout(2, false));

		Group receiverGroup = new Group(parent, SWT.NONE);
		receiverGroup.setText("Receivers");
		data = new GridData(GridData.FILL_BOTH);
		receiverGroup.setData(data);
		receiverGroup.setLayout(new GridLayout(2, false));

		Button cancelButton = new Button(parent, SWT.CANCEL);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				System.out.println("Cancel");
				configShell.close();
			}
		});

		data = new GridData(GridData.HORIZONTAL_ALIGN_END);
		data.horizontalSpan = 2;
		cancelButton.setLayoutData(data);


		Button saveButton = new Button(parent, SWT.SAVE);
		saveButton.setText("Save Changes");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				if(!configSaved){
					System.out.println("Changes had been saved");
					
					configSaved = true;
				}
				configShell.close();
			}
		});
		
		data = new GridData(GridData.HORIZONTAL_ALIGN_END);
		data.horizontalSpan = 2;
		saveButton.setLayoutData(data);

		drawDataDirectoryGroup(dataDirectoryGroup);
		drawReceiversGroup(receiverGroup);

		scrolledComposite.setMinSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		configShell.open();
	}

	
	/**
	 * 
	 * @param dataDirectoryGroup
	 */
	private void drawDataDirectoryGroup(Group directoryParent) {
		final Label pathLabel = new Label(directoryParent, SWT.NONE);
//		pathLabel.setText(dataDirectory.getAbsolutePath());
		pathLabel.setText("C:/Users/Administrator/sblit");
		Button b = new Button(directoryParent, SWT.PUSH);
		b.setText("Select");
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(configShell, SWT.OPEN);
				String result = dialog.open();
				if (result != null && result.length() > 0){
					pathLabel.setText(result);
					// TODO dataDirectory in Configuration ändern
					System.out.println("Basic Data Directory: \"" + result + "\"");
				}
			}
		});
	}

	
	/**
	 * 
	 * @param parent
	 */
	private void drawReceiversGroup(Group parent) {
		// TODO Auto-generated method stub
		String[] testReceivers = new String[10];
		for (int i = 0; i < 10; i++){
			testReceivers[i] = ("Host-" + i*555555555);
		}
		
		final List receiverList = new List(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		for (int i = 0; i < 10; i++){
			receiverList.add(testReceivers[i]);
		}
		
		
		GridData data = new GridData(GridData.FILL_BOTH);
		receiverList.setLayoutData(data);
		
		Button rmReceiverButton = new Button(parent, SWT.CANCEL);
		rmReceiverButton.setText("Remove");
		rmReceiverButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int [] selected = receiverList.getSelectionIndices();
				// TODO Mit einer Schleife, die Elemente aus der List "receivers" löschen
				receiverList.remove(selected);
				configSaved = false;
			}
		});

		data = new GridData(GridData.HORIZONTAL_ALIGN_END);
		data.horizontalSpan = 2;
		rmReceiverButton.setLayoutData(data);


		Button addReceiverButton = new Button(parent, SWT.SAVE);
		addReceiverButton.setText("Add");
		addReceiverButton.addSelectionListener (new SelectionAdapter () {
			@Override
			public void widgetSelected (SelectionEvent e) {
				final Shell dialog = new Shell (configShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				dialog.setText("Adding a Receiver");
				FormLayout formLayout = new FormLayout ();
				formLayout.marginWidth = 10;
				formLayout.marginHeight = 10;
				formLayout.spacing = 10;
				dialog.setLayout (formLayout);

				Label label = new Label (dialog, SWT.NONE);
				label.setText ("Type a Receiver:");
				FormData data = new FormData ();
				label.setLayoutData (data);

				Button cancel = new Button (dialog, SWT.PUSH);
				cancel.setText ("Cancel");
				data = new FormData ();
				data.width = 60;
				data.right = new FormAttachment (100, 0);
				data.bottom = new FormAttachment (100, 0);
				cancel.setLayoutData (data);
				cancel.addSelectionListener (new SelectionAdapter () {
					@Override
					public void widgetSelected (SelectionEvent e) {
						dialog.close ();
					}
				});

				final Text hostname = new Text (dialog, SWT.BORDER);
				data = new FormData ();
				data.width = 90;
				data.left = new FormAttachment (label, 0, SWT.DEFAULT);
				data.right = new FormAttachment (35, 0);
				data.top = new FormAttachment (label, 0, SWT.CENTER);
				data.bottom = new FormAttachment (cancel, 0, SWT.DEFAULT);
				hostname.setLayoutData (data);
				
				final Text key = new Text (dialog, SWT.BORDER);
				data = new FormData ();
				data.width = 90;
				data.left = new FormAttachment (hostname, 0, SWT.DEFAULT);
				data.right = new FormAttachment (100, 0);
				data.top = new FormAttachment (hostname, 0, SWT.CENTER);
				data.bottom = new FormAttachment (cancel, 0, SWT.DEFAULT);
				key.setLayoutData (data);

				Button ok = new Button (dialog, SWT.PUSH);
				ok.setText ("OK");
				data = new FormData ();
				data.width = 60;
				data.right = new FormAttachment (cancel, 0, SWT.DEFAULT);
				data.bottom = new FormAttachment (100, 0);
				ok.setLayoutData (data);
				ok.addSelectionListener (new SelectionAdapter () {
					@Override
					public void widgetSelected (SelectionEvent e) {
						receiverList.add(hostname.getText() + "-" + key.getText());
						// TODO Receivers in List umändern und hier einfach hinzufügen
						dialog.close ();
					}
				});
				
				configSaved = false;
				
				dialog.setDefaultButton (ok);
				dialog.pack ();
				dialog.open ();
			}
		});
		
		
		
		data = new GridData(GridData.HORIZONTAL_ALIGN_END);
		data.horizontalSpan = 2;
		addReceiverButton.setLayoutData(data);
	}
}
