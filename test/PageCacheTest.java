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
	//@Test
	public void initialPageCreation() throws Exception {
		PageCache pageCache = new PageCache("./testMain", "./testRollback", 100, 1024, 32);
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
	
	@Test
	public void freqCacheWrapper() throws Exception {
		FrequencyCacheWrapper wrapper = new FrequencyCacheWrapper("./fcwMain", "./fcwRollback");
		wrapper.mapWord("literallythelongestwordinthehistoryoftheworldbrooooooooooohhhhhhhhhhhhhhhhhhhhkalsdjjjjjjjjjjflaskdfj;lajsdf;lasjdf;lajsd;lfajsd;lfja;sdlfja;dsfj;adf");
		wrapper.commit();
	}

}
