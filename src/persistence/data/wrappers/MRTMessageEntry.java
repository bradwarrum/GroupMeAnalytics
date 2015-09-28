package persistence.data.wrappers;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import persistence.caching.PageEntry;
import persistence.data.TreePointer;

public class MRTMessageEntry implements AutoCloseable{
	private PageEntry backingEntry;
	private static final int COUNT_IND = 0;
	private static final int MESSAGE_ID_IND = COUNT_IND + Short.BYTES;
	private static final short RESERVED_IND = MESSAGE_ID_IND + Integer.BYTES;
	public static final int MIN_ENTRY_SIZE = RESERVED_IND + Integer.BYTES;
	private static ByteBuffer intbuffer = ByteBuffer.allocate(MIN_ENTRY_SIZE);
	public MRTMessageEntry(PageEntry backingEntry) {
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
	
	private short getShort(int entryOffset) {
		intbuffer.clear();
		backingEntry.readData(intbuffer.array(), entryOffset, Short.BYTES);
		return intbuffer.getShort();
	}
	
	private void putInt(int value, int entryOffset) {
		intbuffer.clear();
		intbuffer.putInt(value);
		backingEntry.writeData(intbuffer.array(), entryOffset, Integer.BYTES);
	}
	
	private void putShort(short value, int entryOffset) {
		intbuffer.clear();
		intbuffer.putShort(value);
		backingEntry.writeData(intbuffer.array(), entryOffset, Short.BYTES);
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
	public TreePointer self() {
		return new TreePointer(backingEntry.pageID(), backingEntry.entryIndex(), MessageReferenceTable.ENTRIES_PER_PAGE);
	}
}
