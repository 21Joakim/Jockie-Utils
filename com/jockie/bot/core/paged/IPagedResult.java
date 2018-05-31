package com.jockie.bot.core.paged;

import net.dv8tion.jda.core.EmbedBuilder;

/* This and all the classes implementing it need a re-make */
public interface IPagedResult {
	
	public long getMessageId();
	public void setMessageId(long messageId);
	
	public void startTimeout();
	public void stopTimeout();
	public void restartTimeout();
	
	public void onTimeoutFinish(Runnable finish);
	
	public EmbedBuilder getPageAsEmbed();
	
	public void cancel();
	
	public boolean isCancelable();
	
	public boolean nextPage();
	
	public boolean previousPage();
	
	public boolean setPage(int page);
	
}