import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class CommandParser {
	
	public static class InvalidCommandException extends Exception {

		private static final long serialVersionUID = 5755274880351045973L;

		public InvalidCommandException(String message) {
			super(message);
		}
		
	}
	
	public static Command parseCommand(String commandString) throws InvalidCommandException {
		String[] tokens = commandString.split(" ");
		if (tokens.length < 2) { // Every commandString must have at least two tokens: "commandType protocol"
			throw new InvalidCommandException("Error: the command string must contain at least two tokens.");
		}
		
		String commandTypeString = tokens[0].trim();
		Command.CommandType commandType = null;
		try {
			commandType = Command.CommandType.valueOf(commandTypeString);
		} catch (IllegalArgumentException e) {
			throw new InvalidCommandException(String.format("Error: unrecognized command type: %s", commandTypeString));
		}
		
		List<String> arguments = new ArrayList<String>();
		for (int i = 1; i < tokens.length - 1; i++) {
			arguments.add(tokens[i].trim());
		}
		commandType.validateArguments(arguments);
		
        String protocolString = tokens[tokens.length - 1].trim();
        Protocol protocol = null;
        if ("T".equals(protocolString)) {
            protocol = Protocol.TCP;
            
        } else if ("U".equals(protocolString)) {
        	protocol = Protocol.UDP;            
        } else {
            throw new InvalidCommandException(String.format("Error: Invalid protocol encountered: %s", protocolString));
        }
		
		return new Command(commandType, arguments, protocol);
	}
	
	public static class Command {
		
		public enum CommandType {
			RESERVE("\\w+"),
			BOOKSEAT("\\w+", "\\d+"),
			SEARCH("\\w+"),
			DELETE("\\w+"),
			CLOSE();
			
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
		private Protocol protocol;
		
		public Command(CommandType commandType, List<String> arguments,	Protocol protocol) {
			this.commandType = commandType;
			this.arguments = arguments;
			this.protocol = protocol;
		}

		public CommandType getCommandType() {
			return commandType;
		}
		
		public List<String> getArguments() {
			return arguments;
		}
		
		public Protocol getProtocol() {
			return protocol;
		}

		public boolean isCloseCommand() {
			return commandType == CommandType.CLOSE;
		}
	}
}
