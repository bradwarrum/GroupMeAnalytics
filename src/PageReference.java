
public class PageReference {
	private Page ref;
	private int refCount = 0;
	public PageReference(Page reference) {
		ref = reference;
	}

	public void incrementRefCount() {
		refCount++;
	}

	public void decrementRefCount() {
		refCount--;
		if (refCount < 0) throw new IllegalStateException();
	}

	public boolean isReferenced() {
		return (refCount > 0);
	}

	public void invalidate() {
		ref = null;
	}

	public Page getPage() {
		return ref;
	}
}