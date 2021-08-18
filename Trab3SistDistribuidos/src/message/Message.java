package message;

import java.io.Serializable;
import java.util.Map;

public class Message implements Serializable{
	private static final long serialVersionUID = 1L;
	public String msg;
	public Map <String,Integer> vectorClock;
	public int ClientPort;
	
	public Message(int ClientPort, String message, Map <String,Integer> vectorClock) {
		this.ClientPort = ClientPort;
		this.msg = message;
		this.vectorClock = vectorClock;
	}
}
