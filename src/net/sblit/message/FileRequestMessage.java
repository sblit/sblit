package net.sblit.message;

import org.dclayer.crypto.hash.HashAlgorithm;
import org.dclayer.net.Data;
import org.dclayer.net.component.ArrayPacketComponent;
import org.dclayer.net.component.StringComponent;
import org.dclayer.net.packetcomponent.Child;
import org.dclayer.net.packetcomponent.ParentPacketComponent;

public class FileRequestMessage extends ParentPacketComponent {
	
	@Child(index = 0) public StringComponent path;
	@Child(index = 1, create = false) public ArrayPacketComponent<Data> hashes = new ArrayPacketComponent<Data>() {

		@Override
		protected Data newElementPacketComponent() {
			return new Data(HashAlgorithm.SHA1.getDigestNumBytes());
		}
	};

}
