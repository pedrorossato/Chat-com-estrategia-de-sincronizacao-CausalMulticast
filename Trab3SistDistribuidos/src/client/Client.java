package client;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import causalMulticast.*;

public class Client implements ICausalMulticast{

	private ICausalMulticastAPI causalMulticastAPI;
	
	public static void main(String[] args) {
		try {
			Client client = new Client();
			client.sendMessage("teste");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	
	public Client() {
		causalMulticastAPI = new CausalMulticastAPI();
	}
	public void sendMessage(String msg) {
		causalMulticastAPI.mcsend(msg, this);
	}
	
	@Override
	public void deliver(String msg) {
		System.out.println("Recebi minha própria mensagem enviada: "+ msg);
	}

}