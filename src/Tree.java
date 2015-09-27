
public abstract class Tree {
	private final int PAGE_SIZE;
	private final int ENTRY_SIZE;
	private final int ENTRIES_PER_PAGE;
	
	protected final PageCache pageCache;
	protected final TreeHeader header;
	public Tree(String mainFile, String rollbackFile, int maxCachedPages, int pageSize, int entrySize) throws Exception {
		PAGE_SIZE = pageSize;
		ENTRY_SIZE = entrySize;
		ENTRIES_PER_PAGE = PAGE_SIZE / ENTRY_SIZE;
		pageCache = new PageCache(mainFile, rollbackFile, maxCachedPages, pageSize, entrySize);
		pageCache.rollback();
		if (pageCache.numPages() == 0) {
			pageCache.truncateTo(0);
			pageCache.createPage();
			header = new TreeHeader(pageCache.entryAt(0, 0));
			header.writeAll(1, 1, 0);
		} else {
			//Keep page 0 in memory, always
			header = new TreeHeader(pageCache.entryAt(0, 0));
			pageCache.truncateTo(header.pageCount());
		}
		pageCache.commit();
	}
	
	protected final TreePointer nextAllocationPtr() {
		int pageID = header.pageCount() - 1;
		int entryID = header.finalPageEntryCount();
		if (entryID == ENTRIES_PER_PAGE) {
			entryID = 0;
			pageID++;
		}
		return new TreePointer(pageID, entryID, ENTRIES_PER_PAGE);
	}
	
	protected final PageEntry addEntry() throws Exception {
		int pageID = header.pageCount() - 1;
		if (pageID < 0) throw new AssertionError("File corruption");
		int entryIndex = header.finalPageEntryCount();
		if (header.finalPageEntryCount() == ENTRIES_PER_PAGE) {
			entryIndex = 0;
			header.finalPageEntryCount(1);
			pageID = pageCache.createPage();
			header.incrementPageCount();
		} else {
			header.incrementFinalPageEntryCount();
		}
		return pageCache.entryAt(pageID, entryIndex);
	}
	
	protected final PageEntry getEntry(int pageID, int entryIndex) throws Exception {
		if (pageID == 0 && entryIndex == 0) return null;
		if (pageID < header.pageCount() || (pageID == header.pageCount() && entryIndex < header.finalPageEntryCount())) {
			return pageCache.entryAt(pageID, entryIndex);
		}
		throw new IllegalArgumentException("Entry does not exist in the tree");
	}
	
	protected final PageEntry getEntry(TreePointer pointer) throws Exception {
		return getEntry(pointer.pageID(), pointer.entryIndex());
	}
	
	public void commit() throws Exception {
		pageCache.commit();
	}
}
