package com.jockie.bot.core.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.IEndlessArgument;
import com.jockie.bot.core.command.CommandTrigger;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.exception.CancelException;
import com.jockie.bot.core.command.exception.parser.ArgumentParseException;
import com.jockie.bot.core.command.exception.parser.OutOfContentException;
import com.jockie.bot.core.command.exception.parser.ParseException;
import com.jockie.bot.core.command.exception.parser.PassiveCommandException;
import com.jockie.bot.core.command.factory.ICommandEventFactory;
import com.jockie.bot.core.command.factory.impl.CommandEventFactoryImpl;
import com.jockie.bot.core.command.impl.DummyCommand.AlternativeCommand;
import com.jockie.bot.core.command.manager.IErrorManager;
import com.jockie.bot.core.command.manager.IReturnManager;
import com.jockie.bot.core.command.manager.impl.ErrorManagerImpl;
import com.jockie.bot.core.command.manager.impl.ReturnManagerImpl;
import com.jockie.bot.core.command.parser.ICommandParser;
import com.jockie.bot.core.command.parser.impl.CommandParserImpl;
import com.jockie.bot.core.cooldown.ICooldown;
import com.jockie.bot.core.cooldown.ICooldownManager;
import com.jockie.bot.core.cooldown.impl.CooldownManagerImpl;
import com.jockie.bot.core.utility.StringUtility;
import com.jockie.bot.core.utility.function.TriConsumer;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.JDALogger;

public class CommandListener implements EventListener {
	
	private static final Logger LOG = JDALogger.getLog(CommandListener.class);
	
	public final BiConsumer<CommandEvent, EnumSet<Permission>> defaultMissingPermissionsFunction = (event, permissions) -> {
		String message = String.format("I am missing the %s permission%s to execute that command", 
			StringUtility.concat(permissions, (permission) -> "**" + permission.getName() + "**"),
			permissions.size() == 1 ? "" : "s");
		
		if(event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_SEND)) {
			event.getChannel().sendMessage(message).queue();
			
			return;
		}
		
		String messageWithContext = message + String.format(" in %s, %s", event.getChannel().getName(), event.getGuild().getName());
		event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(messageWithContext).queue());
	};
	
	public final BiConsumer<CommandEvent, Permission> defaultMissingPermissionExceptionFunction = (event, permission) -> {
		this.defaultMissingPermissionsFunction.accept(event, EnumSet.of(permission));
	};
	
	public final BiConsumer<CommandEvent, EnumSet<Permission>> defaultMissingAuthorPermissionsFunction = (event, permissions) -> {
		String message = String.format("You are missing the %s permission%s to execute that command",
			StringUtility.concat(permissions, (permission) -> "**" + permission.getName() + "**"),
			permissions.size() == 1 ? "" : "s");
		
		event.reply(message).queue();
	};
	
	public final BiConsumer<CommandEvent, ICooldown> defaultCooldownFunction = (event, cooldown) -> {
		event.replyFormat("Slow down, try again in %s seconds", (double) cooldown.getTimeRemainingMillis()/1000).queue();
	};
	
	public final Consumer<CommandEvent> defaultNsfwFunction = (event) -> {
		event.reply("NSFW commands are not allowed in non-NSFW channels!").queue();
	};
	
	protected List<ICommand> getFailureCommands(List<Failure> failures) {
		Set<ICommand> commands = new LinkedHashSet<>();
		for(Failure failure : failures) {
			if(failure.getReason() instanceof PassiveCommandException) {
				continue;
			}
			
			ICommand command = failure.getCommand();
			if(command instanceof DummyCommand && !(command instanceof AlternativeCommand)) {
				command = ((DummyCommand) command).getActualCommand();
			}
			
			commands.add(command);
		}
		
		FAILURES:
		for(Failure failure : failures) {
			if(!(failure.getReason() instanceof PassiveCommandException)) {
				continue;
			}
			
			List<ICommand> subCommands = failure.getCommand().getSubCommands();
			for(ICommand subCommand : subCommands) {
				if(commands.contains(subCommand)) {
					continue FAILURES;
				}
			}
			
			commands.addAll(subCommands);
		}
		
		return new ArrayList<>(commands);
	}
	
	protected boolean checkDefaultPermissions(Message message) {
		if(!message.getChannelType().isGuild()) {
			return true;
		}
		
		Member bot = message.getGuild().getSelfMember();
		if(!bot.hasPermission(message.getTextChannel(), Permission.MESSAGE_SEND)) {
			message.getAuthor().openPrivateChannel().queue((channel) -> {
				channel.sendMessageFormat("I am missing the **%s** permission in %s, %s", Permission.MESSAGE_SEND.getName(), message.getChannel().getName(), message.getGuild().getName()).queue();
			});
			
			return false;
		}
		
		/* 
		 * TODO: This permission may not always be required,
		 * I think it is only required when sending the help response
		 */
		if(!bot.hasPermission(message.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
			message.getChannel().sendMessageFormat("I am missing the **%s** permission", Permission.MESSAGE_EMBED_LINKS.getName()).queue();
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * @return the effective parse failure
	 */
	protected Failure findParseFailure(List<Failure> failures) {
		for(Failure failure : failures) {
			Throwable reason = failure.getReason();
			if(reason instanceof ArgumentParseException && !(reason instanceof OutOfContentException)) {
				return failure;
			}
		}
		
		return null;
	}
	
	/**
	 * @return whether or not it was handled
	 */
	protected boolean handleParseFailure(Message message, String prefix, Failure failure) {
		ArgumentParseException parseException = (ArgumentParseException) failure.getReason();
		
		IArgument<?> argument = parseException.getArgument();
		String value = parseException.getValue();
		
		/* 
		 * TODO: Need a better way to check permissions, 
		 * alternatively just ignore permissions and let the user handle it.
		 * 
		 * We could handle it through the permission exception.
		 */
		if(!this.checkDefaultPermissions(message)) {
			return true;
		}
		
		BiConsumer<Message, String> errorConsumer = argument.getErrorConsumer();
		if(errorConsumer != null) {
			errorConsumer.accept(message, value);
			
			return true;
		}
		
		if(this.errorManager.handle(argument, message, value)) {
			return true;
		}
		
		return false;
	}
	
	public final TriConsumer<Message, String, List<Failure>> defaultMessageParseFailureFunction = (message, prefix, failures) -> {		
		List<ICommand> commands = this.getFailureCommands(failures);
		
		/* 
		 * Check whether or not any commands are left, 
		 * I think this could happen if a passive command was found with no sub-commands
		 */
		if(commands.isEmpty()) {
			return;
		}
		
		if(commands.size() == 1) {
			Failure failure = this.findParseFailure(failures);
			if(failure != null) {
				if(this.handleParseFailure(message, prefix, failure)) {
					return;
				}
			}
		}
		
		if(this.helpFunction == null) {
			return;
		}
		
		commands = commands.stream()
			.map((command) -> new CommandTrigger(command.getCommandTrigger(), command))
			.sorted(CommandTriggerComparator.INSTANCE)
			.map(CommandTrigger::getCommand)
			.collect(Collectors.toList());
		
		this.helpFunction.accept(message, prefix, commands);
	};
	
	private boolean hasEndlessArgument(ICommand command) {
		for(IArgument<?> argument : command.getArguments()) {
			if(argument instanceof IEndlessArgument<?>) {
				return true;
			}
		}
		
		return false;
	}
	
	/* TODO: Failures or commands, failures give more control but are less user-friendly */
	public final TriConsumer<Message, String, List<ICommand>> defaultHelpFunction = (message, prefix, commands) -> {
		if(!this.checkDefaultPermissions(message)) {
			return;
		}
		
		/* 
		 * Check whether or not any commands are left, 
		 * I think this could happen if a passive command was found with no sub-commands
		 */
		if(commands.isEmpty()) {
			return;
		}
		
		boolean endless = false;
		
		StringBuilder description = new StringBuilder();
		for(int i = 0; i < commands.size(); i++) {
			ICommand command = commands.get(i);
			
			endless = endless || this.hasEndlessArgument(command);
			
			description.append(command.getCommandTrigger())
				.append(" ")
				.append(command.getArgumentInfo());
			
			if(i < commands.size() - 1) {
				description.append("\n");
			}
		}
		
		StringBuilder footer = new StringBuilder("* = required argument.");
		if(endless) {
			footer.append(" [] = repeating argument.");
		}
		
		MessageEmbed embedMessage = new EmbedBuilder().setDescription(description)
			.setFooter(footer.toString(), null)
			.setAuthor("Help", null, message.getJDA().getSelfUser().getEffectiveAvatarUrl())
			.build();
		
		message.getChannel().sendMessageEmbeds(embedMessage).queue();
	};
	
	public final BiPredicate<CommandEvent, ICommand> defaultBotPermissionCheck = (event, command) -> {
		if(!event.getChannelType().isGuild()) {
			return true;
		}
		
		long neededPermissions = Permission.getRaw(command.getBotDiscordPermissions()) | Permission.MESSAGE_SEND.getRawValue();
		long currentPermissions = Permission.getRaw(event.getSelfMember().getPermissions(event.getTextChannel()));
		
		long permissions = (neededPermissions & ~currentPermissions);
		
		if(permissions != 0) {
			if(this.missingPermissionsFunction != null) {
				this.missingPermissionsFunction.accept(event, Permission.getPermissions(permissions));
			}
			
			return false;
		}
		
		return true;
	};
	
	public final BiPredicate<CommandEvent, ICommand> defaultAuthorPermissionCheck = (event, command) -> {
		if(!event.getChannelType().isGuild()) {
			return true;
		}
		
		Set<Permission> authorPermissions = command.getAuthorDiscordPermissions();
		if(authorPermissions.size() > 0) {
			long neededPermissions = Permission.getRaw(authorPermissions);
			long currentPermissions = Permission.getRaw(event.getMember().getPermissions(event.getTextChannel()));
			
			long permissions = (neededPermissions & ~currentPermissions);
			
			if(permissions != 0) {
				if(this.missingAuthorPermissionFunction != null) {
					this.missingAuthorPermissionFunction.accept(event, Permission.getPermissions(permissions));
				}
				
				return false;
			}
		}
		
		return true;
	};
	
	public final BiPredicate<CommandEvent, ICommand> defaultNsfwCheck = (event, command) -> {
		if(!event.getChannelType().isGuild()) {
			return true;
		}
		
		if(command.isNSFW() && !event.getTextChannel().isNSFW()) {
			if(this.nsfwFunction != null) {
				this.nsfwFunction.accept(event);
			}
			
			return false;
		}
		
		return true;
	};
	
	protected BiConsumer<CommandEvent, Permission> missingPermissionExceptionFunction = this.defaultMissingPermissionExceptionFunction;
	protected BiConsumer<CommandEvent, EnumSet<Permission>> missingPermissionsFunction = this.defaultMissingPermissionsFunction;
	protected BiConsumer<CommandEvent, EnumSet<Permission>> missingAuthorPermissionFunction = this.defaultMissingAuthorPermissionsFunction;
	
	protected BiConsumer<CommandEvent, ICooldown> cooldownFunction = this.defaultCooldownFunction;
	protected Consumer<CommandEvent> nsfwFunction = this.defaultNsfwFunction;
	protected TriConsumer<Message, String, List<ICommand>> helpFunction = this.defaultHelpFunction;
	
	protected TriConsumer<Message, String, List<Failure>> messageParseFailureFunction = this.defaultMessageParseFailureFunction;
	
	protected Set<Long> developers = new LinkedHashSet<>();
	
	protected Set<CommandStore> commandStores = new LinkedHashSet<>();
	
	protected Set<CommandEventListener> commandEventListeners = new CopyOnWriteArraySet<>();
	
	protected ExecutorService commandExecutor = Executors.newCachedThreadPool();
	
	protected ICooldownManager cooldownManager = new CooldownManagerImpl();
	
	protected IReturnManager returnManager = new ReturnManagerImpl();
	
	protected IErrorManager errorManager = new ErrorManagerImpl();
	
	protected ICommandParser commandParser = new CommandParserImpl();
	
	protected ICommandEventFactory commandEventFactory = new CommandEventFactoryImpl();
	
	protected Set<Predicate<Message>> preParseChecks = new LinkedHashSet<>();
	protected Set<BiPredicate<CommandEvent, ICommand>> preExecuteChecks = new LinkedHashSet<>();
	
	protected List<String> defaultPrefixes = Collections.emptyList();
	
	protected Function<Message, List<String>> prefixFunction;
	
	protected boolean caseSensitivePrefixes = true;
	
	protected boolean allowMentionPrefix = true;
	
	protected boolean filterStackTrace = true;
	
	public CommandListener() {
		this.addDefaultPreExecuteChecks();
	}
	
	protected void forEachCommandEventListener(Consumer<CommandEventListener> listenerConsumer) {
		for(CommandEventListener listener : this.commandEventListeners) {
			/* Wrapped in a try catch because we don't want the execution of this to fail just because we couldn't rely on an event handler not to throw an exception */
			try {
				listenerConsumer.accept(listener);
			}catch(Throwable e) {
				LOG.error("One of the CommandEventListeners had an uncaught exception", e);
			}
		}
	}
	
	/**
	 * Add command event listeners
	 * 
	 * @param commandEventListeners the command event listeners to register
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener addCommandEventListener(@Nonnull CommandEventListener... commandEventListeners) {
		Checks.noneNull(commandEventListeners, "commandEventListeners");
		
		for(CommandEventListener commandEventListener : commandEventListeners) {
			this.commandEventListeners.add(commandEventListener);
		}
		
		return this;
	}
	
	/**
	 * Remove command event listeners
	 * 
	 * @param commandEventListeners the command event listeners to unregister
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener removeCommandEventListener(@Nonnull CommandEventListener... commandEventListeners) {
		Checks.noneNull(commandEventListeners, "commandEventListeners");
		
		for(CommandEventListener commandEventListener : commandEventListeners) {
			this.commandEventListeners.remove(commandEventListener);
		}
		
		return this;
	}
	
	/**
	 * Get all registered event listeners
	 * 
	 * @return an unmodifiable set of event listeners
	 */
	@Nonnull
	public Set<CommandEventListener> getCommandEventListeners() {
		return Collections.unmodifiableSet(this.commandEventListeners);
	}
	
	/**
	 * @return a list of all registered commands, does not include developer ({@link ICommand#isDeveloperCommand()}) or hidden ({@link ICommand#isHidden()}) commands
	 */
	@Nonnull
	public List<ICommand> getAllCommands() {
		return this.getAllCommands(false, false);
	}
	
	/**
	 * @param includeDeveloper whether or not commands that match {@link ICommand#isDeveloperCommand()} should be returned
	 * @param includeHidden whether or not commands that match {@link ICommand#isHidden()} should be returned
	 * 
	 * @return a list of all registered commands
	 */
	@Nonnull
	public List<ICommand> getAllCommands(boolean includeDeveloper, boolean includeHidden) {
		return this.getCommandStores().stream()
			.map(CommandStore::getCommands)
			.flatMap(Set::stream)
			.map((command) -> command.getAllCommandsRecursive(false))
			.flatMap(List::stream)
			.filter((command) -> !command.isPassive())
			.filter((command) -> !(!includeDeveloper && command.isDeveloperCommand()))
			.filter((command) -> !(!includeHidden && command.isHidden()))
			.collect(Collectors.toList());
	}
	
	/**
	 * @param message the event which will be used to verify the commands
	 * 
	 * @return a list of all registered commands verified ({@link ICommand#isAccessible(Message, CommandListener)}) with the current event, 
	 * this does not include hidden ({@link ICommand#isHidden()}) commands
	 */
	@Nonnull
	public List<ICommand> getAllCommands(@Nonnull Message message) {
		return this.getAllCommands(message, false);
	}
	
	/**
	 * @param message the event which will be used to verify the commands
	 * @param includeHidden whether or not commands that match {@link ICommand#isHidden()} should be returned
	 * 
	 * @return a list of all registered commands verified ({@link ICommand#isAccessible(Message, CommandListener)}) with the current event
	 */
	@Nonnull
	public List<ICommand> getAllCommands(@Nonnull Message message, boolean includeHidden) {
		Checks.notNull(message, "message");
		
		return this.getCommandStores().stream()
			.map(CommandStore::getCommands)
			.flatMap(Set::stream)
			.map((command) -> command.getAllCommandsRecursive(false))
			.flatMap(List::stream)
			.filter((command) -> !command.isPassive())
			.filter((command) -> !(!includeHidden && command.isHidden()))
			.filter((command) -> command.isAccessible(message, this))
			.collect(Collectors.toList());
	}
	
	/**
	 * @param commandClass the class of the command instance to get
	 * 
	 * @return the registered command instance of the provided class
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends ICommand> T getCommand(@Nonnull Class<T> commandClass) {
		Checks.notNull(commandClass, "commandClass");
		
		return (T) this.getCommandStores().stream()
			.map(CommandStore::getCommands)
			.flatMap(Set::stream)
			.filter((command) -> command.getClass().equals(commandClass))
			.findFirst()
			.orElse(null);
	}
	
	/**
	 * Add a list of command stores
	 * 
	 * @param commandStores the command stores to register
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener addCommandStores(@Nonnull CommandStore... commandStores) {
		Checks.noneNull(commandStores, "commandStores");
		
		for(CommandStore commandStore : commandStores) {
			this.commandStores.add(commandStore);
		}
		
		return this;
	}
	
	/**
	 * Remove a list of command stores
	 * 
	 * @param commandStores the command stores to unregister
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener removeCommandStores(@Nonnull CommandStore... commandStores) {
		Checks.noneNull(commandStores, "commandStores");
		
		for(CommandStore commandStore : commandStores) {
			this.commandStores.remove(commandStore);
		}
		
		return this;
	}
	
	/**
	 * @return an unmodifiable set of command stores
	 */
	@Nonnull
	public Set<CommandStore> getCommandStores() {
		return Collections.unmodifiableSet(this.commandStores);
	}
	
	/**
	 * @param prefixes the prefixes to set as default
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener setDefaultPrefixes(@Nonnull String... prefixes) {
		Checks.noneNull(prefixes, "prefixes");
		
		/* 
		 * From the longest prefix to the shortest so that if the bot for instance has two prefixes one being "hello" 
		 * and the other being "hello there" it would recognize that the prefix is "hello there" instead of it thinking that
		 * "hello" is the prefix and "there" being the command.
		 */
		Arrays.sort(prefixes, (a, b) -> Integer.compare(b.length(), a.length()));
		
		this.defaultPrefixes = List.of(prefixes);
		
		return this;
	}
	
	/**
	 * @return a list of default prefixes which will be checked for when the bot receives a Message, 
	 * additionally the mention of the bot is by default a prefix <b>(not included in this list)</b>
	 */
	
	@Nonnull
	public List<String> getDefaultPrefixes() {
		return Collections.unmodifiableList(this.defaultPrefixes);
	}
	
	/**
	 * This is true by default and should most of the times be enabled as it is a pretty good thing to have, a bot's mention is defined as &lt;@{@link User#getId()}&gt;
	 * 
	 * @param allowMentionPrefix a boolean that will determine whether or not the bot's tag can be used as a prefix
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener setAllowMentionPrefix(boolean allowMentionPrefix) {
		this.allowMentionPrefix = allowMentionPrefix;
		
		return this;
	}
	
	/**
	 * @return whether or not the bot's mention can be used as a prefix
	 */
	public boolean isAllowMentionPrefix() {
		return this.allowMentionPrefix;
	}
	
	/**
	 * @param caseSensitive whether or not prefixes should be case-sensitive
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener setCaseSensitivePrefixes(boolean caseSensitive) {
		this.caseSensitivePrefixes = caseSensitive;
		
		return this;
	}
	
	/**
	 * @return whether or not prefixes are case-sensitive
	 */
	public boolean isCaseSensitivePrefixes() {
		return this.caseSensitivePrefixes;
	}
	
	/**
	 * @param filter a boolean that will determine whether or not command exceptions should be filtered to only show the command's stack trace
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener setFilterStackTrace(boolean filter) {
		this.filterStackTrace = filter;
		
		return this;
	}
	
	/**
	 * @return whether or not command exceptions should be filtered to only show the command's stack trace
	 */
	public boolean isFilterStackTrace() {
		return this.filterStackTrace;
	}
	
	/**
	 * Register an array of ids as developers
	 * 
	 * @param ids the ids of the developers to register
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener addDevelopers(@Nonnull long... ids) {
		Checks.notNull(ids, "ids");
		
		for(long id : ids) {
			this.developers.add(id);
		}
		
		return this;
	}
	
	/**
	 * Register an array of ids as developers
	 * 
	 * @param ids the ids of the developers to register
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener addDevelopers(@Nonnull String... ids) {
		Checks.notNull(ids, "ids");
		
		long[] longIds = new long[ids.length];
		for(int i = 0; i < ids.length; i++) {
			Checks.isSnowflake(ids[i], "id");
			
			longIds[i] = Long.parseLong(ids[i]);
		}
		
		return this.addDevelopers(longIds);
	}
	
	/**
	 * Register an array of ids (as a Snowflake) as developers
	 * 
	 * @param ids the ids (as a Snowflake) of the developers to register
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener addDevelopers(@Nonnull ISnowflake... ids) {
		Checks.noneNull(ids, "ids");
		
		for(ISnowflake id : ids) {
			this.developers.add(id.getIdLong());
		}
		
		return this;
	}
	
	/**
	 * Unregister an array of ids from the developers
	 * 
	 * @param ids the ids of the developers to unregister
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener removeDevelopers(@Nonnull long... ids) {
		for(long id : ids) {
			this.developers.remove(id);
		}
		
		return this;
	}
	
	/**
	 * Unregister an array of ids from the developers
	 * 
	 * @param ids the ids (as a Snowflake) of the developers to unregister
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener removeDevelopers(@Nonnull ISnowflake... ids) {
		Checks.noneNull(ids, "ids");
		
		for(ISnowflake id : ids) {
			this.developers.remove(id.getIdLong());
		}
		
		return this;
	}
	
	/**
	 * Unregister an array of ids from the developers
	 * 
	 * @param ids the ids of the developers to unregister
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener removeDevelopers(@Nonnull String... ids) {
		Checks.notNull(ids, "ids");
		
		long[] longIds = new long[ids.length];
		for(int i = 0; i < ids.length; i++) {
			Checks.isSnowflake(ids[i]);
			
			longIds[i] = Long.parseLong(ids[i]);
		}
		
		return this.removeDevelopers(longIds);
	}
	
	/**
	 * @return the developers which should be checked for in {@link ICommand#isAccessible(Message, CommandListener)} if the command has {@link ICommand#isDeveloperCommand()}
	 */
	@Nonnull
	public Set<Long> getDevelopers() {
		return Collections.unmodifiableSet(this.developers);
	}
	
	/**
	 * @return whether or not the provided id is the id of a developer
	 */
	public boolean isDeveloper(long id) {
		return this.developers.contains(id);
	}
	
	/**
	 * @return whether or not the provided id is the id of a developer
	 */
	public boolean isDeveloper(@Nonnull String id) {
		Checks.isSnowflake(id);
		
		return this.developers.contains(Long.valueOf(id));
	}
	
	/**
	 * @return whether or not the provided id (as a Snowflake) is the id of a developer
	 */
	public boolean isDeveloper(@Nonnull ISnowflake id) {
		Checks.notNull(id, "id");
		
		return this.developers.contains(id.getIdLong());
	}
	
	/**
	 * @param function the function which will return a set amount of prefixes for the specific context,
	 * for instance you can return guild or user specific prefixes
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 * 
	 * @see #getPrefixes(Message)
	 */
	@Nonnull
	public CommandListener setPrefixesFunction(@Nullable Function<Message, List<String>> function) {
		this.prefixFunction = function;
		
		return this;
	}
	
	/**
	 * @return the current prefix function
	 * 
	 * @see #setPrefixesFunction(Function)
	 */
	@Nullable
	public Function<Message, List<String>> getPrefixesFunction() {
		return this.prefixFunction;
	}
	
	/**
	 * @param message the message to get the prefix from, used as context
	 * 
	 * @return this will return a set of prefixes for the specific context,
	 * if a function was not set through {@link #setPrefixesFunction(Function)}
	 * the default function, {@link #getDefaultPrefixes()}, will instead be used
	 */
	@Nonnull
	public List<String> getPrefixes(@Nonnull Message message) {
		Checks.notNull(message, "message");
		
		if(this.prefixFunction != null) {
			List<String> prefixes = this.prefixFunction.apply(message);
			
			/* 
			 * Should we also check if the length of the list is greater than 0 or
			 * can we justify giving the user the freedom of not returning any prefixes at all? 
			 * After all the mention prefix is hard-coded (UPDATE: they can now be disabled)
			 */
			if(prefixes != null /* && prefixes.length > 0 */) {
				/* This could possibly be immutable depending on what the user returns */
				prefixes = new ArrayList<>(prefixes);
				
				/* 
				 * From the longest prefix to the shortest so that if the bot for instance has two prefixes one being "hello" 
				 * and the other being "hello there" it would recognize that the prefix is "hello there" instead of it thinking that
				 * "hello" is the prefix and "there" being the command.
				 */
				prefixes.sort((a, b) -> Integer.compare(b.length(), a.length()));
				
				/* Make this unmodifiable for consistency */
				return Collections.unmodifiableList(prefixes);
			}else{
				LOG.warn("The prefix function returned a null object, returning the default prefixes instead");
			}
		}
		
		return Collections.unmodifiableList(this.getDefaultPrefixes());
	}
	
	/**
	 * @param consumer the function which will be called when the command failed due to missing permission 
	 * ({@link net.dv8tion.jda.api.exceptions.PermissionException PermissionException} being thrown)
	 * <br><br>
	 * <b>Parameter type definitions:</b>
	 * <br><b>CommandEvent</b> - The command which was triggered's event
	 * <br><b>Permission</b> - The missing permission which was acquired through {@link PermissionException#getPermission()}
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener setMissingPermissionExceptionFunction(@Nullable BiConsumer<CommandEvent, Permission> consumer) {
		this.missingPermissionExceptionFunction = consumer;
		
		return this;
	}
	
	/**
	 * @return the current missing permission exception function
	 * 
	 * @see #setMissingPermissionExceptionFunction(BiConsumer)
	 */
	@Nullable
	public BiConsumer<CommandEvent, Permission> getMissingPermissionExceptionFunction() {
		return this.missingPermissionExceptionFunction;
	}
	
	/**
	 * @param consumer the function which will be called when the bot does not have the required permissions to execute the command, 
	 * gotten from {@link ICommand#getBotDiscordPermissions()}
	 * <br><br>
	 * <b>Parameter type definitions:</b>
	 * <br><b>CommandEvent</b> - The command which was triggered's event
	 * <br><b>EnumSet&#60;Permission&#62;</b> - The missing permissions
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener setMissingPermissionFunction(@Nullable BiConsumer<CommandEvent, EnumSet<Permission>> consumer) {
		this.missingPermissionsFunction = consumer;
		
		return this;
	}
	
	/**
	 * @return the current missing permission function
	 * 
	 * @see #setMissingPermissionFunction(BiConsumer)
	 */
	@Nullable
	public BiConsumer<CommandEvent, EnumSet<Permission>> getMissingPermissionFunction() {
		return this.missingPermissionsFunction;
	}
	
	/**
	 * @param consumer the function which will be called when the author does not have the required permissions to execute the command, 
	 * gotten from {@link ICommand#getAuthorDiscordPermissions()}
	 * <br><br>
	 * <b>Parameter type definitions:</b>
	 * <br><b>CommandEvent</b> - The command which was triggered's event
	 * <br><b>EnumSet&#60;Permission&#62;</b> - The missing permissions
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener setMissingAuthorPermissionFunction(@Nullable BiConsumer<CommandEvent, EnumSet<Permission>> consumer) {
		this.missingAuthorPermissionFunction = consumer;
		
		return this;
	}
	
	/**
	 * @return the current missing author permission function
	 * 
	 * @see #setMissingAuthorPermissionFunction(BiConsumer)
	 */
	@Nullable
	public BiConsumer<CommandEvent, EnumSet<Permission>> getMissingAuthorPermissionFunction() {
		return this.missingAuthorPermissionFunction;
	}
	
	/**
	 * @param consumer the function which will be called if a command (for the current context) is on cooldown
	 * <br><br>
	 * <b>Parameter type definitions:</b>
	 * <br><b>CommandEvent</b> - The command which was triggered's event
	 * <br><b>ICooldown</b> - The cooldown which was hindering the command from being executed
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener setCooldownFunction(@Nullable BiConsumer<CommandEvent, ICooldown> consumer) {
		this.cooldownFunction = consumer;
		
		return this;
	}
	
	/**
	 * @return the current cooldown function
	 * 
	 * @see #setCooldownFunction(BiConsumer)
	 */
	@Nullable
	public BiConsumer<CommandEvent, ICooldown> getCooldownFunction() {
		return this.cooldownFunction;
	}
	
	/**
	 * @param consumer the function which will be called if a command is NSFW and the channel which it was triggered in is not an NSFW channel
	 * <br><br>
	 * <b>Parameter type definitions:</b>
	 * <br><b>CommandEvent</b> - The command which was triggered's event
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener setNSFWFunction(@Nullable Consumer<CommandEvent> consumer) {
		this.nsfwFunction = consumer;
		
		return this;
	}
	
	/**
	 * @return the current nsfw function
	 * 
	 * @see #setNSFWFunction(Consumer)
	 */
	@Nullable
	public Consumer<CommandEvent> getNSFWFunction() {
		return this.nsfwFunction;
	}
	
	/**
	 * @param consumer the function that will be called when a message could not be parsed correctly
	 * <br><br>
	 * <b>Parameter type definitions:</b>
	 * <br><b>Message</b> - The message that triggered this
	 * <br><b>String</b> - The prefix used to trigger this command
	 * <br><b>List&#60;ICommand&#62;</b> - A list of all commands which could not be parsed correctly throughout the parsing of the message
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener setHelpFunction(@Nullable TriConsumer<Message, String, List<ICommand>> consumer) {
		this.helpFunction = consumer;
		
		return this;
	}
	
	/**
	 * @return the current help function
	 * 
	 * @see #setHelpFunction(TriConsumer)
	 */
	@Nullable
	public TriConsumer<Message, String, List<ICommand>> getHelpFunction() {
		return this.helpFunction;
	}
	
	/**
	 * @param consumer the function that will be called when a message could not be parsed correctly
	 * <br><br>
	 * <b>Parameter type definitions:</b>
	 * <br><b>Message</b> - The message that triggered this
	 * <br><b>String</b> - The prefix used to trigger this command
	 * <br><b>List&#60;Failure&#62;</b> - A list of all failures which happened throughout the parsing of the message
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener setMessageParseFailureFunction(@Nullable TriConsumer<Message, String, List<Failure>> consumer) {
		this.messageParseFailureFunction = consumer;
		
		return this;
	}
	
	/**
	 * @return the current message parse failure function
	 * 
	 * @see #setMessageParseFailureFunction(TriConsumer)
	 */
	@Nullable
	public TriConsumer<Message, String, List<Failure>> getMessageParseFailureFunction() {
		return this.messageParseFailureFunction;
	}
	
	/**
	 * Set the executor service which will be used in executing async commands
	 * 
	 * @param executorService the executor service
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener setCommandExecutor(@Nonnull ExecutorService executorService) {
		Checks.notNull(executorService, "executorService");
		
		this.commandExecutor = executorService;
		
		return this;
	}
	
	/**
	 * @return the {@link ExecutorService} used to execute async commands
	 */
	@Nonnull
	public ExecutorService getCommandExecutor() {
		return this.commandExecutor;
	}
	
	/**
	 * Set the cooldown manager which will be used to handle command cooldowns
	 * 
	 * @param cooldownManager the cooldown manager
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener setCooldownManager(@Nonnull ICooldownManager cooldownManager) {
		Checks.notNull(cooldownManager, "cooldownManager");
		
		this.cooldownManager = cooldownManager;
		
		return this;
	}
	
	/**
	 * @return the {@link ICooldownManager} which is handling the command cooldowns
	 */
	@Nonnull
	public ICooldownManager getCoooldownManager() {
		return this.cooldownManager;
	}
	
	/**
	 * Set the return manager which will be used to handle what the commands
	 * return
	 * 
	 * @param returnManager the return manager
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener setReturnManager(@Nonnull IReturnManager returnManager) {
		Checks.notNull(returnManager, "returnManager");
		
		this.returnManager = returnManager;
		
		return this;
	}
	
	/**
	 * @return the {@link IReturnManager} which will be used to handle what
	 * the command returns
	 */
	@Nonnull
	public IReturnManager getReturnManager() {
		return this.returnManager;
	}
	
	/**
	 * Set the error manager which will be used to handle what happens
	 * when an argument is incorrectly parsed
	 * 
	 * @param errorManager the error manager
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener setErrorManager(@Nonnull IErrorManager errorManager) {
		Checks.notNull(errorManager, "errorManager");
		
		this.errorManager = errorManager;
		
		return this;
	}
	
	/**
	 * @return the {@link IErrorManager} which will be used to handle 
	 * what happens when an argument is incorrectly parsed
	 */
	@Nonnull
	public IErrorManager getErrorManager() {
		return this.errorManager;
	}
	
	/**
	 * Set the command parser which will be used to parse the commands 
	 * when {@link #handle(Message)} is called
	 * 
	 * @param commandParser the command parser
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener setCommandParser(@Nonnull ICommandParser commandParser) {
		Checks.notNull(commandParser, "commandParser");
		
		this.commandParser = commandParser;
		
		return this;
	}
	
	/**
	 * @return the {@link ICommandParser} which will be used to parse commands
	 * when {@link #handle(Message)} is called
	 */
	@Nonnull
	public ICommandParser getCommandParser() {
		return this.commandParser;
	}
	
	/**
	 * Set the {@link CommandEvent} factory which will be used to create
	 * the CommandEvent for the {@link ICommandParser}
	 * 
	 * @param commandEventFactory the {@link CommandEvent} factory
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener setCommandEventFactory(@Nonnull ICommandEventFactory commandEventFactory) {
		Checks.notNull(commandEventFactory, "commandEventFactory");
		
		this.commandEventFactory = commandEventFactory;
		
		return this;
	}
	
	/**
	 * @return the {@link ICommandEventFactory} which will be used to create
	 * the CommandEvent for the {@link ICommandParser}
	 */
	@Nonnull
	public ICommandEventFactory getCommandEventFactory() {
		return this.commandEventFactory;
	}
	
	/**
	 * Add a pre-parse check which will determine whether or not the message should be parsed, 
	 * this could be useful if you for instance blacklist a user or server
	 * 
	 * @param predicate the predicate to register
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener addPreParseCheck(@Nonnull Predicate<Message> predicate) {
		Checks.notNull(predicate, "predicate");
		
		this.preParseChecks.add(predicate);
		
		return this;
	}
	
	/**
	 * Remove a pre-parse check
	 * 
	 * @param predicate the predicate to unregister
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 * 
	 * @see #addPreParseCheck(Predicate)
	 */
	@Nonnull
	public CommandListener removePreParseCheck(@Nullable Predicate<Message> predicate) {
		this.preParseChecks.remove(predicate);
		
		return this;
	}
	
	/**
	 * @return an unmodifiable set of all the registered pre-parse checks
	 */
	@Nonnull
	public Set<Predicate<Message>> getPreParseChecks() {
		return Collections.unmodifiableSet(this.preParseChecks);
	}
	
	/**
	 * Add a pre-command execution check which will determine whether or not the command should be executed, 
	 * this could be useful if you for instance have disabled commands
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener addPreExecuteCheck(@Nonnull BiPredicate<CommandEvent, ICommand> predicate) {
		Checks.notNull(predicate, "predicate");
		
		this.preExecuteChecks.add(predicate);
		
		return this;
	}
	
	/**
	 * Remove a pre-command execution check
	 * 
	 * @param predicate the predicate to unregister
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 * 
	 * @see #addPreExecuteCheck(BiPredicate)
	 */
	@Nonnull
	public CommandListener removePreExecuteCheck(@Nullable BiPredicate<CommandEvent, ICommand> predicate) {
		this.preExecuteChecks.remove(predicate);
		
		return this;
	}
	
	/** 
	 * Remove the default registered pre-execute checks, these include bot and author permission checks as well as NSFW
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener removeDefaultPreExecuteChecks() {
		return this.removePreExecuteCheck(this.defaultBotPermissionCheck)
			.removePreExecuteCheck(this.defaultAuthorPermissionCheck)
			.removePreExecuteCheck(this.defaultNsfwCheck);
	}
	
	/** 
	 * Add the default registered pre-execute checks, these include bot and author permission checks as well as NSFW
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	@Nonnull
	public CommandListener addDefaultPreExecuteChecks() {
		return this.addPreExecuteCheck(this.defaultBotPermissionCheck)
			.addPreExecuteCheck(this.defaultAuthorPermissionCheck)
			.addPreExecuteCheck(this.defaultNsfwCheck);
	}
	
	/**
	 * @return an unmodifiable set of all the registered pre-execute checks
	 */
	@Nonnull
	public Set<BiPredicate<CommandEvent, ICommand>> getPreExecuteChecks() {
		return Collections.unmodifiableSet(this.preExecuteChecks);
	}
	
	@Override
	public void onEvent(GenericEvent event) {
		if(event instanceof MessageReceivedEvent) {
			this.handle(((MessageReceivedEvent) event).getMessage());
		}
	}
	
	public static class Failure {
		
		private ICommand command;
		
		private Throwable reason;
		
		public Failure(ICommand command, Throwable reason) {
			this.command = command;
			this.reason = reason;
		}
		
		public ICommand getCommand() {
			return this.command;
		}
		
		public Throwable getReason() {
			return this.reason;
		}
	}
	
	/**
	 * Get the prefix for a message
	 * 
	 * @param message the message to get the prefix from
	 * 
	 * @return the prefix extracted from the provided message
	 */
	@Nullable
	protected String extractPrefix(@Nonnull Message message) {
		Checks.notNull(message, "message");
		
		String contentRaw = message.getContentRaw();
		
		/* Needs to work for both non-nicked mentions and nicked mentions */
		long botId = message.getJDA().getSelfUser().getIdLong();
		if(this.allowMentionPrefix) {
			if(contentRaw.startsWith("<@" + botId + "> ") || contentRaw.startsWith("<@!" + botId + "> ")) {
				return contentRaw.substring(0, contentRaw.indexOf(" ") + 1);
			}
		}
		
		if(!this.caseSensitivePrefixes) {
			contentRaw = contentRaw.toLowerCase();
		}
		
		for(String prefix : this.getPrefixes(message)) {
			if(!this.caseSensitivePrefixes) {
				prefix = prefix.toLowerCase();
			}
			
			if(contentRaw.startsWith(prefix)) {
				return prefix;
			}
		}
		
		return null;
	}
	
	protected Map<Object, Object> orderingKeys = new ConcurrentHashMap<>();
	
	protected static class QueuedCommand {
		
		public QueuedCommand(ICommand command, CommandEvent event, long timeStarted, Object[] arguments) {
			this.command = command;
			this.event = event;
			this.timeStarted = timeStarted;
			this.arguments = arguments;
		}
		
		public ICommand command;
		
		public CommandEvent event;
		
		public long timeStarted;
		
		public Object[] arguments;
		
	}
	
	protected Map<Object, BlockingQueue<QueuedCommand>> queuedCommands = Collections.synchronizedMap(new HashMap<>());
	
	/**
	 * Parse the message and execute the command (if any)
	 * 
	 * @param message the message to parse
	 * 
	 * @return the {@link CommandEvent} which was parsed, may be null if no command could be found
	 */
	@Nullable
	public CommandEvent handle(@Nonnull Message message) {
		Checks.notNull(message, "message");
		
		long timeStarted = System.nanoTime();
		
		for(Predicate<Message> predicate : this.preParseChecks) {
			try {
				if(!predicate.test(message)) {
					return null;
				}
			}catch(Throwable e) {
				LOG.error("One of the pre-parse checks had an uncaught exception", e);
				
				return null;
			}
		}
		
		String contentRaw = message.getContentRaw();
		
		String prefix = this.extractPrefix(message);
		if(prefix == null) {
			return null;
		}
		
		this.forEachCommandEventListener((listener) -> listener.onPrefixedMessage(message, prefix));
		
		contentRaw = contentRaw.substring(prefix.length());
		
		List<Failure> possibleCommands = new ArrayList<>();
		
		/* 
		 * This accounts for a big part of the execution time and the more commands
		 * that are registered, including sub commands, the slower it gets.
		 * 
		 * This is done to allow commands to be mutable. Meaning that commands
		 * can change name or sub/parent command and everything would still
		 * function correctly.
		 * 
		 * TODO: Immutable commands (as a performance upgrade)
		 * An option to have the commands be immutable and that way improve
		 * performance should be added.
		 */
		List<CommandTrigger> commands = this.getCommandStores().stream()
			.map(CommandStore::getCommands)
			.flatMap(Set::stream)
			.map((command) -> command.getAllCommandsRecursiveWithTriggers(message))
			.flatMap(List::stream)
			.filter((commandTrigger) -> commandTrigger.getCommand().isAccessible(message, this))
			.sorted(CommandTriggerComparator.INSTANCE)
			.collect(Collectors.toList());
		
		for(CommandTrigger commandTrigger : commands) {
			ICommand command = commandTrigger.getCommand();
			
			String contentToParse = contentRaw;
			String trigger = commandTrigger.getTrigger();
			
			/* 
			 * TODO: Should the checking of whether it is the correct command trigger or not (as well as if it starts with a space)
			 * be the responsibility of the CommandListener or the CommandParser? 
			 * 
			 * I think it makes sense for it to be checked in the CommandListener but I am a bit split, opinions?
			 */
			
			if(!command.isCaseSensitive()) {
				contentToParse = contentToParse.toLowerCase();
			}
			
			if(!contentToParse.startsWith(command.isCaseSensitive() ? trigger : trigger.toLowerCase())) {
				continue;
			}
			
			contentToParse = contentRaw.substring(trigger.length());
			
			/* Happens if the command for instance would be "ping" and the content is "pingasd"*/
			if(contentToParse.length() > 0 && contentToParse.charAt(0) != ' ') {
				continue;
			}
			
			CommandEvent commandEvent;
			try {
				commandEvent = this.commandParser.parse(this, command, message, prefix, trigger, contentToParse, timeStarted);
				if(commandEvent == null) {
					continue;
				}
			}catch(ParseException e) {
				possibleCommands.add(new Failure(command, e));
				
				continue;
			}
			
			this.queueCommand(command, commandEvent, timeStarted, commandEvent.getArguments());
			
			return commandEvent;
		}
		
		if(possibleCommands.size() > 0) {
			if(this.messageParseFailureFunction != null) {
				this.messageParseFailureFunction.accept(message, prefix, possibleCommands);
			}
		}else{
			this.forEachCommandEventListener((listener) -> listener.onUnknownCommand(message, prefix));
		}
		
		return null;
	}
	
	private void drainCommandQueue(Object orderingKey, BlockingQueue<QueuedCommand> queue) {
		QueuedCommand queuedCommand;
		while((queuedCommand = queue.poll()) != null) {
			this.executeCommand(queuedCommand.command, queuedCommand.event, queuedCommand.timeStarted, queuedCommand.arguments);
		}
		
		synchronized(this.queuedCommands) {
			if(queue.isEmpty()) {
				this.queuedCommands.remove(orderingKey);
				return;
			}
		}
		
		this.drainCommandQueue(orderingKey, queue);
	}
	
	/**
	 * <b style="color: red">Used internally, use at your own risk</b>
	 * <br><br>
	 * Queue a command
	 * <br><br>
	 * This is similar to {@link #executeCommand(ICommand, CommandEvent, long, Object...)} but it checks if
	 * the command is async and queues it if it is, otherwise it functions the same way.
	 * 
	 * @param command the command to execute
	 * @param event the context
	 * @param timeStarted the time as {@link System#nanoTime()} when this started parsing
	 * @param arguments the arguments to execute the provided command with
	 */
	public void queueCommand(@Nonnull ICommand command, @Nonnull CommandEvent event, long timeStarted, @Nonnull Object... arguments) {
		Checks.notNull(command, "command");
		Checks.notNull(event, "event");
		Checks.notNull(arguments, "arguments");
		
		if(!command.isExecuteAsync()) {
			this.executeCommand(command, event, timeStarted, arguments);
			
			return;
		}
		
		Object orderingKey = command.getAsyncOrderingKey(event);
		if(orderingKey == null) {
			this.commandExecutor.execute(() -> this.executeCommand(command, event, timeStarted, arguments));
			
			return;
		}
		
		if(orderingKey.getClass().isPrimitive() || orderingKey instanceof String) {
			orderingKey = this.orderingKeys.computeIfAbsent(orderingKey, (key) -> new Object());
		}
		
		Object finalKey = orderingKey;
		/* TODO: Can we do this without locking here? */
		synchronized(this.queuedCommands) {
			boolean created = !this.queuedCommands.containsKey(finalKey);
			
			BlockingQueue<QueuedCommand> queue = this.queuedCommands.computeIfAbsent(orderingKey, (key) -> new LinkedBlockingQueue<>());
			queue.add(new QueuedCommand(command, event, timeStarted, arguments));
			
			if(!created) {
				return;
			}
			
			this.commandExecutor.execute(() -> this.drainCommandQueue(finalKey, queue));
		}
	}
	
	/**
	 * <b style="color: red">Used internally, use at your own risk</b>
	 * <br><br>
	 * Execute a command
	 * <br><br>
	 * Exceptions caused by the command is not thrown by this method, they are handled
	 * the same way as the command being executed normally.
	 * 
	 * @param command the command to execute
	 * @param event the context
	 * @param timeStarted the time as {@link System#nanoTime()} when this started parsing
	 * @param arguments the arguments to execute the provided command with
	 */
	public void executeCommand(@Nonnull ICommand command, @Nonnull CommandEvent event, long timeStarted, @Nonnull Object... arguments) {
		Checks.notNull(command, "command");
		Checks.notNull(event, "event");
		Checks.notNull(arguments, "arguments");
		
		ICommand actualCommand;
		if(command instanceof DummyCommand) {
			actualCommand = ((DummyCommand) command).getActualCommand();
		}else{
			actualCommand = command;
		}
		
		for(BiPredicate<CommandEvent, ICommand> predicate : this.preExecuteChecks) {
			try {
				if(!predicate.test(event, actualCommand)) {
					return;
				}
			}catch(Throwable e) {
				this.forEachCommandEventListener((listener) -> listener.onCommandExecutionException(command, event, e));
				
				LOG.error("Attempted to execute command (" + event.getCommand().getCommandTrigger() + ") with arguments " + Arrays.deepToString(event.getArguments()) + " but failed", e);
				
				/* Better to return if a pre-execute check fails than to continue to the command */
				return;
			}
		}
		
		try {
			/* TODO: Should this also be added to the pre-execute predicates? */
			ICooldown cooldown = this.cooldownManager.getCooldown(actualCommand, event.getMessage());
			if(cooldown != null && cooldown.getTimeRemainingMillis() > 0) {
				if(this.cooldownFunction != null) {
					this.cooldownFunction.accept(event, cooldown);
				}
				
				return;
			}
			
			if(command.getCooldownDuration() > 0) {
				/* Add the cooldown before the command has executed so that in case the command has a long execution time it will not get there */
				this.cooldownManager.applyCooldown(actualCommand, event.getMessage());
			}
			
			command.execute(event, arguments);
			
			this.forEachCommandEventListener((listener) -> listener.onCommandExecuted(command, event));
		}catch(Throwable e) {
			if(command.getCooldownDuration() > 0) {
				/* If the command execution fails then no cooldown should be applied */
				this.cooldownManager.removeCooldown(actualCommand, event.getMessage());
			}
			
			if(e instanceof CancelException) {
				return;
			}
			
			if(e instanceof PermissionException) {
				LOG.warn("Attempted to execute command (" + event.getCommandTrigger() + ") with arguments " + Arrays.deepToString(arguments) + 
					", though it failed due to missing permissions, time elapsed " + (System.nanoTime() - timeStarted) + 
					", error message (" + e.getMessage() + ")");
				
				this.forEachCommandEventListener((listener) -> listener.onCommandExecutionException(command, event, (PermissionException) e));
				
				if(this.missingPermissionExceptionFunction != null) {
					this.missingPermissionExceptionFunction.accept(event, ((PermissionException) e).getPermission());
				}
				
				return;
			}
			
			this.forEachCommandEventListener((listener) -> listener.onCommandExecutionException(command, event, e));
			
			LOG.error("Attempted to execute command (" + event.getCommand().getCommandTrigger() + ") with arguments " + Arrays.deepToString(event.getArguments()) + " but failed", e);
			
			return;
		}
		
		LOG.info("Executed command (" + event.getCommandTrigger() + ") with the arguments " + Arrays.deepToString(arguments) + ", time elapsed " + (System.nanoTime() - timeStarted));
	}
}