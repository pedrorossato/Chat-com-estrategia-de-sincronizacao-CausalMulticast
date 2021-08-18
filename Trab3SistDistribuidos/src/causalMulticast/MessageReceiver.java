package causalMulticast;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import message.Message;

public class MessageReceiver extends Thread {
	private CausalMulticastAPI causalMulticastAPI;
	private Message receivedMessage;
	byte[] receivedMessageByteArray = new byte[1000];
	
	public MessageReceiver(CausalMulticastAPI causalMulticastAPI) {
		this.causalMulticastAPI = causalMulticastAPI;
	}
	
	@Override
	public void run() {
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
		        System.out.println("Msg recebida de ("+ receivedMessage.ClientPort+") : " + receivedMessage.msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
