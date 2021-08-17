package causalMulticast;

import java.util.ArrayList;

import client.Client;

public class CausalMulticastAPI implements ICausalMulticastAPI {
	
	private ArrayList<String> grupo;
	
	 public CausalMulticastAPI() {
		
	}
	 
	@Override
	public void mcsend(String msg, Client client) {
		// TODO Auto-generated method stub
		
		client.deliver(msg);
	}
	
}
