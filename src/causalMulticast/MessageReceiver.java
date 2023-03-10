package causalMulticast;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import message.Message;


/** Class que representa o receptor de mensagens unicast de outros middlewares
 * @author Pedro H. V. Rossato e Gabriel Fuhr
 *
 */
public class MessageReceiver extends Thread {
	
	/** Middleware associado ao receptor de mensagens
	 * 
	 */
	private CausalMulticastAPI causalMulticastAPI;
	/** Vetore de relógios lógicos associado ao receptor de mensagens
	 * 
	 */
	public  Map<Integer,Integer> vectorClock;
	/** Mensagem recebida (Objeto)
	 * 
	 */
	private Message receivedMessage;
	/** Mensagem recebida (byte array)
	 * 
	 */
	byte[] receivedMessageByteArray = new byte[1000];
	/** Mensagens que serão entregues atrasadas pois há dependencia de uma mensagem que ainda não chegou
	 * 
	 */
	public ArrayList<Message> delayedMessages;
	
	public MessageReceiver(CausalMulticastAPI causalMulticastAPI) {
		this.causalMulticastAPI = causalMulticastAPI;
		this.vectorClock = new HashMap<Integer,Integer>();
		this.delayedMessages = new ArrayList<Message>();
	}
	
	/** Método que recebe mensagens ciclicamente
	 *	
	 */
	@Override
	public void run() {
		synchronized (this) {
			System.out.println("Recebendo mensagens...");
			try {
				causalMulticastAPI.unicastSocket = new DatagramSocket(causalMulticastAPI.ClientPort);
				while(true) {
					DatagramPacket receivePacket = new DatagramPacket(receivedMessageByteArray, receivedMessageByteArray.length);
					causalMulticastAPI.unicastSocket.receive(receivePacket);
					byte[] data = receivePacket.getData();
					ByteArrayInputStream byteArrayinputStream = new ByteArrayInputStream(data);
					ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayinputStream);
					receivedMessage = (Message) objectInputStream.readObject();
					printMessageAndReceiverClockVectors();
					Boolean entregar = addMessageToDelayedMessagesIfHasDependency();
					deliverDelayedMessages();			
					if(entregar) {
						deliverMessage();
					}
					deliverDelayedMessages();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/** Método que checar se o vetor de relogios está adiantado em relação ao do middleware
	 * @return True se a mensagem deve ser entregue, ou seja, não depende de outra mensagem anterior
	 */
	private Boolean addMessageToDelayedMessagesIfHasDependency() {
		Boolean entregar = true;
		for (Map.Entry<Integer, Integer> entry : vectorClock.entrySet()) {
			int key = entry.getKey();
			int value = entry.getValue();
			if(value < receivedMessage.vectorClock.get(key)) {
				delayedMessages.add(receivedMessage);
				System.out.println("A mensagem "+receivedMessage.msg+" está esperando para ser entregue");
				entregar = false;
				break;
			}
		}
		return entregar;
	}

	/** Entrega a mensagem recebida
	 * 
	 */
	private void deliverMessage() {
		vectorClock.put(receivedMessage.ClientPort, vectorClock.get(receivedMessage.ClientPort)+1);
		System.out.println("Recebi a mensagem: " + receivedMessage.msg);
	}

	/** Método para printar no console os clock vectors do receiver do middleware e da mensagem recebida
	 * 
	 */
	private void printMessageAndReceiverClockVectors() {
		System.out.println("Vetor atual:");
		for (Map.Entry<Integer, Integer> entry : vectorClock.entrySet()) {
			int key = entry.getKey();
			int value = entry.getValue();
			System.out.println("["+key+"] = ["+value+"]");
		}
		System.out.println("Vetor da mensagem:");
		for (Map.Entry<Integer, Integer> entry : receivedMessage.vectorClock.entrySet()) {
			int key = entry.getKey();
			int value = entry.getValue();
			System.out.println("["+key+"] = ["+value+"]");
		}
	}

	/** Método para entregar (se possível) uma mensagem que esteja atrasada
	 * 
	 */
	private void deliverDelayedMessages() {
		synchronized (this) {
			Boolean entregar = true;
			ArrayList<Message> mensagensASerEntregue = new ArrayList<Message>();
			for(Message mensagemAtrasada : delayedMessages) {
				for (Map.Entry<Integer, Integer> entry : mensagemAtrasada.vectorClock.entrySet()) {
					int key = entry.getKey();
					int value = entry.getValue();
					if(value > vectorClock.get(key)) {
						entregar = false;
						break;
					}
				}
				if(entregar) {
					mensagensASerEntregue.add(mensagemAtrasada);
				}
			}
			for(Message mensagemASerEntregue : mensagensASerEntregue) {
				System.out.println("Recebi a mensagem: "+ mensagemASerEntregue.msg);
				delayedMessages.remove(mensagemASerEntregue);
			}
			mensagensASerEntregue.clear();
		}
	}
}
