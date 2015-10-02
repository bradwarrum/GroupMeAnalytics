package persistence;

import java.io.IOException;
import java.util.Enumeration;

import core.Options;
import network.groupme.GroupMeRequester;
import network.models.JSONMessageResponse;
import persistence.sql.HistoryDatabase;

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
			totalProcessed += processMessageSet(resp);
			
		}
		System.out.println("Loaded " + totalProcessed + " messages from local storage");
		return totalProcessed;
	}

	private int processMessageSet(JSONMessageResponse response) {
		int processed = 0;
		for (int i = 0; i < response.data.messages.size(); i++) {
			JSONMessageResponse.Message respmsg = response.data.messages.get(i);
			GroupMeRequester.correctSystemClassification(respmsg);
			GMMessage msg = new GMMessage(respmsg.text, (byte)128, i);
			try {
				if (!respmsg.system)					
					freqSystem.processMessage(msg);
				history.processMessage(respmsg); 
			} catch (Exception e) {
				System.out.println("Error processing message " + i + " of " + response.data.messages.size() + ":");
				break;
			}
			processed++;			
		}


		return processed;
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
					processed += processMessageSet(resp);
				}

			}catch (IOException e) {
				resp = null;
			}
			percentage = ((float)processed / remaining) * 100;
			System.out.println("Percentage complete: " + String.format("%.2f", percentage) + "%");
		} while (resp != null && resp.meta.actualStatus == 200 && (!Options.SINGULAR_FETCH || processed < 200));
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
