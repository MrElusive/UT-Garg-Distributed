import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {  
    private String hostAddress;
    private int tcpPort;
    private int udpPort;
    
    // Each handler is in charge of sending and receiving a string message over its protocol
    private TCPMessageHandler tcpMessageHandler;
    private ClientUDPMessageHandler udpMessageHandler;
    
    public Client(String hostAddress, int tcpPort, int udpPort) throws UnknownHostException, IOException {
        this.hostAddress = hostAddress;
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
        
        tcpMessageHandler = new TCPMessageHandler(hostAddress, tcpPort);
        udpMessageHandler = new ClientUDPMessageHandler(hostAddress, udpPort);
    }
        
    public MessageHandler getMessageHandler(Protocol protocol) {
    	switch (protocol) {
	    	case TCP:
	    		return tcpMessageHandler;
	    	case UDP:
	    		return udpMessageHandler;
	    	default:
	    		throw new RuntimeException(String.format("Error: Unhandled protocol: %s", protocol.toString()));
    	}
    }
      
    public static void main (String[] args) {
    
        if (args.length != 3) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <hostAddress>: the address of the server");
            System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
            System.out.println("\t(3) <udpPort>: the port number for UDP connection");
            System.exit(1);
        }
        
        String hostAddress = args[0];
        int tcpPort = Integer.parseInt(args[1]);
        int udpPort = Integer.parseInt(args[2]);
        
        // Create the client object
        Client client = null;
        try {
        	client = new Client(hostAddress, tcpPort, udpPort);
        } catch (Exception e) {
            System.out.println("Error: Could not connect to server.");
            e.printStackTrace();
            System.exit(1);
        }
        
        // Acquire user input
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            try {
                // Parse and validate the user's command string
                String commandString = scanner.nextLine().trim();
                CommandParser.Command command = CommandParser.parseCommand(commandString);
                
                // Acquire the appropriate message handler from the client, using the protocol specified in the command
                MessageHandler messageHandler = client.getMessageHandler(command.getProtocol());
                
                // Send and receive a message
                messageHandler.sendMessage(commandString);
                String response = messageHandler.receiveMessage();
                
                System.out.println("Server Response: ");
                System.out.println(response);
                
                // If we sent a shutdown command to the server, then we should exit the program
                if (command.isShutdownCommand()) {
                	System.out.println("Client is Exiting");
                	break;
                }
                
            } catch (CommandParser.InvalidCommandException e) {
				System.out.println(e
				.getMessage());
			} catch(Exception e) {
			    e.printStackTrace();
			}
        }
        // Release scanner resources
        scanner.close();
    }
}
