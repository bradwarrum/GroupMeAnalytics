package persistence.data.wrappers;
import java.nio.ByteBuffer;

import persistence.caching.PageEntry;
import persistence.data.TreePointer;

public class MRTWordEntry implements AutoCloseable {
	private PageEntry backingEntry;
	private static final int HEADER_IND = 0;
	private static final short EOS_MASK = (short)0x8000;
	private static final short MESSAGE_OFFSET_MASK = 0x7FFF;
	private static final int NEXT_IND = HEADER_IND + Short.BYTES;
	private static final short WORD_REF_IND = NEXT_IND + Integer.BYTES;
	public static final int MIN_ENTRY_SIZE = WORD_REF_IND + Integer.BYTES;
	private static ByteBuffer intbuffer = ByteBuffer.allocate(MIN_ENTRY_SIZE);
	public MRTWordEntry(PageEntry backingEntry) {
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
	
	public boolean endOfSequence(){
		return ((getShort(HEADER_IND) & EOS_MASK) != 0);
	}
	
	public void endOfSequence(boolean value) {
		short header = getShort(HEADER_IND);
		if (value) {
			header |= EOS_MASK;
		} else {
			header &= ~EOS_MASK;
		}
		putShort(header, HEADER_IND);
	}
	
	public short messageOffset(){
		return (short)(getShort(HEADER_IND) & MESSAGE_OFFSET_MASK);
	}
	
	public void messageOffset(short offset) {
		short header = getShort(HEADER_IND);
		header &= MESSAGE_OFFSET_MASK;
		header |= offset;
		putShort(header, HEADER_IND);
	}
	
	public TreePointer next() {
		return new TreePointer(getInt(NEXT_IND), MessageReferenceTable.ENTRIES_PER_PAGE);
	}
	
	public void next(TreePointer next) {
		putInt(next.rawValue(), NEXT_IND);
	}
	
	public TreePointer word(){
		return new TreePointer(getInt(WORD_REF_IND), WordTree.ENTRIES_PER_PAGE);
	}

	public void word(TreePointer word) {
		putInt(word.rawValue(), WORD_REF_IND);
	}
	public TreePointer self() {
		return new TreePointer(backingEntry.pageID(), backingEntry.entryIndex(), MessageReferenceTable.ENTRIES_PER_PAGE);
	}
}
