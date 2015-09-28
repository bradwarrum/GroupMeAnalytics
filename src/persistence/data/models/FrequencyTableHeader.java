package persistence.data.models;

import java.nio.ByteBuffer;

import persistence.caching.PageEntry;
import persistence.data.structures.FrequencyTable;
import persistence.data.structures.Tree;
import persistence.data.structures.TreePointer;

public class FrequencyTableHeader extends TreeEntry{
	private static final int TOTAL_WORD_COUNT = 0;
	private static final int UNIQUE_WORD_COUNT = TOTAL_WORD_COUNT + Long.BYTES;
	public static final int MIN_ENTRY_SIZE = UNIQUE_WORD_COUNT + Integer.BYTES;
	private static ByteBuffer intbuffer = ByteBuffer.allocate(MIN_ENTRY_SIZE);
	
	public FrequencyTableHeader(PageEntry backingEntry) {
		super(backingEntry);
	}
	
	@Override
	protected ByteBuffer buffer() {
		return intbuffer;
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
	@Override
	public TreePointer self() {
		return new TreePointer(pageID(), entryIndex(), FrequencyTable.ENTRIES_PER_PAGE);
	}

}
