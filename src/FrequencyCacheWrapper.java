import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class FrequencyCacheWrapper {
	public static int MAX_CACHED_PAGES = 8192;
	public static int PAGE_SIZE = 1024;
	public static int ENTRY_SIZE = FrequencyEntry.ENTRY_SIZE;
	public static int ENTRIES_PER_PAGE = PAGE_SIZE / ENTRY_SIZE;
		
	private PageCache pageCache;
	private FrequencyHeaderEntry header;
	public FrequencyCacheWrapper(String mainFile, String rollbackFile) throws Exception {
		pageCache = new PageCache(mainFile, rollbackFile, MAX_CACHED_PAGES, PAGE_SIZE, ENTRY_SIZE);
		pageCache.rollback();
		if (pageCache.numPages() == 0) {
			pageCache.truncateTo(0);
			pageCache.createPage();
			header = new FrequencyHeaderEntry(pageCache.entryAt(0, 0));
			header.writeAll(1, 1, 0);
		} else {
			//Keep page 0 in memory, always
			header = new FrequencyHeaderEntry(pageCache.entryAt(0, 0));
			pageCache.truncateTo(header.pageCount());
		}
		pageCache.commit();
	}
	
	public FrequencyEntry addEntry() throws Exception {
		int pageID = header.pageCount() - 1;
		if (pageID < 0) throw new AssertionError("File corruption");
		int entryIndex = header.finalPageEntryCount();
		if (header.finalPageEntryCount() == ENTRIES_PER_PAGE) {
			entryIndex = 0;
			header.finalPageEntryCount(1);
			pageID = pageCache.createPage();
			header.incrementPageCount();
		} else {
			header.incrementFinalPageEntryCount();
		}
		return new FrequencyEntry(pageCache.entryAt(pageID, entryIndex));
	}
	
	public FrequencyEntry getEntry(int pageID, int entryIndex) throws Exception {
		if (pageID == 0 && entryIndex == 0) return null;
		if (pageID < header.pageCount() || (pageID == header.pageCount() && entryIndex < header.finalPageEntryCount())) {
			return new FrequencyEntry(pageCache.entryAt(pageID, entryIndex));
		}
		throw new IllegalArgumentException("Entry does not exist in the tree");
	}
	
	public FrequencyEntry getEntry(TreePointer pointer) throws Exception {
		return getEntry(pointer.pageID(), pointer.entryIndex());
	}
	
	/**
	 * Reserves a word in the frequency tree, if it does not already exist.
	 * @param word The word to add to the frequency tree
	 * @throws Exception 
	 * @returns The pointer to the first element of the frequency table, or -1 if the word was not previously mapped in the tree
	 */
	public int mapWord(String word, int insertionPointer) throws Exception {
		if (!word.matches("[a-zA-z\']+")) return -1;
		byte[] letters = word.toLowerCase().getBytes(Charset.forName("US-ASCII"));
		boolean previouslyMapped = true;
		// Get root node
		TreePointer firstEntry 	= new TreePointer(header.firstEntry());
		FrequencyEntry parent  	= null;
		FrequencyEntry current 	= getEntry(firstEntry);
		FrequencyEntry prev 	= null;
		
		for (int i = 0; i < letters.length; i++) {
			byte b = letters[i];
			if (current == null) {
				previouslyMapped = false;
				current = addEntry();
				current.value(b);
				if (prev != null) {prev.next(current.self()); prev.close(); }
				if (parent != null && prev == null) {parent.child(current.self());}
				if (parent != null) { current.parent(parent.self()); parent.close(); }
				else if (prev == null) {
					header.firstEntry(current.self().rawValue());
				}
				parent = current;
				current = null;
				prev = null;
			} else {
				if (current.value() == b) {
					if (parent != null) {parent.close(); }
					if (prev != null) {prev.close();}
					parent = current;
					current = getEntry(current.child());
					prev = null;
				} else if (current.value() < b) {
					if (prev != null) {prev.close();}
					prev = current;					
					current = getEntry(current.next());
					i--;
				} else {
					previouslyMapped = false;
					FrequencyEntry insert = addEntry();
					insert.value(b);
					if (parent != null) {
						insert.parent(parent.self());
						if (prev == null) parent.child(insert.self());
						parent.close();
					} else {
						if (prev == null) 
							header.firstEntry(insert.self().rawValue());
					}
					if (prev != null) {
						prev.next(insert.self());
						prev.close();
					}
					insert.next(current.self());
					parent = insert;
					if (current != null) current.close();
					current = null;
					prev = null;
				}
			}
		}
		int returnVal;
		if (!previouslyMapped) {
			parent.firstFrequency(insertionPointer);
			returnVal = insertionPointer;
		} else {
			returnVal = parent.firstFrequency();
		}
		parent.close();
		if (current != null) {current.close();}
		//Prev must be null
		if (prev != null) throw new AssertionError("How is previous null here?");
		return -1;
		
		
	}
	
	public void commit() throws Exception {
		pageCache.commit();
	}
}
