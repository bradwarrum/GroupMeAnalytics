package persistence.data.wrappers;
import java.nio.ByteBuffer;

import persistence.caching.PageEntry;
import persistence.data.TreePointer;

public class FrequencyTableEntry implements AutoCloseable{
	private PageEntry backingEntry;
	private static final int MEMBER_ID_IND = 0;
	private static final int COUNT_IND = MEMBER_ID_IND + Byte.BYTES;
	private static final int NEXT_IND = COUNT_IND + Integer.BYTES;
	private static final int LOOKUP_PTR_IND = NEXT_IND + Integer.BYTES;
	public static final int MIN_ENTRY_SIZE = LOOKUP_PTR_IND + Integer.BYTES;
	private static ByteBuffer intbuffer = ByteBuffer.allocate(MIN_ENTRY_SIZE);
	public FrequencyTableEntry(PageEntry backingEntry) {
		this.backingEntry = backingEntry;
	}
	@Override
	public void close() throws Exception {
		backingEntry.close();
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
	
	public byte memberID() {
		return backingEntry.readData(MEMBER_ID_IND);
	}
	
	public void memberID(byte newID) {
		backingEntry.writeData(newID, MEMBER_ID_IND);
	}
	
	public int count() {
		return getInt(COUNT_IND);
	}
	
	public void count(int count) {
		putInt(count, COUNT_IND);
	}
	
	public TreePointer next() {
		return new TreePointer(getInt(NEXT_IND), FrequencyTable.ENTRIES_PER_PAGE);
	}
	
	public void next(TreePointer next) {
		putInt(next.rawValue(), NEXT_IND);
	}
	
	public TreePointer pointer() {
		return new TreePointer(getInt(LOOKUP_PTR_IND), MessageReferenceTable.ENTRIES_PER_PAGE);
	}
	
	public void pointer(TreePointer pointer) {
		putInt(pointer.rawValue(), LOOKUP_PTR_IND);
	}
	
	public TreePointer self() {
		return new TreePointer(backingEntry.pageID(), backingEntry.entryIndex(), FrequencyTable.ENTRIES_PER_PAGE);
	}
}
