package persistence.data.wrappers;

import java.nio.ByteBuffer;

import persistence.caching.PageEntry;
import persistence.data.TreePointer;

public class FrequencyTableHeader implements AutoCloseable{
	private PageEntry backingEntry;
	private static final int TOTAL_WORD_COUNT = 0;
	private static final int UNIQUE_WORD_COUNT = TOTAL_WORD_COUNT + Long.BYTES;
	public static final int MIN_ENTRY_SIZE = UNIQUE_WORD_COUNT + Integer.BYTES;
	private static ByteBuffer intbuffer = ByteBuffer.allocate(MIN_ENTRY_SIZE);
	public FrequencyTableHeader(PageEntry backingEntry) {
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
	
	private long getLong(int entryOffset) {
		intbuffer.clear();
		backingEntry.readData(intbuffer.array(), entryOffset, Long.BYTES);
		return intbuffer.getLong();
	}
	
	private void putLong(long value, int entryOffset) {
		intbuffer.clear();
		intbuffer.putLong(value);
		backingEntry.writeData(intbuffer.array(), entryOffset, Long.BYTES);
	}
	
	public long totalWordCount(){
		return getLong(TOTAL_WORD_COUNT);
	}
	
	public void totalWordCount(long count) {
		putLong(count, TOTAL_WORD_COUNT);
	}
	
	public int uniqueWordCount(){
		return getInt(UNIQUE_WORD_COUNT);
	}
	
	public void uniqueWordCount(int count){
		putInt(count, UNIQUE_WORD_COUNT);
	}
	
	public TreePointer self() {
		return new TreePointer(backingEntry.pageID(), backingEntry.entryIndex(), FrequencyTable.ENTRIES_PER_PAGE);
	}
}
