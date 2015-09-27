
public class FrequencySystem {
	private final WordTree wordTree;
	private final FrequencyTable freqTable;
	private final MessageReferenceTable msgRefs;
	
	public FrequencySystem() throws Exception {
		wordTree = new WordTree("./data/words", "./data/words.rbjf");
		freqTable = new FrequencyTable("./data/freq", "./data/freq.rbjf");
		msgRefs = new MessageReferenceTable("./data/msgref", "./data/msgref.rbjf");
	}
	
	public void processMessage(GMMessage message) throws Exception {
		String[] words = message.message().replaceAll("[.,\"()?!*;:]", "").split(" ");
		
		TreePointer lastMsgRef = null;
		for (String word : words) {
			TreePointer wordTreeEntry = wordTree.mapWord(word);
			if (wordTreeEntry == null) {
				lastMsgRef = null;
				continue;
			}
			TreePointer freqTableFirst = wordTree.getExternalPointer(wordTreeEntry);
			
			TreePointer freqTableActual = freqTable.incrementCount(message.memberID(), freqTableFirst);
			if (freqTableFirst == null) 
				wordTree.modifyExternalPointer(wordTreeEntry, freqTableActual);
			
			TreePointer msgRefHead = msgRefs.pushMessageReference(message.messageID(), freqTable.getExternalPointer(freqTableActual));
			freqTable.setExternalPointer(freqTableActual, msgRefHead);
			if (lastMsgRef != null) msgRefs.setTrailingReference(msgRefHead, wordTreeEntry);
			lastMsgRef = msgRefHead;
		}
		
	}
	
	public void commit() throws Exception {
		//TODO: Make this atomic
		wordTree.commit();
		freqTable.commit();
		msgRefs.commit();
	}
}
