package lang.handlers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import lang.parsing.Command;
import network.groupme.GroupMeRequester;
import network.models.JSONUrbanResponse;
import network.models.JSONUrbanResponse.Definition;

public class DefinitionHandler implements CommandHandler {

	private final static Command[] supportedCommands = new Command[] {Command.DEFINE};
	private GroupMeRequester requester;
	public DefinitionHandler(GroupMeRequester requester) {
		this.requester = requester;
	}
	@Override
	public void process(Command cmd, String senderID, String extra) {
		if (extra == null || extra.length() > 64) return;
		JSONUrbanResponse definitions = requester.getDefinition(extra);
		if (definitions == null) return;
		if (!definitions.result_type.equals("exact")) {
			requester.send("Could not find a definition for \"" + extra + "\".");
			return;
		}
		Definition def = definitions.list.get(0);
		if (def == null) {
			requester.send("Could not find a definition for \"" + extra + "\".");
			return;
		}
		String response = "Definition for \"" + def.word + "\":\n\n";
		String defcontent = def.definition.substring(0, Math.min(350, def.definition.length()));
		if (defcontent.length() != def.definition.length()) {
			defcontent += "...";
		}
		String example = def.example.substring(0, Math.min(350,  def.example.length()));
		if (example.length() != def.example.length()) {
			example += "...";
		}
		response += defcontent + "\n\nExample:\n\n";
		response += "'" + example + "'\n\nMore at " + def.permalink;
		
		requester.send(response);
		
		
	}

	@Override
	public String getHandlerName() {
		return "URBAN DICTIONARY HANDLER";
	}

	@Override
	public Command[] supportedCommands() {
		return supportedCommands;
	}

}
