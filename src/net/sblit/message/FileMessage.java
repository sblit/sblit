package net.sblit.message;

import org.dclayer.net.component.ArrayPacketComponent;
import org.dclayer.net.component.DataComponent;
import org.dclayer.net.component.StringComponent;
import org.dclayer.net.packetcomponent.Child;
import org.dclayer.net.packetcomponent.ParentPacketComponent;

public class FileMessage extends ParentPacketComponent {
	
	@Child(index = 0) public ArrayPacketComponent<DataComponent> synchronizedDevices = new ArrayPacketComponent<DataComponent>() {

		@Override
		protected DataComponent newElementPacketComponent() {
			return new DataComponent();
		}
	};
	@Child(index = 1) public DataComponent fileContent = new DataComponent();
	@Child(index = 2) public StringComponent filePath = new StringComponent();
	@Child(index = 3) public DataComponent hash = new DataComponent();
	
}
