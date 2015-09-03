public interface MessageHandler {
    void sendMessage(String message);
    String receiveMessage();
    void close();
}
