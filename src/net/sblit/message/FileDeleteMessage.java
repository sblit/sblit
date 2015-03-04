package net.sblit.message;

import org.dclayer.net.component.StringComponent;
import org.dclayer.net.packetcomponent.Child;
import org.dclayer.net.packetcomponent.ParentPacketComponent;

public class FileDeleteMessage extends ParentPacketComponent {
	
	@Child(index = 0) public StringComponent filePath;

}
