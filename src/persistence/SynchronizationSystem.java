package persistence;

import java.io.IOException;
import java.util.Enumeration;

import core.Options;
import network.groupme.GroupMeRequester;
import network.models.JSONMessageResponse;
import network.models.JSONMessageResponse.Message;
import persistence.sql.HistoryDatabase;
import persistence.sql.MessageProcessingStats;

public class SynchronizationSystem {
	private final GroupMeRequester gmReq;
	private final FrequencySystem freqSystem;
	private final HistoryDatabase history;
	private final MessageStorage messageStorage;
	private boolean maybeDirty = true;
	public SynchronizationSystem(GroupMeRequester gmReq, FrequencySystem freqSystem, MessageStorage messageStorage, HistoryDatabase history) throws Exception {
		this.gmReq = gmReq;
		this.freqSystem = freqSystem;
		this.history = history;
		this.messageStorage = messageStorage;
		
		String largestHist = history.latestMessageID();
		String largestStorage = messageStorage.largestMessageID();
		if (!largestHist.equals(largestStorage)) {
			System.out.println("Message ID disparity, rebuilding databases");
			relink();
		} else {
			String largestFreq = history.translateToGMID(freqSystem.getLargestMessageIDRecorded());
			if (!largestHist.equals(largestFreq)) {
				System.out.println("Message ID disparity, rebuilding databases");
				relink();
			}
		}
	}

	private String relink() throws Exception {
		if (!maybeDirty) return messageStorage.largestMessageID();
		System.out.println("Removing word associations and linkages...");
		freqSystem.destroy();
		history.drop();
		int totalProcessed = 0;
		String latestMessage = "0";
		int iteration = 0;
		for (Enumeration<String> messages = messageStorage.getMessageHistory(); messages.hasMoreElements();) {
			JSONMessageResponse resp = gmReq.getMessagesFromString(messages.nextElement());
			try {
				totalProcessed += processMessageSet(resp).processed;
			}catch (Exception e) {
				System.out.println("Error occurred while processing message set " + iteration);
			}
			if (resp.data.messages.size() > 0) {
				Message finalMessage = resp.data.messages.get(resp.data.messages.size() - 1);

				if (finalMessage.messageID.compareTo(latestMessage) > 0) {
					latestMessage = finalMessage.messageID;
				}
			} else {
				break;
			}
			iteration++;
		}
		freqSystem.commit();
		history.commit();
		messageStorage.commitMessages();
		System.out.println("Loaded " + totalProcessed + " messages from local storage");
		maybeDirty = false;
		return latestMessage;
	}



	private MessageProcessingStats processMessageSet(JSONMessageResponse response) throws Exception {
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
				throw e;
			}
			processed++;			
		}

		return new MessageProcessingStats(processed, lastMessageID, lastMessageGMID, lastUserID);
	}


	@SuppressWarnings("unused")
	public int synchronize(boolean relink) throws Exception {
		String latestMessage = relink ? relink() : history.latestMessageID();
		int currMessageCount = history.totalNumMessages();		
		if (!Options.FETCH_NEW) return 0;		
		
		int remaining = -1;
		int processed = 0;
		float percentage = 0;
		JSONMessageResponse resp;
		do {
			try {
				resp = gmReq.getMessages(latestMessage);
				
				if (resp != null && resp.data.messages.size() > 0) {
					if (remaining < 0) {
						remaining = resp.data.count - currMessageCount;
						System.out.println("Retrieving " + remaining + " messages from server");
					}
					if (remaining > 0) System.out.println("Percentage : " + String.format("%.2f", (double)processed / remaining * 100.0 ));
						
					
					messageStorage.saveChunk(resp);
					MessageProcessingStats stats = processMessageSet(resp);
					processed += stats.processed;
					latestMessage = stats.lastMessageGMID;
				}else{
					resp = null;
				}

			}catch (IOException e) {
				resp = null;
			}

		} while (resp != null);
		freqSystem.commit();
		history.commit();
		messageStorage.commitMessages();
		return processed;
	}

}
