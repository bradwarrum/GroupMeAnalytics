package network.models;

public class JSONBotMessage {
	private final String text;
	private final String bot_id;
	public JSONBotMessage(String message, String botID) {
		bot_id = botID;
		text = message;
	}
}
