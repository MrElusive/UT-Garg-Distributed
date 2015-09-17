import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.io.IOException;
import java.net.DatagramPacket;

// This UDP message handler is intended to be used by the client
// See UDPMessageHandler and MessageHandler for more details
public class ClientUDPMessageHandler extends UDPMessageHandler {
	private String hostAddress;
	private int udpPort;
	private byte[] receiveData = new byte[GlobalConstants.MAX_BUFFER_LENGTH];
	private DatagramPacket receivePacket;

	public ClientUDPMessageHandler(String hostAddress, int udpPort) throws SocketException {
		super(new DatagramSocket());

		this.hostAddress = hostAddress;
		this.udpPort = udpPort;

		receivePacket = new DatagramPacket(receiveData, receiveData.length);
	}

	public synchronized void sendMessage(String message) throws IOException {
		byte[] sendData = message.getBytes();
		if (sendData.length > GlobalConstants.MAX_BUFFER_LENGTH) {
			throw new RuntimeException(String.format("Error: Message too long"));
		}

		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(hostAddress), udpPort);
		super.datagramSocket.send(sendPacket);
	}

	public synchronized String receiveMessage() throws IOException {
		super.datagramSocket.receive(receivePacket);
		return new String(receivePacket.getData(), 0, receivePacket.getLength());
	}
}