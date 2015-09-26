import java.io.IOException;
import java.nio.ByteBuffer;

public class FrequencyCacheWrapper {
	private static int MAX_CACHED_PAGES = 8192;
	private static int PAGE_SIZE = 1024;
	private static int ENTRY_SIZE = 16;
		
	private PageCache pageCache;
	private FrequencyHeaderEntry header;
	public FrequencyCacheWrapper(String mainFile, String rollbackFile) throws Exception {
		pageCache = new PageCache(mainFile, rollbackFile, MAX_CACHED_PAGES, PAGE_SIZE, ENTRY_SIZE);
		pageCache.rollback();
		if (pageCache.numPages() == 0) {
			pageCache.truncateTo(0);
			pageCache.createPage();
			header = new FrequencyHeaderEntry(pageCache.entryAt(0, 0));
			header.writeAll(1, 1);
		} else {
			//Keep page 0 in memory, always
			header = new FrequencyHeaderEntry(pageCache.entryAt(0, 0));
			pageCache.truncateTo(header.pageCount());
		}
		pageCache.commit();
	}
}
