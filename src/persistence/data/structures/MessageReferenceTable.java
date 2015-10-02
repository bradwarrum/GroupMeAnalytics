package persistence.data.structures;
import persistence.caching.PageEntry;
import persistence.data.models.MRTMessageEntry;
import persistence.data.models.MRTWordEntry;

public class MessageReferenceTable extends Tree {
	private static final int MAX_CACHED_PAGES = 1024;
	private static final int PAGE_SIZE = 1024;
	private static final int ENTRY_SIZE = Math.max(MRTWordEntry.MIN_ENTRY_SIZE, MRTMessageEntry.MIN_ENTRY_SIZE);
	public static final int ENTRIES_PER_PAGE = PAGE_SIZE / ENTRY_SIZE;
	
	public MessageReferenceTable(String mainFile, String rollbackFile) throws Exception {
		super(mainFile, rollbackFile, MAX_CACHED_PAGES, PAGE_SIZE, ENTRY_SIZE);
	}
	
	private MRTWordEntry getWordEntry(TreePointer pointer) throws Exception {
		PageEntry pe = getEntry(pointer);
		return (pe == null) ? null : new MRTWordEntry(pe);
	}
	
	private MRTWordEntry addWordEntry() throws Exception {
		PageEntry pe = addEntry();
		return (pe == null) ? null : new MRTWordEntry(pe);
	}
	
	private MRTMessageEntry getMessageEntry(TreePointer pointer) throws Exception {
		PageEntry pe = getEntry(pointer);
		return (pe == null) ? null : new MRTMessageEntry(pe);
	}
	
	private MRTMessageEntry addMessageEntry() throws Exception {
		PageEntry pe = addEntry();
		return (pe == null) ? null : new MRTMessageEntry(pe);
	}
	
	public TreePointer startMessage(int messageID) throws Exception {
		MRTMessageEntry newMessage = addMessageEntry();
		newMessage.messageID(messageID);
		TreePointer pointer = newMessage.self();
		newMessage.close();
		return pointer;
	}
	
	public void finishMessage(TreePointer lastWord) throws Exception {
		MRTWordEntry lastWordEntry = getWordEntry(lastWord);
		lastWordEntry.endOfSequence(true);
		MRTMessageEntry header = getMessageEntry(new TreePointer(lastWordEntry.self().rawValue() - lastWordEntry.messageOffset(), ENTRIES_PER_PAGE));
		header.count((short)(lastWordEntry.messageOffset() + 1));
		header.close();
		lastWordEntry.close();
	}
	
	public TreePointer addWord(short wordIndex, TreePointer currentHead) throws Exception{
		return addWord(wordIndex, currentHead, false);
	}
	
	public TreePointer addWord(short wordIndex, TreePointer currentHead, boolean endOfSequence) throws Exception {
		if (currentHead.rawValue() == 0) currentHead = null;
		MRTWordEntry entry = addWordEntry();
		MRTWordEntry head = (currentHead == null) ? null : getWordEntry(currentHead);
		if (endOfSequence) entry.endOfSequence(true);
		entry.messageOffset(wordIndex);
		if (head != null) {
			head.next(entry.self());
			head.close();
		}
		TreePointer entryPtr = entry.self();
		entry.close();
		return entryPtr;
	}
	
}
