package network.groupme;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GroupMeConfig {
	public final String botID;
	public final String groupID;
	public final String accessToken;
	public final String botName;
	private GroupMeConfig(String botID, String groupID, String accessToken, String botName) {
		this.botID = botID;
		this.groupID = groupID;
		this.accessToken = accessToken;
		this.botName = botName;
	}

	public static GroupMeConfig fromConfigFile(File configFile) throws IOException {
		InputStream fis = new FileInputStream(configFile);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader reader = new BufferedReader(isr);
		String line;
		String botID = null, groupID = null, accessToken = null, botName = null;
		while ((line = reader.readLine()) != null) {
			String[] kvp = line.trim().split(":");
			switch (kvp[0]) {
			case "BOT_ID":
				botID = kvp[1];
				break;
			case "GROUP_ID":
				groupID = kvp[1];
				break;
			case "ACCESS_TOKEN":
				accessToken = kvp[1];
				break;
			case "BOT_NAME":
				botName = kvp[1];
			}
		}
		if (botID == null || groupID == null || accessToken == null || botName == null || botName.contains(" ")) throw new IOException("Malformed GroupMe configuration file");
		return new GroupMeConfig(botID, groupID, accessToken, botName);
	}
}
