package lang.parsing;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;

import lang.handlers.CommandHandler;
import network.models.JSONMessageResponse.Message;

public class ChatProcessor {
	private final String BOT_NAME;
	private final HashMap<Command, CommandHandler> handlers;
	
	public ChatProcessor(String botName, HashMap<Command, CommandHandler> handlers) {
		if (botName.matches(" ")) throw new IllegalArgumentException();
		BOT_NAME = botName.toLowerCase();
		this.handlers = handlers;
	}
	
	public Command process(Message message) throws UnsupportedEncodingException {
		if (message.system || message.senderType != "user" || message.senderName.equals("GroupMe") || message.text == null)
			return null;
		String msg = sanitizeInput(message.text);
		String[] nameBody = msg.split(" ", 2);
		if (!nameBody[0].equals(BOT_NAME)) return null;
		byte[] bodyASCII = nameBody[1].getBytes("US-ASCII");
		Command.TypeIndexPair tip = Command.fromByteArray(bodyASCII);
		if (tip != null) {
			Command ctype = tip.type;
			int commandLength = ctype.lengthOfCommandAt(tip.wordIndex);
			String remainder = (commandLength>= nameBody[1].length() - 1) ? null : nameBody[1].substring(commandLength + 1);
			System.out.println(remainder);
			CommandHandler handler = handlers.get(ctype);
			if (handler != null) handler.process(ctype, remainder);
			return ctype;
		}
		return null;
	}

	
	
	private static String sanitizeInput(String message) {
		return message.toLowerCase().replaceAll("[.,()?!*;:]", " ").replaceAll("[\n\t\r ]+", " ").trim();
	}
	
}
