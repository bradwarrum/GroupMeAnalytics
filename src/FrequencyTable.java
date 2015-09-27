
public class FrequencyTable extends Tree {

	private final static int MAX_CACHED_PAGES = 1024;
	private final static int PAGE_SIZE = 1024;
	private final static int ENTRY_SIZE = FrequencyTableEntry.MIN_ENTRY_SIZE;
	public final static int ENTRIES_PER_PAGE = PAGE_SIZE / ENTRY_SIZE;
	
	public FrequencyTable(String mainFile, String rollbackFile) throws Exception {
		super(mainFile, rollbackFile, MAX_CACHED_PAGES, PAGE_SIZE, ENTRY_SIZE);
	}
	
	private FrequencyTableEntry getFrequencyEntry(TreePointer pointer) throws Exception {
		PageEntry pe = getEntry(pointer);
		return (pe == null) ? null : new FrequencyTableEntry(pe);
	}
	
	private FrequencyTableEntry addFrequencyEntry() throws Exception {
		PageEntry pe = addEntry();
		return (pe == null) ? null : new FrequencyTableEntry(pe);
	}
	
	public TreePointer incrementCount(byte memberID, TreePointer firstFrequency) throws Exception {
		FrequencyTableEntry entry = (firstFrequency == null) ? null : getFrequencyEntry(firstFrequency);
		FrequencyTableEntry last = null;
		while (entry != null) {
			if (entry.memberID() == memberID) {
				entry.count(entry.count() + 1);
				TreePointer actualEntry = entry.self();
				entry.close();
				if (last != null) last.close();
				return actualEntry;
			} 
			if (last != null) last.close();
			last = entry;
			entry = getFrequencyEntry(entry.next());
		}
		entry = addFrequencyEntry();
		entry.count(1);
		if (last != null) {
			last.next(entry.self());
			last.close();
		}
		TreePointer actualEntry = entry.self();
		entry.close();
		return actualEntry;
	}
	
	public int getCount(byte memberID, TreePointer firstFrequency) throws Exception {
		if (firstFrequency == null || firstFrequency.rawValue() == 0) throw new IllegalArgumentException("Pointer must not be null");
		FrequencyTableEntry entry = getFrequencyEntry(firstFrequency);
		while (entry != null) {
			if (entry.memberID() == memberID) {
				int count = entry.count();
				entry.close();
				return count;
			}
			FrequencyTableEntry next = getFrequencyEntry(entry.next());
			entry.close();
			entry = next;
		}
		return 0;
	}
	
	public int getTotalCount(TreePointer firstFrequency) throws Exception {
		if (firstFrequency == null || firstFrequency.rawValue() == 0) throw new IllegalArgumentException("Pointer must not be null");
		FrequencyTableEntry entry = getFrequencyEntry(firstFrequency);
		int totalCount = 0;
		while (entry != null) {
			totalCount += entry.count();
			FrequencyTableEntry next = getFrequencyEntry(entry.next());
			entry.close();
			entry = next;
		}
		return totalCount;
	}
	
	public void setExternalPointer(TreePointer pointer, TreePointer externalPointer) throws Exception {
		if (pointer == null || pointer.rawValue() == 0) throw new IllegalArgumentException("Pointer must not be null");
		FrequencyTableEntry entry = getFrequencyEntry(pointer);
		entry.pointer(externalPointer);
		entry.close();
	}
	
	public TreePointer getExternalPointer(TreePointer pointer) throws Exception {
		if (pointer == null || pointer.rawValue() == 0) throw new IllegalArgumentException("Pointer must not be null");
		FrequencyTableEntry entry = getFrequencyEntry(pointer);
		TreePointer returnVal = entry.pointer();
		entry.close();
		return returnVal;
	}

}
