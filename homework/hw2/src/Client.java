import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.IOException;
import java.net.UnknownHostException;

public class Client {  
    private List<String> hostAddress;
    private List<Integer> tcpPort;
    
    // The handler is in charge of sending and receiving a string message over TCP/IP
    private TCPMessageHandler tcpMessageHandler;
    
    public Client(List<String> hostAddress, List<Integer> tcpPort) throws UnknownHostException, IOException {
    	assert(!hostAddress.isEmpty());
    	assert(!tcpPort.isEmpty());
    	assert(hostAddress.size() == tcpPort.size());
    	
        this.hostAddress = hostAddress;
        this.tcpPort = tcpPort;

        tcpMessageHandler = new TCPMessageHandler(hostAddress.get(0), tcpPort.get(0));
    }
        
	private String executeCommand(String commandString) throws IOException {
		tcpMessageHandler.sendMessage(commandString);
		return tcpMessageHandler.receiveMessage();
	}
      
    public static void main (String[] args) {
        
        Scanner scanner = new Scanner(System.in);
        int numServer = scanner.nextInt();
        
        List<String> hostAddressList = new ArrayList<String>(numServer);
        List<Integer> tcpPortList = new ArrayList<Integer>(numServer);  

        for (int i = 0; i < numServer; i++) {
        	String[] socketAddressComponents = scanner.nextLine().trim().split(":");
        	if (socketAddressComponents.length == 2) {
        		System.out.println("Expected socket address format: xxx.xxx.xxx.xxx:xxxxx");
        		System.exit(1);
        	}
        	
        	hostAddressList.add(socketAddressComponents[0].trim());       	        	
        	tcpPortList.add(Integer.parseInt(socketAddressComponents[1].trim()));
        }
        
        // Create the client object
        Client client = null;
        try {
        	client = new Client(hostAddressList, tcpPortList);
        } catch (Exception e) {
            System.out.println("Error: Could not connect to server.");
            e.printStackTrace();
            System.exit(1);
        }
        
        while (scanner.hasNextLine()) {
            try {
                // Parse and validate the user's command string
                String commandString = scanner.nextLine().trim();
                CommandParser.Command command = CommandParser.parseCommand(commandString);
                
                // Acquire the appropriate message handler from the client, using the protocol specified in the command
                String response = client.executeCommand(commandString);
                
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
