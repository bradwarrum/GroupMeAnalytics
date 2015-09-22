import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

public class PageEntry {
	private final int ENTRY_SIZE;
	private final int START_INDEX;
	private PageReference parent;
	public PageEntry(PageReference parent, int startingIndex, int entrySize) {
		this.parent = parent;
		ENTRY_SIZE = entrySize;
		START_INDEX = startingIndex;
		parent.incrementRefCount();
	}
	
	public void free() {
		if (parent != null) {
			parent.decrementRefCount();
			parent = null;
		}
	}
	
	public void writeData(byte[] data, int startIndex) {
		if (parent == null || parent.getPage() == null) throw new IllegalStateException();
		parent.getPage().writeData(data, startIndex, START_INDEX, ENTRY_SIZE);
	}
	
	public void readData(byte[] data, int startIndex) {
		if (parent == null || parent.getPage() == null) throw new IllegalStateException();
		parent.getPage().readData(data, startIndex, START_INDEX, ENTRY_SIZE);
	}

}
