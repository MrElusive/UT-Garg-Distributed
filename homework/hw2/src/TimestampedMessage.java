import java.io.Serializable;

// Bundles a String message and a LamportClock timestamp into a single object that
// can easily be serialized and sent over the network.
public class TimestampedMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	public TimestampedMessage(String message, LamportClock timestamp) {
		// @TODO: Implement me!
	}
	
	public String getMessage() {
		// @TODO: Implement me!
		return null;
	}
	
	public LamportClock getTimestamp() {
		// @TODO: Implement me!
		return null;
	}
}
