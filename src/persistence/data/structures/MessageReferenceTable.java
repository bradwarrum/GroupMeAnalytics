package persistence.data.structures;
import java.util.HashMap;
import java.util.List;

import com.sun.xml.internal.fastinfoset.util.FixedEntryStringIntMap;

import core.Options;
import jdk.nashorn.internal.runtime.FindProperty;
import persistence.GMMessage;
import persistence.caching.PageEntry;
import persistence.data.models.MRTEntry;

public class MessageReferenceTable extends Tree {
	private static final int MAX_CACHED_PAGES = Options.SHARED_MAX_CACHE;
	private static final int PAGE_SIZE = 1024;
	private static final int ENTRY_SIZE = MRTEntry.MIN_ENTRY_SIZE;
	public static final int ENTRIES_PER_PAGE = PAGE_SIZE / ENTRY_SIZE;


	public MessageReferenceTable(String mainFile, String rollbackFile) throws Exception {
		super(mainFile, rollbackFile, MAX_CACHED_PAGES, PAGE_SIZE, ENTRY_SIZE);
	}

	private MRTEntry getWordEntry(TreePointer pointer) throws Exception {
		PageEntry pe = getEntry(pointer);
		return (pe == null) ? null : new MRTEntry(pe);
	}

	private MRTEntry addWordEntry() throws Exception {
		PageEntry pe = addEntry();
		return (pe == null) ? null : new MRTEntry(pe);
	}

	public TreePointer addWord(int messageID, byte memberID, TreePointer currentHead) throws Exception{
		MRTEntry entry = addWordEntry();
		entry.memberID(memberID);
		entry.messageID(messageID);
		MRTEntry head = (currentHead.rawValue() == 0) ? null : getWordEntry(currentHead);
		if (head != null) {
			byte lookaheadID = head.lookaheadID();
			if (lookaheadID == 0) {
				entry.lookaheadID(Options.MAX_LOOKAHEAD_INDEX);
				entry.lookaheadPointer(head.self());
			} else {
				entry.lookaheadID((byte)(lookaheadID - 1));
				entry.lookaheadPointer(head.lookaheadPointer());
			}
			entry.next(head.self());
			head.close();
		}
		// Else, entries are already null / zero valued
		TreePointer entryPtr = entry.self();
		entry.close();
		return entryPtr;
	}

	public GMMessage getMessageData(TreePointer head, boolean next) throws Exception {
		if (head == null || head.rawValue() == 0) return null;
		MRTEntry entry = getWordEntry(head);
		if (entry == null) return null;		
		if (!next) {
			GMMessage message = new GMMessage(null, entry.memberID(), entry.messageID());
			entry.close();
			return message;
		}
		MRTEntry nextEntry = getWordEntry(entry.next());
		entry.close();
		if (nextEntry == null) return null;
		GMMessage message = new GMMessage(null, nextEntry.memberID(), nextEntry.messageID());
		nextEntry.close();
		return message;
	}

	private void cleanup(MRTEntry[] entries) throws Exception {
		for (MRTEntry entry : entries) {
			if (entry != null) {
				entry.close();
				entry = null;
			}
		}
	}

	private boolean checkForSequence(MRTEntry[] entries) {
		int rawPointer = entries[0].self().rawValue();
		for (int i = 1; i < entries.length; i++) {
			if (entries[i] == null) return false;
			if (entries[i].self().rawValue() != (rawPointer + 1)) return false;
			rawPointer = entries[i].self().rawValue();
		}
		return true;
	}

	private boolean checkForIDMatch(MRTEntry[] entries) {
		int messageID = entries[0].messageID();
		for (int i = 1; i < entries.length; i++) {
			if (entries[i] == null) return false;
			if (entries[i].messageID() != messageID) return false;
		}
		return true;
	}

	private int findGlobalMinimum(MRTEntry[] entries) {
		int minInd = 0;
		int minMessageID = Integer.MAX_VALUE;
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].messageID() < minMessageID) {
				minMessageID = entries[i].messageID();
				minInd = i;
			}
		}
		return minInd;
	}


	public GMMessage findSequence(TreePointer[] headPointers, boolean ignoreTrivialMatch) throws Exception {
		if (headPointers == null || headPointers.length == 0) return null;
		MRTEntry[] entries = new MRTEntry[headPointers.length];
		for (int i = 0; i < entries.length; i++) {
			entries[i] = getWordEntry(headPointers[i]);
			if (entries[i] == null) {
				cleanup(entries); return null;
			}
		}
		if (checkForIDMatch(entries) && checkForSequence(entries)) {
			if(ignoreTrivialMatch) {
				MRTEntry forceNext = getWordEntry(entries[0].next());
				if (forceNext == null) {cleanup(entries); return null;}
				entries[0].close();
				entries[0] = forceNext;
			} else {
				GMMessage message = new GMMessage(null, entries[0].memberID(), entries[0].messageID());
				cleanup(entries);
				return message;
			}
		}

		int index = findGlobalMinimum(entries);
		if (index == -1) {
			cleanup(entries); return null;
		}
		int minMessageID = entries[index].messageID();
		int numMatches = 0;

		do {
			index = (index + 1) % entries.length;
			// Take large jumps at first to save iterations on very frequent words
			do {
				if (entries[index].lookaheadPointer().rawValue() == 0) {break;}
				MRTEntry lookaheadEntry = getWordEntry(entries[index].lookaheadPointer());
				if (lookaheadEntry.messageID() >= minMessageID) {
					entries[index].close();
					entries[index] = lookaheadEntry;
				} else {
					break;
				}
			} while(true);

			do {
				if (entries[index].messageID() == minMessageID) {numMatches++; break;}
				if (entries[index].next().rawValue() == 0) {
					//This is the only failure case exit point from the loop, occurs when one of the next pointers hits a null.
					cleanup(entries); 
					return null;
				}
				MRTEntry nextEntry = getWordEntry(entries[index].next());
				entries[index].close();
				entries[index] = nextEntry;
				if (entries[index].messageID() < minMessageID) {
					numMatches = 0;
					minMessageID = entries[index].messageID();
					break;
				}
			} while (entries[index].messageID() >= minMessageID);

			if (numMatches == entries.length && checkForSequence(entries)) {
				GMMessage message = new GMMessage(null, entries[0].memberID(), entries[0].messageID());
				for (int i = 0; i < headPointers.length; i++) {
					headPointers[i] = entries[i].self();
				}
				cleanup(entries);
				return message;
			} else if (numMatches == entries.length) {
				FixThis;
			}
		} while (true);
	}

	public HashMap<Byte, Integer> findFrequencyForSequence(TreePointer[] headPointers) throws Exception {
		HashMap<Byte, Integer> freqMap = new HashMap<>();
		GMMessage seqOutput = null;
		boolean ignoreTrivial = false;
		do {
			seqOutput = findSequence(headPointers, ignoreTrivial);
			if (seqOutput != null) {
				Integer messageID = freqMap.get(seqOutput.memberID());
				if (messageID == null) {
					freqMap.put(seqOutput.memberID(), 1);
				} else {
					freqMap.put(seqOutput.memberID(), messageID + 1);
				}
			}
			ignoreTrivial = true;
		} while (seqOutput != null);
		return freqMap;
	}

}
