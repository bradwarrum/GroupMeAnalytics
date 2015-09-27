
public class MessageReferenceTable extends Tree {
	private static final int MAX_CACHED_PAGES = 1024;
	private static final int PAGE_SIZE = 1024;
	private static final int ENTRY_SIZE = MessageReferenceEntry.MIN_ENTRY_SIZE;
	public static final int ENTRIES_PER_PAGE = PAGE_SIZE / ENTRY_SIZE;
	
	public MessageReferenceTable(String mainFile, String rollbackFile) throws Exception {
		super(mainFile, rollbackFile, MAX_CACHED_PAGES, PAGE_SIZE, ENTRY_SIZE);
	}
	
}
