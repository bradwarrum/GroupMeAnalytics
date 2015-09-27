
public class TreePointer {
	private final static int ENTRIES_PER_PAGE = FrequencyCacheWrapper.ENTRIES_PER_PAGE;
	private final int value;
	public static int convert(int pageID, int entryIndex) {
		return pageID * ENTRIES_PER_PAGE + entryIndex;
	}
	public TreePointer(int rawValue) {
		value = rawValue;
	}

	public TreePointer(int pageID, int entry) {
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