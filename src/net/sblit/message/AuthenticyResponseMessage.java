package net.sblit.message;

import org.dclayer.net.component.DataComponent;
import org.dclayer.net.packetcomponent.Child;
import org.dclayer.net.packetcomponent.ParentPacketComponent;

public class AuthenticyResponseMessage extends ParentPacketComponent {
	
	@Child(index = 0) DataComponent dataComponent;

}
