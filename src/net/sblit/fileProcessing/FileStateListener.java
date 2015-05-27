package net.sblit.fileProcessing;

public interface FileStateListener {
	public void registerFile(String path);
	public void error(String path, String message);
	public void unregisterFile(String path);
	public void deleteFile(String path);
}
