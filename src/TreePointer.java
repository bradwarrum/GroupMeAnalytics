
public class TreePointer {
	private final int ENTRIES_PER_PAGE;
	private final int value;
	public static int convert(int pageID, int entryIndex, int entriesPerPage) {
		return pageID * entriesPerPage + entryIndex;
	}
	public TreePointer(int rawValue, int entriesPerPage) {
		ENTRIES_PER_PAGE = entriesPerPage;
		value = rawValue;
	}

	public TreePointer(int pageID, int entry, int entriesPerPage) {
		ENTRIES_PER_PAGE = entriesPerPage;
		value = pageID * ENTRIES_PER_PAGE + entry;
	}

	public int pageID() {
		return value / ENTRIES_PER_PAGE;
	}

	public int entryIndex() {
		return value % ENTRIES_PER_PAGE;
	}

	public int rawValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "(" + pageID() + ", " + entryIndex() + ")";
	}
}	