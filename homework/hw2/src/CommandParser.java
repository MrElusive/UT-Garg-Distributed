import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// Encapsulates all the logic of parsing and validating a command string entered at the command prompt.
// Example usage:
//      String commandString = getCommandStringFromUserInput();
//      CommandParser.Command command = CommandParser.parseCommand(commandString);
//      CommandParser.CommandType commandType = command.getCommandType()
//      List<String> arguments = command.getArguments();
//      Protocol protocol = command.getProtocol();
// If the command string is improperly formatted, an InvalidCommandException will be thrown with an appropriate message.
public class CommandParser {
	
	public static class InvalidCommandException extends Exception {

		private static final long serialVersionUID = 5755274880351045973L;

		public InvalidCommandException(String message) {
			super(message);
		}		
	}
	
	public static Command parseCommand(String commandString) throws InvalidCommandException {
	    // Check to see that we have at least the minimum number of tokens
		String[] tokens = commandString.split(" ");
		if (tokens.length < 2) { // Every commandString must have at least two tokens: "commandType protocol"
			throw new InvalidCommandException("Error: the command string must contain at least two tokens.");
		}
		
		// Parse out the command type, e.g. reserve -> CommandType.RESERVE, bookSeat -> CommandType.BOOKSEAT
		// @TODO: Determine if the TA cares about case sensitivity for the commands
		String commandTypeString = tokens[0].trim();
		Command.CommandType commandType = null;
		try {
			commandType = Command.CommandType.valueOf(commandTypeString.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new InvalidCommandException(String.format("Error: unrecognized command type: %s", commandTypeString));
		}
		
		// Parse out the arguments and validate them according to the format strings in the CommandType from above
		// For example, CommandType.BOOKSEAT expects arguments that match the following format (regex) strings: "\\w+", "\\d+".
		//      "\\w+" corresponds to a string of characters
		//      "\\d+" corresponds to a string of digits
		List<String> arguments = new ArrayList<String>();
		for (int i = 1; i < tokens.length - 1; i++) {
			arguments.add(tokens[i].trim());
		}
		commandType.validateArguments(arguments);
		
		return new Command(commandType, arguments);
	}
	
	// This class is just a POJO. All the work of parsing/validating is done in the CommandParser class.
	public static class Command {
		
		// Each CommandType stores a list of format (regex) strings that can be used to validate a list of string arguments (see validateArguments())
		public enum CommandType {
			RESERVE("\\w+"),
			BOOKSEAT("\\w+", "\\d+"),
			SEARCH("\\w+"),
			DELETE("\\w+"),
			SHUTDOWN();
			
			private List<String> argumentFormatStrings;
			
			CommandType(String ... argumentFormatStrings) {
				assert (argumentFormatStrings != null);
				
				this.argumentFormatStrings = new ArrayList<String>();
				
				for (String argumentFormatString : argumentFormatStrings) {
					this.argumentFormatStrings.add(argumentFormatString);
				}
			}
			
			public void validateArguments(List<String> arguments) throws InvalidCommandException {
				if (arguments.size() != argumentFormatStrings.size()) {
					throw new InvalidCommandException(
						String.format(
							"Error: %s expects %d arguments. Received %d instead.", 
							this.name(), 
							argumentFormatStrings.size(), 
							arguments.size()
						)
					);
				}
				
				for (int i = 0; i < arguments.size(); i++) {
					if (!Pattern.matches(argumentFormatStrings.get(i), arguments.get(i))) {
						throw new InvalidCommandException(
							String.format(
								"Error: argument \"%s\" does not match expected format \"%s\".", 
								arguments.get(i), 
								argumentFormatStrings.get(i)
							)
						);
					}
				}
			}
		}
		
		private CommandType commandType;
		private List<String> arguments;
		
		public Command(CommandType commandType, List<String> arguments) {
			this.commandType = commandType;
			this.arguments = arguments;
		}

		public CommandType getCommandType() {
			return commandType;
		}
		
		public List<String> getArguments() {
			return arguments;
		}
		
		public boolean isShutdownCommand() {
			return commandType == CommandType.SHUTDOWN;
		}
	}
}
