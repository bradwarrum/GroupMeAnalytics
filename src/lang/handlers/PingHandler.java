package lang.handlers;

import lang.parsing.Command;
import network.groupme.GroupMeRequester;

public class PingHandler implements CommandHandler {

	private final GroupMeRequester sender;
	private static final Command[] supportedCommands = new Command[] {Command.PING};
	public PingHandler(GroupMeRequester sender) {
		this.sender = sender;
	}
	@Override
	public void process(Command cmd, String senderID, String extra) {
		sender.send("Yes my child, I am available to do your bidding.");
	}

	@Override
	public String getHandlerName() {
		return "PING HANDLER";
	}

	@Override
	public Command[] supportedCommands() {
		return supportedCommands;
	}

}
