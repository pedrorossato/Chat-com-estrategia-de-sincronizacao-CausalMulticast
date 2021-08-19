package causalMulticast;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import message.Message;


public class MessageReceiver extends Thread {
	private CausalMulticastAPI causalMulticastAPI;
	public  Map<Integer,Integer> vectorClock;
	private Message receivedMessage;
	byte[] receivedMessageByteArray = new byte[1000];
	public ArrayList<Message> delayedMessages;
	
	public MessageReceiver(CausalMulticastAPI causalMulticastAPI) {
		this.causalMulticastAPI = causalMulticastAPI;
		this.vectorClock = new HashMap<Integer,Integer>();
		this.delayedMessages = new ArrayList<Message>();
	}
	
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

	/** Entrega a mensagem recebida
	 * 
	 */
	private void deliverMessage() {
		vectorClock.put(receivedMessage.ClientPort, vectorClock.get(receivedMessage.ClientPort)+1);
		System.out.println("msg : " + receivedMessage.msg);
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
			for(Message mensagemAtrasada : delayedMessages) {
				Boolean entregarAtrasada = true;
				for (Map.Entry<Integer, Integer> entry : mensagemAtrasada.vectorClock.entrySet()) {
					int key = entry.getKey();
					int value = entry.getValue();
					if(value > vectorClock.get(key)) {
						entregarAtrasada = false;
						break;
					}
				}
				if(entregarAtrasada) {
					System.out.println("msg atrasada sendo entregue : " + mensagemAtrasada.msg);
					delayedMessages.remove(mensagemAtrasada);
				}			
			}
		}
	}
}
