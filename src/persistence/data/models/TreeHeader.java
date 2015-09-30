package persistence.data.models;
import persistence.caching.PageEntry;
import persistence.data.structures.TreePointer;

public class TreeHeader extends TreeEntry {

	private static final int PAGE_COUNT_IND = 0;
	private static final int FP_ENTRY_COUNT_IND = PAGE_COUNT_IND + Integer.BYTES;
	private static final int MIN_ENTRY_SIZE = FP_ENTRY_COUNT_IND + Short.BYTES;
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

	
	public void writeAll(int newPageCount, short newEntryCount) {
		cachedPageCount = newPageCount;
		cachedEntryCount = newEntryCount;
		putInt(newPageCount, PAGE_COUNT_IND);
		putShort(newEntryCount, FP_ENTRY_COUNT_IND);
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
	protected TreePointer self() {
		return new TreePointer(0, 1);
	}
}