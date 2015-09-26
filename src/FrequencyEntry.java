import java.nio.ByteBuffer;

public class FrequencyEntry implements AutoCloseable {

	private PageEntry backingEntry;
	private static final int VALUE_IND = 0;
	private static final int NEXT_IND = 1;
	private static final int CHILD_IND = NEXT_IND + Integer.BYTES;
	private static final int PARENT_IND = CHILD_IND + Integer.BYTES;
	private static final int FIRST_FREQ_IND = PARENT_IND + Integer.BYTES;
	public static final int ENTRY_SIZE = FIRST_FREQ_IND + Integer.BYTES;
	private static ByteBuffer intbuffer = ByteBuffer.allocate(ENTRY_SIZE);
	public FrequencyEntry(PageEntry backingEntry) {
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
		return new TreePointer(getInt(NEXT_IND));
	}
	
	public void next(int pointer) {
		putInt(pointer, NEXT_IND);
	}
	
	public void next(TreePointer pointer) {
		putInt(pointer.rawValue(), NEXT_IND);
	}
	
	public TreePointer child() {
		return new TreePointer(getInt(CHILD_IND));
	}
	
	public void child(int pointer) {
		putInt(pointer, CHILD_IND);
	}
	
	public void child(TreePointer pointer) {
		putInt(pointer.rawValue(), CHILD_IND);
	}
	
	public TreePointer parent() {
		return new TreePointer(getInt(PARENT_IND));
	}
	
	public void parent(int pointer) {
		putInt(pointer, PARENT_IND);
	}
	
	public void parent(TreePointer pointer) {
		putInt(pointer.rawValue(), PARENT_IND);
	}
	
	public int firstFrequency() {
		return getInt(FIRST_FREQ_IND);
	}
	
	public void firstFrequency(int offset) {
		putInt(offset, FIRST_FREQ_IND);
	}

	@Override
	public void close() throws Exception {
		backingEntry.close();
	}
	
	public TreePointer self() {
		return new TreePointer(backingEntry.pageID(), backingEntry.entryIndex());
	}
	
}
