package com.jockie.bot.core.command.impl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.ICommand.ArgumentParsingType;
import com.jockie.bot.core.command.ICommand.ContentOverflowPolicy;
import com.jockie.bot.core.command.exception.CancelException;
import com.jockie.bot.core.cooldown.ICooldown;
import com.jockie.bot.core.cooldown.ICooldownManager;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceState;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

public class CommandEvent {
	
	protected MessageReceivedEvent event;
	protected CommandListener commandListener;
	
	protected ICommand command;
	
	protected Object[] arguments;
	
	protected String prefix;
	protected String commandTrigger;
	
	protected List<String> optionsPresent;
	
	protected ArgumentParsingType parsingType;
	
	protected String contentOverflow;
	
	public CommandEvent(MessageReceivedEvent event, CommandListener listener, ICommand command, 
			Object[] arguments, String prefix, String commandTrigger, List<String> optionsPresent,
			ArgumentParsingType parsingType, String contentOverflow) {
		
		this.event = event;
		this.commandListener = listener;
		
		this.command = command;
		
		this.arguments = arguments;
		
		this.prefix = prefix;
		this.commandTrigger = commandTrigger;
		
		this.optionsPresent = optionsPresent;
		
		this.parsingType = parsingType;
		
		this.contentOverflow = contentOverflow;
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
	
	/** Equivalent to {@link MessageReceivedEvent#getPrivateChannel()} */
	public PrivateChannel getPrivateChannel() {
		return this.event.getPrivateChannel();
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
	
	/** Equivalent to {@link Member#getVoiceState()} called on the Member that executed this 
	 *
	 * @return possibly-null if the event is not from a guild
	 */
	public VoiceState getVoiceState() {
		Member member = this.getMember();
		
		return member != null ? member.getVoiceState() : null;
	}
	
	/** @return the {@link CommandListener} which handled the command */
	public CommandListener getCommandListener() {
		return this.commandListener;
	}
	
	/** @return the command which was triggered */
	public ICommand getCommand() {
		return this.command;
	}
	
	/** @return the actual command; if this command is a DummyCommand the command which it replicates is the command which will be returned */
	public ICommand getActualCommand() {
		if(this.command instanceof DummyCommand) {
			return this.command.getParent();
		}
		
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
	
	/** @return true if the prefix is a mention of the current bot (<@bot_id> or <@!bot_id> bot_id being the value of {@link User#getIdLong()}) */
	public boolean isPrefixMention() {
		long id = this.getSelfUser().getIdLong();
		
		return this.prefix.equals("<@" + id + ">") || this.prefix.equals("<@!" + id + ">");
	}
	
	/** @return true if the person that executed this command is a developer ({@link CommandListener#isDeveloper(long)}) */
	public boolean isDeveloper() {
		return this.commandListener.isDeveloper(this.event.getAuthor());
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
	
	/** @return the argument parsing type used for the parsing of this command */
	public ArgumentParsingType getParsingType() {
		return this.parsingType;
	}
	
	/** @return the overflown content of the parsed command, this will always be empty if {@link ICommand#getContentOverflowPolicy()} is {@link ContentOverflowPolicy#FAIL} */
	public String getContentOverflow() {
		return this.contentOverflow;
	}
	
	/** Equivalent to {@link MessageChannel#sendMessage(CharSequence)}, using the event's channel */
	public MessageAction reply(CharSequence text) {
		return this.getChannel().sendMessage(text);
	}
	
	/** Equivalent to {@link MessageChannel#sendMessage(MessageEmbed)}, using the event's channel */
	public MessageAction reply(MessageEmbed embed) {
		return this.getChannel().sendMessage(embed);
	}
	
	/** Equivalent to {@link MessageChannel#sendMessage(Message)}, using the event's channel */
	public MessageAction reply(Message message) {
		return this.getChannel().sendMessage(message);
	}
	
	/** throws a new CancelException to cancel the execution of the current command */
	public void cancel() {
		throw new CancelException();
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