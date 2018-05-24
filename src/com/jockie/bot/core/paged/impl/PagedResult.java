package com.jockie.bot.core.paged.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import com.jockie.bot.core.paged.IPagedResult;
import com.jockie.bot.core.paged.event.SelectEvent;
import com.jockie.bot.core.paged.event.UpdateEvent;
import com.jockie.bot.core.paged.event.UpdateEvent.UpdateType;
import com.jockie.bot.core.timeout.Timeoutable;

import net.dv8tion.jda.core.EmbedBuilder;

public class PagedResult<Type> extends Timeoutable<PagedResult<Type>> implements IPagedResult {
	
	private int currentPage = 1;
	private int entriesPerPage = 10;
	
	private int maxPages;
	
	private List<? extends Type> entries;
	
	private Function<? super Type, String> displayFunction;
	
	private boolean listIndexes = true;
	private boolean listIndexesContinuously = false;
	
	private boolean cancelable = true;
	
	private Consumer<SelectEvent<PagedResult<Type>, Type>> selectHandler;
	private Consumer<UpdateEvent<PagedResult<Type>>> updateHandler;
	private Consumer<PagedResult<Type>> cancelHandler;
	
	private EmbedBuilder embedBuilder = new EmbedBuilder();
	
	private long messageId;
	
	private int determineMaxPages() {
		return (int) Math.ceil((double) this.entries.size()/(double) this.entriesPerPage);
	}
	
	public PagedResult(List<? extends Type> entries, Function<? super Type, String> displayFunction, Consumer<SelectEvent<PagedResult<Type>, Type>> consumer) {
		this.entries = entries;
		this.displayFunction = displayFunction;
		this.selectHandler = consumer;
		
		/* Default */
		this.timeout = true;
		this.timeoutTime = 30;
		this.timeoutUnit = TimeUnit.SECONDS;
		
		this.maxPages = this.determineMaxPages();
	}
	
	public PagedResult(List<? extends Type> entries, Function<? super Type, String> displayFunction) {
		this(entries, displayFunction, null);
	}
	
	public void setEntriesPerPage(int entriesPerPage) {
		this.entriesPerPage = entriesPerPage;
		
		this.maxPages = this.determineMaxPages();
	}
	
	public void setListIndexes(boolean listIndexes) {
		this.listIndexes = listIndexes;
	}
	
	public boolean isListIndexes() {
		return this.listIndexes;
	}
	
	public void setListIndexesContinuously(boolean listIndexesContinuously) {
		this.listIndexesContinuously = listIndexesContinuously;
	}
	
	public boolean isListIndexesContinuously() {
		return this.listIndexesContinuously;
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
	
	public void select(int index) {
		this.stopTimeout();
		
		if(this.isSelectable()) {
			SelectEvent<PagedResult<Type>, Type> event = new SelectEvent<>();
			event.pagedResult = this;
			event.page = this.currentPage;
			event.index = index;
			event.actualIndex = (this.currentPage - 1) * this.entriesPerPage + (index - 1);
			event.entry = this.getCurrentPageEntries().get(index - 1);
			
			this.selectHandler.accept(event);
		}
	}
	
	public void onCancel(Consumer<PagedResult<Type>> consumer) {
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
	
	public void onUpdate(Consumer<UpdateEvent<PagedResult<Type>>> consumer) {
		this.updateHandler = consumer;
	}
	
	public boolean isSelectable() {
		return this.selectHandler != null;
	}
	
	public int getMaxPages() {
		return this.maxPages;
	}
	
	public int getCurrentPage() {
		return this.currentPage;
	}
	
	public int getEntriesPerPage() {
		return this.entriesPerPage;
	}
	
	public List<? extends Type> getCurrentPageEntries() {
		int start = (this.currentPage - 1) * this.entriesPerPage;
		int end;
		
		if(this.currentPage == maxPages)
			end = this.entries.size() - start;
		else end = this.entriesPerPage;
		
		return this.entries.subList(start, start + end);
	}
	
	public EmbedBuilder getPageAsEmbed() {
		List<? extends Type> entries = getCurrentPageEntries();
		
		this.embedBuilder.setDescription("");
		this.embedBuilder.appendDescription("Page **" + this.currentPage + "**/**" + this.maxPages + "**\n");
		
		for(int i = 0; i < entries.size(); i++) {
			this.embedBuilder.appendDescription("\n" + ((this.listIndexes) ? (this.listIndexesContinuously) ? ((this.currentPage - 1) * this.entriesPerPage + (i + 1)) + " - " : (i + 1) + " - " : "") + this.displayFunction.apply(entries.get(i)));
		}
		
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
			UpdateEvent<PagedResult<Type>> event = new UpdateEvent<>();
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
			UpdateEvent<PagedResult<Type>> event = new UpdateEvent<>();
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
			UpdateEvent<PagedResult<Type>> event = new UpdateEvent<>();
			event.pagedResult = this;
			event.updateType = UpdateType.GO_TO_PAGE;
			
			this.updateHandler.accept(event);
		}
		
		return true;
	}
}