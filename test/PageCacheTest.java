import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PageCacheTest {
	@Test
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
	private static String testString = "The goal here is for you to maintain the level of concentration you normally experience when you read in untimed situations. When you�re really into a book or an article, the rest of the world fades away and you disappear into the page. Unfortunately, time constraints and the pressure of knowing you�re being tested make it difficult to maintain this kind of natural, high-level concentration. Reading actively in the manner we�ve described builds your concentration.";
	@Test
	public void freqCacheWrapper() throws Exception {
		FrequencyCacheWrapper wrapper = new FrequencyCacheWrapper("./data/fcwMain", "./data/fcwRollback");
	    testString = testString.replaceAll("[.,\"()?!*]", "");
	    String[] words = testString.split(" ");
	    int i = 128;
		for (String word : words) {
			if (word.equals("normally")) {
				System.out.println("Here");
			}
			wrapper.mapWord(word, i++);
		}
		wrapper.commit();
	}


}
