package com.jockie.bot.core.command.impl;

import java.util.Collections;
import java.util.List;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

public class CommandEvent {
	
	private MessageReceivedEvent event;
	private CommandListener commandListener;
	
	private String prefix;
	private String alias;
	private String commandTrigger;
	
	private List<String> optionsPresent;
	
	public CommandEvent(MessageReceivedEvent event, CommandListener listener, String prefix, String alias, String commandTrigger, List<String> optionsPresent) {
		this.event = event;
		this.commandListener = listener;
		this.prefix = prefix;
		this.alias = alias;
		this.commandTrigger = commandTrigger;
		
		this.optionsPresent = optionsPresent;
	}
	
	public MessageReceivedEvent getEvent() {
		return this.event;
	}
	
	public CommandListener getCommandListener() {
		return this.commandListener;
	}
	
	public String getPrefix() {
		return this.prefix;
	}
	
	public String getAlias() {
		return this.alias;
	}
	
	public String getCommandTrigger() {
		return this.commandTrigger;
	}
	
	public List<String> getOptionsPresent() {
		return Collections.unmodifiableList(this.optionsPresent);
	}
	
	public MessageAction reply(CharSequence text) {
		return this.event.getChannel().sendMessage(text);
	}
	
	public MessageAction reply(MessageEmbed embed) {
		return this.event.getChannel().sendMessage(embed);
	}
	
	public MessageAction reply(Message message) {
		return this.event.getChannel().sendMessage(message);
	}
}