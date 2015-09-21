import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class TrieCache {
	private static final int MAX_CACHED_PAGES = 100;
	private static Random rand = new Random();
	
	private ByteBuffer buffer;
	private FileChannel channel;
	private HashMap<Integer, TriePage> pageCache = new HashMap<Integer, TriePage>();;
	private int[] markerMap = new int[MAX_CACHED_PAGES];
	private int numMarkers = 0;
	private HashSet<Integer> reservedPages;
	private int nextAvailablePage;

	
	public TrieCache(FileChannel channel) throws IOException {
		this.channel = channel;
		buffer = ByteBuffer.allocate(512);
		initialize();
	}
	
	private void initialize() throws IOException {
		if (channel.size() == 0) {
			//Write the initial page to the file
			buffer.clear();
			if (buffer.hasArray()) {
				java.util.Arrays.fill(buffer.array(), (byte)0);
			} else {
				byte[] bytes = new byte[buffer.capacity()];
				java.util.Arrays.fill(bytes, (byte) 0);
				buffer.put(bytes);
				buffer.flip();
			}
			if (channel.write(buffer, 0) != buffer.capacity()) {
				throw new IOException("Could not write page to file.");
			}
		}
		
	}
	
	private void evict() throws IOException, TrieFormatException {
		while(true) {
			int marker = rand.nextInt(numMarkers);
			int evictID = markerMap[marker];
			if (reservedPages.contains(evictID)) continue;
			writeback(evictID);
			numMarkers--;
			if (marker != numMarkers) {
				markerMap[marker] = markerMap[numMarkers];
			}
			break;
		}
	}
	
	private void bring(int pageID) throws TrieFormatException, IOException {
		if (pageCache.containsKey(pageID)) return;
		if (pageCache.size() == MAX_CACHED_PAGES) {
			evict();
		}
		
		if (TriePage.PAGE_SIZE != channel.read(buffer, pageID * TriePage.PAGE_SIZE)) {
			buffer.clear();
			throw new TrieFormatException("Could not read from the tree file at page ID " + String.valueOf(pageID));
		}
		buffer.flip();
		
		TriePage tp = new TriePage(pageID, numMarkers, buffer);
		markerMap[numMarkers++] = pageID;
		pageCache.put(pageID, tp);
		buffer.clear();
		
	}
	
	private void writeback(int pageID) throws IOException, TrieFormatException {
		TriePage page = pageCache.get(pageID);
		if (page == null) return;
		buffer.clear();
		page.writeback(buffer);
		buffer.flip();
		if (TriePage.PAGE_SIZE != channel.write(buffer, page.getPageID() * TriePage.PAGE_SIZE)) {
			buffer.clear();
			throw new TrieFormatException("Could not write to the tree file at page ID " + String.valueOf(page.getPageID()));
		}
	}
	
	public static void main(String[] args) throws IOException {
		FileChannel channel = FileChannel.open(Paths.get("./tree"), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);		
		TrieCache t = new TrieCache(channel);
	}
}
