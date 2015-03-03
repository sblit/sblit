package net.sblit.message;

import org.dclayer.net.packetcomponent.Child;
import org.dclayer.net.packetcomponent.SwitchPacketComponent;

public class SblitMessage extends SwitchPacketComponent<CallbackMessage> {
	
	public static final int AUTHENTICITY_REQUEST = 0;
	public static final int AUTHENTICITY_RESPONSE = 1;
	
	public static final int FILE_REQUEST = 2;
	public static final int FILE_RESPONSE = 3;
	public static final int FILE_MESSAGE = 4;
	public static final int DELETE_MESSAGE = 5;

	//

	public SblitMessage(Object onReceiveObject) {
		super(onReceiveObject);
	}
	
	public SblitMessage(){
		super();
	}
	
	@Child(index = AUTHENTICITY_REQUEST) public AuthenticityRequestMessage authenticityRequest = new AuthenticityRequestMessage();
	@Child(index = AUTHENTICITY_RESPONSE) public AuthenticityResponseMessage authenticityResponse = new AuthenticityResponseMessage();
	
	@Child(index = FILE_REQUEST) public FileRequestMessage fileRequest = new FileRequestMessage();
	@Child(index = FILE_RESPONSE) public FileResponseMessage fileResponse = new FileResponseMessage();
	@Child(index = FILE_MESSAGE) public FileMessage fileMessage = new FileMessage();
	@Child(index = DELETE_MESSAGE) public FileDeleteMessage deleteMessage = new FileDeleteMessage();
	
	
	// TODO add other messages as follows:
	// @Child(index = REPLACE_ME_WITH_TYPE_ID) public WhatEverMessage whatEver;
	// Create WhatEverMessage class like the AuthenticityRequestMessage class
	
}
