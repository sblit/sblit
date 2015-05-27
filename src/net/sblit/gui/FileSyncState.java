package net.sblit.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public enum FileSyncState {
	SYNCHRONISING("state_synchronising.gif", "Synchronising ..."),
	PAUSED("state_paused.png", "On hold."),
	ERROR("state_error.gif", "An error occured."),
	SYNCHRONISED("state_synchronised.png", "Up to Date.");
	
	private String filePath;
	private String description;
	
	FileSyncState(String filePath, String description) {
		this.filePath = filePath;
		this.description = description;
	}

	public String getFilePath(){
		return filePath;
	}

	public String getDescription(){
		return description;
	}
	
	public Label getImageLabel(Display display, Composite parent){
		Label imageLabel = new Label(parent, SWT.NONE);
		imageLabel.setImage(new Image(display, filePath));
		return imageLabel;
	}
}