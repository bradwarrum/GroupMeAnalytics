package persistence;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import persistence.data.structures.FrequencyTable;
import persistence.data.structures.MessageReferenceTable;
import persistence.data.structures.TreePointer;
import persistence.data.structures.WordTree;

public class FrequencySystem {
	private final WordTree wordTree;
	private final FrequencyTable freqTable;
	private final MessageReferenceTable msgRefs;
	
	public FrequencySystem() throws Exception {
		wordTree = new WordTree("./data/words", "./data/words.rbjf");
		freqTable = new FrequencyTable("./data/freq", "./data/freq.rbjf");
		msgRefs = new MessageReferenceTable("./data/msgref", "./data/msgref.rbjf");
	}
	
	private String sanitizeInput(String input) {
		return input.replaceAll("[.,\"()?!*;:]", "");
	}
	
	public void processMessage(GMMessage message) throws Exception {
		String[] words = sanitizeInput(message.message()).split(" ");
		
		TreePointer lastMsgRef = null;
		msgRefs.startMessage(message.messageID());
		short wordIndex = 0;
		for (String word : words) {
			TreePointer wordTreeEntry = wordTree.mapWord(word);
			if (wordTreeEntry == null) {
				lastMsgRef = null;
				continue;
			}
			TreePointer freqTableMaster = wordTree.getExternalPointer(wordTreeEntry);		
			if (freqTableMaster == null) {
				freqTableMaster = freqTable.addMasterRecord();
				wordTree.modifyExternalPointer(wordTreeEntry, freqTableMaster);
			}

			TreePointer freqTableMember = freqTable.incrementCount(message.memberID(), freqTableMaster);
			TreePointer msgRefHead = freqTable.getExternalPointer(freqTableMember);
			if (msgRefHead.rawValue() == 79) {
				System.out.println("Debug");
			}			
			msgRefHead = msgRefs.addWord(wordIndex, msgRefHead);
			if (msgRefHead.rawValue() == 79) {
				System.out.println("Debug");
			}
			freqTable.setExternalPointer(freqTableMember, msgRefHead);
			freqTable.setExternalPointer(freqTableMaster, msgRefHead);
			lastMsgRef = msgRefHead;
			
			wordIndex++;
		}
		msgRefs.finishMessage(lastMsgRef);
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
	
	public HashMap<Byte, Integer> getWordCounts(String word, HashSet<Byte> memberIDs) throws Exception {
		word = sanitizeInput(word);
		TreePointer wordTreeEntry = wordTree.find(word);
		if (wordTreeEntry == null) return null;
		TreePointer freqPtr = wordTree.getExternalPointer(wordTreeEntry);
		if (freqPtr == null) return null;
		return freqTable.getCounts(freqPtr, memberIDs);
	}
	
	public int getWordCount(String word, byte memberID) throws Exception {
		word = sanitizeInput(word);		
		TreePointer wordTreeEntry = wordTree.find(word);
		if (wordTreeEntry == null) return 0;
		TreePointer freqPtr = wordTree.getExternalPointer(wordTreeEntry);
		if (freqPtr == null) return 0;
		return freqTable.getCount(freqPtr, memberID);
	}
	/*
	public void getOccurrences(String phrase, int maxCount, List<Integer> messageIDs) throws Exception {
		String[] words = phrase.replaceAll("[.,\"()?!*;:]", "").split(" ");
		List<Integer> occurrences = new ArrayList<Integer>(); 
		if (words.length == 0) {
			return;
		}
		if (words.length > 5) {
			return;
		}
		TreePointer wordTreeEntry = wordTree.find(words[0]);
		if (wordTreeEntry == null) return;
		TreePointer masterPtr = wordTree.getExternalPointer(wordTreeEntry);
		if (masterPtr == null) return;
		
		List<ReferencePair> references = new ArrayList<ReferencePair>();
		TreePointer continuationPtr = msgRefs.getReferences(freqTable.getExternalPointer(masterPtr), maxCount, references); 
		if (words.length == 1) {
			for (ReferencePair pair : references) {
				messageIDs.add(pair.messageID);
			}
		} else {
			for (ReferencePair pair : references) {
				for (int i = 1; i < words.length; i++) {
					
				}
			}
		}
	}*/
	
	public void commit() throws Exception {
		//TODO: Make this atomic
		wordTree.commit();
		freqTable.commit();
		msgRefs.commit();
	}
}
