import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

public class TriePage {
	public static final int PAGE_SIZE = 512;
	
	private int pageID, count;
	private int marker;
	private Boolean dirty;
	private TrieEntry[] entries;
	public TriePage(int pageID, int marker, ByteBuffer buffer) throws TrieFormatException {
		this.marker = marker;
		entries = new TrieEntry[TrieEntry.ENTRY_SIZE / PAGE_SIZE];
		if (buffer.remaining() != PAGE_SIZE) {
			throw new InvalidParameterException("Buffer does not have enough elements to construct a page.");
		}
		translate(pageID, buffer);
	}
	
	private void translate(int pageNum, ByteBuffer buffer) throws TrieFormatException {
		int id_ct = buffer.getInt();
		pageID = id_ct >> 8;
		count = id_ct & 0xFF;
		dirty = false;
		
		if (pageID != pageNum)
			throw new TrieFormatException("The page number does not match the page's location in the file.");
		if (count > entries.length)
			throw new TrieFormatException("Count parameter in page exceeds the number of entries per page.");
		
		for (int i = 0; i < count; i++) {
			entries[i] = new TrieEntry(this, buffer);
		}
	}
	
	public boolean writeback(ByteBuffer buffer) {
		if (!dirty) return false;
		int id_ct = pageID << 8 + count;
		buffer.putInt(id_ct);
		for (int i = 0; i < count; i++) {
			entries[i].writeback(buffer);
		}
		byte[] padding = new byte[buffer.remaining()];
		java.util.Arrays.fill(padding, (byte)0);
		buffer.put(padding);
		clearDirty();
		return true;
	}
	
	public void setDirty() {
		dirty = true;
	}
	
	public void clearDirty() {
		dirty = false;
	}
	
	public int getMarker() {
		return marker;
	}
	
	public void setMarker(int marker) {
		this.marker = marker;
	}
	
	public int getPageID() {
		return pageID;
	}
}
