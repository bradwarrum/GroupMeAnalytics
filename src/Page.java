import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class Page {
	
	private final int PAGE_SIZE;
	
	private final int pageID;
	
	private byte[] data;
	
	public Page(int pageID, ByteBuffer initialData, int pageSize) {
		this.pageID = pageID;
		PAGE_SIZE = pageSize;
		
		data = new byte[PAGE_SIZE];
	
		initialData.get(data);
	}
	
	/**
	 * Fills the buffer with the contents of its internal byte array. Does not modify any parameters on the buffer.
	 * @param buffer The ByteBuffer to fill.
	*/
	public void fill(ByteBuffer buffer) {
		buffer.put(data);
	}
	
	public void writeData(byte[] array, int index, int pageOffset, int byteCount) {
		System.arraycopy(array, index, data, pageOffset, byteCount);
	}
	
	public void writeData(byte data, int index) {
		this.data[index] = data;
	}
	
	public void readData(byte[] array, int index, int pageOffset, int byteCount) {
		System.arraycopy(data, pageOffset, array, index, byteCount);
	}
	
	public byte readData(int index) {
		return this.data[index];
	}
	
	public int size() {
		return PAGE_SIZE;
	}
	
	public int getPageID() {
		return pageID;
	}
}
