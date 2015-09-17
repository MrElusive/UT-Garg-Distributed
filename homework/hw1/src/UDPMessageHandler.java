import java.net.DatagramSocket;

// This class contains functionality and data that is common between the
// ClientUDPMessageHandler and the ServerUDPMessageHandler.
public abstract class UDPMessageHandler implements MessageHandler {

    protected DatagramSocket datagramSocket;
    
    public UDPMessageHandler(DatagramSocket datagramSocket) {
    	assert (datagramSocket != null);
    	
		this.datagramSocket = datagramSocket;
	}
	
	public void close() {
		datagramSocket.close();
	}

}
