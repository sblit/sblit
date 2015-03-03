package net.sblit.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public class SystemTray {
	public static void main(String[] args) {
		Display display = new Display();
		final Shell shell = new Shell(display);
		final ConfigurationDialog configurationDialog = new ConfigurationDialog(shell);

		Image image = new Image(display, "D:\\Dokumente\\Schule\\5DN\\Diplomarbeit\\sblit\\src\\org\\sblit\\gui\\icon.png");

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
					System.out.println("selection");
				}
			});
			item.addListener(SWT.DefaultSelection, new Listener() {
				public void handleEvent(Event event) {
					System.out.println("default selection");
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
			MenuItem printItem = new MenuItem(menu, SWT.PUSH);
			printItem.setText("Print Status");
			printItem.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					System.out.println(configurationDialog.getConfigShell().isDisposed());
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

