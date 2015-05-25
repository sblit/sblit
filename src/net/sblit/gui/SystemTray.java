package net.sblit.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sblit.configuration.Configuration;

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
	final ConfigurationDialog configurationDialog = new ConfigurationDialog();	// TODO Ändern, dass es richtig angezeigt
	final GUI gui = new GUI();
	ToolTip InfoToolTip;
	
	Image image = new Image(display, "bin/net/sblit/gui/icon.png");

	public static void main(String[] args) {
		new SystemTray();
	}
	
	public SystemTray(){

		final Tray systemTray = display.getSystemTray();
				
		if (systemTray != null){
			TrayItem item = new TrayItem(systemTray, SWT.NONE);
			item.setToolTipText("sblit Datasync");
			
			InfoToolTip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_INFORMATION);
			item.setToolTip(InfoToolTip);
			
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
					gui.open();
				}
			});
			item.addListener(SWT.DefaultSelection, new Listener() {
				public void handleEvent(Event event) {
					try {
						// TODO ändern
//						Desktop.getDesktop().open(Configuration.getSblitDirectory());
						Desktop.getDesktop().open(new File("C:\\Users\\Andi\\Dropbox"));
					} catch (Exception e) {
						e.printStackTrace();
						// TODO open a textnote saying, that there is no sblit directory configured yet.
					}
				}
			});
			
			final Menu menu = new Menu(shell, SWT.POP_UP);
			
			MenuItem configurationMenuItem = new MenuItem(menu, SWT.PUSH);
			configurationMenuItem.setText("Configuration");
			configurationMenuItem.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					configurationDialog.open();
				}
			});
			
			MenuItem importing = new MenuItem (menu, SWT.PUSH);
			importing.setText ("&Import Key");
			importing.addListener (SWT.Selection, new Listener () {
				@Override
				public void handleEvent (Event e) {
					// TODO Import the file with the privatekey + Passwd (FileDialog as SaveDialog in Test-SWT)
					importKey();	
				}
			});
			
			MenuItem export = new MenuItem (menu, SWT.PUSH);
			export.setText ("&Export Key");
			export.addListener (SWT.Selection, new Listener () {
				@Override
				public void handleEvent (Event e) {
					// TODO Save the file with the privatekey + Passwd (FileDialog as SaveDialog in Test-SWT)
					exportKey();	
				}
			});
			MenuItem closeMenuItem = new MenuItem(menu, SWT.PUSH);
			closeMenuItem.setText("Shutdown sblit");
			closeMenuItem.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					shell.close();
					// TODO shutdown the programm.
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
		case "Information": notifyUserOnInformation(headline, message); break;
		case "Warning": notifyUserOnWarning(headline, message); break;
		case "Error": notifyUserOnError(headline, message); break;
		default: break;
		}
	}

	private void notifyUserOnInformation(String headline, String message){
		// TODO
		
	}

	private void notifyUserOnWarning(String headline, String message){
		// TODO
		
	}

	private void notifyUserOnError(String headline, String message){
		// TODO
		
	}
	
	private void importKey(){
		// TODO
		
	}
	
	private void exportKey(){
		FileDialog saveKeyDialog = new FileDialog(shell, SWT.SINGLE);
		
		saveKeyDialog.setFilterPath(System.getProperty("user.home"));
//		saveKeyDialog.setFilterPath("C:\Users\Andi\Desktop");
		saveKeyDialog.setFileName("keyFile");
		saveKeyDialog.setFilterNames(new String[] {"All Files (*.*)" });
		saveKeyDialog.setFilterExtensions(new String[] {"*.*"} );			// For Windows
		
		saveKeyDialog.open();
		System.out.println(saveKeyDialog.getFileName());
		byte[] keyInBytes = Configuration.getKey();
		byte[] publicKeyInBytes = Configuration.getPublicAddressKey().toData().getData();
		byte[] temp = new byte[keyInBytes.length + publicKeyInBytes.length];

		System.arraycopy(keyInBytes, 0, temp, 0, keyInBytes.length);
		System.arraycopy(publicKeyInBytes, 0, temp, 0, publicKeyInBytes.length);
		
			try {
//				new FileOutputStream(saveKeyDialog.getFilterPath() + Configuration.slash + saveKeyDialog.getFileName()).write(temp);
				new FileOutputStream(new File(saveKeyDialog.getFilterPath(),saveKeyDialog.getFileName())).write(temp);
			} catch (FileNotFoundException e) {
				// TODO Change to tooltip.
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Change to tooltip.
				e.printStackTrace();
			}

	}
}

