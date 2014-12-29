package org.sblit.filesync;

import java.io.IOException;

import org.dclayer.exception.net.buf.BufException;

public interface Packet {
	public void send() throws BufException, IOException;
}
