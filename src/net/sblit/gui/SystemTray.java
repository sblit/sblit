package net.sblit.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import net.sblit.Sblit;
import net.sblit.configuration.Configuration;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.net.Data;
import org.dclayer.net.buf.StreamByteBuf;
import org.dclayer.net.component.DataComponent;
import org.dclayer.net.component.KeyComponent;
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

	static Display display = new Display();
	final static Shell shell = new Shell(display);
	final UeberblickFenster ueberblickFenster = new UeberblickFenster();		
	static TrayItem item;
	static ToolTip toolTip;

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
					ueberblickFenster.open();				 
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
					new ConfigurationDialog().open();
				}
			});
			MenuItem export = new MenuItem (menu, SWT.PUSH);
			export.setText ("&Export Key");
			export.addListener (SWT.Selection, new Listener () {
				@Override
				public void handleEvent (Event e) {
					DataComponent symmetricKey = new DataComponent();
					symmetricKey.setData(new Data(Configuration.getKey()));

					KeyComponent localPublicKey = new KeyComponent();
					localPublicKey.setKey((Configuration.getPublicAddressKey()));

					exportKey(symmetricKey, localPublicKey);	
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
	public static void notifyUser(String type, String headline, String message){
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
	 * Opens a FileDialog and writes the symmtric Key + the local RSAPublicKey to the chosen destination.
	 */
	public void exportKey(DataComponent symmetricKey, KeyComponent localPublicKey){
		FileDialog saveKeyDialog = new FileDialog(shell, SWT.SAVE);

		saveKeyDialog.setFilterPath(System.getProperty("user.home"));
		saveKeyDialog.setFileName("keyFile");
		saveKeyDialog.setFilterNames(new String[] {"All Files (*.*)" });
		saveKeyDialog.setFilterExtensions(new String[] {"*.*"} );

		String path = saveKeyDialog.open();
		if(path != null){
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
}

