package com.jockie.bot.core.command.impl;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jockie.bot.core.argument.parser.IArgumentParser;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.ICommand.ArgumentParsingType;
import com.jockie.bot.core.command.ICommand.ContentOverflowPolicy;
import com.jockie.bot.core.command.exception.CancelException;
import com.jockie.bot.core.command.manager.IContextManager;
import com.jockie.bot.core.command.manager.impl.ContextManagerFactory;
import com.jockie.bot.core.cooldown.ICooldown;
import com.jockie.bot.core.cooldown.ICooldownManager;

import net.dv8tion.jda.bot.JDABot;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDA.ShardInfo;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.GuildVoiceState;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

public class CommandEvent {
	
	protected Message message;
	protected CommandListener commandListener;
	
	protected ICommand command;
	
	protected Object[] arguments;
	protected String[] rawArguments;
	
	protected String prefix;
	protected String commandTrigger;
	
	protected List<String> optionsPresent;
	
	protected ArgumentParsingType parsingType;
	
	protected String contentOverflow;
	
	protected long timeStarted;
	
	/**
	 * @param message the context for this; the message which was sent to trigger this command
	 * @param listener the command listener which the command is registered to
	 * @param command the command which was parsed
	 * @param arguments the parsed arguments
	 * @param rawArguments the raw arguments before they were processed
	 * @param prefix the prefix which was used to trigger this
	 * @param commandTrigger the String which was used to trigger this command, could be an alias
	 * @param optionsPresent a list of the raw options provided in this command
	 * @param parsingType the type of parsing which was used to parse this command
	 * @param contentOverflow any additional content 
	 * @param timeStarted the time as {@link System#nanoTime()} when this started parsing
	 */
	public CommandEvent(Message message, CommandListener listener, ICommand command, 
			Object[] arguments, String[] rawArguments, String prefix, String commandTrigger, 
			List<String> optionsPresent, ArgumentParsingType parsingType, String contentOverflow, long timeStarted) {
		
		this.message = message;
		this.commandListener = listener;
		
		this.command = command;
		
		this.arguments = arguments;
		this.rawArguments = rawArguments;
		
		this.prefix = prefix;
		this.commandTrigger = commandTrigger;
		
		this.optionsPresent = optionsPresent;
		
		this.parsingType = parsingType;
		
		this.contentOverflow = contentOverflow;
		
		this.timeStarted = timeStarted;
	}
	
	/** @return the message which triggered the command */
	public Message getMessage() {
		return this.message;
	}
	
	/** Equivalent to {@link Message#getJDA()} */
	public JDA getJDA() {
		return this.message.getJDA();
	}
	
	/**
	 * Equivalent to {@link JDABot#getShardManager()}
	 */
	public ShardManager getShardManager() {
		return this.message.getJDA().asBot().getShardManager();
	}
	
	/**
	 * Equivalent to {@link JDA#getShardInfo()}
	 */
	public ShardInfo getShardInfo() {
		return this.message.getJDA().getShardInfo();
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
	
	/** Equivalent to {@link Message#getAuthor()} */
	public User getAuthor() {
		return this.message.getAuthor();
	}
	
	/** Equivalent to {@link Message#getMember()} */
	public Member getMember() {
		return this.message.getMember();
	}
	
	/** Equivalent to {@link Message#getChannel()} */
	public MessageChannel getChannel() {
		return this.message.getChannel();
	}
	
	/** Equivalent to {@link Message#getTextChannel()} */
	public TextChannel getTextChannel() {
		return this.message.getTextChannel();
	}
	
	/** Equivalent to {@link Message#getPrivateChannel()} */
	public PrivateChannel getPrivateChannel() {
		return this.message.getPrivateChannel();
	}
	
	/** Equivalent to {@link Message#getGroup()} */
	public Group getGroup() {
		return this.message.getGroup();
	}
	
	/** Equivalent to {@link Message#getChannelType()} */
	public ChannelType getChannelType() {
		return this.message.getChannelType();
	}
	
	/** Equivalent to {@link Message#getGuild()} */
	public Guild getGuild() {
		return this.message.getGuild();
	}
	
	/** Equivalent to {@link Member#getVoiceState()} called on the Member that executed this 
	 *
	 * @return possibly-null if the event is not from a guild
	 */
	public GuildVoiceState getVoiceState() {
		Member member = this.getMember();
		
		return member != null ? member.getVoiceState() : null;
	}
	
	/** @return the {@link CommandListener} which handled the command */
	public CommandListener getCommandListener() {
		return this.commandListener;
	}
	
	/** @return the command which was executed, this should always return the actual instance of the command */
	public ICommand getCommand() {
		if(this.command instanceof DummyCommand) {
			return this.command.getParent();
		}
		
		return this.command;
	}
	
	/** @return the actual command which was triggered and executed, this could be a {@link DummyCommand} */
	public ICommand getTriggeredCommand() {
		return this.command;
	}
	
	/** @return the processed arguments */
	public Object[] getArguments() {
		return this.arguments;
	}
	
	/** @return the arguments before they were processed, this contains the exact values that were given to the argument parsers ({@link IArgumentParser}) */
	public String[] getRawArguments() {
		return this.rawArguments;
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
	public boolean isAuthorDeveloper() {
		return this.commandListener.isDeveloper(this.message.getAuthor());
	}
	
	/** @return the string which triggered the command, this could be the command or an alias */
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
	
	/** @return the time as {@link System#nanoTime()} when this started parsing */
	public long getTimeStarted() {
		return this.timeStarted;
	}
	
	/** @return the time in nanoseconds since this started parsing */
	public long getTimeSinceStarted() {
		return System.nanoTime() - this.timeStarted;
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
	
	/** Equivalent to {@link MessageChannel#sendFile(File)}, using the event's channel */
	public MessageAction replyFile(File file) {
		return this.getChannel().sendFile(file);
	}
	
	/** Equivalent to {@link MessageChannel#sendFile(byte[], String)}, using the event's channel */
	public MessageAction replyFile(byte[] data, String fileName) {
		return this.getChannel().sendFile(data, fileName);
	}
	
	/** Equivalent to {@link MessageChannel#sendFile(File, Message)}, using the event's channel */
	public MessageAction replyFile(File file, Message message) {
		return this.getChannel().sendFile(file, message);
	}
	
	/** Equivalent to {@link MessageChannel#sendFile(File, String)}, using the event's channel */
	public MessageAction replyFile(File file, String fileName) {
		return this.getChannel().sendFile(file, fileName);
	}
	
	/** Equivalent to {@link MessageChannel#sendFile(InputStream, String)}, using the event's channel */
	public MessageAction replyFile(InputStream data, String fileName) {
		return this.getChannel().sendFile(data, fileName);
	}
	
	/** Equivalent to {@link MessageChannel#sendFile(byte[], String, message)}, using the event's channel */
	public MessageAction replyFile(byte[] data, String fileName, Message message) {
		return this.getChannel().sendFile(data, fileName, message);
	}
	
	/** Equivalent to {@link MessageChannel#sendFile(File, String, Message)}, using the event's channel */
	public MessageAction replyFile(File file, String fileName, Message message) {
		return this.getChannel().sendFile(file, fileName, message);
	}
	
	/** Equivalent to {@link MessageChannel#sendFile(InputStream, String, Message)}, using the event's channel */
	public MessageAction replyFile(InputStream data, String fileName, Message message) {
		return this.getChannel().sendFile(data, fileName, message);
	}
	
	/** Equivalent to {@link MessageChannel#sendMessageFormat(String, Object...)}, using the event's channel */
	public MessageAction replyFormat(String format, Object... args) {
		return this.getChannel().sendMessageFormat(format, args);
	}
	
	/** Equivalent to {@link MessageChannel#sendTyping()}, using the event's channel */
	public RestAction<Void> replyTyping() {
		return this.getChannel().sendTyping();
	}
	
	/** Equivalent to {@link Message#addReaction(Emote)}, using the event's message */
	public RestAction<Void> react(Emote emote) {
		return this.getMessage().addReaction(emote);
	}
	
	/** Equivalent to {@link Message#addReaction(String)}, using the event's message */
	public RestAction<Void> react(String unicode) {
		return this.getMessage().addReaction(unicode);
	}
	
	/** throws a new CancelException to cancel the execution of the current command */
	public void cancel() {
		throw new CancelException();
	}
	
	/** Apply a cooldown to this command */
	public ICooldown applyCooldown() {
		return this.commandListener.getCoooldownManager().applyCooldownAndGet(this.command, this.message);
	}
	
	/** Apply a cooldown to this command */
	public ICooldown applyCooldown(long duration, TimeUnit unit) {
		ICooldownManager manager = this.commandListener.getCoooldownManager();
		ICooldown cooldown = manager.createEmptyCooldown(this.command.getCooldownScope(), duration, unit);
		cooldown.applyContext(this.message);
		
		this.commandListener.getCoooldownManager().applyCooldown(this.command, cooldown);
		
		return cooldown;
	}
	
	/** Remove the cooldown from this command */
	public ICooldown removeCooldown() {
		return this.commandListener.getCoooldownManager().removeCooldown(this.command, this.message);
	}
	
	/**
	 * @param clazz the type of the context
	 * 
	 * @return the context for the provided type, gotten from {@link IContextManager#getContext(CommandEvent, Class)}
	 */
	public <T> T getContext(Class<T> clazz) {
		return ContextManagerFactory.getDefault().getContext(this, clazz);
	}
	
	/**
	 * @param type the type of the context
	 * 
	 * @return the context for the provided type, gotten from {@link IContextManager#getContext(CommandEvent, Type)}
	 */
	public <T> T getContext(Type type) {
		return ContextManagerFactory.getDefault().getContext(this, type);
	}
}