import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPMessageHandler implements MessageHandler {
    private Socket socket;
    BufferedReader reader;
    PrintWriter writer;
    // this is a convenience constructor that creates a Socket object in addition to creating a TCPMessageHandler object.
    public TCPMessageHandler(String hostAddress, int tcpPort) throws UnknownHostException, IOException {
    	this(new Socket(hostAddress, tcpPort));
    }
    
    public TCPMessageHandler(Socket socket) throws IOException {
    	this.socket = socket;
    	
    	reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    	writer = new PrintWriter(socket.getOutputStream());
    }

	public void sendMessage(String message) {
		writer.println(message);
		writer.flush();
    }

	public String receiveMessage() throws IOException {
		return reader.readLine().trim();
	}
	
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			// No need to propagate this exception, hopefully.
		}
	}
    
}