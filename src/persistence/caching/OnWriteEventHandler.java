package persistence.caching;

public interface OnWriteEventHandler {
	public void onWritePage(int pageID);
}
