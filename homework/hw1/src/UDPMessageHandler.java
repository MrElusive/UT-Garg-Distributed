import java.net.DatagramSocket;


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
