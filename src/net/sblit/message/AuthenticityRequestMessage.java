package net.sblit.message;

import org.dclayer.net.component.DataComponent;
import org.dclayer.net.packetcomponent.Child;
import org.dclayer.net.packetcomponent.ParentPacketComponent;

public class AuthenticityRequestMessage extends ParentPacketComponent {
	
	@Child(index = 0) public DataComponent dataComponent;
	
}
