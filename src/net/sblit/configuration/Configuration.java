package net.sblit.configuration;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JOptionPane;

import net.sblit.fileProcessing.FileStateListener;
import net.sblit.filesync.Receiver;

import org.dclayer.application.ApplicationInstance;
import org.dclayer.application.ApplicationInstanceBuilder;
import org.dclayer.application.Service;
import org.dclayer.application.applicationchannel.ApplicationChannel;
import org.dclayer.crypto.key.RSAPrivateKey;
import org.dclayer.exception.crypto.InsufficientKeySizeException;

import net.sblit.crypto.RSAPublicKey;

import org.dclayer.net.Data;

/**
 * Contains the whole configuration for Sblit
 * 
 * @author Nikola
 * 
 */
public class Configuration {

	// TODO add real default dcl port
	private final static int DEFAULT_DCL_PORT = 2000;

	public static final String FOREIGN_FILES = "/foreign_files";
	public static final String LOG_FILE = "/logs.txt";

	public static final String TEMP_FILE_START = ".sblit.";

	private static HashMap<String, Boolean> authenticatedReceivers = new HashMap<String, Boolean>();
	private static File configurationDirectory;
	private static RSAPrivateKey privateAddressKey;
	private static RSAPublicKey publicAddressKey;
	private static ApplicationInstance app = null;
	private static ReceiverConfiguration receiverConfiguration;
	private static DataDirectoryConfiguration dataDirectoryConfiguration;
	private static KeyConfiguration keyConfiguration;
	private static HashMap<Data, ApplicationChannel> channels = new HashMap<>();
	private static HashMap<Data, ApplicationChannel> unauthorizedChannels = new HashMap<>();
	private static FileStateListener fileStateListener;

	public static String slash;
	public static String otherSlash;

	// private String receivers;

	/**
	 * This method has to be called while starting Sblit If Sblit is configured
	 * already, this class reads the configuration from the files. If not, it
	 * initializes the configuration-dialogs and generates the files that are
	 * needed.
	 */
	public static void initialize() {
		String os = System.getProperty("os.name");
		System.out.println(os);
		if (os.contains("Windows")) {
			configurationDirectory = new File(System.getenv("APPDATA")
					+ "/SBLIT/");
			slash = "\\";
			otherSlash = "/";
		} else {
			configurationDirectory = new File(".SBLIT/");
			slash = "/";
			otherSlash = "\\";
		}
		configurationDirectory.mkdir();
		AddressConfiguration addressConfiguration = new AddressConfiguration(
				configurationDirectory, os);
		try {
			Service service = new Service(DEFAULT_DCL_PORT);
			app = new ApplicationInstanceBuilder(service)
					.joinDefaultNetworks(new Receiver())
					.addressKeyPair(addressConfiguration.getKeyPair())
					.connect();

		} catch (Exception e) {
			JOptionPane
					.showMessageDialog(
							null,
							"DCL Service must be installed and running on your device!\nPlease start Sblit again after you made sure that DCL is running!\nDetailed Error: "
									+ e.getMessage(), "DCL not running",
							JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(0);
		}
		receiverConfiguration = new ReceiverConfiguration(
				configurationDirectory.getAbsolutePath());
		keyConfiguration = new KeyConfiguration(
				configurationDirectory.getAbsolutePath(), os);
		System.out.println(configurationDirectory.getAbsolutePath());

		privateAddressKey = addressConfiguration.getPrivateKey();
		try {
			publicAddressKey = new RSAPublicKey(addressConfiguration.getPublicKey().getRSAKeyParameters());
		} catch (InsufficientKeySizeException e) {
			e.printStackTrace();
		}
		dataDirectoryConfiguration = new DataDirectoryConfiguration(
				configurationDirectory.getAbsolutePath());

		keyConfiguration = new KeyConfiguration(
				configurationDirectory.getAbsolutePath(), os);
		if (fileStateListener == null)
			fileStateListener = new FileStateListener() {

				@Override
				public void unregisterFile(String path) {
					// nothing to do
				}

				@Override
				public void registerFile(String path) {
					// nothing to do
				}

				@Override
				public void error(String path, String message) {
					// nothing to do
				}

				@Override
				public void deleteFile(String path) {
					// nothing to do					
				}
			};
	}

	/**
	 * Returns the configured data-directory for Sblit.
	 * 
	 * @return The configured data-directory for Sblit
	 */
	public static File getSblitDirectory() {
		return dataDirectoryConfiguration.getDataDirectory();
	}

	/**
	 * Returns the private address-key for this specific Host. The private
	 * address-key is used to authenticate in the DCL network
	 * 
	 * @return Returns the private address-key for this specific Host.
	 */
	public static RSAPrivateKey getPrivateAddressKey() {
		return privateAddressKey;
	}

	/**
	 * Returns the public address-key for this specific Host. The public
	 * address-key is used for the identification in the DCL network
	 * 
	 * @return Returns the public address-key for this specific Host.
	 */
	public static RSAPublicKey getPublicAddressKey() {
		return publicAddressKey;
	}

	/**
	 * Returns the configuration directory for Sblit. Every file which contains
	 * information, used by Sblit, is saved in this directory.
	 * 
	 * @return Returns the configuration directory for Sblit.
	 */
	public static File getConfigurationDirectory() {
		return configurationDirectory;
	}

	/**
	 * Returns the {@link DCLApplication}, needed for sending and receiving
	 * information between the DCL network and the application.
	 * 
	 * @return Returns the {@link DCLApplication}.
	 */
	public static ApplicationInstance getApp() {
		return app;
	}

	/**
	 * For other devices call getReceivers(String sender)!
	 * 
	 * @return Returns OWN devices! (name->address)
	 */
	public static HashMap<RSAPublicKey, String> getReceiversAndNames() {
		return receiverConfiguration.getReceivers();
	}

	public static HashMap<RSAPublicKey, String> getForeignReceiversAndNames() {
		return receiverConfiguration.getForeignReceivers();
	}

	/**
	 * Returns the symmetric key
	 * 
	 * @return
	 */
	public static byte[] getKey() {
		return keyConfiguration.getKey();
	}

	public static void addReceiver(String name, RSAPublicKey address) {
		receiverConfiguration.addReceiver(name, address);
	}

	public static void removeReceiver(RSAPublicKey address) {
		receiverConfiguration.removeReceiver(address);
	}

	/**
	 * Adds a partner.
	 * 
	 * @param name
	 *            Custom name of the partner (can be null)
	 * @param address
	 *            Address of the partner (can't be null)
	 * @param active
	 *            True if partner saves own files.<br />
	 *            False if partner just receives its files.
	 */
	public static void addPartner(String name, RSAPublicKey address, boolean active) {
		receiverConfiguration.addForeignReceiver(name, address, active);
	}

	public static void removePartner(String address) {
		receiverConfiguration.removeForeignReceiver(address);
	}

	public static void setSblitDirectory(String directory) {
		dataDirectoryConfiguration.setDataDirectory(directory);
	}

	public static void setSymmetricKey(byte[] key) {
		keyConfiguration.setKey(key);
	}

	/**
	 * Use allowChannel(Data receiverData) instead
	 * 
	 * @param receiver
	 */
	@Deprecated
	public static void addAuthenticatedReceiver(String receiver) {
		authenticatedReceivers.put(receiver, true);
	}

	@Deprecated
	public static void removeAuthenticatedReceiver(String reveiver) {
		authenticatedReceivers.remove(reveiver);
	}

	@Deprecated
	public static boolean checkAuthenticatedReceiver(String receiver) {
		return authenticatedReceivers.get(receiver);
	}

	/**
	 * Returns the receivers address-keys. For name + address-key use
	 * getReceiversAndName()
	 * 
	 * @return
	 */
	public static Data[] getReceivers() {
		Collection<RSAPublicKey> receivers = receiverConfiguration.getReceivers()
				.keySet();

		Data[] result = new Data[receivers.size()];
		int i = 0;
		for (RSAPublicKey receiver : receivers) {
			result[i] = receiver.toData();
			i++;
		}
		return result;
	}

	public static Data[] getPartners() {
		Collection<RSAPublicKey> receivers = receiverConfiguration
				.getForeignReceivers().keySet();
		Data[] result = new Data[receivers.size()];
		int i = 0;
		for (RSAPublicKey receiver : receivers) {
			result[i] = receiver.toData();
			i++;
		}
		return result;
	}

	public static Set<Data> getChannels() {
		return channels.keySet();
	}

	public static void removeChannel(Data receiverData) {
		channels.remove(receiverData);
	}

	public static ApplicationChannel getChannel(Data receiverData) {
		return channels.get(receiverData);
	}

	public static void addUnauthorizedChannel(Data receiverData,
			ApplicationChannel channel) {
		unauthorizedChannels.put(receiverData, channel);
	}

	public static void allowChannel(Data receiverData) {
		System.out.println("authenticated: " + receiverData.represent());
		channels.put(receiverData, unauthorizedChannels.get(receiverData));
		unauthorizedChannels.remove(receiverData);
	}

	public static void denyChannel(Data receiverData) {
		System.out.println("denied: " + receiverData.represent());
		unauthorizedChannels.remove(receiverData);
	}

	@Override
	public String toString() {
		String configurationDirectory = Configuration.configurationDirectory
				.getAbsolutePath();
		String privateAddressKey = getPrivateAddressKey().toString();
		String receiversAndNames = getReceiversAndNames().toString();
		String publicAddressKey = Configuration.publicAddressKey.toString();
		String sblitDirectory = getSblitDirectory().getAbsolutePath();
		String password = new String(getKey());
		String channels = getChannels().toString();

		return String
				.format("Configurationdirectory: %s\nsblit-directory: %s\nAddress: %s\nPrivate address-key: %s\nPassword: %s\nReceivers: %s\nAuthenticated receivers: %s",
						configurationDirectory, sblitDirectory,
						publicAddressKey, privateAddressKey, password,
						receiversAndNames, channels);
	}

	/**
	 * Checks, if the receiver is active or passive.
	 * 
	 * @param receiver
	 *            The reveiver to be checked.
	 * @returns True if the receiver is active. <br />
	 *          False if the receiver if passive.
	 */
	public boolean checkActivePartner(String receiver) {
		return receiverConfiguration.checkActiveReceiver(receiver);
	}

	public static void setFileStateListener(FileStateListener fileStateListener) {
		Configuration.fileStateListener = fileStateListener;
	}

	public static FileStateListener getFileStateListener() {
		return fileStateListener;
	}
}
