package causalMulticast;

import java.net.*;
import java.util.ArrayList;

import client.Client;

public class CausalMulticastAPI implements ICausalMulticastAPI {
	
	private ArrayList<String> grupo;
	
	
	public CausalMulticastAPI(){		
		InetAddress mcastaddr;
		try {
			mcastaddr = InetAddress.getByName("228.5.6.7");
			InetSocketAddress group = new InetSocketAddress(mcastaddr, 6789);
			NetworkInterface netIf = NetworkInterface.getByIndex(1);
			MulticastSocket mcs = new MulticastSocket(6789);
			mcs.joinGroup(group, netIf);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	 

	public void refreshGroup() {
		System.out.println("refresh");
	}
	
	@Override
	public void mcsend(String msg, Client client) {
		// TODO Auto-generated method stub
		
		client.deliver(msg);
	}
	
}
