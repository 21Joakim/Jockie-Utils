package com.jockie.bot.core.paged.event;

import com.jockie.bot.core.paged.IPagedResult;

public class SelectEvent<PagedResult extends IPagedResult, Type> {
	
	public PagedResult pagedResult;
	
	public int page, index, actualIndex;
	
	public Type entry;
	
}