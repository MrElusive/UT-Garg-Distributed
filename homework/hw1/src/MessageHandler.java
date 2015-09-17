import java.io.IOException;

// This interface allows us to send and receive messages without having knowledge
// of the underlying protocol. For example,
//      MessageHandler messageHandler = getMessageHandler(protocol); 
//      messageHandler.sendMessage(message);
//      response = messageHandler.receiveMessage();
// In this example, whether the protocol is TCP or UDP, it doesn't affect how we
// send or receive messages from this level of abstraction.
public interface MessageHandler {
    // sends a message over a connection
    void sendMessage(String message) throws IOException;
    
    // receives a message over a connection
    String receiveMessage() throws IOException;
    
    // closes the message handler, releasing any resources such as sockets
    void close();
}
