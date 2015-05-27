package net.sblit.gui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class FileRow{
	private String fileName;
	private String folderPath;
	private FileSyncState fileSyncState;
	private Composite parent;
	private int style;
	private static String FILE_IMG_PATH = "gen_file_icon.png";
	private Label stateImgLabel;
//	Zeit
	
	public FileRow(Composite parent, int style, String fileName, String folderPath, FileSyncState fileSyncState){
		this.fileName = fileName;
		this.fileSyncState = fileSyncState;
		this.folderPath = folderPath;
		this.parent = parent;
		this.style = style;
		drawRow();
	}
	
	private void drawRow(){
		Composite row = new Composite(parent, style);
		GridData layoutData = new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1);
		row.setLayoutData(layoutData);
		GridLayout layout = new GridLayout(4, false);
		layout.marginLeft   = 0;
		layout.marginRight  = 0;
		layout.marginTop    = 0;
		layout.marginBottom = 0;
		layout.horizontalSpacing = 15;
		layout.verticalSpacing   = 10;
		row.setLayout(layout);
		row.setData(new File(folderPath,fileName).getAbsolutePath());
		
		Label fileImgLabel = new Label(row, SWT.NONE);
		fileImgLabel.setImage(new Image(Display.getCurrent(), FILE_IMG_PATH));
		layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		fileImgLabel.setLayoutData(layoutData);
		
		Label fileNameLbl = new Label(row, SWT.NONE);
		fileNameLbl.setText(fileName);
		layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		fileNameLbl.setLayoutData(layoutData);
		
		Label filePathLabel = new Label(row, SWT.NONE);
		filePathLabel.setText(folderPath);
		filePathLabel.setForeground(new Color(Display.getCurrent(), new RGB(170, 170, 170)));
		layoutData = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		filePathLabel.setLayoutData(layoutData);
		
		Label stateImgLabel = fileSyncState.getImageLabel(Display.getCurrent(), row);
		layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		stateImgLabel.setLayoutData(layoutData);
		stateImgLabel.setData("stateImgLabel");
		
//		Label deleteImgLabel = new Label(row, SWT.NONE);
//		deleteImgLabel.setImage(new Image(Display.getCurrent(),DELETE_IMG_PATH));
//		layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
//		deleteImgLabel.setLayoutData(layoutData);
	}
	
	public void setFileSyncState(FileSyncState fileSyncState){
		this.fileSyncState = fileSyncState;
	}
	
	public void updateFileSyncState(FileSyncState fileSyncState){
		this.fileSyncState = fileSyncState;
		stateImgLabel.setImage(new Image(Display.getCurrent(), fileSyncState.getFilePath()));
		stateImgLabel.getParent().layout();
	}
}
