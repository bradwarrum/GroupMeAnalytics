package persistence;

import java.io.IOException;
import java.util.Enumeration;

import core.Options;
import network.groupme.GroupMeRequester;
import network.models.JSONMessageResponse;
import persistence.sql.HistoryDatabase;
import persistence.sql.MessageProcessingStats;

public class SynchronizationSystem {
	private final GroupMeRequester gmReq;
	private final FrequencySystem freqSystem;
	private final HistoryDatabase history;
	private final MessageStorage messageStorage;
	public SynchronizationSystem(GroupMeRequester gmReq, FrequencySystem freqSystem, MessageStorage messageStorage, HistoryDatabase history) {
		this.gmReq = gmReq;
		this.freqSystem = freqSystem;
		this.history = history;
		this.messageStorage = messageStorage;
	}

	private int relink() throws Exception {
		System.out.println("Removing word associations and linkages...");
		freqSystem.destroy();
		history.drop();
		int totalProcessed = 0;
		String latestMessage = "0";
		for (Enumeration<String> messages = messageStorage.getMessageHistory(); messages.hasMoreElements();) {
			JSONMessageResponse resp = gmReq.getMessagesFromString(messages.nextElement());
			totalProcessed += processMessageSet(resp).processed;
			
		}
		System.out.println("Loaded " + totalProcessed + " messages from local storage");
		return totalProcessed;
	}
	


	private MessageProcessingStats processMessageSet(JSONMessageResponse response) {
		int processed = 0;
		int lastMessageID = 0;
		String lastMessageGMID = "0";
		byte lastUserID = (byte) 0xFF;
		for (int i = 0; i < response.data.messages.size(); i++) {
			JSONMessageResponse.Message respmsg = response.data.messages.get(i);
			GroupMeRequester.correctSystemClassification(respmsg);
			try {
				MessageProcessingStats stats = history.processMessage(respmsg); 			
				if (!respmsg.system) {
					GMMessage msg = new GMMessage(respmsg.text, stats.lastUserID, stats.lastMessageID);	
					freqSystem.processMessage(msg);					
				}

				if (stats.lastMessageID > lastMessageID) {
					lastMessageID = stats.lastMessageID;
					lastMessageGMID = stats.lastMessageGMID;
					lastUserID = stats.lastUserID;
				}
			} catch (Exception e) {
				System.out.println("Error processing message " + i + " of " + response.data.messages.size() + ":");
				System.exit(1);
			}
			processed++;			
		}

		return new MessageProcessingStats(processed, lastMessageID, lastMessageGMID, lastUserID);
	}


	@SuppressWarnings("unused")
	public void synchronize(boolean relink) throws Exception {
		if (relink) relink();
		String latestMessage = history.latestMessageID();
		int currMessageCount = history.totalNumMessages();
		int remaining = -1;
		int processed = 0;
		float percentage = 0;
		JSONMessageResponse resp;
		do {
			try {
				resp = gmReq.getMessages(latestMessage);

				if (resp.meta.actualStatus == 200) {
					if (remaining < 0) {
						remaining = resp.data.count - currMessageCount;
						System.out.println("Downloading " + remaining + " messages from the server...");
						System.out.println("Percentage complete: " + percentage + "%");
					}
					//TODO: Get last message id so that we don't fetch duplicates
					messageStorage.saveChunk(gmReq.getStringFromMessages(resp));
					MessageProcessingStats stats = processMessageSet(resp);
					processed += stats.processed;
					latestMessage = stats.lastMessageGMID;
					if (processed == 0) resp = null;
				}

			}catch (IOException e) {
				resp = null;
			}
			percentage = ((float)processed / remaining) * 100;
			System.out.println("Percentage complete: " + String.format("%.2f", percentage) + "%");
		} while (resp != null && resp.meta.actualStatus == 200 && (!Options.SINGULAR_FETCH));
		if (resp == null) {
			System.out.println("Error fetching remote information.  Processed a total of " + processed + " messages of approximately " + remaining + " before failing.");
			System.out.println("The system is usable, but will not contain the most recently updated information.  Force a synchronization to attempt to retrieve new information.");
		}
		freqSystem.commit();
		System.out.println(freqSystem.getTotalWordCount("click"));
		System.out.println(freqSystem.getTotalWordCount("top"));
		System.out.println(freqSystem.getTotalWordCount("the"));

	}
}
