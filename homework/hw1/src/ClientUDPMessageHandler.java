import java.net.DatagramSocket;
import java.net.SocketException;

public class ClientUDPMessageHandler extends UDPMessageHandler {
	private String hostAddress;
	private int udpPort;
	 
	public ClientUDPMessageHandler(String hostAddress, int udpPort) throws SocketException  {
		super(new DatagramSocket());
		
    	this.hostAddress = hostAddress;
    	this.udpPort = udpPort;
    }    
    
    public void sendMessage(String message) {
		// @TODO: Implement me!        
    }

	public String receiveMessage() {
		// @TODO: Implement me!
		return null;
	}
}