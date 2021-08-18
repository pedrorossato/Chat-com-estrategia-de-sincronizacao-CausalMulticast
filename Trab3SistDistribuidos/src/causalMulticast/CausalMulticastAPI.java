package causalMulticast;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import client.Client;
import message.Message;

public class CausalMulticastAPI implements ICausalMulticastAPI {
	
	private MessageReceiver messageReceiver;
	private ArrayList<Integer> grupo;
	public  Map<Integer,Integer> vectorClock;
	private MulticastSocket multicastSocket ;
	public DatagramSocket unicastSocket;
	private static InetAddress multicastAdress;
	private static int MulticastPort = 6789;
	private InetAddress LocalHost;
	public int ClientPort;
	public ArrayList<Message> delayedMessages;
	
	
	public CausalMulticastAPI(){	
		try {
			LocalHost = InetAddress.getLocalHost();
			multicastAdress = InetAddress.getByName("225.0.0.0");
			multicastSocket = new MulticastSocket(MulticastPort);
			vectorClock = new HashMap<Integer, Integer>();
			delayedMessages = new ArrayList<Message>();
			getClientAddress();
			discoverService();
			messageReceiver = new MessageReceiver(this);
			messageReceiver.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void getClientAddress() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Digite a porta referente ao cliente: <porta>");
		String IPePORTA = reader.readLine();
		ClientPort = Integer.parseInt(IPePORTA) ;
		vectorClock.put(ClientPort, 0);
	}
	

	@SuppressWarnings("deprecation")
	private void discoverService() throws UnknownHostException, SocketException, IOException, InterruptedException {
		System.out.println("Descobrindo grupo...");
		grupo = new ArrayList<Integer>();
		multicastSocket.joinGroup(multicastAdress);
		byte[] msgBytes = String.format("%d",ClientPort).getBytes(StandardCharsets.UTF_8);
		DatagramPacket ping = new DatagramPacket(msgBytes, msgBytes.length,multicastAdress,MulticastPort);
		multicastSocket.send(ping);
		int recebidos = 0;
		while(recebidos<2) {
			byte[] buf = new byte[20];
			DatagramPacket recv = new DatagramPacket(buf, buf.length);
			multicastSocket.receive(recv);
			String received = new String(buf);
			int portrecv = Integer.parseInt(received.trim());
			if(!grupo.contains(portrecv)  && portrecv != ClientPort) {
				grupo.add(portrecv);
				vectorClock.put(portrecv, 0);
				recebidos++;
			}
			multicastSocket.send(ping);
			System.out.println("Outros clientes descobertos até agora:");
			grupo.forEach(m -> System.out.println(m.toString()));
			Thread.sleep(1000);
		}
	}
	 
	public void receiveMessage(Message receivedMessage) {
		for (Map.Entry<Integer, Integer> entry : vectorClock.entrySet()) {
		    int key = entry.getKey();
		    int value = entry.getValue();
		    if(value < receivedMessage.vectorClock.get(key)) {
		    	delayedMessages.add(receivedMessage);
		    }
		    //TODO: Entregar mensagem atrasada depois que chegar a nova
		}
	}
	
	@Override
	public void mcsend(String msg, Client client) {
		System.out.println("mandando:" + msg);
		client.deliver(msg);
		try {
			Message message;
			for (Integer membro: grupo) {
				message = new Message(ClientPort,msg,vectorClock);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(message);
				byte[] data = baos.toByteArray();
				DatagramPacket sendPacket = new DatagramPacket(data, data.length,LocalHost, membro);
				unicastSocket.send(sendPacket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
