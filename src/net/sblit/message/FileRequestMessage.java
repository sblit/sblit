package net.sblit.message;

import org.dclayer.net.component.ArrayPacketComponent;
import org.dclayer.net.component.DataComponent;
import org.dclayer.net.component.StringComponent;
import org.dclayer.net.packetcomponent.Child;
import org.dclayer.net.packetcomponent.ParentPacketComponent;

public class FileRequestMessage extends ParentPacketComponent {
	
	@Child(index = 0) public StringComponent path = new StringComponent();
	@Child(index = 1) public ArrayPacketComponent<DataComponent> hashes = new ArrayPacketComponent<DataComponent>() {

		@Override
		protected DataComponent newElementPacketComponent() {
			return new DataComponent();
		}
	};

}
