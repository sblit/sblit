package org.sblit.filesync;

import org.dclayer.exception.net.buf.BufException;

public interface Packet {
	public void send() throws BufException;
}
