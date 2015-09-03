import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPMessageHandler implements MessageHandler {
    private Socket socket;
    
    public TCPMessageHandler(String hostAddress, int tcpPort) throws UnknownHostException, IOException {
    	this(new Socket(hostAddress, tcpPort));
    }
    
    public TCPMessageHandler(Socket socket) {
    	this.socket = socket;
    	// @TODO: use input and output streams from TCP socket to create buffered reader and a print writer
	}

	public void sendMessage(String message) {
		// @TODO: Implement me!        
    }

	public String receiveMessage() {
		// @TODO: Implement me!
		return null;
	}
	
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			// No need to propagate this exception
		}
	}
    
}