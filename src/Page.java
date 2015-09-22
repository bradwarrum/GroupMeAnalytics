import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class Page {
	
	private final int PAGE_SIZE;
	
	private final int pageID;
	
	private Boolean dirty;
	private byte[] data;
	
	public Page(int pageID, ByteBuffer initialData, int pageSize) {
		this.pageID = pageID;
		PAGE_SIZE = pageSize;
		
		data = new byte[PAGE_SIZE];
		dirty = false;
	
		initialData.get(data);
	}
	
	/**
	 * Fills the buffer with the contents of its internal byte array. Does not modify any parameters on the buffer.
	 * @param buffer The ByteBuffer to fill.
	 * @return True if the buffer contains information to write back, false otherwise.
	 */
	public boolean fill(ByteBuffer buffer) {
		if (!dirty) return false;
		buffer.put(data);
		return true;
	}
	
	public void writeData(byte[] array, int index, int pageOffset, int byteCount) {
		System.arraycopy(array, index, data, pageOffset, byteCount);
		setDirty();
	}
	
	public void readData(byte[] array, int index, int pageOffset, int byteCount) {
		System.arraycopy(data, pageOffset, array, index, byteCount);
	}

	private void setDirty() {
		dirty = true;
	}
	
	public void clearDirty() {
		dirty = false;
	}
	
	public int size() {
		return PAGE_SIZE;
	}
	
	public int getPageID() {
		return pageID;
	}
}
