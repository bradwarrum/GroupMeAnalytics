import java.util.HashSet;

import org.junit.Test;

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
	@Test
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


}
