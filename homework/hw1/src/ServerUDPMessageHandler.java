import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

// This UDP message handler is intended for use by the server.
// It differs from the ClientUDPMessageHandler because the DatagramSocket needs to be
// bound to a port.
// See UDPMessageHandler and MessageHandler for more details
public class ServerUDPMessageHandler extends UDPMessageHandler {
	private int len = 1024;
	private byte[] receiveData = new byte[len];
	private DatagramPacket receivePacket;
	
	private volatile boolean newMessageReceived;

	public ServerUDPMessageHandler(int udpPort) throws SocketException {
		super(new DatagramSocket(udpPort));
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		newMessageReceived = false;
	}

	public synchronized void sendMessage(String message) throws IOException {
		if (newMessageReceived != true) {
			throw new RuntimeException("Error: sendMessage() called before receiveMessage()!");
		}
		
		byte[] sendData = message.getBytes();
		if (sendData.length > len) {
			throw new RuntimeException(String.format("Error: Message too long"));
		}
		
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());		
		super.datagramSocket.send(sendPacket);
		newMessageReceived = false;
	}

	public synchronized String receiveMessage() throws IOException {
		if (newMessageReceived != false) {
			throw new RuntimeException("Error: sendMessage() called before receiveMessage()!");
		}
				
		super.datagramSocket.receive(receivePacket);
		
		newMessageReceived = true;
		return new String(receivePacket.getData(), 0, receivePacket.getLength());
	}
}