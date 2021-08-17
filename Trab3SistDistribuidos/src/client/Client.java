package client;

import causalMulticast.*;

public class Client implements ICausalMulticast{

	private ICausalMulticastAPI causalMulticastAPI;
	
	public static void main(String[] args) {
		Client client = new Client();
	}
	
	public Client() {
		causalMulticastAPI = new CausalMulticastAPI();
	}
	public void sendMessage(String msg) {
		causalMulticastAPI.mcsend(msg, this);
	}
	
	@Override
	public void deliver(String msg) {
		System.out.println(msg);
	}

}