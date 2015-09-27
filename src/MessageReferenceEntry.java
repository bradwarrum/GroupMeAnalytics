import java.nio.ByteBuffer;

public class MessageReferenceEntry implements AutoCloseable {
	private PageEntry backingEntry;
	private static final int MESSAGE_ID_IND = 0;
	private static final int NEXT_IND = MESSAGE_ID_IND + Byte.BYTES;
	private static final int TRAILING_IND = NEXT_IND + Integer.BYTES;
	public static final int MIN_ENTRY_SIZE = TRAILING_IND + Integer.BYTES;
	private static ByteBuffer intbuffer = ByteBuffer.allocate(MIN_ENTRY_SIZE);
	public MessageReferenceEntry(PageEntry backingEntry) {
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
	
	public int messageID() {
		return getInt(MESSAGE_ID_IND);
	}
	
	public void messageID(int messageID) {
		putInt(messageID, MESSAGE_ID_IND);
	}
	
	public TreePointer next() {
		return new TreePointer(getInt(NEXT_IND), MessageReferenceTable.ENTRIES_PER_PAGE);
	}
	
	public void next(TreePointer next) {
		putInt(next.rawValue(), NEXT_IND);
	}
	
	public TreePointer trailingWord() {
		return new TreePointer(getInt(TRAILING_IND), MessageReferenceTable.ENTRIES_PER_PAGE);
	}
	
	public void trailingWord(TreePointer trailingWord) {
		putInt(trailingWord.rawValue(), TRAILING_IND);
	}
	
	public TreePointer self() {
		return new TreePointer(backingEntry.pageID(), backingEntry.entryIndex(), MessageReferenceTable.ENTRIES_PER_PAGE);
	}
}
