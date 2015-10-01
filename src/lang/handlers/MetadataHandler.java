package lang.handlers;

import lang.parsing.Command;
import persistence.FrequencySystem;

public class MetadataHandler implements CommandHandler {

	private final FrequencySystem backingData;
	private final static Command[] SUPPORTED_COMMANDS = new Command[] {Command.WRDCT_TOTAL, Command.WRDCT_UNIQUE};
	public MetadataHandler(FrequencySystem backingData) {
		this.backingData = backingData;
	}
	@Override
	public void process(Command cmd, String extra) {
		if (extra != null) return;
		switch(cmd) {
		case WRDCT_TOTAL:
			backingData.getTotalWordCount();
		case WRDCT_UNIQUE:
			backingData.getUniqueWordCount();
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
