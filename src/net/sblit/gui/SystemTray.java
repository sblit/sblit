package net.sblit.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sblit.Sblit;
import net.sblit.configuration.Configuration;

import org.dclayer.crypto.key.RSAKey;
import org.dclayer.crypto.key.RSAPublicKey;
import org.dclayer.exception.crypto.CryptoException;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.Data;
import org.dclayer.net.buf.StreamByteBuf;
import org.dclayer.net.component.DataComponent;
import org.dclayer.net.component.KeyComponent;
import org.dclayer.net.component.RSAKeyComponent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public class SystemTray {

	Display display = new Display();
	final Shell shell = new Shell(display);
	final UeberblickFenster ueberblickFenster = new UeberblickFenster();		// TODO Passt das so?
	TrayItem item;
	ToolTip toolTip;

	Image image = new Image(display, "bin/net/sblit/gui/icon.png");

	public SystemTray(){

		final Tray systemTray = display.getSystemTray();

		if (systemTray != null){
			item = new TrayItem(systemTray, SWT.NONE);
			item.setToolTipText("sblit Datasync");

			toolTip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_INFORMATION);
			item.setToolTip(toolTip);

			item.addListener(SWT.Show, new Listener() {
				public void handleEvent(Event event) {
					System.out.println("Show Event");
				}
			});;
			item.addListener(SWT.Hide, new Listener() {
				public void handleEvent(Event event) {
					System.out.println("hide");
				}
			});
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					//					ueberblickFenster.open();				 TODO ändern
				}
			});
			item.addListener(SWT.DefaultSelection, new Listener() {
				public void handleEvent(Event event) {
					try {
						Desktop.getDesktop().open(Configuration.getSblitDirectory());
					} catch (Exception e) {
						notifyUser("Warning", "No sblit directory found!", "There is no sblit directory configured.");
					}
				}
			});

			final Menu menu = new Menu(shell, SWT.POP_UP);

			MenuItem configurationMenuItem = new MenuItem(menu, SWT.PUSH);
			configurationMenuItem.setText("Configuration");
			configurationMenuItem.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
		//			new ConfigurationDialog().open();
				}
			});
			MenuItem importing = new MenuItem (menu, SWT.PUSH);
			importing.setText ("&Import Key");
			importing.addListener (SWT.Selection, new Listener () {
				@Override
				public void handleEvent (Event e) {
					importKey();	
				}
			});

			MenuItem export = new MenuItem (menu, SWT.PUSH);
			export.setText ("&Export Key");
			export.addListener (SWT.Selection, new Listener () {
				@Override
				public void handleEvent (Event e) {
					exportKey();	
				}
			});
			MenuItem closeMenuItem = new MenuItem(menu, SWT.PUSH);
			closeMenuItem.setText("Shutdown sblit");
			closeMenuItem.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					item.setVisible(false);
					shell.close();
					Sblit.exit();
				}
			});

			item.addListener(SWT.MenuDetect, new Listener() {
				public void handleEvent(Event event) {
					menu.setVisible(true);
				}
			});
			item.setImage(image);
		}
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		image.dispose();
		display.dispose();
		shell.dispose();
	}

	/**
	 * Notifies User by using a tooltip.
	 * @param type Either Information, Warning or Error (decides which icon will be used).
	 * @param headline The text which is on top of the notification
	 * @param message The text that is going to be written
	 */
	public void notifyUser(String type, String headline, String message){
		switch (type) {
		case "Information": toolTip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_INFORMATION); break;
		case "Warning": toolTip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_WARNING); break;
		case "Error": toolTip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_ERROR); break;
		default: break;
		}
		item.setToolTip(toolTip);
		toolTip.setText(headline);
		toolTip.setMessage(message);
		toolTip.setVisible(true);

		/*	TODO Open the Directory with the new File selected if a File has been synchronised.
		toolTip.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});*/
	}

	/**
	 * Opens a {FileDialog}, reads the chosen file and sets the read symmtric Key + add the read remote {RSAPublicKey} to the list of receivers.
	 */
	protected void importKey(){
		FileDialog fileChooserDialog = new FileDialog(shell, SWT.SINGLE);
		fileChooserDialog.setFilterPath(System.getProperty("user.home"));
		fileChooserDialog.setFilterNames(new String[] {"All Files (*.*)" });
		fileChooserDialog.setFilterExtensions(new String[] {"*.*"} );
		fileChooserDialog.open();

		DataComponent symmetricKey = new DataComponent();
		KeyComponent remotePublicKey = new KeyComponent();

		try {
			StreamByteBuf streamByteBuf = new StreamByteBuf(new FileInputStream(new File(fileChooserDialog.getFilterPath(),fileChooserDialog.getFileName())));
			symmetricKey.read(streamByteBuf);
			remotePublicKey.read(streamByteBuf);
		} catch (FileNotFoundException e) {
			notifyUser("Error", "File not found.", "The selected file has not been found.");
		} catch (ParseException e) {
			notifyUser("Error", "Analysing Error.", "An error occured while analysing the key file.");
			e.printStackTrace();
		} catch (BufException e) {
			notifyUser("Error", "Reading Error.", "An error occured while reading the key file.");
			e.printStackTrace();
		} 
		
		// TODO Check if there is already a Key configured and wheather it should be overwritten or not
		// Like: The host of this key file has a different symmetrical key, which causes issues to other devices. 
		// Do you want to overwrite ... 
		Configuration.setSymmetricKey(symmetricKey.getData().getData());
		
		StringInputDialog hostnameDialog = new StringInputDialog();
		hostnameDialog.setMessage("Please type the hostname of the receiver");
		hostnameDialog.setInput("");
		while(hostnameDialog.getInput()!=""){
			hostnameDialog.open();
		}
		// TODO Adding the remotePublicKey to "My Devices" (GUI).
		try {
			Configuration.addReceiver(hostnameDialog.getInput(), (RSAPublicKey)remotePublicKey.getKeyComponent().getKey());
		} catch (CryptoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Opens a {FileDialog} and writes the symmtric Key + the local {RSAPublicKey} to the chosen destination.
	 */
	private void exportKey(){
		FileDialog saveKeyDialog = new FileDialog(shell, SWT.SAVE);

		saveKeyDialog.setFilterPath(System.getProperty("user.home"));
		saveKeyDialog.setFileName("keyFile");
		saveKeyDialog.setFilterNames(new String[] {"All Files (*.*)" });
		saveKeyDialog.setFilterExtensions(new String[] {"*.*"} );

		saveKeyDialog.open();

		DataComponent symmetricKey = new DataComponent();
		symmetricKey.setData(new Data(Configuration.getKey()));

		KeyComponent localPublicKey = new KeyComponent();
		localPublicKey.setKey((Configuration.getPublicAddressKey()));

		try {
			StreamByteBuf streamByteBuf = new StreamByteBuf(new FileOutputStream(new File(saveKeyDialog.getFilterPath(),saveKeyDialog.getFileName())));
			streamByteBuf.write(symmetricKey);
			streamByteBuf.write(localPublicKey);
			notifyUser("Information", "Key has been saved.", "The key file has been saved successfully.");
		} catch (FileNotFoundException e) {
			notifyUser("Information", "Key could not be saved.", "There was no file selected.");
			e.printStackTrace();
		} catch (BufException e) {
			notifyUser("Error", "Writing Error.", "An error occured while saving the key file.");
			e.printStackTrace();
		}
	}
}

