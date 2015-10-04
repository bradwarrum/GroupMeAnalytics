package lang.handlers;

import lang.parsing.Command;
import network.groupme.GroupMeRequester;
import persistence.FrequencySystem;
import persistence.sql.HistoryDatabase;

public class MetadataHandler implements CommandHandler {

	private final FrequencySystem backingData;
	private final HistoryDatabase history;
	private final GroupMeRequester sender;
	private final static Command[] SUPPORTED_COMMANDS = new Command[] {Command.WRDCT_TOTAL, Command.WRDCT_UNIQUE, Command.TOTAL_MESSAGES};
	public MetadataHandler(FrequencySystem backingData, HistoryDatabase history, GroupMeRequester sender) {
		this.backingData = backingData;
		this.history = history;
		this.sender = sender;
	}
	@Override
	public void process(Command cmd, String senderID, String extra) {
		String response = null;
		if (extra != null) return;
		switch(cmd) {
		case WRDCT_TOTAL:
			response = "When I last checked, a total of " + backingData.getTotalWordCount() + " words had been sent to this group.";
			break;
		case WRDCT_UNIQUE:
			long uniqueCt = backingData.getUniqueWordCount();
			long totalCt = backingData.getTotalWordCount();
			response = "When I last looked, " + uniqueCt + " unique words had been sent to this group.  That means " + String.format("%.2f", (double)(totalCt - uniqueCt) / totalCt * 100.0) + "% of words sent to the group have been sent at least once in the past.";
			break;
		case TOTAL_MESSAGES:
			response = "The last time I checked, " + history.totalNumMessages() + " messages had been sent to this group.";
			break;
		}
		if (response != null) {
			sender.send(response);
		}
	}
	@Override
	public String getHandlerName() {
		return "METADATA PROCESSOR";
	}
	@Override
	public Command[] supportedCommands() {
		return SUPPORTED_COMMANDS;
	}

}
