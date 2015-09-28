package persistence.data;
import java.nio.ByteBuffer;

import persistence.caching.PageEntry;

@SuppressWarnings("unused")
public class TreeHeader implements AutoCloseable {
	private PageEntry backingEntry;
	private static final ByteBuffer intbuffer = ByteBuffer.allocate(Integer.BYTES * 2 + Short.BYTES);
	private static final int PAGE_COUNT_IND = 0;
	private static final int FP_ENTRY_COUNT_IND = PAGE_COUNT_IND + Integer.BYTES;
	private static final int FIRST_ENTRY_IND = FP_ENTRY_COUNT_IND + Short.BYTES;
	private static final int MIN_ENTRY_SIZE = FIRST_ENTRY_IND + Integer.BYTES;
	
	private int cachedPageCount = -1;
	private short cachedEntryCount = -1;

	public TreeHeader(PageEntry rawEntry) {
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

	public short finalPageEntryCount() {
		if (cachedEntryCount > 0) return cachedEntryCount;
		intbuffer.clear();
		backingEntry.readData(intbuffer.array(), FP_ENTRY_COUNT_IND, Short.BYTES);
		cachedEntryCount = intbuffer.getShort();
		return cachedEntryCount;
	}

	public void finalPageEntryCount(short newEntryCount) {
		cachedEntryCount = newEntryCount;
		intbuffer.clear();
		intbuffer.putShort(newEntryCount);
		backingEntry.writeData(intbuffer.array(), FP_ENTRY_COUNT_IND, Short.BYTES);
	}
	
	public int firstEntry() {
		intbuffer.clear();
		backingEntry.readData(intbuffer.array(), FIRST_ENTRY_IND, Integer.BYTES);
		return intbuffer.getInt();
	}
	
	public void firstEntry(int entry) {
		intbuffer.clear();
		intbuffer.putInt(entry);
		backingEntry.writeData(intbuffer.array(), FIRST_ENTRY_IND, Integer.BYTES);
	}
	
	public void writeAll(int newPageCount, short newEntryCount, int newFirstEntry) {
		intbuffer.clear();
		cachedPageCount = newPageCount;
		cachedEntryCount = newEntryCount;
		intbuffer.putInt(newPageCount);
		intbuffer.putShort(newEntryCount);
		intbuffer.putInt(newFirstEntry);
		backingEntry.writeData(intbuffer.array(), PAGE_COUNT_IND, MIN_ENTRY_SIZE);
	}
	
	public int incrementPageCount() {
		pageCount();
		pageCount(cachedPageCount + 1);
		return cachedPageCount;
	}
	
	public short incrementFinalPageEntryCount() {
		finalPageEntryCount();
		finalPageEntryCount((short)(cachedEntryCount + 1));
		return cachedEntryCount;
	}

	@Override
	public void close() throws Exception {
		backingEntry.close();
		backingEntry = null;
	}
}