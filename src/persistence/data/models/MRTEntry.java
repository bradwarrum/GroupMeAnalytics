package persistence.data.models;
import persistence.caching.PageEntry;
import persistence.data.structures.MessageReferenceTable;
import persistence.data.structures.TreePointer;

public class MRTEntry extends TreeEntry {
	private static final int NEXT_IND = 0;
	private static final int LOOKAHEAD_PTR_IND = NEXT_IND + Integer.BYTES;
	private static final int LOOKAHEAD_ID_IND = LOOKAHEAD_PTR_IND + Integer.BYTES;
	private static final int MESSAGE_ID_IND = LOOKAHEAD_ID_IND + Byte.BYTES;
	private static final int MEMBER_ID_IND = MESSAGE_ID_IND + Integer.BYTES;
	public static final int MIN_ENTRY_SIZE = MEMBER_ID_IND + Byte.BYTES;
	public MRTEntry(PageEntry backingEntry) {
		super(backingEntry);
	}
	
	public TreePointer next() {
		return new TreePointer(getInt(NEXT_IND), MessageReferenceTable.ENTRIES_PER_PAGE);
	}
	
	public void next(TreePointer next) {
		putInt(next.rawValue(), NEXT_IND);
	}
	
	public TreePointer lookaheadPointer() {
		return new TreePointer(getInt(LOOKAHEAD_PTR_IND), MessageReferenceTable.ENTRIES_PER_PAGE);
	}
	
	public void lookaheadPointer(TreePointer lookaheadPtr) {
		putInt(lookaheadPtr.rawValue(), LOOKAHEAD_PTR_IND);
	}
	
	public byte lookaheadID() {
		return getByte(LOOKAHEAD_ID_IND);
	}
	
	public void lookaheadID(byte id) {
		putByte(id, LOOKAHEAD_ID_IND);
	}
	
	public int messageID() {
		return getInt(MESSAGE_ID_IND);
	}
	
	public void messageID(int id) {
		putInt(id, MESSAGE_ID_IND);
	}
	
	public byte memberID() {
		return getByte(MEMBER_ID_IND);
	}
	
	public void memberID(byte id) {
		putByte(id, MEMBER_ID_IND);
	}
	
	@Override
	public TreePointer self() {
		return new TreePointer(pageID(), entryIndex(), MessageReferenceTable.ENTRIES_PER_PAGE);
	}
}
