package lang.handlers;

import lang.parsing.Command;

public interface CommandHandler {
	public void process(Command cmd, String senderID, String extra);
	public String getHandlerName();
	public Command[] supportedCommands();
}
