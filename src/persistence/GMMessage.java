package persistence;

public class GMMessage {
	private final String message;
	private final byte memberID;
	private final int messageID;
	public GMMessage(String message, byte memberID, int messageID) {
		this.message = message;
		this.memberID = memberID;
		this.messageID = messageID;
	}
	
	public String message() {
		return this.message;
	}
	
	public byte memberID() {
		return this.memberID;
	}
	
	public int messageID() {
		return this.messageID;
	}
}
