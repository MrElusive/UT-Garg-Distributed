import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Server {
	private int serverID;	
	
	private int maxNumberOfSeats;
    private Map<String, Integer> reservedSeats;
    
    private List<String> hostAddressList;
	private List<Integer> tcpPortList;
    
    private ServerSocket serverSocket;
    
    // handles incoming connections from tcp clients
    // for each incoming connection, a tcp handler is created to handle messages over that connection
    // see acceptIncomingTCPConnection() for details
	private Thread serverSocketHandler;
	
    // tracks all the message handlers that are created, so we can close them
    // when the server is stopped
    private List<MessageHandler> messageHandlers;    
		
	// coordinates the activity of all the threads that are handling tcp/udp connections
	private volatile boolean isRunning;	
    
    public Server(int serverID, int maxNumberOfSeats, List<String> hostAddressList, List<Integer> tcpPortList) throws IOException {
    	this.serverID = serverID;
    	
    	this.maxNumberOfSeats = maxNumberOfSeats;
    	this.reservedSeats = new HashMap<String, Integer>(this.maxNumberOfSeats);
    	
    	this.hostAddressList = hostAddressList;
    	this.tcpPortList = tcpPortList;
    	        
        this.isRunning = false;
        
        this.serverSocket = new ServerSocket(tcpPortList.get(serverID));
        this.messageHandlers = new ArrayList<MessageHandler>();
        
        // Create a thread specifically for handling incoming TCP connections over the server socket.
        // It will continually accept new connections until the server is shut down
        this.serverSocketHandler = new Thread(
        	new Runnable() {
			
			@Override
			public void run() {
				while (isRunning) {
					try {
						acceptIncomingTCPConnection();
					} catch (IOException e) {
						synchronized(Server.this) {
							isRunning = false;
							Server.this.notify();
						}
					}
				}				
			}
    	});        
    }
    
	public void start() {
		this.isRunning = true;
		this.serverSocketHandler.start();	
	}

	private void stop() {
		// Close all of the message handlers so they aren't waiting for messages from clients
		for (MessageHandler messageHandler : messageHandlers) {
			messageHandler.close();
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {
		
		}
	}

	private void acceptIncomingTCPConnection() throws IOException {
		Socket socket = serverSocket.accept();
		
		// Create a message handler for the current TCP connection
		TCPMessageHandler tcpMessageHandler = new TCPMessageHandler(socket);
		messageHandlers.add(tcpMessageHandler);
		
		Thread tcpHandler = new Thread(new CommandHandler(tcpMessageHandler));
		tcpHandler.start();
	}
    
    // This method is in charge of all the logic related to executing commands.
    // Commands that arrive over TCP or UDP should both be handle by this method.
    private synchronized String executeCommand(String commandString) throws CommandParser.InvalidCommandException {
    	String result = null;
    	CommandParser.Command command = CommandParser.parseCommand(commandString);
    	
    	// This is for debugging purpose only
    	// Remove when finished
    	System.out.println(commandString);
    	
    	// @TODO: Handle each command type
    	List<String> arguments = command.getArguments();
    	switch (command.getCommandType()) {
    		case RESERVE:
    			result = reserve(arguments.get(0));
    			break;
    		case BOOKSEAT:
    			result = bookSeat(arguments.get(0), Integer.parseInt(arguments.get(1)));
    			break;
    		case SEARCH:
    			result = search(arguments.get(0));
    			break;
    		case DELETE:
    			result = delete(arguments.get(0));
    			break;
    		case SHUTDOWN:
    			result = shutdown();
    			break;
    	}    	   	
    	
    	return result;
    }
    
	private synchronized String shutdown() {
		this.isRunning = false;		
		notify();
		return "Server is shutting down!";
	}

	private String delete(String name) {
		Integer seatNum = reservedSeats.remove(name);
		if (seatNum != null) {
			return seatNum.toString();
		} else {
			return String.format("No reservation found for %s", name);
		}
	}

	private String search(String name) {
		if (reservedSeats.containsKey(name)) {
			return Integer.toString(reservedSeats.get(name));
			
		} else {
			return String.format("No reservation found for %s", name);
		}
	}

	private String bookSeat(String name, int seatNum) {
		if (seatNum < 1 || seatNum > maxNumberOfSeats) {
			return "This isn't Hilbert's Grand Hotel. We don't have that many seats!";
			
		} else if (reservedSeats.size() == maxNumberOfSeats) {
			return "Sold out - No seat available";
			
		} else if (reservedSeats.containsKey(name)) {
			return "Seat already booked against the name provided";
			
		} else if (reservedSeats.containsValue(seatNum)) {
			return String.format("%d is not available", seatNum);
			
		} else {
			reservedSeats.put(name, seatNum);
			return String.format("Seat assigned to you is %d", seatNum);
		}
	}

	private String reserve(String name) {
		if (reservedSeats.size() == maxNumberOfSeats) {
			return "Sold out - No seat available";
			
		} else if (reservedSeats.containsKey(name)) {
			return "Seat already booked against the name provided";
			
		} else {
			for (int i = 1; i <= maxNumberOfSeats; i++) {
	    		if (!reservedSeats.containsValue(i)) {
	    			reservedSeats.put(name, i);
	    			return String.format("Seat assigned to you is %d", i);
	    		}
			}
			
			return "Uh oh, someone stole your seat, dude. This should never happen!";
		}
	}

	public synchronized boolean getIsRunning() {
		return isRunning;
	}
    
    // The CommandHandler is a wrapper around the message handler.
    // It simply waits for a command, executes the command when it receives one, and sends a response back to the client
    private class CommandHandler implements Runnable {
    	private MessageHandler messageHandler;
    	
    	public CommandHandler(MessageHandler messageHandler) {
    		this.messageHandler = messageHandler;
    	}
    	
		@Override
		public void run() {
			while (isRunning) {
				try {
					String command = messageHandler.receiveMessage();
					String response = executeCommand(command);
					messageHandler.sendMessage(response);
				} catch (Exception e) {
					break;
				}
			}			
		}
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int myID = scanner.nextInt();
        int numServer = scanner.nextInt();
        int numSeat = scanner.nextInt();
        
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
        
        // Create the server
        Server server = null;
        try {
        	server = new Server(myID, numSeat, hostAddressList, tcpPortList);
        } catch (IOException e) {
            System.out.println("Error: Could not initialize the server.");
            e.printStackTrace();
            System.exit(1);
        }
        
        // Start the server
        server.start();
        try {
            // Block the main thread until the server is shutdown
        	if (server.getIsRunning()) {
        		synchronized (server) {
        			server.wait();
        		}
        	}
		} catch (Exception e) {		
			e.printStackTrace();
		}
        
        // Close out any pending connections
        server.stop();
    }
}