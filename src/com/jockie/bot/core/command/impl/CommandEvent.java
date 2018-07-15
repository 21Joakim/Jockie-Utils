package com.jockie.bot.core.command.impl;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandEvent {
	
	private MessageReceivedEvent event;
	private CommandListener commandListener;
	
	private String prefix;
	private String alias;
	private String commandTrigger;
	
	public CommandEvent(MessageReceivedEvent event, CommandListener listener, String prefix, String alias, String commandTrigger) {
		this.event = event;
		this.commandListener = listener;
		this.prefix = prefix;
		this.alias = alias;
		this.commandTrigger = commandTrigger;
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
}