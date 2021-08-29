package gui;

interface DocumentPanelListener {
	void documentTableSingleSelection(int documentId, String documentText);
	void documentTableMultipleSelection(int[] documentId);
	void documentTableNoSelection();
	void documentRefreshStarted();
	void documentRefreshChunkComplete();
	void documentRefreshEnded();
}