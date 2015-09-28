package persistence.data.models;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import persistence.caching.PageEntry;
import persistence.data.structures.MessageReferenceTable;
import persistence.data.structures.TreePointer;

public class MRTMessageEntry extends TreeEntry{
	private static final int COUNT_IND = 0;
	private static final int MESSAGE_ID_IND = COUNT_IND + Short.BYTES;
	private static final short RESERVED_IND = MESSAGE_ID_IND + Integer.BYTES;
	public static final int MIN_ENTRY_SIZE = RESERVED_IND + Integer.BYTES;
	private static ByteBuffer intbuffer = ByteBuffer.allocate(MIN_ENTRY_SIZE);
	
	public MRTMessageEntry(PageEntry backingEntry) {
		super(backingEntry);
	}
	@Override
	protected ByteBuffer buffer() {
		return intbuffer;
	}
	
	public short count(){
		return getShort(COUNT_IND);
	}
	
	public void count(short count){
		putShort(count, COUNT_IND);
	}
	
	public int messageID(){
		return getInt(MESSAGE_ID_IND);
	}
	
	public void messageID(int id) {
		putInt(id, MESSAGE_ID_IND);
	}
	@Override
	public TreePointer self() {
		return new TreePointer(pageID(), entryIndex(), MessageReferenceTable.ENTRIES_PER_PAGE);
	}
}
