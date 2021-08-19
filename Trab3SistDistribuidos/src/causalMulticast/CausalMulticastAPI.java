package causalMulticast;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import client.Client;
import message.Message;

public class CausalMulticastAPI implements ICausalMulticastAPI {
	
	private MessageReceiver messageReceiver;
	private ArrayList<Integer> grupo;
	private MulticastSocket multicastSocket ;
	public DatagramSocket unicastSocket;
	private static InetAddress multicastAdress;
	private static int MulticastPort = 6789;
	private InetAddress LocalHost;
	public int ClientPort;
	
	
	public CausalMulticastAPI(){	
		try {
			LocalHost = InetAddress.getLocalHost();
			multicastAdress = InetAddress.getByName("225.0.0.0");
			multicastSocket = new MulticastSocket(MulticastPort);
			messageReceiver = new MessageReceiver(this);
			getClientAddress();
			discoverService();
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
		messageReceiver.vectorClock.put(ClientPort, 0);
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
				messageReceiver.vectorClock.put(portrecv, 0);
				recebidos++;
			}
			multicastSocket.send(ping);
			System.out.println("Outros clientes descobertos até agora:");
			grupo.forEach(m -> System.out.println(m.toString()));
			Thread.sleep(1000);
		}
	}
	
	
	@Override
	public void mcsend(String msg, Client client) {
		try {
			System.out.println("Deseja enviar a mensagem " + msg + " a todos? (s/n)");
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String enviarParaTodos = reader.readLine();
			client.deliver(msg);
			Message message;
			for (Integer membro: grupo) {
				String enviar = null;
				if(enviarParaTodos.equals("n")) {
					System.out.println("Deseja enviar a mensagem " + msg + " para o membro "+ membro+ " ? (s/n)");
					enviar = reader.readLine();
				}
				if(enviarParaTodos.equals("s")|| enviar.equals("s") || enviar == null) {
					message = new Message(ClientPort,msg,messageReceiver.vectorClock);
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
					objectOutputStream.writeObject(message);
					byte[] data = byteArrayOutputStream.toByteArray();
					DatagramPacket sendPacket = new DatagramPacket(data, data.length,LocalHost, membro);
					unicastSocket.send(sendPacket);
				}
			}
			messageReceiver.vectorClock.put(ClientPort, messageReceiver.vectorClock.get(ClientPort)+1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
