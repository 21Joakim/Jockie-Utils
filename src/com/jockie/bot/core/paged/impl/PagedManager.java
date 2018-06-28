package com.jockie.bot.core.paged.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.jockie.bot.core.paged.IPagedResult;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.tuple.Pair;

public class PagedManager {
	
	/* Lul */
	private static Map<Long, Map<Long, Map<Long, Pair<JDA, IPagedResult>>>> pagedResults = Collections.synchronizedMap(new HashMap<>());
	
	/** Only has JDA parameter to support Jockie Music's use case */
	public static void addPagedResult(MessageReceivedEvent event, JDA jda, IPagedResult pagedResult) {
		if(event.getGuild() != null) {
			long guildId = event.getGuild().getIdLong();
			
			if(!PagedManager.pagedResults.containsKey(guildId)) {
				PagedManager.pagedResults.put(guildId, new HashMap<>());
			}
			
			if(!PagedManager.pagedResults.get(guildId).containsKey(event.getTextChannel().getIdLong())) {
				PagedManager.pagedResults.get(guildId).put(event.getTextChannel().getIdLong(), new HashMap<>());
			}

//			Not sure if i want this
//			if(PagedManager.pagedResults.get(guildId).get(event.getTextChannel().getIdLong()).containsKey(event.getAuthor().getIdLong())) {
//				Pair<JDA, IPagedResult> pair = PagedManager.pagedResults.get(guildId).get(event.getTextChannel().getIdLong()).get(event.getAuthor().getIdLong());
//				
//				event.getTextChannel().getMessageById(pair.getRight().getMessageId()).queue(message -> {
//					pair.getRight().stopTimeout();
//					pair.getLeft().getTextChannelById(event.getTextChannel().getIdLong()).deleteMessageById(message.getIdLong()).queue();
//				}, failure -> {});
//			}
					
			jda.getTextChannelById(event.getTextChannel().getIdLong()).sendMessage(pagedResult.getPageAsEmbed().build()).queue(message -> {
				PagedManager.pagedResults.get(guildId).get(event.getTextChannel().getIdLong()).put(event.getAuthor().getIdLong(), Pair.of(jda, pagedResult));
				
				pagedResult.setMessageId(message.getIdLong());
				pagedResult.onTimeoutFinish(() -> {
					if(pagedResult.isDeleteOnTimeout()) {
						jda.getTextChannelById(event.getTextChannel().getIdLong()).deleteMessageById(pagedResult.getMessageId()).queue();
					}
					
					PagedManager.removePagedResult(event);
				});
				
				pagedResult.startTimeout();
			});
		}else{
			throw new IllegalArgumentException("The PagedResults only work for guilds");
		}
	}
	
	public static void addPagedResult(MessageReceivedEvent event, IPagedResult pagedResult) {
		PagedManager.addPagedResult(event, event.getJDA(), pagedResult);
	}
	
	public static void addPagedResult(MessageReceivedEvent event, IPagedResult pagedResult, Message previous) {		
		if(event.getGuild() != null) {
			long guildId = event.getGuild().getIdLong();
		
			if(!PagedManager.pagedResults.containsKey(guildId)) {
				PagedManager.pagedResults.put(guildId, new HashMap<>());
			}
			
			if(!PagedManager.pagedResults.get(guildId).containsKey(event.getTextChannel().getIdLong())) {
				PagedManager.pagedResults.get(guildId).put(event.getTextChannel().getIdLong(), new HashMap<>());
			}
			
//			Not sure if i want this
//			if(PagedManager.pagedResults.get(guildId).get(event.getTextChannel().getIdLong()).containsKey(event.getAuthor().getIdLong())) {
//				Pair<JDA, IPagedResult> pair = PagedManager.pagedResults.get(guildId).get(event.getTextChannel().getIdLong()).get(event.getAuthor().getIdLong());
//				
//				previous.getJDA().getTextChannelById(event.getTextChannel().getIdLong()).getMessageById(pair.getRight().getMessageId()).queue(message -> {
//					pair.getRight().stopTimeout();
//					pair.getLeft().getTextChannelById(event.getTextChannel().getIdLong()).deleteMessageById(message.getIdLong()).queue();
//				}, failure -> {});
//			}
			
			pagedResult.setMessageId(previous.getIdLong());
			
			previous.editMessage(pagedResult.getPageAsEmbed().build()).queue(m -> {
				PagedManager.pagedResults.get(guildId).get(event.getTextChannel().getIdLong()).put(event.getAuthor().getIdLong(), Pair.of(previous.getJDA(), pagedResult));
				
				pagedResult.onTimeoutFinish(() -> {
					if(pagedResult.isDeleteOnTimeout()) {
						previous.getJDA().getTextChannelById(event.getTextChannel().getIdLong()).deleteMessageById(pagedResult.getMessageId()).queue();
					}
					
					PagedManager.removePagedResult(event);
				});
				
				pagedResult.startTimeout();
			});
		}else{
			throw new IllegalArgumentException("The PagedResults only work for guilds");
		}
	}
	
	public static Pair<JDA, IPagedResult> getPagedResult(MessageReceivedEvent event) {
		if(event.getGuild() != null) {
			long guildId = event.getGuild().getIdLong();
			
			if(PagedManager.pagedResults.containsKey(guildId)) {
				if(PagedManager.pagedResults.get(guildId).containsKey(event.getTextChannel().getIdLong())) {
					return PagedManager.pagedResults.get(guildId).get(event.getTextChannel().getIdLong()).get(event.getAuthor().getIdLong());
				}
			}
		}else{
			throw new IllegalArgumentException("The PagedResults only work for guilds");
		}
		
		return null;
	}
	
	public static void removePagedResult(MessageReceivedEvent event) {
		if(event.getGuild() != null) {
			long guildId = event.getGuild().getIdLong();
			
			if(PagedManager.pagedResults.containsKey(guildId)) {
				if(PagedManager.pagedResults.get(guildId).containsKey(event.getTextChannel().getIdLong())) {
					PagedManager.pagedResults.get(guildId).get(event.getTextChannel().getIdLong()).remove(event.getAuthor().getIdLong());
				}
			}
		}else{
			throw new IllegalArgumentException("The PagedResults only work for guilds");
		}
	}
	
	/* Kind of messy */
	public static boolean handlePagedResults(MessageReceivedEvent event) {
		String rawMessage = event.getMessage().getContentRaw().toLowerCase();
		
		Pair<JDA, IPagedResult> pair = PagedManager.getPagedResult(event);
		if(pair != null) {
			JDA jda = pair.getLeft();
			IPagedResult iPagedResult = pair.getRight();
			
			if(iPagedResult != null) {
				long originalMessage = iPagedResult.getMessageId();
				
				if(rawMessage.equals("next page") || rawMessage.equals("next") || rawMessage.equals("previous page") || rawMessage.equals("previous") || rawMessage.startsWith("go to page ") || rawMessage.startsWith("go to ") || rawMessage.equals("cancel")) {
					jda.getTextChannelById(event.getTextChannel().getIdLong()).deleteMessageById(event.getMessageId()).queue();
					
					if(rawMessage.equals("next page") || rawMessage.equals("next")) {
						if(iPagedResult.nextPage()) {
							jda.getTextChannelById(event.getTextChannel().getIdLong()).getMessageById(originalMessage).queue(message -> {
								jda.getTextChannelById(event.getTextChannel().getIdLong()).editMessageById(originalMessage, iPagedResult.getPageAsEmbed().build()).queue();
							}, failure -> {
								jda.getTextChannelById(event.getTextChannel().getIdLong()).sendMessage(iPagedResult.getPageAsEmbed().build()).queue(newMessage -> iPagedResult.setMessageId(newMessage.getIdLong()));
							});
						}else{
							jda.getTextChannelById(event.getTextChannel().getIdLong()).sendMessage("There are no more pages").queue();
						}
					}else if(rawMessage.equals("previous page") || rawMessage.equals("previous")) {
						if(iPagedResult.previousPage()) {
							jda.getTextChannelById(event.getTextChannel().getIdLong()).getMessageById(originalMessage).queue(message -> {
								jda.getTextChannelById(event.getTextChannel().getIdLong()).editMessageById(originalMessage, iPagedResult.getPageAsEmbed().build()).queue();
							}, failure -> {
								jda.getTextChannelById(event.getTextChannel().getIdLong()).sendMessage(iPagedResult.getPageAsEmbed().build()).queue(newMessage -> iPagedResult.setMessageId(newMessage.getIdLong()));
							});
						}else{
							jda.getTextChannelById(event.getTextChannel().getIdLong()).sendMessage("There are no previous pages").queue();
						}
					}else if(rawMessage.startsWith("go to ")) {
						try {
							int page;
							if(rawMessage.startsWith("go to page ")) {
								page = Integer.parseInt(rawMessage.substring("go to page ".length()));
							}else{
								page = Integer.parseInt(rawMessage.substring("go to ".length()));
							}
								
							if(iPagedResult.setPage(page)) {
								jda.getTextChannelById(event.getTextChannel().getIdLong()).getMessageById(originalMessage).queue(message -> {
									jda.getTextChannelById(event.getTextChannel().getIdLong()).editMessageById(originalMessage, iPagedResult.getPageAsEmbed().build()).queue();
								}, failure -> {
									jda.getTextChannelById(event.getTextChannel().getIdLong()).sendMessage(iPagedResult.getPageAsEmbed().build()).queue(newMessage -> iPagedResult.setMessageId(newMessage.getIdLong()));
								});
							}else{
								jda.getTextChannelById(event.getTextChannel().getIdLong()).sendMessage("Invalid page number").queue();
							}
						}catch(Exception e) {
							jda.getTextChannelById(event.getTextChannel().getIdLong()).sendMessage("Invalid page number").queue();
						}
					}else if(iPagedResult.isCancelable() && rawMessage.equals("cancel")) {
						jda.getTextChannelById(event.getTextChannel().getIdLong()).deleteMessageById(originalMessage).queue();
						
						PagedManager.removePagedResult(event);
						
						iPagedResult.cancel();
					}
					
					return true;
				}
				
				if(iPagedResult instanceof PagedResult<?>) {
					PagedResult<?> pagedResult = (PagedResult<?>) iPagedResult;
					
					if(pagedResult.isSelectable()) {
						try {
							int entry = Integer.parseInt(rawMessage);
							
							if(entry > 0 && entry <= pagedResult.getEntriesPerPage()) {
								jda.getTextChannelById(event.getTextChannel().getIdLong()).deleteMessageById(event.getMessageIdLong()).queue();
								jda.getTextChannelById(event.getTextChannel().getIdLong()).deleteMessageById(originalMessage).queue();
								
								PagedManager.removePagedResult(event);
								
								pagedResult.select(entry);
								
								return true;
							}else if(pagedResult.isListIndexesContinuously() && (entry > pagedResult.getCurrentPage() * pagedResult.getEntriesPerPage() - pagedResult.getEntriesPerPage() && entry <= pagedResult.getCurrentPage() * pagedResult.getEntriesPerPage())) {
								jda.getTextChannelById(event.getTextChannel().getIdLong()).deleteMessageById(event.getMessageIdLong()).queue();
								jda.getTextChannelById(event.getTextChannel().getIdLong()).deleteMessageById(originalMessage).queue();
								
								PagedManager.removePagedResult(event);
								
								pagedResult.select(entry - (pagedResult.getCurrentPage() - 1) * pagedResult.getEntriesPerPage());
								
								return true;
							}
						}catch(Exception e) {}
					}
				}
			}
		}
		
		return false;
	}
}