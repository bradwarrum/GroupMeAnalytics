package persistence.data.structures;
import java.util.HashMap;
import java.util.HashSet;

import com.sun.jndi.ldap.EntryChangeResponseControl;

import persistence.caching.PageEntry;
import persistence.data.models.FrequencyTableEntry;
import persistence.data.models.FrequencyTableHeader;

public class FrequencyTable extends Tree {

	private final static int MAX_CACHED_PAGES = 1024;
	private final static int PAGE_SIZE = 1024;
	private final static int ENTRY_SIZE = FrequencyTableEntry.MIN_ENTRY_SIZE;
	public final static byte MASTER_MEMBER = (byte)0xFF;
	public final static int ENTRIES_PER_PAGE = PAGE_SIZE / ENTRY_SIZE;
	
	private FrequencyTableHeader freqHeader;
	public FrequencyTable(String mainFile, String rollbackFile) throws Exception {
		super(mainFile, rollbackFile, MAX_CACHED_PAGES, PAGE_SIZE, ENTRY_SIZE);
		if (header.pageCount() == 1 && header.finalPageEntryCount() == 1) {
			freqHeader = new FrequencyTableHeader(addEntry());
		} else {
			freqHeader = new FrequencyTableHeader(getEntry(0, 1));
		}
	}
	
	private FrequencyTableEntry getFrequencyEntry(TreePointer pointer) throws Exception {
		PageEntry pe = getEntry(pointer);
		return (pe == null) ? null : new FrequencyTableEntry(pe);
	}
	
	private FrequencyTableEntry addFrequencyEntry() throws Exception {
		PageEntry pe = addEntry();
		return (pe == null) ? null : new FrequencyTableEntry(pe);
	}
	
	public TreePointer addMasterRecord() throws Exception {
		FrequencyTableEntry entry = addFrequencyEntry();
		entry.memberID(MASTER_MEMBER);
		TreePointer pointer = entry.self();
		entry.close();
		return pointer;
	}
	
	public TreePointer incrementCount(byte memberID, TreePointer firstFrequency) throws Exception {
		FrequencyTableEntry masterEntry = (firstFrequency == null) ? null : getFrequencyEntry(firstFrequency);
		if (masterEntry.memberID() != MASTER_MEMBER) {masterEntry.close(); throw new Exception("Pointer does not point to a master entry."); }
		FrequencyTableEntry entry = getFrequencyEntry(masterEntry.next());
		FrequencyTableEntry last = null;
		freqHeader.totalWordCount(freqHeader.totalWordCount() + 1);
		while (entry != null) {
			if (entry.memberID() == memberID) {
				entry.count(entry.count() + 1);
				masterEntry.count(masterEntry.count() + 1);
				TreePointer actualEntry = entry.self();
				entry.close();
				masterEntry.close();
				if (last != null) last.close();
				return actualEntry;
			} 
			if (last != null) last.close();
			last = entry;
			entry = getFrequencyEntry(entry.next());
		}
		entry = addFrequencyEntry();
		entry.count(1);
		entry.memberID(memberID);
		freqHeader.uniqueWordCount(freqHeader.uniqueWordCount() + 1);
		masterEntry.count(masterEntry.count() + 1);
		if (last != null) {
			last.next(entry.self());
			last.close();
		} else {
			masterEntry.next(entry.self());
		}
		TreePointer actualEntry = entry.self();
		entry.close();
		masterEntry.close();
		return actualEntry;
	}
	
	public int getCount(TreePointer masterRecord, byte memberID) throws Exception {
		if (masterRecord == null || masterRecord.rawValue() == 0) throw new IllegalArgumentException("Pointer must not be null");
		FrequencyTableEntry masterEntry = getFrequencyEntry(masterRecord);
		if (masterEntry == null) throw new Exception("Pointer does not correspond to a master record.");
		if (masterEntry.memberID() != MASTER_MEMBER) {
			masterEntry.close(); 
			throw new Exception("Pointer does not correspond to a master record.");
		}
		FrequencyTableEntry entry = getFrequencyEntry(masterEntry.next());
		int count = 0;
		while (entry != null) {
			if (entry.memberID() == memberID) {
				count = entry.count();
				entry.close();
				break;
			}
			FrequencyTableEntry next = getFrequencyEntry(entry.next());
			entry.close();
			entry = next;
		}
		masterEntry.close();
		return count;
	}
	
	public int getTotalCount(TreePointer masterRecord) throws Exception {
		if (masterRecord == null || masterRecord.rawValue() == 0) throw new IllegalArgumentException("Pointer must not be null");
		FrequencyTableEntry masterEntry = getFrequencyEntry(masterRecord);
		if (masterEntry == null) throw new Exception("Pointer does not correspond to a master record.");
		if (masterEntry.memberID() != MASTER_MEMBER) {
			masterEntry.close(); 
			throw new Exception("Pointer does not correspond to a master record.");
		}
		int totalCount = masterEntry.count();
		masterEntry.close();
		return totalCount;
	}
	
	public HashMap<Byte, Integer> getCounts(TreePointer masterRecord, HashSet<Byte>memberIDs) throws Exception {
		
		if (masterRecord == null || masterRecord.rawValue() == 0) throw new IllegalArgumentException("Pointer must not be null");
		FrequencyTableEntry masterEntry = getFrequencyEntry(masterRecord);
		if (masterEntry == null) throw new Exception("Pointer does not correspond to a master record.");
		if (masterEntry.memberID() != MASTER_MEMBER) {
			masterEntry.close();
			throw new Exception("Pointer does not correspond to a master record.");
		}
		HashMap<Byte, Integer> countMap = new HashMap<Byte, Integer>(memberIDs.size());
		FrequencyTableEntry entry = getFrequencyEntry(masterEntry.next());
		while (entry != null) {
			if (memberIDs.remove(entry.memberID())) {
				countMap.put(entry.memberID(), entry.count());
			}
			FrequencyTableEntry next = getFrequencyEntry(entry.next());
			entry.close();
			entry = next;
		}
		for (byte b : memberIDs) {
			countMap.put(b,  0);
		}
		memberIDs.clear();
		masterEntry.close();
		return countMap;
		
	}
	
	public long getTotalWordCount() {
		return freqHeader.totalWordCount();
	}
	
	public int getUniqueWordCount(){
		return freqHeader.uniqueWordCount();
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
