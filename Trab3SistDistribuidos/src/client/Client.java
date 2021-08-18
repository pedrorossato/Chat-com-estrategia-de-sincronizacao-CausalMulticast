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
			while(true) {
				client.sendMessage();				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	
	public Client() {
		causalMulticastAPI = new CausalMulticastAPI();
	}
	
	public void sendMessage() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Digite uma mensagem para enviar:");
		String mensagem = reader.readLine();
		causalMulticastAPI.mcsend(mensagem, this);
	}
	
	@Override
	public void deliver(String msg) {
		System.out.println("Recebi minha própria mensagem enviada: "+ msg);
	}

}