package net.sblit.gui;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public class SystemTray {
	
	public static void main(String[] args) {
		new SystemTray();
	}
	
	public SystemTray(){
		Display display = new Display();
		final Shell shell = new Shell(display);
		final ConfigurationDialog configurationDialog = new ConfigurationDialog();
		final GUI gui = new GUI();

		Image image = new Image(display, "bin\\net\\sblit\\gui\\icon.png");

		final Tray systemTray = display.getSystemTray();

		if (systemTray != null){
			TrayItem item = new TrayItem(systemTray, SWT.NONE);
			item.setToolTipText("sblit 0.0");
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
//						Desktop.getDesktop().open(Configuration.getSblitDirectory());
						Desktop.getDesktop().open(new File("C:\\Users\\Andi\\Dropbox"));
					} catch (Exception e) {
						e.printStackTrace();
						//TODO open a textnote saying, that there is no sblit directory configured yet.
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
			MenuItem export = new MenuItem (menu, SWT.PUSH);
			export.setText ("&Export Private Key + Password");
			export.addListener (SWT.Selection, new Listener () {
				@Override
				public void handleEvent (Event e) {
					// TODO Save the file with the privatekey + Passwd (FileDialog as SaveDialog in Test-SWT
					FileDialog saveKeyDialog = new FileDialog(shell, SWT.SAVE);
					saveKeyDialog.setFilterPath(System.getProperty("user.home"));
					saveKeyDialog.setFileName("publicKey.txt");
					saveKeyDialog.open();
					try {
						new BufferedWriter(new FileWriter(saveKeyDialog.getFileName())).write("" +	Configuration.getKey().toString());
					} catch (Exception ex) {
//						ex.printStackTrace();
					}
				}
			});
			MenuItem closeMenuItem = new MenuItem(menu, SWT.PUSH);
			closeMenuItem.setText("Shutdown sblit");
			closeMenuItem.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					shell.close();
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
}

