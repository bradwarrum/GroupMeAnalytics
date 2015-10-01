package persistence;

import java.io.IOException;

import core.Options;
import network.groupme.GroupMeRequester;
import network.models.JSONMessageResponse;
import persistence.sql.HistoryDatabase;

public class SynchronizationSystem {
	private final GroupMeRequester gmReq;
	private final FrequencySystem freqSystem;
	private final HistoryDatabase history;
	public SynchronizationSystem(GroupMeRequester gmReq, FrequencySystem freqSystem, HistoryDatabase history) {
		this.gmReq = gmReq;
		this.freqSystem = freqSystem;
		this.history = history;
	}

	@SuppressWarnings("unused")
	public void synchronize() {
		String latestMessage = history.latestMessageID();
		int currMessageCount = history.totalNumMessages();
		int remaining = -1;
		int processed = 0;
		float percentage = 0;
		JSONMessageResponse resp;
		do {
			try {
				resp = gmReq.getMessages(latestMessage);
			} catch (IOException e) {
				resp = null;
			}
			if (resp.meta.actualStatus == 200) {
				if (remaining < 0) {
					remaining = resp.data.count - currMessageCount;
					System.out.println("Downloading " + remaining + " messages from the server...");
					System.out.println("Percentage complete: " + percentage + "%");
				}

			}
			for (int i = 0; i < resp.data.messages.size(); i++) {
				JSONMessageResponse.Message respmsg = resp.data.messages.get(i);
				if (!respmsg.system && respmsg.senderType.equals("user") && !respmsg.senderName.equals("GroupMe")) {
					GMMessage msg = new GMMessage(respmsg.text, (byte)128, i);
					try {
						freqSystem.processMessage(msg);
					} catch (Exception e) {
						System.out.println("Error processing message " + i + " of " + resp.data.messages.size() + ":");
						break;
					}
				}
				processed++;
			}
			percentage = ((float)processed / remaining) * 100;
			System.out.println("Percentage complete: " + percentage + "%");
		}while (resp != null && resp.meta.actualStatus == 200 && (!Options.SINGULAR_FETCH));
		if (resp == null) {
			System.out.println("Error fetching remote information.  Processed a total of " + processed + " messages of approximately " + remaining + " before failing.");
			System.out.println("The system is usable, but will not contain the most recently updated information.  Force a synchronization to attempt to retrieve new information.");
		}
		try {
			freqSystem.commit();
			System.out.println(freqSystem.getTotalWordCount("click"));
			System.out.println(freqSystem.getTotalWordCount("top"));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
