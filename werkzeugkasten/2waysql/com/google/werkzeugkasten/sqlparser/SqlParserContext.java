package com.google.werkzeugkasten.sqlparser;

import java.util.List;

import com.google.werkzeugkasten.meta.ChainContext;

public interface SqlParserContext extends ChainContext<Status> {

	char[] getFullText();

	int getCursor();

	void setCursor(int index);

	List<Status> getStatusCopy();

	void setStatus(List<Status> status);

	void addStatus(Status status);

	void resetStatus();
}