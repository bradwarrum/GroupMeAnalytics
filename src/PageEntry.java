import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

public class PageEntry {
	private final int ENTRY_SIZE;
	private final int START_INDEX;
	private PageReference parent;
	private OnWriteEventHandler dirtyEvent;
	public PageEntry(OnWriteEventHandler dirtyEvent, PageReference parent, int startingIndex, int entrySize) {
		this.parent = parent;
		ENTRY_SIZE = entrySize;
		START_INDEX = startingIndex;
		parent.incrementRefCount();
		this.dirtyEvent = dirtyEvent;;
	}
	
	public void close() {
		if (parent != null) {
			parent.decrementRefCount();
			parent = null;
		}
	}
	
	public void writeData(byte[] data, int entryOffset, int count) {
		if (data.length < count) throw new IllegalArgumentException("Invalid buffer size");
		if (entryOffset + count > ENTRY_SIZE || entryOffset < 0) throw new IllegalArgumentException("Invalid entry offset");
		if (parent == null || parent.getPage() == null) throw new IllegalStateException();
		parent.getPage().writeData(data, 0, START_INDEX + entryOffset, count);		
		dirtyEvent.onWritePage(parent.getPage().getPageID());		
	}

	public void readData(byte[] data, int entryOffset, int count) {
		if (data.length < count) throw new IllegalArgumentException("Invalid buffer size");
		if (entryOffset + count > ENTRY_SIZE || entryOffset < 0) throw new IllegalArgumentException("Invalid entry offset");
		if (parent == null || parent.getPage() == null) throw new IllegalStateException();
		parent.getPage().readData(data, 0, START_INDEX + entryOffset, count);
	}

}
