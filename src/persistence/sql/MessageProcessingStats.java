package persistence.sql;

public class MessageProcessingStats {
	public final int processed;
	public final int lastMessageID;
	public final String lastMessageGMID;
	public final byte lastUserID;
	public MessageProcessingStats(int processed, int lastMessageID, String GMID, byte lastUserID) {
		this.processed = processed;
		this.lastMessageID = lastMessageID;
		this.lastMessageGMID = GMID;
		this.lastUserID = lastUserID;
	}
}