package persistence.data.wrappers;
import persistence.caching.PageEntry;
import persistence.data.Tree;
import persistence.data.TreePointer;

public class MessageReferenceTable extends Tree {
	private static final int MAX_CACHED_PAGES = 1024;
	private static final int PAGE_SIZE = 1024;
	private static final int ENTRY_SIZE = MessageReferenceEntry.MIN_ENTRY_SIZE;
	public static final int ENTRIES_PER_PAGE = PAGE_SIZE / ENTRY_SIZE;
	
	public MessageReferenceTable(String mainFile, String rollbackFile) throws Exception {
		super(mainFile, rollbackFile, MAX_CACHED_PAGES, PAGE_SIZE, ENTRY_SIZE);
	}
	
	private MessageReferenceEntry getReferenceEntry(TreePointer pointer) throws Exception {
		PageEntry pe = getEntry(pointer);
		return (pe == null) ? null : new MessageReferenceEntry(pe);
	}
	
	private MessageReferenceEntry addReferenceEntry() throws Exception {
		PageEntry pe = addEntry();
		return (pe == null) ? null : new MessageReferenceEntry(pe);
	}
	
	public TreePointer pushMessageReference(int messageID, TreePointer currentHead) throws Exception {
		MessageReferenceEntry newHead = addReferenceEntry();
		newHead.messageID(messageID);
		newHead.next(currentHead);
		TreePointer headPtr = newHead.self();
		newHead.close();
		return headPtr;
	}
	
	public void setTrailingReference(TreePointer forEntry, TreePointer externalRef) throws Exception {
		if (forEntry == null || forEntry.rawValue() == 0) throw new IllegalArgumentException("Pointer cannot be null");
		MessageReferenceEntry entry = getReferenceEntry(forEntry);
		entry.trailingWord(externalRef);
		entry.close();
	}
	
	public TreePointer getTrailingReference(TreePointer forEntry) throws Exception {
		if (forEntry == null || forEntry.rawValue() == 0) throw new IllegalArgumentException("Pointer cannot be null");
		MessageReferenceEntry entry = getReferenceEntry(forEntry);
		TreePointer ptr = entry.trailingWord();
		entry.close();
		return ptr;
	}
}
