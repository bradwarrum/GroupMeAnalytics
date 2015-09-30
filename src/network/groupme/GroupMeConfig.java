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
	private GroupMeConfig(String botID, String groupID, String accessToken) {
		this.botID = botID;
		this.groupID = groupID;
		this.accessToken = accessToken;
	}

	public static GroupMeConfig fromConfigFile(File configFile) throws IOException {
		InputStream fis = new FileInputStream(configFile);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader reader = new BufferedReader(isr);
		String line;
		String botID = null, groupID = null, accessToken = null;
		while ((line = reader.readLine()) != null) {
			String[] kvp = line.split(":");
			switch (kvp[0]) {
			case "BOT_ID":
				botID = kvp[1];
				break;
			case "GROUP_ID":
				groupID = kvp[1];
				break;
			case "ACCESS_TOKEN":
				accessToken = kvp[1];
			}
		}
		if (botID == null || groupID == null || accessToken == null) throw new IOException("Malformed GroupMe configuration file");
		return new GroupMeConfig(botID, groupID, accessToken);
	}
}
