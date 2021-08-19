package client;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import causalMulticast.*;


/** Classe que representa um cliente usando o middleware de causal multicast
 * @author Pedro H. V. Rossato e Gabriel Fuhr
 *
 */
public class Client implements ICausalMulticast{
	
	/** API CausalMulticast para o cliente usar
	 * 
	 */
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
	
	/** Envia uma mensagem por meio da api causal multicast
	 * @throws IOException
	 */
	public void sendMessage() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String mensagem = reader.readLine();
		causalMulticastAPI.mcsend(mensagem, this);
	}
	
	/** Método que entrega uma mensagem ao cliente através do callback
	 * @param msg Mensagem a ser entregue ao cliente
	 */
	@Override
	public void deliver(String msg) {
		System.out.println("Recebi a mensagem: "+ msg);
	}

}