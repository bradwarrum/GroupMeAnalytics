package persistence.data.models;
import java.nio.ByteBuffer;

import persistence.caching.PageEntry;
import persistence.data.structures.FrequencyTable;
import persistence.data.structures.MessageReferenceTable;
import persistence.data.structures.TreePointer;

public class FrequencyTableEntry extends TreeEntry{
	private static final int MEMBER_ID_IND = 0;
	private static final int COUNT_IND = MEMBER_ID_IND + Byte.BYTES;
	private static final int NEXT_IND = COUNT_IND + Integer.BYTES;
	private static final int LOOKUP_PTR_IND = NEXT_IND + Integer.BYTES;
	public static final int MIN_ENTRY_SIZE = LOOKUP_PTR_IND + Integer.BYTES;
	private static ByteBuffer intbuffer = ByteBuffer.allocate(MIN_ENTRY_SIZE);
	
	public FrequencyTableEntry(PageEntry backingEntry) {
		super(backingEntry);
	}
	
	public byte memberID() {
		return getByte(MEMBER_ID_IND);
	}
	
	public void memberID(byte newID) {
		putByte(newID, MEMBER_ID_IND);
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
		return new TreePointer(pageID(), entryIndex(), FrequencyTable.ENTRIES_PER_PAGE);
	}

	@Override
	protected ByteBuffer buffer() {
		return intbuffer;
	}
}
