import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;

import org.junit.Test;

import com.sun.org.apache.xml.internal.serializer.utils.Messages;

import core.GMAnalytics;
import lang.handlers.CommandHandler;
import lang.parsing.ChatProcessor;
import lang.parsing.Command;
import network.groupme.GroupMeConfig;
import network.groupme.GroupMeRequester;
import network.models.JSONMessageResponse;
import persistence.FrequencySystem;
import persistence.GMMessage;
import persistence.caching.PageCache;
import persistence.caching.PageEntry;
import persistence.data.structures.WordTree;

public class PageCacheTest {
	//@Test
	public void initialPageCreation() throws Exception {
		PageCache pageCache = new PageCache("./data/testMain", "./data/testRollback", 100, 1024, 32);
		if (pageCache.numPages() == 0) {
			pageCache.createPage();
		}
		PageEntry entry = pageCache.entryAt(0, 0);
		byte[] data = new byte[32];
		java.util.Arrays.fill(data, (byte)1);
		entry.writeData(data, 0, 32);
		pageCache.commit();
		entry.close();
		
	}
	private static String testString = "The goal here is for you to maintain the level of concentration you normally experience when you read in untimed situations. When you’re really into a book or an article, the rest of the world fades away and you disappear into the page. Unfortunately, time constraints and the pressure of knowing you’re being tested make it difficult to maintain this kind of natural, high-level concentration. Reading actively in the manner we’ve described builds your concentration.";
	//private static String testString = "A B C A B C";
	//@Test
	public void freqCacheWrapper() throws Exception {
		WordTree wrapper = new WordTree("./data/fcwMain", "./data/fcwRollback");
	    testString = testString.replaceAll("[.,\"()?!*:;]", "");
	    String[] words = testString.split(" ");
		for (String word : words) {
			if (word.equals("normally")) {
				System.out.println("Here");
			}
			wrapper.mapWord(word);
		}
		wrapper.commit();
	}
	//@Test
	public void freqSys() throws Exception {
		FrequencySystem sys = new FrequencySystem();
		HashSet<String> words = new HashSet<String>();
		int wordct = 0;
		for (String s : testString.toLowerCase().replaceAll("[.,\"()?!*;:]", "").split(" ")) {
			words.add(s);
			wordct++;
		}
		GMMessage msg = new GMMessage(testString, (byte)128, 123456);
		sys.processMessage(msg);
		sys.commit();
		System.out.println(wordct);
		for (String word : words) {
			System.out.println(word + " : " + sys.getTotalWordCount(word));
		}
	}
	private static final String[] messages = new String[] {"Jarvis. Message count me boy", "Jarvis, message cnt", "jvaris message count", "jarvis message count", "jarvis message number 1", "jarvis, remember when #thathappened?"};
	//@Test
	public void chatProcessor() throws UnsupportedEncodingException {
		JSONMessageResponse rsp = new JSONMessageResponse();
		JSONMessageResponse.Message msg = rsp.new Message();
		msg.senderName = "Brad";
		msg.system = false;
		msg.senderType = "user";
		ChatProcessor proc = new ChatProcessor("jarvis", new HashMap<Command, CommandHandler>());
		for (String s : messages) {
			msg.text = s;
			System.out.println(proc.process(msg));
		}
	}
	//@Test
	public void GMMessages() throws IOException{
		GroupMeConfig config = GroupMeConfig.fromConfigFile(new File("./data/gmconfig.txt"));
		GroupMeRequester receiver = new GroupMeRequester(config);
		JSONMessageResponse resp = receiver.getMessages("0");
	}
	
	@Test
	public void maintest() {
		GMAnalytics.main(null);
	}

}
