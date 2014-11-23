package org.sblit.configuration;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.swing.JOptionPane;

import org.dclayer.lib.DCLApplication;
import org.sblit.Sblit;

/**
 * Contains the whole configuration for Sblit
 * @author Nikola
 *
 */
public class Configuration {
	
	public static final String FOREIGN_FILES = "/foreign_files";
	public static final String LOG_FILE = "/logs.txt";
	
	private static File configurationDirectory;
	private static PrivateKey privateAddressKey;
	private static PublicKey publicAddressKey;
	private static DCLApplication app = null;
	private static ReceiverConfiguration receiverConfiguration;
	private static DataDirectoryConfiguration dataDirectoryConfiguration;
	private static KeyConfiguration keyConfiguration;
	//private String receivers;
	
	/**
	 * This method has to be called while starting Sblit
	 * If Sblit is configured already, this class reads the configuration from the files. 
	 * If not, it initializes the configuration-dialogs and generates the files that are needed.
	 */
	public final static void initialize() {
		try {
			app = new DCLApplication(new InetSocketAddress(InetAddress.getLocalHost(), 2000), Sblit.APPLICATION_IDENTIFIER);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "DCL Service must be installed and running on your device!\nPlease start Sblit again after you made sure that DCL is running!\nDetailed Error: " + 
						e.getMessage(), "DCL not running", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(0);
		}
		String os = System.getProperty("os.name");
		System.out.println(os);
		if(os.contains("Windows")){
			configurationDirectory = new File(System.getenv("APPDATA") + "/SBLIT/");
		} else {
			configurationDirectory = new File("~/.SBLIT/");
		}
		receiverConfiguration = new ReceiverConfiguration(configurationDirectory.getAbsolutePath());
		keyConfiguration = new KeyConfiguration(configurationDirectory.getAbsolutePath(), os);
		System.out.println(configurationDirectory.getAbsolutePath());
		
//	TODO	AddressConfiguration addressConfiguration = new AddressConfiguration(configurationDirectory, os);
//	TODO	privateAddressKey = addressConfiguration.getPrivateKey();
//	TODO	publicAddressKey = addressConfiguration.getPublicKey();
		dataDirectoryConfiguration = new DataDirectoryConfiguration(configurationDirectory.getAbsolutePath());
		
		
		//TODO passwordConfiguration
	}
	/**
	 * Returns the configured data-directory for Sblit. 
	 * @return
	 *  The configured data-directory for Sblit
	 */
	public static File getSblitDirectory(){
		return dataDirectoryConfiguration.getDataDirectory();
	}
	
	/**
	 * Returns the private address-key for this specific Host. 
	 * The private address-key is used to authenticate in the DCL network 
	 * @return
	 * Returns the private address-key for this specific Host.
	 */
	public static PrivateKey getPrivateAddressKey(){
		return privateAddressKey;
	}
	
	/**
	 * Returns the public address-key for this specific Host.
	 * The public address-key is used for the identification in the DCL network
	 * @return
	 * Returns the public address-key for this specific Host.
	 */
	public static PublicKey getPublicAddressKey(){
		return publicAddressKey;
	}
	
	/**
	 * Returns the configuration directory for Sblit.
	 * Every file which contains information, used by Sblit, is saved in this directory.
	 * @return
	 * Returns the configuration directory for Sblit.
	 */
	public static File getConfigurationDirectory(){
		return configurationDirectory;
	}
	
	/**
	 * Returns the {@link DCLApplication}, needed for sending and receiving information between the DCL network and the application.
	 * @return
	 * Returns the {@link DCLApplication}.
	 */
	public static DCLApplication getApp(){
		return app;
	}
	/**
	 * For other devices call getReceivers(String sender)!
	 * @return
	 * Returns OWN devices!
	 */
	public static String[] getReceivers(){
		return receiverConfiguration.getReceivers();
	}
	/**
	 * Returns the symmetric key
	 * @return
	 */
	public static byte[] getKey(){
		return keyConfiguration.getKey();
	}
	
	protected static void addReceiver(String receiver){
		receiverConfiguration.addReceiver(receiver);
	}
	
	protected static void setSblitDirectory(String directory){
		dataDirectoryConfiguration.setDataDirectory(directory);
	}
	
	protected void setSymmetricKey(byte[] key){
		keyConfiguration.setKey(key);
	}
	
}
