package lang.handlers;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lang.parsing.Command;
import network.groupme.GroupMeRequester;
import persistence.FrequencySystem;
import persistence.sql.HistoryDatabase;
import persistence.sql.HistoryDatabase.UserMapEntry;

public class FrequencyHandler implements CommandHandler {

	private final static Pattern wordExtractor = Pattern.compile("\"(.*?)\"");
	private final static Command[] supportedCommands = new Command[] {Command.FREQ, Command.FREQ_SIMILAR, Command.FREQ_SELF};
	
	
	private final GroupMeRequester sender;
	private final FrequencySystem frequencySystem;
	private final HistoryDatabase historyDB;
	public FrequencyHandler(FrequencySystem frequencySystem, HistoryDatabase history, GroupMeRequester sender) {
		this.sender = sender;
		this.frequencySystem = frequencySystem;
		this.historyDB = history;
	}
	@Override
	public void process(Command cmd, String senderID, String extra) {
		if (extra == null) return;
		Matcher matcher = wordExtractor.matcher(extra);
		if (!matcher.find()) return;
		String phrase = matcher.group(1).trim();
		if (phrase.contains(" ")) {
			sender.send("Sorry, but I can't find frequency data for multi-word phrases. I should be able to search for the first occurrence of a phrase, though, so try that.");
			return;
		}
		switch (cmd) {
		case FREQ:
			HashMap<Byte, Integer> counts;
			int totalcount;
			try {
				totalcount = frequencySystem.getTotalWordCount(phrase);
				counts = frequencySystem.getWordCountAllUsers(phrase);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			if (totalcount == 0) {
				sender.send("By my account, the word \"" + phrase + "\" has never been said in this group.");
				return;
			}
			try {
				counts = frequencySystem.getWordCountAllUsers(phrase);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			String response = "The word \"" + phrase + "\" has been said " + totalcount + " times in this group.\n\nFrequency by user:\n";
			for (Entry<Byte, Integer> entry : counts.entrySet()) {
				String username = historyDB.usernameFromInternalID(entry.getKey());
				if (username == null) continue;
				response += username + " : " + entry.getValue() + " (" + String.format("%.2f", (float)entry.getValue() / totalcount * 100.0) + "%)\n";
			}
			sender.send(response);
			break;
		case FREQ_SELF:
			UserMapEntry user = historyDB.userFromID(senderID);
			if (user.username == null) {
				sender.send("Well this is awkward... I don't know who you are yet.  Wait a little while and try again, I'm a slow learner.");
				return;
			}
			int wordct;
			try {
				wordct = frequencySystem.getWordCount(phrase, (byte)user.internalID);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			if (wordct == 0) {
				sender.send("Hey " + user.username + ", it doesn't look like you've ever said \"" + phrase + "\" before in this group...");
			} else {
				sender.send(user.username + ", you have said \"" + phrase + "\" " + wordct + " times.");
			}
			break;
		}
	}

	@Override
	public String getHandlerName() {
		return "FREQUENCY HANDLER";
	}

	@Override
	public Command[] supportedCommands() {
		return supportedCommands;
	}

}
