package synapticloop.crosswordr.exception;

public class CrosswordrException extends Exception {
	public CrosswordrException(String message, Exception exception) {
		super(message, exception);
	}

	private static final long serialVersionUID = 6321463505225680651L;

}
