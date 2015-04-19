package net.sblit.gui;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import net.sblit.configuration.Configuration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Draws the configuration-dialog. At this GUI
 * the user can set all configuration parameters of sblit.
 * @author Andreas Novak
 *
 */
public class ConfigurationDialog{
	//TODO static?
	Shell configShell;
	String[] receivers;
	File dataDirectory;
	boolean ReceiverConfigSaved  = true; //TODO implement that shit
	boolean PartnerConfigSaved   = true; //TODO implement that shit
	boolean DirectoryConfigSaved = true; //TODO implement that shit

	public ConfigurationDialog(){
		this.configShell = new Shell (Display.getCurrent());
		create();
	}

	public void create(){
		configShell.setText("Configuration");

		// Adds a prompt for closing configuration-gui without saving
		configShell.addListener (SWT.Close, new Listener () {
			@Override
			public void handleEvent (Event event) {
				if (ReceiverConfigSaved == false || PartnerConfigSaved == false || DirectoryConfigSaved == false){
					int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO;
					MessageBox messageBox = new MessageBox (configShell, style);
					messageBox.setText ("Alert");
					messageBox.setMessage ("You have unsaved changes. Do you want to continue anyway?");
					event.doit = messageBox.open () == SWT.YES;
				}
			}
		});

		GridLayout layout = new GridLayout(2, false);
		configShell.setLayout(layout);

		Group receiverGroup = new Group(configShell, 0);
		receiverGroup.setText("My Devices");
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		receiverGroup.setLayoutData(layoutData);
		layout = new GridLayout(3, false);
		receiverGroup.setLayout(layout);

		Group partnerGroup = new Group(configShell, 0);
		partnerGroup.setText("Partner");
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		partnerGroup.setLayoutData(layoutData);
		layout = new GridLayout(3, false);
		partnerGroup.setLayout(layout);

		Group directoryGroup = new Group(configShell, 0);
		directoryGroup.setText("Directory");
		layoutData = new GridData(SWT.FILL, SWT.TOP, false, false, 2, 1);
		directoryGroup.setLayoutData(layoutData);
		layout = new GridLayout(2, false);
		layout.horizontalSpacing = 15;
		layout.marginLeft = 1;
		directoryGroup.setLayout(layout);

		Composite quitBtns = new Composite(configShell, 0);
		layoutData = new GridData(SWT.RIGHT, SWT.BOTTOM, true, false, 2, 1);
		quitBtns.setLayoutData(layoutData);
		layout = new GridLayout(2, false);
		layout.marginRight = -5;
		layout.marginTop = -5;
		layout.marginBottom = -5;
		quitBtns.setLayout(layout);

		drawReceiverGroup(receiverGroup);
		drawPartnerGroup(partnerGroup);
		drawDirectoryGroup(directoryGroup);
		drawquitBtns(quitBtns);

		configShell.pack();
	}

	public void open(){
		configShell.open();
	}

	public void close(){
		configShell.close();
	}

	private void drawquitBtns(Composite parent) {
		Button cancelBtn = new Button(parent, SWT.PUSH);
		cancelBtn.setText("Cancel");
		cancelBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override public void widgetSelected(final SelectionEvent e)
			{
				if (ReceiverConfigSaved == false || PartnerConfigSaved == false || DirectoryConfigSaved == false){
					int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO;
					MessageBox messageBox = new MessageBox (configShell, style);
					messageBox.setText ("Alert");
					messageBox.setMessage ("You have unsaved changes. Do you want to continue anyway?");
					if(messageBox.open () == SWT.YES){
						close();
					}
				} else {
					close();
				}
			}
		});
		GridData layoutData = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		cancelBtn.setLayoutData(layoutData);

		Button saveBtn = new Button(parent, SWT.PUSH);
		saveBtn.setText("Save Changes");
		saveBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override public void widgetSelected(final SelectionEvent e)
			{
				if (ReceiverConfigSaved == false){
					
				}
				if (PartnerConfigSaved == false){
					
				}
				if (DirectoryConfigSaved == false){
					
				}
				close();
			}
		});
		layoutData = new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1);
		saveBtn.setLayoutData(layoutData);		
	}
	
	private void writeTableToFile(Table table, File file){
		
	}

	private void drawDirectoryGroup(Group parent) {
		final Label directoryPathLbl = new Label(parent, 0);
		try {
			directoryPathLbl.setText(Configuration.getSblitDirectory().toString());
		} catch (NullPointerException e){
			directoryPathLbl.setText("No Directory set.");
		}
		GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		directoryPathLbl.setLayoutData(layoutData);

		Button selectBtn = new Button(parent, SWT.PUSH);
		selectBtn.setText("Select");
		selectBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override public void widgetSelected(final SelectionEvent e)
			{
				DirectoryDialog dirDialog = new DirectoryDialog(configShell);
				dirDialog.open();
				directoryPathLbl.setText(dirDialog.getFilterPath().toString());
			}
		});
		layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		selectBtn.setLayoutData(layoutData);
	}

	private void drawReceiverGroup(Group parent) {
		Label receiverFileStateLbl = new Label(parent, SWT.BOLD);
		receiverFileStateLbl.setText("");
		GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		receiverFileStateLbl.setLayoutData(layoutData);

		Composite importExportCmpst = new Composite(parent, 0);
		layoutData = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 2, 1);
		importExportCmpst.setLayoutData(layoutData);
		GridLayout layout = new GridLayout(2, false);
		layout.marginRight = -4;
		layout.marginTop = -5;
		layout.marginBottom = -5;
		importExportCmpst.setLayout(layout);

		Button importBtn = new Button(importExportCmpst, SWT.PUSH);
		importBtn.setText("Import");
		layoutData = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		importBtn.setLayoutData(layoutData);

		Button exportBtn = new Button(importExportCmpst, SWT.PUSH);
		exportBtn.setText("Export");
		layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		exportBtn.setLayoutData(layoutData);

		final Table receiverTable = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		fillReceiverTable(receiverTable, dummyReceiverHashMap());
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		receiverTable.setLayoutData(layoutData);

		Button editReceiverBtn = new Button(parent, SWT.PUSH);
		editReceiverBtn.setText("Edit");
		editReceiverBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override public void widgetSelected(final SelectionEvent e)
			{
				editReceiver(receiverTable);
			}
		});
		layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		editReceiverBtn.setLayoutData(layoutData);

		Button addReceiverBtn = new Button(parent, SWT.PUSH);
		addReceiverBtn.setText("Add");
		addReceiverBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override public void widgetSelected(final SelectionEvent e)
			{
				addReceiver(receiverTable);
			}
		});
		layoutData = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		addReceiverBtn.setLayoutData(layoutData);

		Button removeReceiverBtn = new Button(parent, SWT.PUSH);
		removeReceiverBtn.setText("Remove");
		layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		removeReceiverBtn.setLayoutData(layoutData);
	}
	
	private void editReceiver(Table receiverTable){
		String[] input = new String[2];
		try{
		TableItem selection = receiverTable.getSelection()[0];
		String oldHost = selection.getText(0).toString();
		String oldKey  = selection.getText(1).toString();					
		StringInputDialog receiverDialog = new StringInputDialog();
		receiverDialog.setMessage("Edit the receiver.  Format: hostname;publicKey");
		receiverDialog.setInput(oldHost + ";" + oldKey);
		input = receiverDialog.open();
		String newHost = input[0];
		String newKey = input[1];
		if(oldHost!=newHost && oldKey!=newKey){
			ReceiverConfigSaved = false;
			selection.setText(0, newHost);
			
			selection.setText(1, newKey);
		}
		} catch(ArrayIndexOutOfBoundsException ex){
			// No Row Selected
		}
	}
	
	private void addReceiver(Table receiverTable){
		String[] input = new String[2];
		try{
		TableItem selection = receiverTable.getSelection()[0];
		String oldHost = selection.getText(0).toString();
		String oldKey  = selection.getText(1).toString();					
		StringInputDialog receiverDialog = new StringInputDialog();
		receiverDialog.setMessage("Edit the receiver.  Format: hostname;publicKey");
		receiverDialog.setInput(oldHost + ";" + oldKey);
		input = receiverDialog.open();
		String newHost = input[0];
		String newKey = input[1];
		if(oldHost!=newHost && oldKey!=newKey){
			ReceiverConfigSaved = false;
			selection.setText(0, newHost);
			
			selection.setText(1, newKey);
		}
		} catch(ArrayIndexOutOfBoundsException ex){
			// No Row Selected
		}
	}

	private boolean checkInput(String[] input){
		//TODO Implement the checking method
		return true;
	}

	private void drawPartnerGroup(Group parent) {
		Label receiverFileStateLbl = new Label(parent, SWT.BOLD);
		receiverFileStateLbl.setText("");
		GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		layoutData.verticalSpan = 6;
		receiverFileStateLbl.setLayoutData(layoutData);

		Table receiverTable = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		fillPartnerTable(receiverTable, true);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		receiverTable.setLayoutData(layoutData);

		Button editBtn = new Button(parent, SWT.PUSH);
		editBtn.setText("Edit");
		layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		editBtn.setLayoutData(layoutData);

		Button addReceiverBtn = new Button(parent, SWT.PUSH);
		addReceiverBtn.setText("Add");
		layoutData = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		addReceiverBtn.setLayoutData(layoutData);

		Button removeReceiverBtn = new Button(parent, SWT.PUSH);
		removeReceiverBtn.setText("Remove");
		layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		removeReceiverBtn.setLayoutData(layoutData);
	}

	private void fillReceiverTable(Table table, HashMap<String, String> receivers) {
		table.setLinesVisible (true);
		table.setHeaderVisible (true);

		TableColumn hostnameColumn = new TableColumn(table, SWT.LEFT);
		hostnameColumn.setText("Hostname");

		TableColumn keyColumn = new TableColumn(table, SWT.LEFT);
		keyColumn.setText("Public Key");

		TableItem item = null;;

		for (Map.Entry<String, String> entry : receivers.entrySet())
		{
			item = new TableItem(table, SWT.NONE);
			item.setText(0, "" + entry.getKey());
			item.setText(1, "" + entry.getValue());
		}

		table.getColumn(0).pack();
		table.getColumn(1).pack();
	}

	/**
	 * 
	 * @return A {HashMap} with 4 rows of dummy data (hostname - rnd key)
	 */
	private HashMap<String, String> dummyReceiverHashMap() {
		HashMap<String, String> hashMap = new HashMap<>();
		String[] hostnames = {"Laptop","Desktop", "Arbeit", "Opa-PC"};

		for(String s : hostnames) {
			hashMap.put(s, getRandomHexString(40));
		}
		System.out.println(hashMap.toString());
		return hashMap;
	}

	private void fillPartnerTable(Table table, boolean insertDummyData) {
		if (insertDummyData){
			table.setLinesVisible (true);
			table.setHeaderVisible (true);

			TableColumn keyColumn = new TableColumn(table, SWT.LEFT);
			keyColumn.setText("Public Key");

			int n = 10;
			TableItem item;
			for(int i = 0; i < n; i++) {
				item = new TableItem(table, SWT.NONE);
				item.setText(0, getRandomHexString(40));
			}
			table.getColumn(0).pack();
		}

	}

	private String getRandomHexString(int numchars){
		Random r = new Random();
		StringBuffer sb = new StringBuffer();
		while(sb.length() < numchars){
			sb.append(Integer.toHexString(r.nextInt()));
		}

		return sb.toString().substring(0, numchars);
	}

	public Shell getConfigShell(){
		return configShell;
	}

	private void centerShell(Shell shell, Display display) {
		Rectangle bounds = display.getBounds();
		int width = 600;
		int height = 450;

		shell.setBounds((bounds.width-width)/2, (bounds.height-height)/2, width, height);;
	}
}