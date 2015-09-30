package lang.handlers;

import lang.parsing.Command;

public interface CommandHandler {
	public void process(Command cmd, String extra);
}
