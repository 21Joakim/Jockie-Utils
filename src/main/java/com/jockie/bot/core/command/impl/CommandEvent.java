package com.jockie.bot.core.command.impl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.cooldown.ICooldown;
import com.jockie.bot.core.cooldown.ICooldownManager;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

public class CommandEvent {
	
	private MessageReceivedEvent event;
	private CommandListener commandListener;
	
	private ICommand command;
	
	private Object[] arguments;
	
	private String prefix;
	private String alias;
	private String commandTrigger;
	
	private List<String> optionsPresent;
	
	public CommandEvent(MessageReceivedEvent event, CommandListener listener, ICommand command, 
			Object[] arguments, String prefix, String alias, String commandTrigger, List<String> optionsPresent) {
		
		this.event = event;
		this.commandListener = listener;
		
		this.command = command;
		
		this.arguments = arguments;
		
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
	
	public ICommand getCommand() {
		return this.command;
	}
	
	public Object[] getArguments() {
		return this.arguments;
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
	
	public ICooldown applyCooldown() {
		return this.commandListener.getCoooldownManager().createCooldownAndGet(this.command, this.event);
	}
	
	public ICooldown applyCooldown(long duration, TimeUnit unit) {
		ICooldownManager manager = this.commandListener.getCoooldownManager();
		ICooldown cooldown = manager.createEmptyCooldown(this.command.getCooldownScope(), duration, unit);
		cooldown.applyContext(this.event);
		
		this.commandListener.getCoooldownManager().applyCooldown(this.command, cooldown);
		
		return cooldown;
	}
	
	public ICooldown removeCooldown() {
		return this.commandListener.getCoooldownManager().removeCooldown(this.command, this.event);
	}
}