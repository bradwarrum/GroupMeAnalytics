package persistence.data.models;

import persistence.caching.PageEntry;
import persistence.data.structures.TreePointer;
import persistence.data.structures.WordTree;

public class WordTreeHeader extends TreeEntry {

	private static final int FIRST_ENTRY_IND = 0;
	public static final int MIN_ENTRY_SIZE = FIRST_ENTRY_IND + Integer.BYTES;
	public WordTreeHeader(PageEntry backingEntry) {
		super(backingEntry);
	}
	
	public TreePointer firstEntryInd() {
		return new TreePointer(getInt(FIRST_ENTRY_IND), WordTree.ENTRIES_PER_PAGE);
	}
	
	public void firstEntryInd(TreePointer pointer) {
		putInt(pointer.rawValue(), FIRST_ENTRY_IND);
	}

	@Override
	protected TreePointer self() {
		return new TreePointer(pageID(), entryIndex(), WordTree.ENTRIES_PER_PAGE);
	}

}
