package org.sblit.gui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * The main class for the graphical user interface.
 * @author Andreas Novak
 *
 */
public class GUI {

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		Menu menubar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menubar);
		MenuItem dataitem = new MenuItem(menubar, SWT.CASCADE);
		dataitem.setText("&Datei");

		Menu datamenu = new Menu (shell, SWT.DROP_DOWN);
		dataitem.setMenu (datamenu);
		MenuItem export = new MenuItem (datamenu, SWT.PUSH);
		export.addListener (SWT.Selection, new Listener () {
			@Override
			public void handleEvent (Event e) {
				exportPrivateKey(null);
			}
		});
		export.setText ("&Export Private Key + Password\tCtrl+E");
		export.setAccelerator (SWT.MOD1 + 'A');

		shell.setSize (400, 200);
		shell.open();
		
		// run the event loop as long as the window is open
		while (!shell.isDisposed()) {
		    // read the next OS event queue and transfer it to a SWT event 
			if (!display.readAndDispatch()){
				// if there are currently no other OS event to process
				// sleep until the next OS event is available 
				display.sleep ();
			}
		}

		// disposes all associated windows and their components
		display.dispose ();
	}

	/**
	 * Exports the generated Private Key + Password.
	 */
	public static void exportPrivateKey(File destination){
		// TODO Implement the export function.
		System.out.println ("Choose file");
	}
}
