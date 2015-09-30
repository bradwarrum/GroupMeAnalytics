package persistence.data.models;

import java.nio.ByteBuffer;

import persistence.caching.PageEntry;
import persistence.data.structures.TreePointer;

public abstract class TreeEntry implements AutoCloseable{
	private PageEntry backingEntry;
	private static final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	protected abstract TreePointer self();
	
	public TreeEntry(PageEntry backingEntry) {
		this.backingEntry = backingEntry;
	}
	
	protected byte getByte(int entryOffset) {
		return backingEntry.readData(entryOffset);
	}
	
	protected void putByte(byte value, int entryOffset) {
		backingEntry.writeData(value, entryOffset);
	}
	
	protected int getInt(int entryOffset) {
		buffer.clear();
		backingEntry.readData(buffer.array(), entryOffset, Integer.BYTES);
		return buffer.getInt();
	}
	
	protected short getShort(int entryOffset) {
		buffer.clear();
		backingEntry.readData(buffer.array(), entryOffset, Short.BYTES);
		return buffer.getShort();
	}
	
	protected void putInt(int value, int entryOffset) {
		buffer.clear();
		buffer.putInt(value);
		backingEntry.writeData(buffer.array(), entryOffset, Integer.BYTES);
	}
	
	protected void putShort(short value, int entryOffset) {
		buffer.clear();
		buffer.putShort(value);
		backingEntry.writeData(buffer.array(), entryOffset, Short.BYTES);
	}
	
	protected long getLong(int entryOffset) {
		buffer.clear();
		backingEntry.readData(buffer.array(), entryOffset, Long.BYTES);
		return buffer.getLong();
	}
	
	protected void putLong(long value, int entryOffset) {
		buffer.clear();
		buffer.putLong(value);
		backingEntry.writeData(buffer.array(), entryOffset, Long.BYTES);
	}
	protected int pageID() {
		return backingEntry.pageID();
	}
	protected int entryIndex() {
		return backingEntry.entryIndex();
	}
	@Override
	public void close() throws Exception {
		backingEntry.close();
		backingEntry = null;
	}
}
