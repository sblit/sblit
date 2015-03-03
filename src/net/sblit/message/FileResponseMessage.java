package net.sblit.message;

import org.dclayer.net.component.DataComponent;
import org.dclayer.net.component.StringComponent;
import org.dclayer.net.packetcomponent.Child;
import org.dclayer.net.packetcomponent.ParentPacketComponent;

public class FileResponseMessage extends ParentPacketComponent{

	@Child(index = 0) public StringComponent path;
	@Child(index = 1) public DataComponent need;
	@Child(index = 2) public DataComponent newHash;
	
}
