package org.sblit.filesync.exceptions;

@SuppressWarnings("serial")
public class TimestampException extends Exception {
	@Override
	public String getMessage() {
		
		return super.getMessage() + "\nTimestamp Exception: Exception occured, because two timestamps were the same";
	}
}
