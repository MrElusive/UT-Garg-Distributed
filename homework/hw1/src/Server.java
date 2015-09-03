import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
	private int numberOfSeats;
    private Map<String, Integer> reservedSeats;
    
    private ServerSocket serverSocket;
    
    // tracks all the message handlers that are created, so we can close them
    // when the server is stopped
    private List<MessageHandler> messageHandlers;
    
    // handles incoming connections from tcp clients
    // for each incoming connection, a tcp handler is created to handle messages over that connection
    // see acceptIncomingTCPConnection() for details
	private Thread serverSocketHandler;
	
	// handles messages over a udp connection
	private Thread udpHandler;
	
	// coordinates the activity of all the threads that are handling tcp/udp connections
	private volatile boolean isRunning;	
    
    public Server(int numberOfSeats, int tcpPort, int udpPort) throws IOException {
    	this.numberOfSeats = numberOfSeats;
    	this.reservedSeats = new HashMap<String, Integer>(this.numberOfSeats);
    	        
        this.isRunning = false;
        
        this.serverSocket = new ServerSocket(tcpPort);
        this.messageHandlers = new ArrayList<MessageHandler>();
        
        this.serverSocketHandler = new Thread(
        	new Runnable() {
			
			@Override
			public void run() {
				while (isRunning) {
					try {
						acceptIncomingTCPConnection();
					} catch (IOException e) {
						e.printStackTrace();
						isRunning = false;
						Server.this.notify();
					}
				}				
			}
    	});
        
        ServerUDPMessageHandler serverUDPMessageHandler = new ServerUDPMessageHandler(udpPort);
        messageHandlers.add(serverUDPMessageHandler);
        
        udpHandler = new Thread(new CommandHandler(serverUDPMessageHandler));
    }
    
    public void start() {
		this.isRunning = true;
		this.serverSocketHandler.start();	
		this.udpHandler.start();
	}

	private void stop() {
		// close all of the message handlers so they aren't waiting for messages from clients
		for (MessageHandler messageHandler : messageHandlers) {
			messageHandler.close();
		}
	}

	private void acceptIncomingTCPConnection() throws IOException {
		Socket socket = serverSocket.accept();
		
		TCPMessageHandler tcpMessageHandler = new TCPMessageHandler(socket);
		messageHandlers.add(tcpMessageHandler);
		
		Thread tcpHandler = new Thread(new CommandHandler(tcpMessageHandler));
		tcpHandler.start();
	}
    
    private synchronized String executeCommand(String commandString) throws CommandParser.InvalidCommandException {
    	String result = null;
    	CommandParser.Command command = CommandParser.parseCommand(commandString);
    	
    	// @TODO: Handle each command type
    	
    	// Shutdown the server and notify all other threads
    	if (command.isCloseCommand()) {
    		this.isRunning = false;
    		notify();
    	}
    	
    	return result;
    }
    
	public boolean getIsRunning() {
		return isRunning;
	}
    
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
					e.printStackTrace();
				}
			}			
		}
    }
    
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <N>: the total number of available seats");
            System.out.println("\t\t\tassume the seat numbers are from 1 to N");
            System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
            System.out.println("\t(3) <udpPort>: the port number for UDP connection");

            System.exit(1);
        }
        
        int N = Integer.parseInt(args[0]);
        int tcpPort = Integer.parseInt(args[1]);
        int udpPort = Integer.parseInt(args[2]);
        
        Server server = null;
        try {
        	server = new Server(N, tcpPort, udpPort);
        } catch (IOException e) {
            System.out.println("Error: Could not initialize the server.");
            e.printStackTrace();
            System.exit(1);
        }
        
        server.start();
        try {
        	while (server.getIsRunning()) {
        		server.wait();
        	}
		} catch (InterruptedException e) {		
			e.printStackTrace();
		}
        
        server.stop();
    }
}
