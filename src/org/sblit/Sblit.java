package org.sblit;

import org.sblit.configuration.Configuration;
import org.sblit.filesync.PacketStarts;

/**
 * 
 * @author Nikola
 *
 */
public class Sblit {
	
	public static final String APPLICATION_IDENTIFIER = "org.sblit";
	
	public static void main(String[] args) {
		new Sblit();

	}
	
	public Sblit() {
		//TODO Check configuration
		
		Configuration.initialize();
		System.out.println(Configuration.getSblitDirectory());
		
		//TODO Share public key
		
		//TODO Find the other Devices that belong to the same group
		
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				try {
//					FileSender fileSender = new FileSender(null, null, configuration.getApp());
//					DirectoryWatcher directoryWatcher = new DirectoryWatcher(new File("C:\\Users\\Nikola\\Documents"), new File("C:\\Users\\Nikola\\test.txt"), "1");
//					fileSender.send(directoryWatcher.getFilesToPush(), directoryWatcher.getLogFile());
//				} catch (IOException | BufException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} 
//			}
//		}).run(); 
	}

}
