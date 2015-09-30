package lang.parsing;

import java.io.UnsupportedEncodingException;

public enum Command {
	TOTAL_MESSAGES("message count", "count all messages"),
	WRDCT_UNIQUE("unique count", "count unique words", "count all unique words", "unique word count", "unique wordcount"),
	WRDCT_TOTAL("total count", "count all words", "total wordcount", "total word count"),
	FREQ("count", "frequency of", "frequency for"),
	FREQ_SIMILAR("count words like", "count like", "count similar", "count words similar to", "count words starting with", "count starting with"),
	LOOKUP("find recent occurrences of", "find", "find last occurrence of", "find most recent occurrence of", "last message containing", "messages containing", "message containing"),
	MSG_BY_ID("message number", "get message with id", "get message number", "message id", "id"),
	MSG_BY_DATE("messages from", "messages on", "messages"),
	MSG_TAG("tag", "save", "remember", "remember this as", "remember this as the time"),
	MSG_RECALL("recall", "load", "remind me about", "remember when", "what happened", "what happened when"),
	THIS_DAY_IN_HISTORY("this day in history");
	
	private final String[] commands;
	private static TrieNode<TypeIndexPair> commandTree = null;
	static {
		for (Command ct : Command.values()) {
			for (int i = 0; i < ct.commands.length; i++) {
				try {
					commandTree = TrieNode.addString(ct.commands[i].getBytes("US-ASCII"), commandTree, ct.new TypeIndexPair(ct, i));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
	}
	private Command(String...commandStrings) {
		this.commands = commandStrings;
	}
	
	public class TypeIndexPair {
		public final Command type;
		public final int wordIndex;
		public TypeIndexPair(Command type, int index) {
			this.type = type;
			this.wordIndex = index;
		}
	}
	
	public int lengthOfCommandAt(int index) {
		return commands[index].length();
	}
	
	public static TypeIndexPair fromByteArray(byte[] message) {
		return TrieNode.bestMatch(message, commandTree);
	}
}
