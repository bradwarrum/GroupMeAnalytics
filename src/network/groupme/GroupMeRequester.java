package network.groupme;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Scanner;

import javax.net.ssl.SSLException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import core.Options;
import javafx.util.Pair;
import network.models.JSONBotMessage;
import network.models.JSONMessageResponse;
import network.models.JSONUrbanResponse;

public class GroupMeRequester {
	private final GroupMeConfig config;
	private static final String API = "https://api.groupme.com/v3";
	private static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
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
	public static void correctSystemClassification(JSONMessageResponse.Message message) {
		if (!message.system && (message.senderName.equals("GroupMe") || !message.senderType.equals("user"))) {
			message.system = true;
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
		String content = getRequest(istream);
		JSONMessageResponse resp =  GSON.fromJson(content, JSONMessageResponse.class);
		resp.meta.actualStatus = connection.getResponseCode();
		istream.close();
		return resp;
	}

	public static JSONMessageResponse getMessagesFromString(String content) {
		JSONMessageResponse resp =  GSON.fromJson(content,  JSONMessageResponse.class);
		resp.meta.actualStatus = 200;
		return resp;
	}

	public static JSONMessageResponse.Message getMessageFromString(String content) {
		JSONMessageResponse.Message msg = GSON.fromJson(content, JSONMessageResponse.Message.class);
		return msg;
	}
	
	public static String getStringFromMessages(JSONMessageResponse response) {
		return GSON.toJson(response);
	}

	public void send(String message) {
		try {
			System.out.println(message);
			if (!Options.SEND_RESPONSE) return;
			
			JSONBotMessage botmsg = new JSONBotMessage(message, config.botID);
			URL url = buildURL(API + "/bots/post", new Pair<String, String> ("token", config.accessToken));
			if (url == null) throw new IllegalArgumentException("Invalid URL");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			byte[] utf8msg = GSON.toJson(botmsg).getBytes(Charset.forName("UTF-8"));
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Length", String.valueOf(utf8msg.length));
			connection.setRequestProperty("Charset", "utf-8");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Content-Language", "en-US");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.connect();
			OutputStream output = connection.getOutputStream();
			output.write(utf8msg);
			output.close();
			connection.getResponseCode();
		}catch (IOException e) {
			System.out.println("Couldn't not publish the response to the group.");
			e.printStackTrace();
		}
	}
	
	public JSONUrbanResponse getDefinition(String phrase) {
		try {
		String defparam = URLEncoder.encode(phrase, "UTF-8");
		URL url = buildURL("http://api.urbandictionary.com/v0/define", new Pair<String,String>("term", defparam));
		if (url == null) throw new IllegalArgumentException("Invalid URL");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		String data = getRequest(connection.getInputStream());
		return GSON.fromJson(data, JSONUrbanResponse.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected String getRequest(InputStream stream) {
		byte[] chunk = new byte[512];
		StringBuffer request = new StringBuffer();
		int ofs = 0;
		int read = 0;
		try {
			do {
				read = stream.read(chunk, 0, 512);
				if (read > 0) {
					request.append(new String(chunk, 0, read));
					ofs += read;
				}
			}while (read>=0);
		}catch (IOException e) {
			System.out.println("WARNING: REQUEST BUFFER FAILURE");
			return "";
		}

		return request.toString();
	}
}
