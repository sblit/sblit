package net.sblit.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sblit.fileProcessing.FileStateListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

public class UeberblickFenster implements FileStateListener{
	//	File fileList = new File(Configuration.getConfigurationDirectory().getAbsoluteFile(),"fileList.txt");
	File fileList = new File("C:\\Users\\Andi\\AppData\\Roaming\\SBLIT","fileList.txt");
	Display display = Display.getCurrent();
	Shell shell = new Shell(display);
	private Label syncStateImgLbl;
	private Label syncStateTextLbl;
	private Composite contentComposite;

	public static void main(String[] args) {
		new UeberblickFenster();
	}

	public UeberblickFenster() {
		shell.setImage(new Image(display, "bin/net/sblit/gui/icon.png"));
		shell.setText("Summary");
		shell.addListener (SWT.Close, new Listener () {
			@Override
			public void handleEvent (Event event) {
				shell.setVisible(false);
				event.doit = SWT.CANCEL == SWT.YES;
			}
		});
		shell.addShellListener(new ShellListener() {

			@Override
			public void shellIconified(ShellEvent e) {

			}

			@Override
			public void shellDeiconified(ShellEvent e) {

			}

			@Override
			public void shellDeactivated(ShellEvent e) {
				shell.setVisible(false);
			}

			@Override
			public void shellClosed(ShellEvent e) {

			}

			@Override
			public void shellActivated(ShellEvent e) {

			}
		});

		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing   = 0;
		layout.marginLeft   = -5;
		layout.marginTop    = -5;
		layout.marginBottom = -5;
		layout.marginRight  = -5;
		shell.setLayout(layout);

		Color headerGrey = new Color(display, new RGB(230, 230, 230));

		Composite headerCmpst = new Composite(shell, SWT.BACKGROUND | SWT.BORDER);
		headerCmpst.setBackground(headerGrey);
		GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		headerCmpst.setLayoutData(layoutData);
		layout = new GridLayout(5, false);
		layout.horizontalSpacing = 10;
		layout.verticalSpacing   = 10;
		layout.marginLeft   = 0;
		layout.marginTop    = 0;
		layout.marginBottom = 0;
		layout.marginRight  = 0;
		headerCmpst.setLayout(layout);

		FileSyncState fileSyncState = FileSyncState.SYNCHRONISED;
		layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		syncStateImgLbl = fileSyncState.getImageLabel(display, headerCmpst);
		syncStateImgLbl.setBackground(headerGrey);;
		syncStateImgLbl.setLayoutData(layoutData);

		syncStateTextLbl = new Label(headerCmpst, SWT.NONE);
		syncStateTextLbl.setText(fileSyncState.getDescription());
		syncStateTextLbl.setBackground(headerGrey);
		layoutData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		syncStateTextLbl.setLayoutData(layoutData); 


		final ScrolledComposite fileScrolledComposite = new ScrolledComposite(shell, SWT.BORDER | SWT.V_SCROLL);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		fileScrolledComposite.setLayoutData(layoutData);
		contentComposite = new Composite(fileScrolledComposite, SWT.NONE);
		contentComposite.setLayout(new GridLayout(10, true));
		fileScrolledComposite.setContent(contentComposite);
		fileScrolledComposite.setExpandHorizontal(true);
		fileScrolledComposite.setExpandVertical(true);
		fileScrolledComposite.setMinSize(contentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		fileScrolledComposite.setShowFocusedControl(true);

		layout = new GridLayout(1, false);
		layout.marginLeft   = -7;
		layout.marginRight  = -7;
		layout.marginTop    = -7;
		layout.marginBottom = -7;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 4;
		contentComposite.setLayout(layout);

		drawFileRows(contentComposite);
	}

	public void open(){
		centerShell();
		shell.open();
		shell.setVisible(true);
	}

	private void drawFileRows(Composite parent){
		List<String[]> fileList = readFileList();
		for(int i = 0; i < fileList.size(); i++) {
			new FileRow(parent, SWT.BORDER, fileList.get(i)[0], fileList.get(i)[1], FileSyncState.SYNCHRONISED);
		}
	}

	private List<String[]> readFileList(){
		List<String[]> arrayBuffer = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileList));
			String buffer;
			while((buffer = br.readLine()) != null){
				arrayBuffer.add(buffer.split(";"));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return arrayBuffer;
	}

	public void updateSyncState(final FileSyncState newFileSyncState){
		syncStateImgLbl.setImage(new Image(display, newFileSyncState.getFilePath()));
		syncStateTextLbl.setText(newFileSyncState.getDescription());
		syncStateImgLbl.getParent().layout();
	}


	@Override
	public void registerFile(String path) {
		// TODO Auto-generated method stub
		File tempFile = new File(path);
		FileRow newFileRow = new FileRow(contentComposite, SWT.BORDER, tempFile.getName(), tempFile.getPath(), FileSyncState.SYNCHRONISING);
		contentComposite.layout();
	}



	@Override
	public void error(String path, String message) {
		for(Control control : contentComposite.getChildren()){
			if(control.getData()==path){
				Composite tempComposite = (Composite)control;
				for(Control tempControl : tempComposite.getChildren()){
					Widget rowElement = (Widget)tempControl;
					if(rowElement.getData()=="stateImgLabel"){
						Label stateImgLabel = (Label)rowElement;
						stateImgLabel.setImage(new Image(Display.getCurrent(), FileSyncState.ERROR.getFilePath()));
						stateImgLabel.getParent().layout();
						SystemTray.notifyUser("Error", "An error occured", message);
					}
				}
			}
		}
	}

	private void writeFiletoFilesList(File synchronisisedFile){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileList));
			bw.write(synchronisisedFile.getName() + ";" + synchronisisedFile.getPath());
			bw.write('\n');
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void unregisterFile(String path) {
		for(Control control : contentComposite.getChildren()){
			if(control.getData()==path){
				Composite tempComposite = (Composite)control;
				for(Control tempControl : tempComposite.getChildren()){
					Widget rowElement = (Widget)tempControl;
					if(rowElement.getData()=="stateImgLabel"){
						Label stateImgLabel = (Label)rowElement;
						stateImgLabel.setImage(new Image(Display.getCurrent(), FileSyncState.SYNCHRONISED.getFilePath()));
						stateImgLabel.getParent().layout();
						SystemTray.notifyUser("Information", "File successfully synchronised.", "The file at \"" + path + "\" is now synchronised.");
						writeFiletoFilesList(new File(path));
					}
				}
			}
		}
	}

	private void centerShell() {
		Rectangle bounds = Display.getCurrent().getBounds();
		int width = 350;
		int height = 275;

		shell.setBounds((bounds.width-width)-50, (bounds.height-height)-50, width, height);
	}

	private void searchAndDestroy(File file){
		String newFile = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String buffer = "";
			while((buffer = br.readLine()) != null){
				if(!(buffer.contains(file.getName() + ";" + file.getPath()))){
					newFile+=buffer;
				}
			}
			br.close();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(newFile);
			bw.flush();
			bw.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void deleteFile(String path) {
		for(Control control : contentComposite.getChildren()){
			if(control.getData()==path){
				Composite tempComposite = (Composite)control;

				tempComposite.dispose();

				searchAndDestroy(new File(path));
			}
		}	
	}
}
