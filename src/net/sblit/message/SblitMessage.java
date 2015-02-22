package net.sblit.message;

import org.dclayer.net.packetcomponent.Child;
import org.dclayer.net.packetcomponent.SwitchPacketComponent;

public class SblitMessage extends SwitchPacketComponent<CallbackMessage> {
	
	public static final int AUTHENTICITY_REQUEST = 0;
	public static final int AUTHENTICITY_RESPONSE = 1;
	
	public static final int FILE_REQUEST = 2;
	public static final int FILE_RESPONSE = 3;
	public static final int FILE_MESSAGE = 4;

	//

	public SblitMessage() {
		super();
	}
	
	@Child(index = AUTHENTICITY_REQUEST) public AuthenticityRequestMessage authenticityRequest;
	@Child(index = AUTHENTICITY_RESPONSE) public AuthenticityResponseMessage authenticityResponse;
	
	@Child(index = FILE_REQUEST) public FileRequestMessage fileRequest;
	@Child(index = FILE_RESPONSE) public FileResponseMessage fileResponse;
	@Child(index = FILE_MESSAGE) public FileMessage fileMessage;
	
	// TODO add other messages as follows:
	// @Child(index = REPLACE_ME_WITH_TYPE_ID) public WhatEverMessage whatEver;
	// Create WhatEverMessage class like the AuthenticityRequestMessage class
	
}
