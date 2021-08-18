package causalMulticast;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import client.Client;
import message.Message;

public class CausalMulticastAPI implements ICausalMulticastAPI {
	
	private MessageReceiver messageReceiver;
	private ArrayList<Integer> grupo;
	private  Map <String,Integer> vectorClock;
	private MulticastSocket multicastSocket ;
	public DatagramSocket unicastSocket;
	private static InetAddress multicastAdress;
	private static int MulticastPort = 6789;
	private String ClientIP;
	private InetAddress LocalHost;
	public int ClientPort;
	
	
	public CausalMulticastAPI(){	
		try {
			LocalHost = InetAddress.getLocalHost();
			ClientIP = InetAddress.getLocalHost().getHostAddress();
			multicastAdress = InetAddress.getByName("225.0.0.0");
			multicastSocket = new MulticastSocket(MulticastPort);
			getClientAddress();
			discoverService();
			messageReceiver = new MessageReceiver(this);
			messageReceiver.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


//	private void receiveMessages() {
//		try {
//			unicastSocket = new DatagramSocket(ClientPort);
//			while(true) {
//				DatagramPacket receivePacket = new DatagramPacket(mensagemRecebida, mensagemRecebida.length);
//				unicastSocket.receive(receivePacket);
//				byte[] data = receivePacket.getData();
//		        ByteArrayInputStream byteArrayinputStream = new ByteArrayInputStream(data);
//		        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayinputStream);
//		        Message message = (Message) objectInputStream.readObject();
//		        System.out.println(message.msg);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}


	private void getClientAddress() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Digite a porta referente ao cliente: <porta>");
		String IPePORTA = reader.readLine();
		//ClientIP = IPePORTA.split("\\s+")[0];
		ClientPort = Integer.parseInt(IPePORTA) ;
	}


	@SuppressWarnings("deprecation")
	private void discoverService() throws UnknownHostException, SocketException, IOException, InterruptedException {
		System.out.println("Descobrindo grupo...");
		grupo = new ArrayList<Integer>();
		multicastSocket.joinGroup(multicastAdress);
		byte[] msgBytes = String.format("%s %d",ClientIP,ClientPort).getBytes(StandardCharsets.UTF_8);
		DatagramPacket ping = new DatagramPacket(msgBytes, msgBytes.length,multicastAdress,MulticastPort);
		multicastSocket.send(ping);
		int recebidos = 0;
		while(recebidos<2) {
			byte[] buf = new byte[20];
			DatagramPacket recv = new DatagramPacket(buf, buf.length);
			multicastSocket.receive(recv);
			String received = new String(buf);
			String iprecv = received.split("\\s+")[0];
			int portrecv = Integer.parseInt(received.split("\\s+")[1].trim());
			if(!grupo.contains(portrecv)  && portrecv != ClientPort) {
				grupo.add(portrecv);
				recebidos++;
			}
			multicastSocket.send(ping);
			System.out.println("IPs descobertos até agr:");
			grupo.forEach(m -> System.out.println(m.toString()));
			Thread.sleep(1000);
		}
	}
	 

	public void refreshGroup() {
		System.out.println("refresh");
	}
	
	@Override
	public void mcsend(String msg, Client client) {
		System.out.println("mandando:" + msg);
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
				client.deliver(msg);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
