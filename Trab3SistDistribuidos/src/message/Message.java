package message;

import java.io.Serializable;
import java.util.Map;

/** Classe que representa uma mensagem serializavel
 * @author Pedro H. V. Rossato e Gabriel Fuhr
 *
 */
public class Message implements Serializable{
	private static final long serialVersionUID = 1L;
	/** Mensagem 
	 * 
	 */
	public String msg;
	/** Vetor de relógios lógicos
	 * 
	 */
	public Map <Integer,Integer> vectorClock;
	/** Porta do cliente que enviou a mensagem
	 * 
	 */
	public int ClientPort;
	
	public Message(int ClientPort, String message, Map <Integer,Integer> vectorClock) {
		this.ClientPort = ClientPort;
		this.msg = message;
		this.vectorClock = vectorClock;
	}
}
