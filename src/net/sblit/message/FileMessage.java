package net.sblit.message;

import org.dclayer.crypto.hash.HashAlgorithm;
import org.dclayer.net.Data;
import org.dclayer.net.component.ArrayPacketComponent;
import org.dclayer.net.component.DataComponent;
import org.dclayer.net.component.StringComponent;
import org.dclayer.net.packetcomponent.Child;
import org.dclayer.net.packetcomponent.ParentPacketComponent;

public class FileMessage extends ParentPacketComponent {
	
	@Child(index = 0, create = false) public ArrayPacketComponent<DataComponent> synchronizedDevices = new ArrayPacketComponent<DataComponent>() {

		@Override
		protected DataComponent newElementPacketComponent() {
			return new DataComponent();
		}
	};
	@Child(index = 1) public DataComponent fileContent;
	@Child(index = 2) public StringComponent filePath;
	@Child(index = 3, create = false) public ArrayPacketComponent<Data> hashes = new ArrayPacketComponent<Data>() {

		@Override
		protected Data newElementPacketComponent() {
			return new Data(HashAlgorithm.SHA1.getDigestNumBytes());
		}
	};
	
}
