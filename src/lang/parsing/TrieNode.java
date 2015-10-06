package lang.parsing;

public class TrieNode<T> {
	private TrieNode<T> next = null;
	private TrieNode<T> child = null;
	private final byte value;
	private T pointer;
	public TrieNode(byte value) {
		this.value = value;
		pointer = null;
	}
	
	public TrieNode(byte value, T pointer) {
		this.value = value;
		this.pointer = pointer;
	}
	
	public static<T> TrieNode<T> addString(byte[] asciiString, TrieNode<T> head, T lookup) {
		if (head == null) head = (asciiString.length == 1) ? new TrieNode<T>(asciiString[0], lookup) : new TrieNode<T>(asciiString[0]);
		TrieNode<T> current = head;
		int i ;
		for (i = 0; i < asciiString.length;) {
			byte b = asciiString[i];
			boolean lastIteration = i == asciiString.length - 1;
			while(current.value != b) {
				if (current.next == null) current.next = new TrieNode<T>(b);
				current = current.next;
			}
			i++;
			if (current.child == null) {
				if (lastIteration) current.pointer = lookup;
				else current.child = new TrieNode<T>(asciiString[i]);
			}
			current = current.child;
		}
		return head;
	}
	
	public static<T> T bestMatch(byte[] asciiString, TrieNode<T> head) {
		int i = 0;
		T bestMatch = null;
		do {
			if (i >= asciiString.length) return bestMatch;
			while (head.value != asciiString[i]) {
				if (head.next == null) return bestMatch;
				head = head.next;
			}
			if (head.pointer != null) bestMatch = head.pointer;
			i++;
		} while ((head = head.child) != null);
		return bestMatch;
	}
	
	public static<T> void printit(TrieNode<T> head) {
		if (head == null) return;
		while( head != null) {
			System.out.print((char)head.value);
			printit(head.child);
			System.out.print(" ");
			head = head.next;
		}
		
		
	}
	
	public static void main(String[] args) throws Exception {
		TrieNode<Command>commandTree = null;
		String[] strings = new String[] {"here", "we", "are", "in", "a", "wang", "filled", "area"};
		String[] nonmatch = new String[] {"her", "b", "w", "", "want"};
		String[] match = new String[] {"here", "went", "arena"};

		for (String s : strings) {
			commandTree = addString(s.getBytes("US-ASCII"), commandTree, Command.FREQ);
		}
		TrieNode.printit(commandTree);
		for (String s : nonmatch) {
			if (bestMatch(s.getBytes("US-ASCII"), commandTree) != null) throw new Exception();
		}
		for (String s : match) {
			if (bestMatch(s.getBytes("US-ASCII"), commandTree) != Command.FREQ) throw new Exception();
		}
		
	}
}
