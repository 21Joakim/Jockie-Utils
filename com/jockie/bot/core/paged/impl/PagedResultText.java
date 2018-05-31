package com.jockie.bot.core.paged.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.jockie.bot.core.paged.IPagedResult;
import com.jockie.bot.core.paged.event.UpdateEvent;
import com.jockie.bot.core.paged.event.UpdateEvent.UpdateType;

import com.jockie.bot.core.timeout.Timeoutable;
import net.dv8tion.jda.core.EmbedBuilder;

public class PagedResultText extends Timeoutable<PagedResultText> implements IPagedResult {
	
	private int currentPage = 1;
	
	private String text;
	
	private List<String> pages;
	
	private int maxPages;
	
	private boolean cancelable = true;
	
	private Consumer<UpdateEvent<PagedResultText>> updateHandler;
	private Consumer<PagedResultText> cancelHandler;
	
	private EmbedBuilder embedBuilder = new EmbedBuilder();
	
	private long messageId;
	
	private List<String> determinePagesContent() {
		List<String> pages = new ArrayList<>();
		int from, to = 0;
		
		while(this.text.length() > to) {
			from = to;
			
			to = from + 1980;
			if(this.text.length() >= to) {
				int indexOf = this.text.substring(from, to).lastIndexOf("\n");
				if(indexOf != -1) {
					to = from + indexOf;
				}
			}else{
				to = this.text.length();
			}
			
			pages.add(this.text.substring(from, to));
		}
		
		return pages;
	}
	
	public PagedResultText(String text) {
		this.text = text;
		
		/* Default */
		this.timeout = true;
		this.timeoutTime = 120;
		this.timeoutUnit = TimeUnit.SECONDS;
		
		this.pages = this.determinePagesContent();
		this.maxPages = this.pages.size();
	}
	
	public void setCancelable(boolean cancelable) {
		this.cancelable = cancelable;
	}
	
	public boolean isCancelable() {
		return this.cancelable;
	}
	
	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}
	
	public long getMessageId() {
		return this.messageId;
	}
	
	public void onCancel(Consumer<PagedResultText> consumer) {
		this.cancelHandler = consumer;
	}
	
	public void cancel() {
		this.stopTimeout();
		
		if(this.isCancelable()) {
			if(this.cancelHandler != null) {
				this.cancelHandler.accept(this);
			}
		}
	}
	
	public void onUpdate(Consumer<UpdateEvent<PagedResultText>> consumer) {
		this.updateHandler = consumer;
	}
	
	public int getMaxPages() {
		return this.maxPages;
	}
	
	public int getCurrentPage() {
		return this.currentPage;
	}
	
	public EmbedBuilder getPageAsEmbed() {
		this.embedBuilder.setDescription("");
		this.embedBuilder.appendDescription("Page **" + this.currentPage + "**/**" + this.maxPages + "**\n\n");
		this.embedBuilder.appendDescription(this.pages.get(this.currentPage - 1));
		
		String footer = "";
		if(this.currentPage + 1 <= this.maxPages) {
			footer = footer + "next page | ";
		}
		
		if(this.currentPage - 1 > 0) {
			footer = footer + "previous page | ";
		}
		
		if(this.maxPages > 2) {
			footer = footer + "go to page <page> | ";
		}
		
		if(this.cancelable) {
			footer = footer + "cancel";
		}else{
			footer = footer.substring(footer.length() - 3);
		}
		
		this.embedBuilder.setFooter(footer, null);
		
		return this.embedBuilder;
	}
	
	public EmbedBuilder getEmbedBuilder() {
		return this.embedBuilder;
	}
	
	public boolean setPage(int page) {
		if(page > this.maxPages) {
			return false;
		}
		
		if(page < 1) {
			return false;
		}
		
		if(page == this.currentPage) {
			return false;
		}
		
		this.currentPage = page;
		
		this.restartTimeout();
		
		if(this.updateHandler != null) {
			UpdateEvent<PagedResultText> event = new UpdateEvent<>();
			event.pagedResult = this;
			event.updateType = UpdateType.GO_TO_PAGE;
			
			this.updateHandler.accept(event);
		}
		
		return true;
	}
	
	public boolean nextPage() {
		if(this.currentPage + 1 > this.maxPages) {
			return false;
		}
		
		this.currentPage = this.currentPage + 1;
		
		this.restartTimeout();
		
		if(this.updateHandler != null) {
			UpdateEvent<PagedResultText> event = new UpdateEvent<>();
			event.pagedResult = this;
			event.updateType = UpdateType.GO_TO_PAGE;
			
			this.updateHandler.accept(event);
		}
		
		return true;
	}
	
	public boolean previousPage() {
		if(this.currentPage - 1 < 1) {
			return false;
		}
		
		this.currentPage = this.currentPage - 1;
		
		this.restartTimeout();
		
		if(this.updateHandler != null) {
			UpdateEvent<PagedResultText> event = new UpdateEvent<>();
			event.pagedResult = this;
			event.updateType = UpdateType.GO_TO_PAGE;
			
			this.updateHandler.accept(event);
		}
		
		return true;
	}
}