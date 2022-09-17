package com.jockie.bot.core.command.impl;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.ICommand.ArgumentParsingType;
import com.jockie.bot.core.command.ICommand.ContentOverflowPolicy;
import com.jockie.bot.core.command.exception.CancelException;
import com.jockie.bot.core.command.manager.IContextManager;
import com.jockie.bot.core.command.manager.impl.ContextManagerFactory;
import com.jockie.bot.core.cooldown.ICooldown;
import com.jockie.bot.core.cooldown.ICooldownManager;
import com.jockie.bot.core.property.IPropertyContainer;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.ShardInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

/* 
 * TODO: Refactor to CommandEventImpl with a CommandEvent interface,
 * this would break our current naming scheme of all interfaces starting
 * with I, so not sure if we should opt for ICommandEvent instead, I just
 * want the change to be as compatible with previous versions as possible.
 */
public class CommandEvent implements IPropertyContainer {
	
	protected Message message;
	protected CommandListener commandListener;
	
	protected ICommand command;
	
	protected Object[] arguments;
	protected String[] rawArguments;
	
	protected String prefix;
	protected String commandTrigger;
	
	protected Map<String, Object> options;
	
	protected ArgumentParsingType parsingType;
	
	protected String contentOverflow;
	
	protected long timeStarted;
	
	protected Map<String, Object> properties = new HashMap<>();
	
	/**
	 * @param message the context for this; the message which was sent to trigger this command
	 * @param listener the command listener which the command is registered to
	 * @param command the command which was parsed
	 * @param arguments the parsed arguments
	 * @param rawArguments the raw arguments before they were processed
	 * @param prefix the prefix which was used to trigger this
	 * @param commandTrigger the String which was used to trigger this command, could be an alias
	 * @param options a map of the raw options and their values provided in this command
	 * @param parsingType the type of parsing which was used to parse this command
	 * @param contentOverflow any additional content 
	 * @param timeStarted the time as {@link System#nanoTime()} when this started parsing
	 */
	public CommandEvent(Message message, CommandListener listener, ICommand command, 
			Object[] arguments, String[] rawArguments, String prefix, String commandTrigger, 
			Map<String, Object> options, ArgumentParsingType parsingType, String contentOverflow, long timeStarted) {
		
		this.message = message;
		this.commandListener = listener;
		
		this.command = command;
		
		this.arguments = arguments;
		this.rawArguments = rawArguments;
		
		this.prefix = prefix;
		this.commandTrigger = commandTrigger;
		
		this.options = options;
		
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
	 * Equivalent to {@link JDA#getShardManager()}
	 */
	public ShardManager getShardManager() {
		return this.getJDA().getShardManager();
	}
	
	/**
	 * Equivalent to {@link JDA#getShardInfo()}
	 */
	public ShardInfo getShardInfo() {
		return this.getJDA().getShardInfo();
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
	public MessageChannelUnion getChannel() {
		return this.message.getChannel();
	}
	
	/** Equivalent to {@link Message#getGuildChannel()} */
	public GuildMessageChannelUnion getGuildChannel() {
		return this.message.getGuildChannel();
	}
	
	/** 
	 * Equivalent to {@link GuildMessageChannelUnion#asTextChannel()} called on {@link CommandEvent#getGuildChannel()}
	 * 
	 * @deprecated this serves as compatibility for earlier versions, prefer {@link CommandEvent#getGuildChannel()}
	 */
	@Deprecated
	public TextChannel getTextChannel() {
		return this.getGuildChannel().asTextChannel();
	}
	
	/** 
	 * Equivalent to {@link MessageChannelUnion#asPrivateChannel()} called on {@link CommandEvent#getChannel()}
	 * 
	 * @deprecated this serves as compatibility for earlier versions, prefer {@link CommandEvent#getChannel()}
	 */
	@Deprecated
	public PrivateChannel getPrivateChannel() {
		return this.getChannel().asPrivateChannel();
	}
	
	/** Equivalent to {@link Message#getChannelType()} */
	public ChannelType getChannelType() {
		return this.message.getChannelType();
	}
	
	/** Equivalent to {@link ChannelType#isGuild()} called on the ChannelType of the Channel the message was sent from */
	public boolean isFromGuild() {
		return this.getChannelType().isGuild();
	}
	
	/** Equivalent to {@link Message#isFromType(ChannelType)} */
	public boolean isFromType(ChannelType channelType) {
		return this.message.isFromType(channelType);
	}
	
	/** Equivalent to {@link Message#getGuild()} */
	public Guild getGuild() {
		return this.message.getGuild();
	}
	
	/** Equivalent to {@link Member#getVoiceState()} called on {@link CommandEvent#getMember()}
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
			return ((DummyCommand) this.command).getActualCommand();
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
	
	/** @return the arguments before they were processed, this contains the exact values that were given to the argument parsers */
	public String[] getRawArguments() {
		return this.rawArguments;
	}
	
	/** @return the prefix which was used to trigger this command */
	public String getPrefix() {
		return this.prefix;
	}
	
	/** @return true if the prefix is a mention of the current bot, &lt;@bot_id&gt; or &lt;@!bot_id&gt; bot_id being the value of {@link User#getIdLong()} */
	public boolean isPrefixMention() {
		long id = this.getSelfUser().getIdLong();
		return this.prefix.equals("<@" + id + "> ") || this.prefix.equals("<@!" + id + "> ");
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
	public Map<String, Object> getOptions() {
		return Collections.unmodifiableMap(this.options);
	}
	
	/** @return the option by the provided name or null if there is no option by that name */
	public <T> T getOption(String name, Class<T> type) {
		return this.getOption(name);
	}
	
	/** @return the option by the provided name or null if there is no option by that name */
	@SuppressWarnings("unchecked")
	public <T> T getOption(String name) {
		return (T) this.options.get(name);
	}
	
	/** @return whether or not the option specified is present */
	public boolean isOptionPresent(String option) {
		return this.options.containsKey(option);
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
	@Nonnull
	public MessageCreateAction reply(CharSequence text) {
		return this.getChannel().sendMessage(text);
	}
	
	/** Equivalent to {@link MessageChannel#sendMessageEmbeds(MessageEmbed, MessageEmbed...)}, using the event's channel */
	@Nonnull
	public MessageCreateAction reply(MessageEmbed embed, MessageEmbed... other) {
		return this.getChannel().sendMessageEmbeds(embed, other);
	}
	
	/** Equivalent to {@link MessageChannel#sendMessage(MessageCreateData)}, using the event's channel */
	@Nonnull
	public MessageCreateAction reply(MessageCreateData message) {
		return this.getChannel().sendMessage(message);
	}
	
	/** 
	 * Equivalent to {@link MessageChannel#sendMessage(MessageCreateData)}, using the event's channel
	 * and created with {@link MessageCreateData#fromMessage(Message)}
	 */
	@Nonnull
	public MessageCreateAction reply(Message message) {
		return this.getChannel().sendMessage(MessageCreateData.fromMessage(message));
	}
	
	/** Equivalent to {@link MessageChannel#sendFiles(FileUpload...)}, using the event's channel */
	@Nonnull
	public MessageCreateAction replyFiles(FileUpload... files) {
		return this.getChannel().sendFiles(files);
	}
	
	/** 
	 * Equivalent to {@link MessageChannel#sendFiles(FileUpload...)}, using the event's channel
	 * and created with {@link FileUpload#fromData(File)}
	 */
	@Nonnull
	public MessageCreateAction replyFile(File file) {
		return this.getChannel().sendFiles(FileUpload.fromData(file));
	}
	
	/** 
	 * Equivalent to {@link MessageChannel#sendFiles(FileUpload...)}, using the event's channel
	 * and created with {@link FileUpload#fromData(byte[], String)}
	 */
	@Nonnull
	public MessageCreateAction replyFile(byte[] data, String fileName) {
		return this.getChannel().sendFiles(FileUpload.fromData(data, fileName));
	}
	
	/** 
	 * Equivalent to {@link MessageChannel#sendFiles(FileUpload...)}, using the event's channel
	 * and created with {@link FileUpload#fromData(File, String)}
	 */
	@Nonnull
	public MessageCreateAction replyFile(File file, String fileName) {
		return this.getChannel().sendFiles(FileUpload.fromData(file, fileName));
	}
	
	/** 
	 * Equivalent to {@link MessageChannel#sendFiles(FileUpload...)}, using the event's channel
	 * and created with {@link FileUpload#fromData(InputStream, String)}
	 */
	@Nonnull
	public MessageCreateAction replyFile(InputStream data, String fileName) {
		return this.getChannel().sendFiles(FileUpload.fromData(data, fileName));
	}
	
	/** Equivalent to {@link MessageChannel#sendMessageFormat(String, Object...)}, using the event's channel */
	@Nonnull
	public MessageCreateAction replyFormat(String format, Object... args) {
		return this.getChannel().sendMessageFormat(format, args);
	}
	
	/** Equivalent to {@link MessageChannel#sendTyping()}, using the event's channel */
	@Nonnull
	public RestAction<Void> replyTyping() {
		return this.getChannel().sendTyping();
	}
	
	/** Equivalent to {@link Message#addReaction(Emoji)}, using the event's message */
	@Nonnull
	public RestAction<Void> react(Emoji emoji) {
		return this.getMessage().addReaction(emoji);
	}
	
	/** Equivalent to {@link Message#addReaction(Emoji)} with {@link Emoji#fromFormatted(String)}, using the event's message */
	@Nonnull
	public RestAction<Void> react(String emoji) {
		return this.getMessage().addReaction(Emoji.fromFormatted(emoji));
	}
	
	/** 
	 * Throws a {@link CancelException} which is then caught by the
	 * {@link CommandListener} to cancel the execution of the current command
	 */
	public void cancel() {
		throw new CancelException();
	}
	
	/** Apply a cooldown to this command */
	@Nonnull
	public ICooldown applyCooldown() {
		return this.commandListener.getCoooldownManager().applyCooldownAndGet(this.getCommand(), this.message);
	}
	
	/** Apply a cooldown to this command */
	@Nonnull
	public ICooldown applyCooldown(long duration, TimeUnit unit) {
		ICooldownManager manager = this.commandListener.getCoooldownManager();
		ICooldown cooldown = manager.createEmptyCooldown(this.getCommand().getCooldownScope(), duration, unit);
		cooldown.applyContext(this.message);
		
		this.commandListener.getCoooldownManager().applyCooldown(this.getCommand(), cooldown);
		
		return cooldown;
	}
	
	/** Remove the cooldown from this command */
	@Nullable
	public ICooldown removeCooldown() {
		return this.commandListener.getCoooldownManager().removeCooldown(this.getCommand(), this.message);
	}
	
	/**
	 * @param type the type of the context
	 * 
	 * @return the context for the provided type, gotten from {@link IContextManager#getContext(CommandEvent, Class)}
	 */
	@Nullable
	public <T> T getContext(@Nonnull Class<T> type) {
		return ContextManagerFactory.getDefault().getContext(this, type);
	}
	
	/**
	 * @param type the type of the context
	 * 
	 * @return the context for the provided type, gotten from {@link IContextManager#getContext(CommandEvent, Type)}
	 */
	@Nullable
	public <T> T getContext(@Nonnull Type type) {
		return ContextManagerFactory.getDefault().getContext(this, type);
	}
	
	/**
	 * Get a custom property
	 * 
	 * @param name the property name
	 * @param defaultValue the default value if the property does not exist
	 * 
	 * @return the property value or the provided default value if it does not exist
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T> T getProperty(@Nonnull String name, @Nullable T defaultValue) {
		return (T) this.properties.getOrDefault(name, defaultValue);
	}
	
	@Override
	@Nonnull
	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(this.properties);
	}
	
	/**
	 * Set a custom property, this can be useful if you need to pass a
	 * property from a pre-execute check
	 * 
	 * @param name the property name
	 * @param value the property value
	 * 
	 * @return the {@link CommandEvent} instance, useful for chaining
	 */
	@Nonnull
	public <T> CommandEvent setProperty(@Nullable String name, @Nullable T value) {
		this.properties.put(name, value);
		
		return this;
	}
}