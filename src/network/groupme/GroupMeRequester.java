package network.groupme;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.util.Pair;
import network.models.JSONMessageResponse;

public class GroupMeRequester {
	private final GroupMeConfig config;
	private static final String API = "https://api.groupme.com/v3";
	private static Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
	public GroupMeRequester(GroupMeConfig config) {
		this.config = config;
	}
	@SafeVarargs
	private static URL buildURL(String url, Pair<String, String>...params) {
		if (params != null) {
			char prefix = '?';
			for (Pair<String,String> param : params) {
				url += prefix + param.getKey() + "=" + param.getValue();
				prefix = '&';
			}
		}
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			return null;
		}
	}
	public JSONMessageResponse getMessages(String afterID) throws IOException {
		URL url = buildURL(API + "/groups/" + config.groupID + "/messages",
				new Pair<String, String>("after_id", afterID),
				new Pair<String, String>("limit", "100"),
				new Pair<String, String>("token", config.accessToken));
		if (url == null) throw new IllegalArgumentException("Invalid URL");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		InputStream istream;
		if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
			istream = connection.getErrorStream();
		} else {
			istream = connection.getInputStream();
		}
		Scanner scanner = new Scanner(istream);
		String content = scanner.useDelimiter("\\A").next();
		JSONMessageResponse resp =  GSON.fromJson(content, JSONMessageResponse.class);
		resp.meta.actualStatus = connection.getResponseCode();
		istream.close();
		return resp;

	}
}
