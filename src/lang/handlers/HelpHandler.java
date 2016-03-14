package lang.handlers;

import network.groupme.GroupMeRequester;
import lang.parsing.Command;

public class HelpHandler implements CommandHandler {

	private static final Command[] supportedCommands = new Command[] {Command.HELP};
	private final GroupMeRequester sender;
	public HelpHandler(GroupMeRequester sender) {
		this.sender = sender;
	}
	@Override
	public void process(Command cmd, String senderID, String extra) {
		sender.send("Help Docs:\n\n" +
					"'Jarvis refresh': Update message and user definitions\n" +
				    "'Jarvis define <PHRASE>': Look up a phrase on UrbanDictionary\n" +
					"'Jarvis frequency of \"<PHRASE\"': Find frequency of a phrase by user\n" +
				    "'Jarvis are you there': See if Jarvis is currently operating\n\n" +
					"See all aliases and inactive commands at https://github.com/bradwarrum/GroupMeAnalytics/blob/master/src/lang/parsing/Command.java");
	}

	@Override
	public String getHandlerName() {
		return "HELP DOC HANDLER";
	}

	@Override
	public Command[] supportedCommands() {
		return supportedCommands;
	}

}
