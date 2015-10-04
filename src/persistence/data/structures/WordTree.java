package persistence.data.structures;
import java.nio.charset.Charset;

import jdk.nashorn.internal.ir.annotations.Ignore;
import persistence.caching.PageEntry;
import persistence.data.models.WordTreeEntry;
import persistence.data.models.WordTreeHeader;

public class WordTree extends Tree {
	private static int MAX_CACHED_PAGES = 256; //Minimum is 5 so far
	private static int PAGE_SIZE = 1024;
	private static int ENTRY_SIZE = WordTreeEntry.ENTRY_SIZE;
	public static int ENTRIES_PER_PAGE = PAGE_SIZE / ENTRY_SIZE;
		
	private WordTreeHeader wordTreeHeader;
	public WordTree(String mainFile, String rollbackFile) throws Exception {
		super(mainFile, rollbackFile, MAX_CACHED_PAGES, PAGE_SIZE, ENTRY_SIZE);
		if (header.pageCount() == 1 && header.finalPageEntryCount() == 1) {
			wordTreeHeader = new WordTreeHeader(addEntry());
		} else {
			wordTreeHeader = new WordTreeHeader(getEntry(0, 1));
		}
	}
	
	private WordTreeEntry getWordTreeEntry(int pageID, int entryIndex) throws Exception {
		PageEntry pe = getEntry(pageID, entryIndex);
		return (pe == null) ? null : new WordTreeEntry(pe);
	}
	
	private WordTreeEntry getWordTreeEntry(TreePointer pointer) throws Exception {
		PageEntry pe = getEntry(pointer);
		return (pe == null) ? null : new WordTreeEntry(pe);
	}
	
	private WordTreeEntry addWordTreeEntry() throws Exception {
		PageEntry pe = addEntry();
		return (pe == null) ? null : new WordTreeEntry(pe);
	}
	
	private String sanitize(String word) {
		word = word.replace('’', '\'');
		word = word.replace('‘', '\'');
		return word;
	}
	
	/**
	 * Reserves a word in the frequency tree, if it does not already exist.
	 * @param word The word to add to the frequency tree
	 * @throws Exception 
	 * @returns The pointer to the final letter of the inserted word.
	 */
	@SuppressWarnings("resource")
	public TreePointer mapWord(String word) throws Exception {
		word = sanitize(word);		
		if (!word.matches("[a-zA-z\']+")) return null;
		byte[] letters = word.toLowerCase().getBytes(Charset.forName("US-ASCII"));
		// Get root node
		TreePointer firstEntry 	= wordTreeHeader.firstEntryInd();
		WordTreeEntry parent  	= null;
		WordTreeEntry current 	= getWordTreeEntry(firstEntry);
		WordTreeEntry prev 	= null;
		
		for (int i = 0; i < letters.length; i++) {
			byte b = letters[i];
			if (current == null) {
				current = addWordTreeEntry();
				current.value(b);
				current.childCount(1);
				if (prev != null) {prev.next(current.self()); prev.close(); }
				if (parent != null && prev == null) {parent.child(current.self());}
				if (parent != null) { current.parent(parent.self()); parent.close(); }
				else if (prev == null) {
					wordTreeHeader.firstEntryInd(current.self());
				}
				parent = current;
				current = null;
				prev = null;
			} else {
				if (current.value() == b) {
					current.childCount(current.childCount() + 1);
					if (parent != null) {parent.close(); }
					if (prev != null) {prev.close();}
					parent = current;
					current = getWordTreeEntry(current.child());
					prev = null;
				} else if (current.value() < b) {
					if (prev != null) {prev.close();}
					prev = current;					
					current = getWordTreeEntry(current.next());
					i--;
				} else {
					WordTreeEntry insert = addWordTreeEntry();
					insert.value(b);
					insert.childCount(1);
					if (parent != null) {
						insert.parent(parent.self());
						if (prev == null) parent.child(insert.self());
						parent.close();
					} else {
						if (prev == null) 
							wordTreeHeader.firstEntryInd(insert.self());
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
		TreePointer returnVal = parent.self();
		parent.close();
		if (current != null) {
			current.close();
		}
		//Prev must be null
		if (prev != null) throw new AssertionError("How is previous null here?");
		return returnVal;
	}
	
	@SuppressWarnings("resource")
	public TreePointer find(String word) throws Exception {
		word = sanitize(word);		
		if (!word.matches("[a-zA-z\']+")) return null;
		byte[] letters = word.toLowerCase().getBytes(Charset.forName("US-ASCII"));
		TreePointer firstEntry = wordTreeHeader.firstEntryInd();
		WordTreeEntry current = getWordTreeEntry(firstEntry);
		WordTreeEntry tmp;
		int i = 0;
		do {
			byte b = letters[i];			
			if (current == null) return null;
			if (current.value() == b) {
				if (i != letters.length - 1) {
					tmp = current;
					current = getWordTreeEntry(current.child());
					tmp.close();
				}
				i++;
			} else if (current.value() < b) {
				tmp = current;
				current = getWordTreeEntry(current.next());
				tmp.close();
			} else {
				current.close();
				return null;
			}
		} while ( i < letters.length);
		TreePointer retval = current.self();
		current.close();
		return retval;
	}
	
	@SuppressWarnings("resource")
	public boolean matches(TreePointer finalLetter, String word) throws Exception {
		word = sanitize(word);
		if (!word.matches("[a-zA-z\']+")) return false;
		byte[] letters = word.toLowerCase().getBytes(Charset.forName("US-ASCII"));
		if (letters.length == 0) return false;
		WordTreeEntry current = getWordTreeEntry(finalLetter);
		for (int i = letters.length - 1; i >= 0; i--) {
			if (current == null) return false;
			if (current.value() != letters[i]) {
				current.close();
				return false;
			}
			WordTreeEntry tmp = current;
			current = getWordTreeEntry(current.next());
			tmp.close();
		}
		if (current == null) {
			return true;
		} else {
			current.close();
			return false;
		}
	}
	
	public void modifyExternalPointer(TreePointer entry, TreePointer externalPointerVal) throws Exception {
		WordTreeEntry wte = getWordTreeEntry(entry);
		wte.firstFrequency(externalPointerVal);
		wte.close();
	}
	
	public TreePointer getExternalPointer(TreePointer entry) throws Exception {
		WordTreeEntry wte = getWordTreeEntry(entry);
		TreePointer pointer = wte.firstFrequency();
		pointer = (pointer.rawValue() == 0) ? null : pointer;
		wte.close();
		return pointer;
	}
}
