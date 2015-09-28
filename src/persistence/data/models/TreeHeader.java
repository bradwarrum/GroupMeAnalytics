package persistence.data.models;
import java.nio.ByteBuffer;

import persistence.caching.PageEntry;
import persistence.data.structures.TreePointer;

public class TreeHeader extends TreeEntry {

	private static final int PAGE_COUNT_IND = 0;
	private static final int FP_ENTRY_COUNT_IND = PAGE_COUNT_IND + Integer.BYTES;
	private static final int FIRST_ENTRY_IND = FP_ENTRY_COUNT_IND + Short.BYTES;
	private static final int MIN_ENTRY_SIZE = FIRST_ENTRY_IND + Integer.BYTES;
	private static final ByteBuffer intbuffer = ByteBuffer.allocate(MIN_ENTRY_SIZE);
	private int cachedPageCount = -1;
	private short cachedEntryCount = -1;

	public TreeHeader(PageEntry rawEntry) {
		super(rawEntry);
	}
	public int pageCount() {
		if (cachedPageCount > 0) return cachedPageCount;
		cachedPageCount = getInt(PAGE_COUNT_IND);
		return cachedPageCount;
	}

	public void pageCount(int newPageCount) {
		cachedPageCount = newPageCount;
		putInt(newPageCount, PAGE_COUNT_IND);
	}

	public short finalPageEntryCount() {
		if (cachedEntryCount > 0) return cachedEntryCount;
		cachedEntryCount = getShort(FP_ENTRY_COUNT_IND);
		return cachedEntryCount;
	}

	public void finalPageEntryCount(short newEntryCount) {
		cachedEntryCount = newEntryCount;
		putShort(newEntryCount, FP_ENTRY_COUNT_IND);
	}
	
	public int firstEntry() {
		return getInt(FIRST_ENTRY_IND);
	}
	
	public void firstEntry(int entry) {
		putInt(entry, FIRST_ENTRY_IND);
	}
	
	public void writeAll(int newPageCount, short newEntryCount, int newFirstEntry) {
		cachedPageCount = newPageCount;
		cachedEntryCount = newEntryCount;
		putInt(newPageCount, PAGE_COUNT_IND);
		putShort(newEntryCount, FP_ENTRY_COUNT_IND);
		putInt(newFirstEntry, FIRST_ENTRY_IND);
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
	protected ByteBuffer buffer() {
		return intbuffer;
	}
	@Override
	protected TreePointer self() {
		return new TreePointer(0, 1);
	}
}