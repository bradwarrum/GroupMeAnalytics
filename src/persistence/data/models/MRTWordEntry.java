package persistence.data.models;
import java.nio.ByteBuffer;

import persistence.caching.PageEntry;
import persistence.data.structures.MessageReferenceTable;
import persistence.data.structures.TreePointer;
import persistence.data.structures.WordTree;

public class MRTWordEntry extends TreeEntry {
	private static final int HEADER_IND = 0;
	private static final short EOS_MASK = (short)0x8000;
	private static final short MESSAGE_OFFSET_MASK = 0x7FFF;
	private static final int NEXT_IND = HEADER_IND + Short.BYTES;
	private static final short WORD_REF_IND = NEXT_IND + Integer.BYTES;
	public static final int MIN_ENTRY_SIZE = WORD_REF_IND + Integer.BYTES;
	private static ByteBuffer intbuffer = ByteBuffer.allocate(MIN_ENTRY_SIZE);
	public MRTWordEntry(PageEntry backingEntry) {
		super(backingEntry);
	}
	
	public boolean endOfSequence(){
		return ((getShort(HEADER_IND) & EOS_MASK) != 0);
	}
	
	public void endOfSequence(boolean value) {
		short header = getShort(HEADER_IND);
		if (value) {
			header |= EOS_MASK;
		} else {
			header &= ~EOS_MASK;
		}
		putShort(header, HEADER_IND);
	}
	
	public short messageOffset(){
		return (short)(getShort(HEADER_IND) & MESSAGE_OFFSET_MASK);
	}
	
	public void messageOffset(short offset) {
		short header = getShort(HEADER_IND);
		header &= MESSAGE_OFFSET_MASK;
		header |= offset;
		putShort(header, HEADER_IND);
	}
	
	public TreePointer next() {
		return new TreePointer(getInt(NEXT_IND), MessageReferenceTable.ENTRIES_PER_PAGE);
	}
	
	public void next(TreePointer next) {
		putInt(next.rawValue(), NEXT_IND);
	}
	
	public TreePointer word(){
		return new TreePointer(getInt(WORD_REF_IND), WordTree.ENTRIES_PER_PAGE);
	}

	public void word(TreePointer word) {
		putInt(word.rawValue(), WORD_REF_IND);
	}
	
	@Override
	public TreePointer self() {
		return new TreePointer(pageID(), entryIndex(), MessageReferenceTable.ENTRIES_PER_PAGE);
	}

	@Override
	protected ByteBuffer buffer() {
		return intbuffer;
	}
}
