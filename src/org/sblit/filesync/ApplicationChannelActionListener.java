package org.sblit.filesync;

import java.io.IOException;
import java.io.InputStream;

import org.dclayer.application.applicationchannel.ApplicationChannel;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.Data;
import org.dclayer.net.buf.StreamByteBuf;
import org.dclayer.net.component.DataComponent;
import org.sblit.configuration.Configuration;
import org.sblit.crypto.SymmetricEncryption;
import org.sblit.fileProcessing.FileProcessor;
import org.sblit.filesync.requests.AuthenticyRequest;

public class ApplicationChannelActionListener implements
		org.dclayer.application.applicationchannel.ApplicationChannelActionListener {

	Receiver receiver;
	protected ApplicationChannel applicationChannel;

	public ApplicationChannelActionListener(Receiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void onApplicationChannelDisconnected(ApplicationChannel applicationChannel) {
		System.out.println("Disconnected: " + Configuration.getReceiversAndNames().get(applicationChannel.getRemotePublicKey()));
//		try {
//			applicationChannel.getInputStream().close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		try {
//			applicationChannel.getOutputStream().close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		Configuration.removeChannel(applicationChannel.getRemotePublicKey().toData());

	}

	@Override
	public void onApplicationChannelConnected(ApplicationChannel applicationChannel) {
		
		this.applicationChannel = applicationChannel;
		Runnable r = new Runnable() {

			private synchronized void read(StreamByteBuf streamByteBuf, DataComponent dataComponent) throws ParseException, BufException{
				dataComponent.read(streamByteBuf);
			}
			
			@Override
			public void run() {
				DataComponent dataComponent = new DataComponent();

				InputStream inputStream = ApplicationChannelActionListener.this.applicationChannel
						.getInputStream();
				StreamByteBuf streamByteBuf = new StreamByteBuf(inputStream);
				byte[] buf = new byte[0];
				for (;;) {
					byte[] received = new byte[0];
					try {
						read(streamByteBuf, dataComponent);
						System.out.println(String.format("received: %s\nData component: %s", new String(dataComponent
								.getData().getData()),dataComponent.represent()));
						try {
							received = dataComponent.getData().getData();
							if(buf.length > 0){
								byte[] temp = new byte[buf.length + received.length];
								for(int i = 0; i < buf.length; i++){
									temp[i] = buf[i];
								}
								for(int i = 0; i < received.length;i++) {
									temp[i+buf.length] = received[i];
								}
								received = temp;
							}
							Data sourceAddressData = ApplicationChannelActionListener.this.applicationChannel
									.getRemotePublicKey().toData();
							
							if (new String(received).startsWith(PacketStarts.AUTHENTICY_REQUEST
									.toString())) {
								receiver.handleAuthenticyRequest(received,
										ApplicationChannelActionListener.this.applicationChannel);
							} else if (new String(received)
									.startsWith(PacketStarts.AUTHENTICY_RESPONSE.toString())) {
								receiver.handleAuthenticyResponse(received, sourceAddressData);
							} else {
								System.out.println("lenght: " + received.length);
								received = new SymmetricEncryption(Configuration.getKey())
										.decrypt(received);
								String s = new String(received);
								if (Configuration.getChannels().contains(sourceAddressData)) {
									if (s.startsWith(PacketStarts.CONFLICT_REQUEST.toString())) {
										receiver.handleConflictRequest(received, sourceAddressData);
									} else if (s.startsWith(PacketStarts.CONFLICT_RESPONSE
											.toString())) {
										receiver.handleConflictResponse(received);
									} else if (s.startsWith(PacketStarts.FILE_REQUEST.toString())) {
										receiver.handleFileRequest(received, sourceAddressData);
									} else if (s.startsWith(PacketStarts.FILE_RESPONSE.toString())) {
										receiver.handleFileResponse(received, sourceAddressData);
									} else {
										// FileProcessor fileProcessor =
										new FileProcessor(received);
										// byte[] checksum = receive();
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (BufException e) {
						buf = received;
					}

				}
			}

		};
		new Thread(r).start();
		try {
			new AuthenticyRequest(applicationChannel).send();
		} catch (BufException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	}
	// public void onApplicationChannelConnected(ApplicationChannel
	// applicationChannel) {
	// Configuration.addUnauthorizedChannel(applicationChannel.getRemotePublicKey().toData(),
	// applicationChannel);
	// System.out.println("Connected with \"" +
	// applicationChannel.getRemotePublicKey() + "\"");
	// try {
	// new AuthenticyRequest(applicationChannel).send();
	// } catch (BufException e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// } catch (IOException e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// }
	//
	// BufferedReader reader = new BufferedReader(new
	// InputStreamReader(applicationChannel.getInputStream()));
	// InputStream in = applicationChannel.getInputStream();
	// // StreamByteBuf buffer = new StreamByteBuf(in);
	// // try {
	// // DataComponent d = new DataComponent();
	// // d.read(buffer);
	// // System.out.println("Data: \"" + new String(d.getData().getData()) +
	// "\"");
	// // } catch (ParseException e2) {
	// // // TODO Auto-generated catch block
	// // e2.printStackTrace();
	// // } catch (BufException e2) {
	// // // TODO Auto-generated catch block
	// // e2.printStackTrace();
	// // }
	// int i = 0;
	// // try {
	// // while ((i = in.available()) < 1) {
	// // System.out.println(i);
	// //
	// // }
	// // } catch (IOException e1) {
	// // // TODO Auto-generated catch block
	// // e1.printStackTrace();
	// // }
	// System.out.println(i);
	// while (true) {
	// try {
	// // TODO handle foreign files
	// //int i;
	// System.out.println("lesen...");
	// byte[] received ;
	// String s = reader.readLine();//new String(received);
	// System.out.println("Received: \"" + s + "\"");
	// Data sourceAddressData =
	// applicationChannel.getRemotePublicKey().toData();
	// if (s.startsWith(PacketStarts.AUTHENTICY_REQUEST.toString())) {
	// receiver.handleAuthenticyRequest(s.getBytes(), sourceAddressData);
	// } else if (s.startsWith(PacketStarts.AUTHENTICY_RESPONSE.toString())) {
	// receiver.handleAuthenticyResponse(s.getBytes(), sourceAddressData);
	// }
	// received = new
	// SymmetricEncryption(Configuration.getKey()).decrypt(s.getBytes());
	// if (Configuration.getChannels().contains(sourceAddressData)) {
	// if (s.startsWith(PacketStarts.CONFLICT_REQUEST.toString())) {
	// receiver.handleConflictRequest(received, sourceAddressData);
	// } else if (s.startsWith(PacketStarts.CONFLICT_RESPONSE.toString())) {
	// receiver.handleConflictResponse(received);
	// } else if (s.startsWith(PacketStarts.FILE_REQUEST.toString())) {
	// receiver.handleFileRequest(received, sourceAddressData);
	// } else if (s.startsWith(PacketStarts.FILE_RESPONSE.toString())) {
	// receiver.handleFileResponse(received, sourceAddressData);
	// } else {
	// // FileProcessor fileProcessor =
	// new FileProcessor(received);
	// // byte[] checksum = receive();
	// }
	// }
	// } catch (BufException | DataLengthException e) {
	// e.printStackTrace();
	// System.exit(0);
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (InvalidCipherCryptoException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// }

}
