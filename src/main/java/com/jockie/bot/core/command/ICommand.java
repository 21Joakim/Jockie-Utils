package com.jockie.bot.core.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.IEndlessArgument;
import com.jockie.bot.core.category.ICategory;
import com.jockie.bot.core.command.exception.parser.ContentOverflowException;
import com.jockie.bot.core.command.exception.parser.OptionParseException;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandListener;
import com.jockie.bot.core.command.parser.ICommandParser;
import com.jockie.bot.core.cooldown.ICooldown;
import com.jockie.bot.core.option.IOption;
import com.jockie.bot.core.property.IPropertyContainer;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.internal.utils.Checks;

public interface ICommand extends IPropertyContainer {
	
	/**
	 * This is used to determine how the {@link ICommandParser} should handle a command when an unknown option is provided
	 */
	public static enum UnknownOptionPolicy {
		/** Adds the option to the map of provided options which can then be accessed through {@link CommandEvent#getOptions()}  */
		ADD,
		/** Includes the option content as an argument instead of an option */
		INCLUDE,
		/** Ignores (removes) the option from the message */
		IGNORE,
		/** Fails the command */
		FAIL;
	}
	
	/**
	 * This is used to determine how the {@link ICommandParser} should handle a command when a duplicate option is provided
	 */
	public static enum DuplicateOptionPolicy {
		/** Uses the first defined option and ignores the rest */
		USE_FIRST,
		/** Uses the last defined option and ignores the rest */
		USE_LAST,
		/** Combines all the options in to a list */
		COMBINE,
		/** Fails the command with a {@link DuplicateOptionPolicy} */
		FAIL;
	}
	
	/**
	 * This is used to determine how the {@link ICommandParser} should handle a command when an option fails to parse correctly
	 */
	public static enum OptionParsingFailurePolicy {
		/** Includes the option content as an argument instead of an option */
		INCLUDE,
		/** Ignores (removes) the options from the message, the option will not be set */
		IGNORE,
		/** Fails the command with a {@link OptionParseException} */
		FAIL;
	}
	
	/**
	 * This is used to determine how the {@link ICommandParser} should handle a command when a message has more content than the command can take
	 */
	public static enum ContentOverflowPolicy {
		/** Ignores the rest of the content, this content can be acccessed through {@link CommandEvent#getContentOverflow()} */
		IGNORE,
		/** Fails the command with a {@link ContentOverflowException} */
		FAIL;
	}
	
	/**
	 * This is used to determine which parsing types are allowed for the {@link ICommandParser}
	 */
	public static enum ArgumentParsingType {
		/** Positional arguments are arguments which are not specified by key=value but rather the order/index they are in, for instance
		 * <b>!create role hello 8</b> where "hello" is the role name and "8" is the raw Discord permissions
		 */
		POSITIONAL,
		/** Named arguments are arguments which are not specified by their order/index but rather key=value, for instance 
		 * <b>!create role name=hello permissions=8</b>
		 */
		NAMED;
	}
	
	/**
	 * This is used to determine how the {@link ICommandParser} should handle spaces in argument
	 */
	public static enum ArgumentTrimType {
		/** Does nothing */
		NONE,
		/**
		 * Removes any leading and trailing spaces as long as they are not explicitly
		 * entered through a quote
		 */
		LENIENT,
		/** 
		 * Removes any leading and trailing spaces no matter what, 
		 * even if the argument was quoted like the following <b>" hello "</b> 
		 * it would still end up without any spaces
		 */
		STRICT;
	}
	
	/**
	 * @return the command which the command listener should look for
	 */
	@Nonnull
	public String getCommand();
	
	/**
	 * @return a boolean that will prove if this command should be able to be triggered by guild messages
	 */
	public boolean isGuildTriggerable();
	
	/**
	 * @return a boolean that will prove if this command should be able to be triggered by private messages
	 */
	public boolean isPrivateTriggerable();
	
	/**
	 * @return the command's arguments.
	 */
	@Nonnull
	public List<IArgument<?>> getArguments();
	
	/**
	 * @return a boolean that will prove if this command is hidden and should therefore not be shown in help commands
	 */
	public boolean isHidden();
	
	/**
	 * @return a short description of what the command does, preferable use would be a help command
	 */
	@Nullable
	public String getShortDescription();
	
	/**
	 * @return a description of what this command does
	 */
	@Nullable
	public String getDescription();
	
	/**
	 * @return all the possible aliases for this command
	 */
	@Nonnull
	public List<String> getAliases();
	
	/**
	 * @return all options for this command
	 */
	@Nonnull
	public List<IOption<?>> getOptions();
	
	/**
	 * @return a {@link UnknownOptionPolicy} which is used to determine how the {@link CommandListener} should handle a command when an unknown option is provided
	 */
	@Nonnull
	public UnknownOptionPolicy getUnknownOptionPolicy();
	
	/**
	 * @return a {@link DuplicateOptionPolicy} which is used to determine how the {@link CommandListener} should handle a command when a duplicate option is provided
	 */
	@Nonnull
	public DuplicateOptionPolicy getDuplicateOptionPolicy();
	
	/**
	 * @return a {@link OptionParsingFailurePolicy} which is used to determine how the {@link ICommandParser} should handle a command when an option fails to parse correctly
	 */
	@Nonnull
	public OptionParsingFailurePolicy getOptionParsingFailurePolicy();
	
	/**
	 * @return a {@link ContentOverflowPolicy} which is used to determine how the {@link CommandListener} should handle a command when a message has more content than the command can take
	 */
	@Nonnull
	public ContentOverflowPolicy getContentOverflowPolicy();
	
	/**
	 * @return an array of {@link ArgumentParsingType}s which is used to determine how the arguments are allowed to be defined
	 */
	@Nonnull
	public Set<ArgumentParsingType> getAllowedArgumentParsingTypes();
	
	/**
	 * @return the argument trim type, this is used to determine how spaces in arguments are handled
	 */
	@Nonnull
	public ArgumentTrimType getArgumentTrimType();
	
	/**
	 * @return the discord permissions required for this command to function correctly.
	 */
	@Nonnull
	public Set<Permission> getBotDiscordPermissions();
	
	/**
	 * @return the discord permissions the author is required to have to trigger this command
	 */
	@Nonnull
	public Set<Permission> getAuthorDiscordPermissions();
	
	/**
	 * @return a boolean that will prove if this command is a <strong>developer</strong> command, if it is a developer command it can only be triggered by developers/authorised users
	 */
	public boolean isDeveloperCommand();
	
	/**
	 * @return a boolean that will prove if this command can be triggered by a bot {@link net.dv8tion.jda.api.entities.User#isBot() User.isBot()}
	 */
	public boolean isBotTriggerable();
	
	/**
	 * @return a boolean that will prove if this command is case sensitive.<p>
	 * For instance if {@link com.jockie.bot.core.command.ICommand#getCommand() Command.getCommand()} 
	 * is equal to <strong>ping</strong> and {@link com.jockie.bot.core.command.ICommand#isCaseSensitive() Command.isCaseSensitive()} 
	 * is <strong>false</strong> then the command could be triggered by any message that {@link String#toLowerCase()} would be equal to <strong>ping</strong>.<br>
	 * On the other hand if {@link com.jockie.bot.core.command.ICommand#isCaseSensitive() Command.isCaseSensitive()} is <strong>true</strong> and
	 * {@link com.jockie.bot.core.command.ICommand#getCommand() Command.getCommand()} is equal to <strong>PiNg</strong> 
	 * then the command could only be triggered if the message is equal to <strong>PiNg</strong> 
	 */
	public boolean isCaseSensitive();
	
	/**
	 * @return the cooldown duration in milliseconds which will be applied to a user
	 * after they use this command. If the cooldown is less than or equal to 0 no
	 * cooldown will be applied
	 */
	public long getCooldownDuration();
	
	/**
	 * @return the scope of which the cooldown should be applied to, for instance if {@link ICooldown.Scope#USER_GUILD}
	 * is used it will be applied for a user per a guild basis.
	 */
	@Nonnull
	public ICooldown.Scope getCooldownScope();
	
	/**
	 * @return a boolean that will tell whether the command should be executed on a separate thread or not
	 */
	public boolean isExecuteAsync();
	
	/**
	 * @param event the context
	 * 
	 * @return an object that will determine what order asynchronous commands should be executed in
	 */
	@Nullable
	public Object getAsyncOrderingKey(@Nonnull CommandEvent event);
	
	/**
	 * @return the parent of this command, a parent is used to get the full trigger for this command, 
	 * for instance if the parent's command trigger was "mute" and this command's trigger was "all" the whole trigger would be "mute all"
	 */
	@Nullable
	public ICommand getParent();
	
	/**
	 * @return a boolean that will tell whether or not this command has a parent
	 * 
	 * @see ICommand#getParent()
	 */
	public default boolean hasParent() {
		return this.getParent() != null;
	}
	
	/**
	 * @return the category of this command
	 */
	@Nullable
	public ICategory getCategory();
	
	/**
	 * @return a boolean to prove whether this command is passive or not, a passive command will not have any executable method and might for instance only have sub-commands
	 */
	public boolean isPassive();
	
	/**
	 * @return a boolean to prove whether this command is NSFW or not, NSFW commands will not be usable in non-NSFW channels
	 */
	public boolean isNSFW();
	
	/**
	 * @return all sub-commands for this command
	 */
	@Nonnull
	public List<ICommand> getSubCommands();
	
	/**
	 * Checks whether this command can be executed in the context provided, this
	 * is checked before a command is parsed and should include checks such as
	 * {@link CommandListener#isDeveloper(long)}
	 * 
	 * @param message the context
	 * @param commandListener which {@link CommandListener} handled the request
	 * 
	 * @return whether or not the command can be executed in the provided context
	 */
	public default boolean isAccessible(@Nonnull Message message, @Nonnull CommandListener commandListener) {
		Checks.notNull(message, "message");
		Checks.notNull(commandListener, "commandListener");
		
		if(message.getAuthor().getIdLong() == message.getJDA().getSelfUser().getIdLong()) {
			return false;
		}
		
		if(!this.isBotTriggerable() && message.getAuthor().isBot()) {
			return false;
		}
		
		if(!this.isGuildTriggerable() && message.getChannelType().isGuild()) {
			return false;
		}
		
		if(!this.isPrivateTriggerable() && message.getChannelType() == ChannelType.PRIVATE) {
			return false;
		}
		
		if(this.isDeveloperCommand() && !commandListener.isDeveloper(message.getAuthor().getIdLong())) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * This is what should be executed when this command is considered to be valid
	 * 
	 * @param event the event which triggered the command
	 * @param arguments the arguments which triggered the command
	 * 
	 * @throws Throwable if the command in any way fails
	 */
	public void execute(@Nonnull CommandEvent event, @Nonnull Object... arguments) throws Throwable;
	
	/**
	 * @param message the context
	 * 
	 * @return all commands which are related to this command, sub-commands and dummy commands as well as all the aliases, with the appropriate triggers
	 * 
	 * @see ICommand#getAllCommandsRecursiveWithTriggers(Message, String)
	 */
	@Nonnull
	public default List<CommandTrigger> getAllCommandsRecursiveWithTriggers(@Nonnull Message message) {
		return this.getAllCommandsRecursiveWithTriggers(message, "");
	}
	
	/**
	 * @param message the context
	 * @param prefix the start of the trigger, used for recursively getting sub-commands
	 * 
	 * @return all commands which are related to this command, sub-commands and dummy commands as well as all the aliases, with the appropriate triggers
	 */
	
	/* This system should really be reconsidered. For instance, the message parameter used for context is not exactly required 
	 * and is only used for custom alias functions. As much as I do want to have the ability to have an alias function 
	 * that feature does not justify poor design.
	 * 
	 * I also want to make this whole thing better, this is mostly an internal method to get both the aliases and 
	 * commands for everything from the command itself to deeply embedded sub-commands. I am sure there is a better more 
	 * user-friendly way of having this or doing away with it completely, after all it is 99% an internal method.
	 */
	@Nonnull
	public default List<CommandTrigger> getAllCommandsRecursiveWithTriggers(@Nonnull Message message, @Nonnull String prefix) {
		Checks.notNull(message, "message");
		Checks.notNull(prefix, "prefix");
		
		List<CommandTrigger> commands = new ArrayList<>();
		
		commands.add(new CommandTrigger((prefix + " " + this.getCommand()).trim(), this));
		
		List<String> aliases = this.getAliases();
		for(String alias : aliases) {
			commands.add(new CommandTrigger((prefix + " " + alias).trim(), this));
		}
		
		for(ICommand command : this.getSubCommands()) {
			commands.addAll(command.getAllCommandsRecursiveWithTriggers(message, (prefix + " " + this.getCommand()).trim()));
			
			for(String alias : aliases) {
				commands.addAll(command.getAllCommandsRecursiveWithTriggers(message, (prefix + " " + alias).trim()));
			}
		}
		
		return commands;
	}
	
	/** 
	 * @param includeDummyCommands whether or not {@link com.jockie.bot.core.command.impl.DummyCommand DummyCommand}s should be included
	 * 
	 * @return all commands which are related to this command, sub-commands and optional dummy commands
	 */
	@Nonnull
	/* Including a default implementation in case people wants to make their own ICommand implementation */
	/* Not sure if the includeDummyCommands variable should exist or not, it is more or less an internal thing and is not really supposed to be used */
	public default List<ICommand> getAllCommandsRecursive(boolean includeDummyCommands) {
		List<ICommand> commands = new ArrayList<>();
		commands.add(this);
		
		for(ICommand command : this.getSubCommands()) {
			commands.addAll(command.getAllCommandsRecursive(includeDummyCommands));
		}
		
		return commands;
	}
	
	/** 
	 * @return all commands which are related to this command, this includes sub-commands
	 * 
	 * @see ICommand#getAllCommandsRecursive(boolean)
	 */
	@Nonnull
	public default List<ICommand> getAllCommandsRecursive() {
		return this.getAllCommandsRecursive(false);
	}
	
	/**
	 * @param command the command to get the argument info for
	 * 
	 * @return the information about all the arguments of the provided command
	 */
	/* TODO: Reconsider this to allow custom implementations */
	@Nonnull
	public static String getArgumentInfo(@Nonnull ICommand command) {
		Checks.notNull(command, "command");
		
		StringBuilder builder = new StringBuilder();
		
		List<IArgument<?>> arguments = command.getArguments();
		for(int i = 0; i < arguments.size(); i++) {
			IArgument<?> argument = arguments.get(i);
			
			String name = argument.getName();
			if(name == null) {
				name = "argument " + (i + 1);
			}
			
			builder.append("<").append(name).append(">");
			
			if(argument instanceof IEndlessArgument) {
				IEndlessArgument<?> endlessArgument = (IEndlessArgument<?>) argument;
				
				builder.append("[")
					.append(endlessArgument.getMinArguments())
					.append("-")
					.append((endlessArgument.getMaxArguments() != 0) ? endlessArgument.getMaxArguments() + "]" : "...]");
			}
			
			if(!argument.hasDefault()) {
				builder.append("*");
			}
			
			if(i < arguments.size() - 1) {
				builder.append(" ");
			}
		}
		
		return builder.toString();
	}
	
	/**
	 * @return the information about all the arguments of this command
	 * 
	 * @see ICommand#getArgumentInfo()
	 */
	@Nonnull
	public default String getArgumentInfo() {
		return ICommand.getArgumentInfo(this);
	}
	
	/**
	 * 
	 * @param command the command to get the usage for
	 * @param prefix the prefix displayed along with the usage
	 * 
	 * @return full usage information for the provided command, this includes the prefix, command and {@link #getArgumentInfo()}
	 */
	@Nonnull
	public static String getUsage(@Nonnull ICommand command, @Nonnull String prefix) {
		Checks.notNull(command, "command");
		Checks.notNull(prefix, "prefix");
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(prefix)
			.append(command.getCommandTrigger())
			.append(" ")
			.append(command.getArgumentInfo());
		
		return builder.toString();
	}
	
	/**
	 * @param command the command to get the usage for
	 * 
	 * @return full usage information for the provided command, this includes the command and {@link #getArgumentInfo()}
	 * 
	 * @see ICommand#getUsage(ICommand, String)
	 */
	@Nonnull
	public static String getUsage(@Nonnull ICommand command) {
		return ICommand.getUsage(command, "");
	}
	
	/**
	 * @param prefix the prefix displayed along with the usage
	 * 
	 * @return full usage information for this command, this includes the prefix, command and {@link #getArgumentInfo()}
	 * 
	 * @see ICommand#getUsage(ICommand, String)
	 */
	@Nonnull
	public default String getUsage(@Nonnull String prefix) {
		return ICommand.getUsage(this, prefix);
	}
	
	/**
	 * @return full usage information for this command, this includes the command and {@link #getArgumentInfo()}
	 * 
	 * @see ICommand#getUsage(ICommand, String)
	 */
	@Nonnull
	public default String getUsage() {
		return ICommand.getUsage(this);
	}
	
	/**
	 * @param command the command to get the trigger for
	 * 
	 * @return the actual trigger for the provided command which is created by recursively getting the parents of this command
	 */
	@Nonnull
	public static String getCommandTrigger(@Nonnull ICommand command) {
		Checks.notNull(command, "command");
		
		String trigger = command.getCommand();
		
		ICommand parent = command;
		while((parent = parent.getParent()) != null) {
			trigger = (parent.getCommand() + " " + trigger).trim();
		}
		
		return trigger;
	}
	
	/**
	 * @return the actual trigger for this command which is created by recursively getting the parents of this command
	 */
	@Nonnull
	public default String getCommandTrigger() {
		return ICommand.getCommandTrigger(this);
	}
	
	/**
	 * This uses {@link #getTopParent()} recursively to get the top parent
	 * 
	 * @param command the command to get the top parent for
	 * 
	 * @return the top parent of the provided command or the command itself if it has no parent
	 */
	@Nonnull
	public static ICommand getTopParent(@Nonnull ICommand command) {
		Checks.notNull(command, "command");
		
		ICommand parent = command.getParent();
		if(parent != null && command.hasParent()) {
			while(parent != null && parent.hasParent()) {
				parent = parent.getParent();
			}
			
			return parent;
		}
		
		return command;
	}
	
	/**
	 * @return the top parent of this command or itself if it has no parent
	 * 
	 * @see ICommand#getTopParent(ICommand)
	 */
	@Nonnull
	public default ICommand getTopParent() {
		return ICommand.getTopParent(this);
	}
}