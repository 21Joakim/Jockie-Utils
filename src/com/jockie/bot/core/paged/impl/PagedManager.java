package com.jockie.bot.core.paged.impl;

import java.util.HashMap;
import java.util.Map;

import com.jockie.bot.core.paged.IPagedResult;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.requests.ErrorResponse;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.tuple.Pair;

public class PagedManager {
	
	private static Map<Long, Map<Long, Map<Long, Pair<JDA, IPagedResult>>>> pagedResults = new HashMap<>();
	
	private static RestAction<Message> getMessageIsDeleted(JDA jda, String channelId, String messageId) {
		return new RestAction<Message>(jda, Route.Messages.GET_MESSAGE.compile(channelId, messageId)) {
			protected void handleResponse(Response response, Request<Message> request) {
				if(!response.isOk()) {
					if(ErrorResponse.fromCode(response.getObject().getInt("code")).equals(ErrorResponse.UNKNOWN_MESSAGE)) {
						request.onSuccess(null);
					}else{
						request.onFailure(response);
					}
				}else request.onSuccess(this.api.getEntityBuilder().createMessage(response.getObject(), jda.getTextChannelById(channelId), false));
			}
		};
	}
	
	public static void addPagedResult(MessageReceivedEvent event, JDA jda, IPagedResult pagedResult) {
		if(!event.getChannelType().isGuild()) {
			throw new IllegalArgumentException("The PagedResults only work for guilds");
		}
		
		Long guildId;
		if(event.getGuild() != null) {
			guildId = event.getGuild().getIdLong();
		}else{
			guildId = null;
		}
		
		if(!PagedManager.pagedResults.containsKey(guildId)) {
			PagedManager.pagedResults.put(guildId, new HashMap<>());
		}
		
		if(!PagedManager.pagedResults.get(guildId).containsKey(event.getTextChannel().getIdLong())) {
			PagedManager.pagedResults.get(guildId).put(event.getTextChannel().getIdLong(), new HashMap<>());
		}
		
		if(PagedManager.pagedResults.get(guildId).get(event.getTextChannel().getIdLong()).containsKey(event.getAuthor().getIdLong())) {
			Pair<JDA, IPagedResult> pair = PagedManager.pagedResults.get(guildId).get(event.getTextChannel().getIdLong()).get(event.getAuthor().getIdLong());
			
			RestAction<Message> getMessageIsDeleted = PagedManager.getMessageIsDeleted(jda, event.getTextChannel().getId(), String.valueOf(pair.getRight().getMessageId()));
			
			getMessageIsDeleted.queue(message -> {
				if(message != null) {
					pair.getRight().stopTimeout();
					pair.getLeft().getTextChannelById(event.getTextChannel().getIdLong()).deleteMessageById(message.getIdLong()).queue();
				}
			});
		}
				
		jda.getTextChannelById(event.getTextChannel().getIdLong()).sendMessage(pagedResult.getPageAsEmbed().build()).queue(message -> {
			PagedManager.pagedResults.get(guildId).get(event.getTextChannel().getIdLong()).put(event.getAuthor().getIdLong(), Pair.of(jda, pagedResult));
			
			pagedResult.setMessageId(message.getIdLong());
			pagedResult.onTimeoutFinish(() -> {
				jda.getTextChannelById(event.getTextChannel().getIdLong()).deleteMessageById(pagedResult.getMessageId()).queue();
				
				PagedManager.removePagedResult(event);
			});
			
			pagedResult.startTimeout();
		});
	}
	
	public static void addPagedResult(MessageReceivedEvent event, IPagedResult pagedResult) {
		PagedManager.addPagedResult(event, event.getJDA(), pagedResult);
	}
	
	public static void addPagedResult(MessageReceivedEvent event, IPagedResult pagedResult, Message previous) {
		if(!event.getChannelType().isGuild()) {
			throw new IllegalArgumentException("The PagedResults only work for guilds");
		}
		
		Long guildId;
		if(event.getGuild() != null) {
			guildId = event.getGuild().getIdLong();
		}else{
			guildId = null;
		}
		
		if(!PagedManager.pagedResults.containsKey(guildId)) {
			PagedManager.pagedResults.put(guildId, new HashMap<>());
		}
		
		if(!PagedManager.pagedResults.get(guildId).containsKey(event.getTextChannel().getIdLong())) {
			PagedManager.pagedResults.get(guildId).put(event.getTextChannel().getIdLong(), new HashMap<>());
		}
		
		if(PagedManager.pagedResults.get(guildId).get(event.getTextChannel().getIdLong()).containsKey(event.getAuthor().getIdLong())) {
			Pair<JDA, IPagedResult> pair = PagedManager.pagedResults.get(guildId).get(event.getTextChannel().getIdLong()).get(event.getAuthor().getIdLong());
			
			RestAction<Message> getMessageIsDeleted = PagedManager.getMessageIsDeleted(previous.getJDA(), event.getTextChannel().getId(), String.valueOf(pair.getRight().getMessageId()));
			
			getMessageIsDeleted.queue(message -> {
				if(message != null) {
					pair.getRight().stopTimeout();
					pair.getLeft().getTextChannelById(event.getTextChannel().getIdLong()).deleteMessageById(message.getIdLong()).queue();
				}
			});
		}
		
		pagedResult.setMessageId(previous.getIdLong());
		
		previous.editMessage(pagedResult.getPageAsEmbed().build()).queue(m -> {
			PagedManager.pagedResults.get(guildId).get(event.getTextChannel().getIdLong()).put(event.getAuthor().getIdLong(), Pair.of(previous.getJDA(), pagedResult));
			
			pagedResult.onTimeoutFinish(() -> {
				previous.getJDA().getTextChannelById(event.getTextChannel().getIdLong()).deleteMessageById(pagedResult.getMessageId()).queue();
				
				PagedManager.removePagedResult(event);
			});
			
			pagedResult.startTimeout();
		});
	}
	
	public static Pair<JDA, IPagedResult> getPagedResult(MessageReceivedEvent event) {
		Long guildId;
		if(event.getGuild() != null) {
			guildId = event.getGuild().getIdLong();
		}else{
			guildId = null;
		}
		
		if(PagedManager.pagedResults.containsKey(guildId)) {
			if(PagedManager.pagedResults.get(guildId).containsKey(event.getTextChannel().getIdLong())) {
				return PagedManager.pagedResults.get(guildId).get(event.getTextChannel().getIdLong()).get(event.getAuthor().getIdLong());
			}
		}
		
		return null;
	}
	
	public static void removePagedResult(MessageReceivedEvent event) {
		Long guildId;
		if(event.getGuild() != null) {
			guildId = event.getGuild().getIdLong();
		}else{
			guildId = null;
		}
		
		if(PagedManager.pagedResults.containsKey(guildId)) {
			if(PagedManager.pagedResults.get(guildId).containsKey(event.getTextChannel().getIdLong())) {
				PagedManager.pagedResults.get(guildId).get(event.getTextChannel().getIdLong()).remove(event.getAuthor().getIdLong());
			}
		}
	}	
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
						
					RestAction<Message> getMessageIsDeleted = PagedManager.getMessageIsDeleted(jda, event.getTextChannel().getId(), originalMessage + "");
					if(rawMessage.equals("next page") || rawMessage.equals("next")) {
						if(iPagedResult.nextPage()) {
							getMessageIsDeleted.queue(message -> {
								if(message != null) {
									jda.getTextChannelById(event.getTextChannel().getIdLong()).editMessageById(originalMessage, iPagedResult.getPageAsEmbed().build()).queue();
								}else{
									jda.getTextChannelById(event.getTextChannel().getIdLong()).sendMessage(iPagedResult.getPageAsEmbed().build()).queue(newMessage -> iPagedResult.setMessageId(newMessage.getIdLong()));
								}
							});
						}else{
							jda.getTextChannelById(event.getTextChannel().getIdLong()).sendMessage("There are no more pages").queue();
						}
					}else if(rawMessage.equals("previous page") || rawMessage.equals("previous")) {
						if(iPagedResult.previousPage()) {
							getMessageIsDeleted.queue(message -> {
								if(message != null) {
									jda.getTextChannelById(event.getTextChannel().getIdLong()).editMessageById(originalMessage, iPagedResult.getPageAsEmbed().build()).queue();
								}else{
									jda.getTextChannelById(event.getTextChannel().getIdLong()).sendMessage(iPagedResult.getPageAsEmbed().build()).queue(newMessage -> iPagedResult.setMessageId(newMessage.getIdLong()));
								}
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
								getMessageIsDeleted.queue(message -> {
									if(message != null) {
										jda.getTextChannelById(event.getTextChannel().getIdLong()).editMessageById(originalMessage, iPagedResult.getPageAsEmbed().build()).queue();
									}else{
										jda.getTextChannelById(event.getTextChannel().getIdLong()).sendMessage(iPagedResult.getPageAsEmbed().build()).queue(newMessage -> iPagedResult.setMessageId(newMessage.getIdLong()));
									}
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