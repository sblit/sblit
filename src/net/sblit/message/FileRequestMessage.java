package net.sblit.message;

import org.dclayer.net.component.DataComponent;
import org.dclayer.net.packetcomponent.Child;
import org.dclayer.net.packetcomponent.ParentPacketComponent;

public class FileRequestMessage extends ParentPacketComponent {
	
	@Child(index = 0) DataComponent oldHash;
	@Child(index = 1) DataComponent newHash;

}
