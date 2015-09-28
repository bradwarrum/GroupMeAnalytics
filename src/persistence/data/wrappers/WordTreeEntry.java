package persistence.data.wrappers;
import java.nio.ByteBuffer;

import persistence.caching.PageEntry;
import persistence.data.TreePointer;

public class WordTreeEntry implements AutoCloseable {

	private PageEntry backingEntry;
	private static final int VALUE_IND = 0;
	private static final int NEXT_IND = 1;
	private static final int CHILD_IND = NEXT_IND + Integer.BYTES;
	private static final int PARENT_IND = CHILD_IND + Integer.BYTES;
	private static final int FIRST_FREQ_IND = PARENT_IND + Integer.BYTES;
	public static final int ENTRY_SIZE = FIRST_FREQ_IND + Integer.BYTES;
	private static ByteBuffer intbuffer = ByteBuffer.allocate(ENTRY_SIZE);
	public WordTreeEntry(PageEntry backingEntry) {
		this.backingEntry = backingEntry;
	}
	
	private int getInt(int entryOffset) {
		intbuffer.clear();
		backingEntry.readData(intbuffer.array(), entryOffset, Integer.BYTES);
		return intbuffer.getInt();
	}
	
	private void putInt(int value, int entryOffset) {
		intbuffer.clear();
		intbuffer.putInt(value);
		backingEntry.writeData(intbuffer.array(), entryOffset, Integer.BYTES);
	}
	
	public byte value() {
		return backingEntry.readData(VALUE_IND);
	}
	
	public void value(byte val) {
		backingEntry.writeData(val, VALUE_IND);
	}
	
	public TreePointer next() {
		return new TreePointer(getInt(NEXT_IND), WordTree.ENTRIES_PER_PAGE);
	}
	
	public void next(int pointer) {
		putInt(pointer, NEXT_IND);
	}
	
	public void next(TreePointer pointer) {
		putInt(pointer.rawValue(), NEXT_IND);
	}
	
	public TreePointer child() {
		return new TreePointer(getInt(CHILD_IND), WordTree.ENTRIES_PER_PAGE);
	}
	
	public void child(int pointer) {
		putInt(pointer, CHILD_IND);
	}
	
	public void child(TreePointer pointer) {
		putInt(pointer.rawValue(), CHILD_IND);
	}
	
	public TreePointer parent() {
		return new TreePointer(getInt(PARENT_IND), WordTree.ENTRIES_PER_PAGE);
	}
	
	public void parent(int pointer) {
		putInt(pointer, PARENT_IND);
	}
	
	public void parent(TreePointer pointer) {
		putInt(pointer.rawValue(), PARENT_IND);
	}
	
	public TreePointer firstFrequency() {
		return new TreePointer(getInt(FIRST_FREQ_IND), FrequencyTable.ENTRIES_PER_PAGE);
	}
	
	public void firstFrequency(TreePointer offset) {
		putInt(offset.rawValue(), FIRST_FREQ_IND);
	}

	@Override
	public void close() throws Exception {
		backingEntry.close();
	}
	
	@Override
	public String toString() {
		return String.valueOf((char)value()) + " @ (" + String.valueOf(backingEntry.pageID()) + ", " + String.valueOf(backingEntry.entryIndex()) + ") NEXT: " + next().toString() + ", CHILD: " + child().toString() + ", PARENT: " + parent().toString();
	}
	
	public TreePointer self() {
		return new TreePointer(backingEntry.pageID(), backingEntry.entryIndex(), WordTree.ENTRIES_PER_PAGE);
	}
	
}