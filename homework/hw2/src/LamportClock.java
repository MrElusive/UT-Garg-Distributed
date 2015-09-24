import java.io.Serializable;

// A logical clock that implements the total-order semantics of Lamport's clock
public class LamportClock implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public LamportClock(long processId) {
		// @TODO: Implement me!
	}

	public long get() {
		// @TODO: Implement me!		
		return 0L;
	}
	
	public void increment() {
		// @TODO: Implement me!		
	}
	
	public boolean isLessThan(LamportClock clock) {
		// @TODO: Implement me!
		return false;
	}
}
