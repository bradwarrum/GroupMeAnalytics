package persistence;
import java.util.HashMap;

import persistence.data.structures.FrequencyTable;
import persistence.data.structures.MessageReferenceTable;
import persistence.data.structures.TreePointer;
import persistence.data.structures.WordTree;

public class FrequencySystem {
	private WordTree wordTree;
	private FrequencyTable freqTable;
	private MessageReferenceTable msgRefs;
	
	public FrequencySystem() throws Exception {
		wordTree = new WordTree("./data/words", "./data/words.rbjf");
		freqTable = new FrequencyTable("./data/freq", "./data/freq.rbjf");	
		msgRefs = new MessageReferenceTable("./data/msgref", "./data/msgref.rbjf");
	}
	
	public void destroy() throws Exception{
		wordTree.truncate();
		freqTable.truncate();
		msgRefs.truncate();
		wordTree = new WordTree("./data/words", "./data/words.rbjf");
		freqTable = new FrequencyTable("./data/freq", "./data/freq.rbjf");
		msgRefs = new MessageReferenceTable("./data/msgref", "./data/msgref.rbjf");		
	}
	private final static String WHITESPACE = "[\\s.,\"()?!*;:# ]";
	private static String sanitizeInput(String input) {
		return input.replaceAll(WHITESPACE + "{2,}", " ").replaceAll("^" + WHITESPACE, "").replaceAll(WHITESPACE + "$", "").trim().toLowerCase();
	}
	
	public void processMessage(GMMessage message) throws Exception {
		if (message.message() == null) return;
		String[] words = sanitizeInput(message.message()).split(" ");
		for (String word : words) {
			TreePointer wordTreeEntry = wordTree.mapWord(word);
			if (wordTreeEntry == null) {
				continue;
			}
			TreePointer freqTableMaster = wordTree.getExternalPointer(wordTreeEntry);		
			if (freqTableMaster == null) {
				freqTableMaster = freqTable.addMasterRecord();
				wordTree.modifyExternalPointer(wordTreeEntry, freqTableMaster);
			}

			TreePointer freqTableMember = freqTable.incrementCount(message.memberID(), freqTableMaster);
			TreePointer msgRefHead = freqTable.getExternalPointer(freqTableMaster);

			msgRefHead = msgRefs.addWord(message.messageID(), message.memberID(), msgRefHead);
			
			//freqTable.setExternalPointer(freqTableMember, msgRefHead);
			freqTable.setExternalPointer(freqTableMaster, msgRefHead);
		}
	}
	
	public int getLargestMessageIDRecorded() throws Exception{
		return msgRefs.largestMessageID();
	}
	
	public long getTotalWordCount() {
		return freqTable.getTotalWordCount();
	}
	
	public int getUniqueWordCount() {
		return freqTable.getUniqueWordCount();
	}
	public int getTotalWordCount(String word) throws Exception {
		word = sanitizeInput(word);
		TreePointer wordTreeEntry = wordTree.find(word);
		if (wordTreeEntry == null) return 0;
		TreePointer freqPtr = wordTree.getExternalPointer(wordTreeEntry);
		if (freqPtr == null) return 0;
		return freqTable.getTotalCount(freqPtr);
	}
	
	public HashMap<Byte, Integer> getPhraseCountAllUsers(String phrase) throws Exception {
		phrase = sanitizeInput(phrase);
		if (phrase.contains(" ")) {
			String[] words = phrase.split(" ");
			if (words.length > 5) return null;
			return getPhraseCountAllUsers(words);
		}
		
		TreePointer wordTreeEntry = wordTree.find(phrase);
		if (wordTreeEntry == null) return null;
		TreePointer freqPtr = wordTree.getExternalPointer(wordTreeEntry);
		if (freqPtr == null) return null;
		return freqTable.getAllMemberCounts(freqPtr);
	}
	
	public int getWordCount(String word, byte memberID) throws Exception {
		word = sanitizeInput(word);		
		TreePointer wordTreeEntry = wordTree.find(word);
		if (wordTreeEntry == null) return 0;
		TreePointer freqPtr = wordTree.getExternalPointer(wordTreeEntry);
		if (freqPtr == null) return 0;
		return freqTable.getCount(freqPtr, memberID);
	}

	private TreePointer getMessageRefEntry(String word) throws Exception {
		TreePointer wordTreeEntry = wordTree.find(word);
		if (wordTreeEntry == null) return null;
		TreePointer freqEntry = wordTree.getExternalPointer(wordTreeEntry);
		if (freqEntry == null) return null;
		TreePointer msgRefEntry = freqTable.getExternalPointer(freqEntry);
		return msgRefEntry;
	}
	
	private HashMap<Byte, Integer> getPhraseCountAllUsers(String[] words) throws Exception {
		TreePointer[] messageRefHeads = new TreePointer[words.length];
		for (int i = 0; i < words.length; i++) {
			messageRefHeads[i] = getMessageRefEntry(words[i]);
			if (messageRefHeads[i] == null) return null;
		}
		return msgRefs.findFrequencyForSequence(messageRefHeads);
	}
	
	public void commit() throws Exception {
		//TODO: Make this atomic
		wordTree.commit();
		freqTable.commit();
		msgRefs.commit();
	}
}
