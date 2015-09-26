
public class TreePointer {
	private final int value;
	public static int convert(int pageID, int entryIndex) {
		return pageID * FrequencyCacheWrapper.PAGE_SIZE + entryIndex;
	}
	public TreePointer(int rawValue) {
		value = rawValue;
	}

	public TreePointer(int pageID, int entry) {
		value = pageID * FrequencyCacheWrapper.PAGE_SIZE + entry;
	}

	public int pageID() {
		return value / FrequencyCacheWrapper.PAGE_SIZE;
	}

	public int entryIndex() {
		return value & (FrequencyCacheWrapper.ENTRY_SIZE - 1);
	}

	public int rawValue() {
		return value;
	}
}	