package org.sblit.configuration;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JOptionPane;

import org.dclayer.application.ApplicationInstance;
import org.dclayer.application.ApplicationInstanceBuilder;
import org.dclayer.application.Service;
import org.dclayer.application.applicationchannel.ApplicationChannel;
import org.dclayer.crypto.key.RSAPrivateKey;
import org.dclayer.crypto.key.RSAPublicKey;
import org.dclayer.net.Data;

/**
 * Contains the whole configuration for Sblit
 * @author Nikola
 *
 */
public class Configuration {
	
	//TODO add real default dcl port
	private final static int DEFAULT_DCL_PORT = 132321;
	
	public static final String FOREIGN_FILES = "/foreign_files";
	public static final String LOG_FILE = "/logs.txt";
	
	private static HashMap<String, Boolean> authenticatedReceivers = new HashMap<String,Boolean>();
	private static File configurationDirectory;
	private static RSAPrivateKey privateAddressKey;
	private static RSAPublicKey publicAddressKey;
	private static ApplicationInstance app = null;
	private static ReceiverConfiguration receiverConfiguration;
	private static DataDirectoryConfiguration dataDirectoryConfiguration;
	private static KeyConfiguration keyConfiguration;
	private static HashMap<Data,ApplicationChannel> channels;
	//private String receivers;
	
	/**
	 * This method has to be called while starting Sblit
	 * If Sblit is configured already, this class reads the configuration from the files. 
	 * If not, it initializes the configuration-dialogs and generates the files that are needed.
	 */
	public final static void initialize() {
		try {
			Service service = new Service(DEFAULT_DCL_PORT);
			app = new ApplicationInstanceBuilder(service).connect();
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
	public static RSAPrivateKey getPrivateAddressKey(){
		return privateAddressKey;
	}
	
	/**
	 * Returns the public address-key for this specific Host.
	 * The public address-key is used for the identification in the DCL network
	 * @return
	 * Returns the public address-key for this specific Host.
	 */
	public static RSAPublicKey getPublicAddressKey(){
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
	public static ApplicationInstance getApp(){
		return app;
	}
	/**
	 * For other devices call getReceivers(String sender)!
	 * @return
	 * Returns OWN devices! (name->address)
	 */
	public static HashMap<String,String> getReceiversAndNames(){
		return receiverConfiguration.getReceivers();
	}
	/**
	 * Returns the symmetric key
	 * @return
	 */
	public static byte[] getKey(){
		return keyConfiguration.getKey();
	}
	
	protected static void addReceiver(String name, String address){
		receiverConfiguration.addReceiver(name,address);
	}
	
	protected static void setSblitDirectory(String directory){
		dataDirectoryConfiguration.setDataDirectory(directory);
	}
	
	protected static void setSymmetricKey(byte[] key){
		keyConfiguration.setKey(key);
	}
	@Deprecated
	public static void addAuthenticatedReceiver(String receiver){
		authenticatedReceivers.put(receiver, true);
	}
	@Deprecated
	public static void removeAuthenticatedReceiver(String reveiver){
		authenticatedReceivers.remove(reveiver);
	}
	@Deprecated
	public static boolean checkAuthenticatedReceiver(String receiver){
		return authenticatedReceivers.get(receiver);
	}
	/**
	 * Returns the receivers address-keys
	 * For name + address-key use getReceiversAndName()
	 * @return
	 */
	public static Data[] getReceivers(){
		Collection<String> receivers = receiverConfiguration.getReceivers().values();
		Data[] result = new Data[receivers.size()];
		int i = 0;
		for(String receiver: receivers){
			result[i] = new Data(receiver.getBytes());
		}
		return result;
	}
	
	public static Set<Data> getChannels() {
		return channels.keySet();
	}
	
	public static void addChannel(Data receiverData,ApplicationChannel channel){
		channels.put(receiverData, channel);
	}
	
	public static void removeChannel(Data receiverData){
		channels.remove(receiverData);
	}
	
	public static ApplicationChannel getChannel(Data receiverData){
		return channels.get(receiverData);
	}
}
