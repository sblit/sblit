package net.sblit.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import net.sblit.configuration.Configuration;

import org.dclayer.crypto.key.RSAPublicKey;
import org.dclayer.exception.crypto.CryptoException;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.buf.StreamByteBuf;
import org.dclayer.net.component.DataComponent;
import org.dclayer.net.component.KeyComponent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
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
	static Shell configShell;
	HashMap<net.sblit.crypto.RSAPublicKey, String> receiverChanges = new HashMap<>();
	HashMap<KeyComponent, String> partnerChanges = new HashMap<>();	

	public ConfigurationDialog(){
		configShell = new Shell (Display.getCurrent(), SWT.CLOSE | SWT.RESIZE | SWT.SYSTEM_MODAL);
		create();
		centerShell();
	}

	private void create(){
		configShell.setText("Configuration");

		// Adds a prompt for closing configuration-gui without saving
		configShell.addListener (SWT.Close, new Listener () {
			@Override
			public void handleEvent (Event event) {
				if (receiverChanges.size() != 0 || partnerChanges.size() != 0){
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
		configShell.dispose();
	}

	/**
	 * Draws the Group including the save button and the cancel button
	 * @param parent
	 */
	private void drawquitBtns(Composite parent) {
		Button cancelBtn = new Button(parent, SWT.PUSH);
		cancelBtn.setText("Cancel");
		cancelBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override public void widgetSelected(final SelectionEvent e)
			{
				if (receiverChanges.size() != 0 || partnerChanges.size() != 0){
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
				saveChangesAction();
				close();
			}
		});
		layoutData = new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1);
		saveBtn.setLayoutData(layoutData);		
	}


	private void saveChangesAction(){
		for (Entry<net.sblit.crypto.RSAPublicKey, String> entry : receiverChanges.entrySet())
		{
			if(entry.getValue() == null){
				Configuration.removeReceiver(entry.getKey());
			} else {
				Configuration.addReceiver(entry.getValue(), entry.getKey());
			}
		}
	}


	/**
	 * Draws the Group where the sblit directory can be chosen
	 * @param parent
	 */
	private void drawDirectoryGroup(Group parent) {
		final Label directoryPathLbl = new Label(parent, 0);
		try {
			directoryPathLbl.setText(Configuration.getSblitDirectory().toString());
		} catch (NullPointerException e){
			directoryPathLbl.setText("D:\\Documents\\sblit");
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
				if(dirDialog.getFilterPath().toString() != ""){
					directoryPathLbl.setText(dirDialog.getFilterPath().toString());
				}
			}
		});
		layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		selectBtn.setLayoutData(layoutData);
	}


	/**
	 * Draws the Group including the table of the receivers
	 * @param parent
	 */
	private void drawReceiverGroup(Group parent) {
		final Table receiverTable = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		fillReceiverTable(receiverTable);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		receiverTable.setLayoutData(layoutData);

		Button editReceiverBtn = new Button(parent, SWT.PUSH);
		editReceiverBtn.setText("Edit Hostname");
		editReceiverBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override public void widgetSelected(final SelectionEvent e)
			{
				editReceiverAction(receiverTable);
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
				addReceiverAction(receiverTable);
			}
		});
		layoutData = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		addReceiverBtn.setLayoutData(layoutData);

		Button removeReceiverBtn = new Button(parent, SWT.PUSH);
		removeReceiverBtn.setText("Remove");
		removeReceiverBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override public void widgetSelected(final SelectionEvent e)
			{
				removeReceiverAction(receiverTable);
			}
		});
		layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		removeReceiverBtn.setLayoutData(layoutData);
	}

	private void editReceiverAction(Table receiverTable){
		try{
			TableItem selection = receiverTable.getSelection()[0];
			String oldHost = selection.getText(0);
			StringInputDialog receiverDialog = new StringInputDialog();
			receiverDialog.setMessage("Put in a new hostname:");
			receiverDialog.setInput(oldHost);
			String input = receiverDialog.open();
			if(input != null && oldHost!=input){
				receiverChanges.put((net.sblit.crypto.RSAPublicKey) selection.getData(), input);
				selection.setText(0, input);
			}
		} catch(ArrayIndexOutOfBoundsException ex){
			// No Row Selected
		}
	}

	private void removeReceiverAction(Table receiverTable){
		try{
			TableItem selection = receiverTable.getSelection()[0];
			receiverChanges.put((net.sblit.crypto.RSAPublicKey) selection.getData(), null);
			selection.dispose();
		} catch(ArrayIndexOutOfBoundsException ex){
			// No Row Selected
		}
	}

	/**
	 * Opens a FileDialog, reads the chosen file and sets the read symmtric Key + add the read remote RSAPublicKey to the list of receivers.
	 */
	private void addReceiverAction(Table receiverTable){
		FileDialog fileChooserDialog = new FileDialog(configShell, SWT.SINGLE);
		fileChooserDialog.setFilterPath(System.getProperty("user.home"));
		fileChooserDialog.setFilterNames(new String[] {"All Files (*.*)" });
		fileChooserDialog.setFilterExtensions(new String[] {"*.*"} );
		String path = fileChooserDialog.open();

		DataComponent symmetricKey = new DataComponent();
		KeyComponent remotePublicKey = new KeyComponent();

		if(path != null){
			try {
				StreamByteBuf streamByteBuf = new StreamByteBuf(new FileInputStream(new File(fileChooserDialog.getFilterPath(),fileChooserDialog.getFileName())));
				symmetricKey.read(streamByteBuf);
				remotePublicKey.read(streamByteBuf);
			} catch (FileNotFoundException e) {
				SystemTray.notifyUser("Error", "File not found.", "The selected file has not been found.");
			} catch (ParseException e) {
				SystemTray.notifyUser("Error", "Analysing Error.", "An error occured while analysing the key file.");
				e.printStackTrace();
			} catch (BufException e) {
				SystemTray.notifyUser("Error", "Reading Error.", "An error occured while reading the key file.");
				e.printStackTrace();
			} 

			// TODO Check if there is already a Key configured and wheather it should be overwritten or not
			// Like: The host of this key file has a different symmetrical key, which causes issues to other devices. 
			// Do you want to overwrite ... 
			Configuration.setSymmetricKey(symmetricKey.getData().getData());


			try {
				RSAPublicKey remoteKeyTempDCL = (RSAPublicKey) remotePublicKey.getKeyComponent().getKey();
				net.sblit.crypto.RSAPublicKey remoteKeyTempSblit = new net.sblit.crypto.RSAPublicKey(remoteKeyTempDCL.getRSAKeyParameters());

				if(remoteKeyExists(receiverTable, remoteKeyTempSblit)==false){

					StringInputDialog hostnameDialog = new StringInputDialog();
					hostnameDialog.setMessage("Please type the hostname of the receiver");
					hostnameDialog.setInput("Laptop");
					String input = hostnameDialog.open();

					if(input != null){
						TableItem newReceiver = new TableItem(receiverTable, SWT.NONE);
						newReceiver.setText(0, input);
						newReceiver.setText(1, remoteKeyTempSblit.hashCode() + "");
						newReceiver.setData(remoteKeyTempSblit);

						receiverChanges.put(remoteKeyTempSblit, input);
					}
				}
			} catch (CryptoException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// TODO Adding the remotePublicKey to "My Devices" (GUI).
		}
	}
	

	private boolean remoteKeyExists(Table table,
			net.sblit.crypto.RSAPublicKey key) {
		for(int i = 0; i < table.getItemCount(); i++){
			if(table.getItem(i).getData()==key){
				return true;
			}
		}
		return false;
	}

	/**
	 * Draws the Group including the table of partners
	 * @param parent
	 */
	private void drawPartnerGroup(Group parent) {
		Table receiverTable = new Table(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		fillPartnerTable(receiverTable);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
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

	private void fillReceiverTable(Table table) {
		table.setLinesVisible (true);
		table.setHeaderVisible (true);

		TableColumn hostnameColumn = new TableColumn(table, SWT.LEFT);
		hostnameColumn.setText("Hostname");

		TableColumn keyColumn = new TableColumn(table, SWT.LEFT);
		keyColumn.setText("Public Key");

		TableItem item = null;
		
		for (Entry<net.sblit.crypto.RSAPublicKey, String> entry : Configuration.getReceiversAndNames().entrySet())
		{
			
			item = new TableItem(table, SWT.NONE);
			item.setText(0, entry.getValue());		// Value = Hostname
			item.setText(1, entry.getKey().hashCode() + "");		// Key   = RSA PublicKey
			item.setData(entry.getKey());
		}

		table.getColumn(0).pack();
		table.getColumn(1).pack();
	}


	/**
	 * 
	 * @return A HashMap with 4 rows of dummy data (hostname - rnd key)
	 */
	//	private HashMap<String, String> dummyReceiverHashMap() {
	//		HashMap<String, String> hashMap = new HashMap<>();
	//		String[] hostnames = {"Laptop","Desktop", "Arbeit", "Opa-PC"};
	//
	//		for(String s : hostnames) {
	//			hashMap.put(s, getRandomHexString(40));
	//		}
	//		System.out.println(hashMap.toString());
	//		return hashMap;
	//	}

	private void fillPartnerTable(Table table) {

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

	private void centerShell() {
		Rectangle bounds = Display.getCurrent().getBounds();
		int width = 500;
		int height = 375;

		configShell.setBounds((bounds.width-width)/2, (bounds.height-height)/2, width, height);;
	}
}
