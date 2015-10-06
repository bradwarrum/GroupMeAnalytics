package persistence.caching;

public class PageEntry {
	private final int ENTRY_SIZE;
	private final int ENTRY_INDEX;
	private final int pageID;
	private PageReference parent;
	private OnWriteEventHandler dirtyEvent;
	public PageEntry(OnWriteEventHandler dirtyEvent, PageReference parent, int entryIndex, int entrySize) {
		this.parent = parent;
		this.pageID = parent.getPage().getPageID();
		ENTRY_SIZE = entrySize;
		ENTRY_INDEX = entryIndex;
		parent.incrementRefCount();
		this.dirtyEvent = dirtyEvent;
	}
	
	public void close() {
		if (parent != null) {
			parent.decrementRefCount();
			parent = null;
		}
	}
	
	public int pageID() {
		if (parent != null) {
			return pageID;
		}
		throw new IllegalStateException();
	}
	
	public int entryIndex() {
		if (parent != null) {
			return ENTRY_INDEX;
		}
		throw new IllegalStateException();
	}
	
	public void writeData(byte[] data, int entryOffset, int count) {
		if (data.length < count) throw new IllegalArgumentException("Invalid buffer size");
		if (entryOffset + count > ENTRY_SIZE || entryOffset < 0) throw new IllegalArgumentException("Invalid entry offset");
		if (parent == null || parent.getPage() == null) throw new IllegalStateException();
		parent.getPage().writeData(data, 0, (ENTRY_INDEX * ENTRY_SIZE) + entryOffset, count);		
		dirtyEvent.onWritePage(pageID);		
	}
	
	public void writeData(byte data, int entryOffset) {
		if (entryOffset >= ENTRY_SIZE || entryOffset < 0) throw new IllegalArgumentException("Invalid entry offset");
		if (parent == null || parent.getPage() == null) throw new IllegalStateException();
		parent.getPage().writeData(data, (ENTRY_INDEX * ENTRY_SIZE) + entryOffset);
		dirtyEvent.onWritePage(pageID);
	}

	public void readData(byte[] data, int entryOffset, int count) {
		if (data.length < count) throw new IllegalArgumentException("Invalid buffer size");
		if (entryOffset + count > ENTRY_SIZE || entryOffset < 0) throw new IllegalArgumentException("Invalid entry offset");
		if (parent == null || parent.getPage() == null) throw new IllegalStateException();
		parent.getPage().readData(data, 0, (ENTRY_INDEX * ENTRY_SIZE) + entryOffset, count);
	}
	
	public byte readData(int entryOffset) {
		if (entryOffset >= ENTRY_SIZE || entryOffset < 0) throw new IllegalArgumentException("Invalid entry offset");
		if (parent == null || parent.getPage() == null) throw new IllegalStateException();
		return parent.getPage().readData((ENTRY_INDEX * ENTRY_SIZE) + entryOffset);
	}
	
	/*@Override
	protected void finalize() {
		close();
	}*/

}
