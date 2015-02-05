package net.sblit.message;

import org.dclayer.net.packetcomponent.Child;
import org.dclayer.net.packetcomponent.SwitchPacketComponent;

public class SblitMessage extends SwitchPacketComponent<CallbackMessage> {
	
	public static final int AUTHENTICITY_REQUEST = 0;
	public static final int AUTHENTICITY_RESPONSE = 1;
	
	public static final int FILE_REQUEST = 2;
	public static final int FILE_RESPONSE = 3;

	public static final int CONFLICT_REQUEST = 4;
	public static final int CONFLICT_RESPONSE = 5;
	
	//

	public SblitMessage() {
		super(CallbackMessage.class);
	}
	
	@Child(index = AUTHENTICITY_REQUEST) public AuthenticityRequestMessage authenticityRequest;
	
	// TODO add other messages as follows:
	// @Child(index = REPLACE_ME_WITH_TYPE_ID) public WhatEverMessage whatEver;
	// Create WhatEverMessage class like the AuthenticityRequestMessage class
	
}
