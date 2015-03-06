package net.sblit.gui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * The main class for the graphical user interface.
 * @author Andreas Novak
 *
 */
public class GUI {

	public static void main(String[] args) {
		Display display = new Display();
		final Shell shell = new Shell(display);
		
		shell.addListener (SWT.Close, new Listener () {
			// Prompt for closing sblit
			@Override
			public void handleEvent (Event event) {
				int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO;
				MessageBox messageBox = new MessageBox (shell, SWT.ABORT);
				messageBox.setText ("Information");
				messageBox.setMessage ("Do you really want to close sblit?");
				event.doit = messageBox.open () == SWT.YES;
			}
		});
		
		// Basic Menubar
		Menu menubar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menubar);
		// datamenu
		MenuItem dataitem = new MenuItem(menubar, SWT.CASCADE);
		dataitem.setText("&Data");
		Menu datamenu = new Menu (shell, SWT.DROP_DOWN);
		dataitem.setMenu (datamenu);
		// Export-Item on datamenu
		MenuItem export = new MenuItem (datamenu, SWT.PUSH);
		export.addListener (SWT.Selection, new Listener () {
			@Override
			public void handleEvent (Event e) {
				// TODO Save the file with the privatekey + Passwd (FileDialog as SaveDialog in Test-SWT
			}
		});
		export.setText ("&Export Private Key + Password\tCtrl+E");
		export.setAccelerator (SWT.MOD1 + 'E');
		// Configuration-Item on datamenu
		MenuItem configure = new MenuItem (datamenu, SWT.PUSH);
		configure.addListener (SWT.Selection, new Listener () {
			@Override
			public void handleEvent (Event e) {
				new ConfigurationDialog().open();
			}
		});
		configure.setText ("&Configure\tCtrl+Alt+C");
		configure.setAccelerator (SWT.MOD1 + SWT.MOD3 + 'C');
		
		
		
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
