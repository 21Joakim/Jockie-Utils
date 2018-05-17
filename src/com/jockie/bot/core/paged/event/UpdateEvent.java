package com.jockie.bot.core.paged.event;

import com.jockie.bot.core.paged.IPagedResult;

public class UpdateEvent<PagedResult extends IPagedResult> {
	
	public PagedResult pagedResult;
	
	public UpdateType updateType;
	
	public enum UpdateType {
		NEXT_PAGE,
		PREVIOUS_PAGE,
		GO_TO_PAGE;
	}
}