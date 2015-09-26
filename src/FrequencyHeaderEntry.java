import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public class FrequencyHeaderEntry implements AutoCloseable {
	private PageEntry backingEntry;
	private static final ByteBuffer intbuffer = ByteBuffer.allocate(Integer.BYTES * 2);
	private static final int PAGE_COUNT_IND = 0;
	private static final int FP_ENTRY_COUNT_IND = PAGE_COUNT_IND + Integer.BYTES;
	
	private int cachedPageCount = -1;
	private int cachedEntryCount = -1;

	public FrequencyHeaderEntry(PageEntry rawEntry) {
		backingEntry = rawEntry;
	}
	public int pageCount() {
		if (cachedPageCount > 0) return cachedPageCount;
		intbuffer.clear();
		backingEntry.readData(intbuffer.array(), PAGE_COUNT_IND, Integer.BYTES);
		cachedPageCount = intbuffer.getInt();
		return cachedPageCount;
	}

	public void pageCount(int newPageCount) {
		cachedPageCount = newPageCount;
		intbuffer.clear();
		intbuffer.putInt(newPageCount);
		backingEntry.writeData(intbuffer.array(), PAGE_COUNT_IND, Integer.BYTES);
	}

	public int finalPageEntryCount() {
		if (cachedEntryCount > 0) return cachedEntryCount;
		intbuffer.clear();
		backingEntry.readData(intbuffer.array(), FP_ENTRY_COUNT_IND, Integer.BYTES);
		return intbuffer.getInt();
	}

	public void finalPageEntryCount(int newEntryCount) {
		cachedEntryCount = newEntryCount;
		intbuffer.clear();
		intbuffer.putInt(newEntryCount);
		backingEntry.writeData(intbuffer.array(), FP_ENTRY_COUNT_IND, Integer.BYTES);
	}
	
	public void writeAll(int newPageCount, int newEntryCount) {
		intbuffer.clear();
		cachedPageCount = newPageCount;
		cachedEntryCount = newEntryCount;
		intbuffer.putInt(newPageCount);
		intbuffer.putInt(newEntryCount);
		backingEntry.writeData(intbuffer.array(), PAGE_COUNT_IND, Integer.BYTES * 2);
	}
	
	public int incrementPageCount() {
		pageCount(cachedPageCount + 1);
		return cachedPageCount;
	}
	
	public int incrementFinalPageEntryCount() {
		finalPageEntryCount(cachedEntryCount + 1);
		return cachedEntryCount;
	}

	@Override
	public void close() throws Exception {
		backingEntry.close();
		backingEntry = null;
	}
}