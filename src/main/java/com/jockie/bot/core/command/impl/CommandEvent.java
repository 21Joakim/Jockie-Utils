package com.jockie.bot.core.command.impl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.cooldown.ICooldown;
import com.jockie.bot.core.cooldown.ICooldownManager;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

public class CommandEvent {
	
	private MessageReceivedEvent event;
	private CommandListener commandListener;
	
	private ICommand command;
	
	private Object[] arguments;
	
	private String prefix;
	private String commandTrigger;
	
	private List<String> optionsPresent;
	
	public CommandEvent(MessageReceivedEvent event, CommandListener listener, ICommand command, 
			Object[] arguments, String prefix, String commandTrigger, List<String> optionsPresent) {
		
		this.event = event;
		this.commandListener = listener;
		
		this.command = command;
		
		this.arguments = arguments;
		
		this.prefix = prefix;
		this.commandTrigger = commandTrigger;
		
		this.optionsPresent = optionsPresent;
	}
	
	/** @return the message event which triggered the command */
	public MessageReceivedEvent getEvent() {
		return this.event;
	}
	
	/** Equivalent to {@link MessageReceivedEvent#getJDA()} */
	public JDA getJDA() {
		return this.event.getJDA();
	}
	
	/** Equivalent to {@link JDA#getSelfUser()} */
	public User getSelfUser() {
		return this.getJDA().getSelfUser();
	}
	
	/** Equivalent to {@link Guild#getSelfMember()}
	 * 
	 * @return possibly-null if the event is not from a guild
	 */
	public Member getSelfMember() {
		Guild guild = this.getGuild();
		
		return guild != null ? guild.getSelfMember() : null;
	}
	
	/** Equivalent to {@link MessageReceivedEvent#getAuthor()} */
	public User getAuthor() {
		return this.event.getAuthor();
	}
	
	/** Equivalent to {@link MessageReceivedEvent#getMember()} */
	public Member getMember() {
		return this.event.getMember();
	}
	
	/** Equivalent to {@link MessageReceivedEvent#getChannel()} */
	public MessageChannel getChannel() {
		return this.event.getChannel();
	}
	
	/** Equivalent to {@link MessageReceivedEvent#getTextChannel()} */
	public TextChannel getTextChannel() {
		return this.event.getTextChannel();
	}
	
	/** Equivalent to {@link MessageReceivedEvent#getChannelType()} */
	public ChannelType getChannelType() {
		return this.event.getChannelType();
	}
	
	/** Equivalent to {@link MessageReceivedEvent#getGuild()} */
	public Guild getGuild() {
		return this.event.getGuild();
	}
	
	/** Equivalent to {@link MessageReceivedEvent#getMessage()} */
	public Message getMessage() {
		return this.event.getMessage();
	}
	
	/** @return the {@link CommandListener} which handled the command */
	public CommandListener getCommandListener() {
		return this.commandListener;
	}
	
	/** @return the command which was triggered */
	public ICommand getCommand() {
		return this.command;
	}
	
	/** @return the processed arguments */
	public Object[] getArguments() {
		return this.arguments;
	}
	
	/** @return the prefix which was used to trigger this command */
	public String getPrefix() {
		return this.prefix;
	}
	
	/** @return the string which triggered the command, this could the command or an alias */
	public String getCommandTrigger() {
		return this.commandTrigger;
	}
	
	/** @return all the options which were present when the command was executed */
	public List<String> getOptionsPresent() {
		return Collections.unmodifiableList(this.optionsPresent);
	}
	
	/** @return whether or not the option specified is present */
	public boolean isOptionPresent(String option) {
		return this.optionsPresent.contains(option);
	}
	
	/** Equivalent to {@link MessageChannel#sendMessage(CharSequence)}, using the event's channel */
	public MessageAction reply(CharSequence text) {
		return this.getChannel().sendMessage(text);
	}
	
	/** Equivalent to {@link MessageChannel#sendMessage(CharSequence)}, using the event's channel */
	public MessageAction reply(MessageEmbed embed) {
		return this.getChannel().sendMessage(embed);
	}
	
	/** Equivalent to {@link MessageChannel#sendMessage(CharSequence)}, using the event's channel */
	public MessageAction reply(Message message) {
		return this.getChannel().sendMessage(message);
	}
	
	/** Apply a cooldown to this command */
	public ICooldown applyCooldown() {
		return this.commandListener.getCoooldownManager().createCooldownAndGet(this.command, this.event);
	}
	
	/** Apply a cooldown to this command */
	public ICooldown applyCooldown(long duration, TimeUnit unit) {
		ICooldownManager manager = this.commandListener.getCoooldownManager();
		ICooldown cooldown = manager.createEmptyCooldown(this.command.getCooldownScope(), duration, unit);
		cooldown.applyContext(this.event);
		
		this.commandListener.getCoooldownManager().applyCooldown(this.command, cooldown);
		
		return cooldown;
	}
	
	/** Remove the cooldown from this command */
	public ICooldown removeCooldown() {
		return this.commandListener.getCoooldownManager().removeCooldown(this.command, this.event);
	}
}