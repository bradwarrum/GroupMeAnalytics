package lang.handlers;

import lang.parsing.Command;
import persistence.FrequencySystem;

public class MetadataHandler implements CommandHandler {

	private final FrequencySystem backingData;
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

}
