package persistence.caching;

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
		if (refCount < 0) {
			System.out.println("Bad free");
			throw new IllegalStateException();
		}
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