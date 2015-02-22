package net.sblit.filesync;

import net.sblit.Sblit;
import net.sblit.configuration.Configuration;
import net.sblit.converter.Converter;

import org.dclayer.application.NetworkEndpointActionListener;
import org.dclayer.application.networktypeslotmap.NetworkEndpointSlot;
import org.dclayer.crypto.key.Key;
import org.dclayer.net.Data;
import org.dclayer.net.llacache.LLA;

/**
 * 
 * @author Nikola
 * 
 */

public class Receiver implements NetworkEndpointActionListener {

	public Receiver() {

	}

	@Override
	public void onJoin(NetworkEndpointSlot networkEndpointSlot, Data ownAddressData) {
		System.out.println(String.format("joined: %s, local address: %s", networkEndpointSlot,
				ownAddressData));
		for (Data partner : Configuration.getReceivers()) {
			//System.out.println(new String(partner.getData()));
			Configuration.getApp().requestApplicationChannel(networkEndpointSlot,
					Sblit.APPLICATION_IDENTIFIER, Converter.dataToKey(partner),
					new ApplicationChannelActionListener(this));
		}

	}

	@Override
	public void onReceive(NetworkEndpointSlot networkEndpointSlot, Data data, Data sourceAddressData) {
		// TODO auto-generated method stuff
	}

	@Override
	public ApplicationChannelActionListener onApplicationChannelRequest(
			NetworkEndpointSlot networkEndpointSlot, Key remotePublicKey, String actionIdentifier,
			LLA remoteLLA) {
		if (Configuration.getReceiversAndNames().containsKey(remotePublicKey.toData())) {
			return new ApplicationChannelActionListener(this);
		} else {
			return null;
		}
	}
}
