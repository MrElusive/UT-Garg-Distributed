import java.net.DatagramSocket;
import java.net.SocketException;

public class ServerUDPMessageHandler extends UDPMessageHandler {
		 
	public ServerUDPMessageHandler(int udpPort) throws SocketException  {
		super(new DatagramSocket(udpPort));
    }    
    
    public void sendMessage(String message) {
		// @TODO: Implement me!        
    }

	public String receiveMessage() {
		// @TODO: Implement me!
		return null;
	}
}