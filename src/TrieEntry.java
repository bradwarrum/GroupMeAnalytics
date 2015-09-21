import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

public class TrieEntry {
	public static final int ENTRY_SIZE = 13;
	
	private TriePage parent;
	private byte value;
	private int next, child, pointer;
	public TrieEntry(TriePage parentPage, ByteBuffer buffer) {
		if (buffer.remaining() < ENTRY_SIZE)
			throw new InvalidParameterException("Not enough bytes in buffer to construct entry.");
		parent = parentPage;
		value = buffer.get();
		next = buffer.getInt();
		child = buffer.getInt();
		pointer = buffer.getInt();
	}
	
	public void writeback(ByteBuffer buffer) {
		buffer.put(value);
		buffer.putInt(next);
		buffer.putInt(child);
		buffer.putInt(pointer);
	}
	
	public int getNext() {
		return next;
	}
	
	public int getChild() {
		return child;
	}
	
	public int getPointer() {
		return pointer;
	}
	
	public byte getValue() {
		return value;
	}
	
	public void setValue(byte value) {
		this.value = value;
		parent.setDirty();
	}
	
	public void setChild(int child) {
		this.child = child;
		parent.setDirty();
	}
	
	public void setNext(int next) {
		this.next = next;
		parent.setDirty();
	}
	
	public void setPointer(int pointer) {
		this.pointer = pointer;
		parent.setDirty();
	}
}
