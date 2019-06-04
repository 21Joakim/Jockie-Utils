package com.jockie.bot.core.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.IEndlessArgument;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.exception.CancelException;
import com.jockie.bot.core.command.exception.CommandExecutionException;
import com.jockie.bot.core.command.exception.parser.ParseException;
import com.jockie.bot.core.command.manager.IReturnManager;
import com.jockie.bot.core.command.manager.impl.ReturnManagerImpl;
import com.jockie.bot.core.command.parser.ICommandParser;
import com.jockie.bot.core.command.parser.impl.CommandParserImpl;
import com.jockie.bot.core.cooldown.ICooldown;
import com.jockie.bot.core.cooldown.ICooldownManager;
import com.jockie.bot.core.cooldown.impl.CooldownManagerImpl;
import com.jockie.bot.core.utility.TriConsumer;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.tuple.Pair;

public class CommandListener implements EventListener {
	
	/* More specific goes first */
	protected static final Comparator<Pair<String, ICommand>> COMMAND_COMPARATOR = new Comparator<Pair<String, ICommand>>() {
		public int compare(Pair<String, ICommand> pair, Pair<String, ICommand> pair2) {
			ICommand command = pair.getRight(), command2 = pair2.getRight();
			
			/* Check the trigger length, the longer the more specific so it goes first */
			if(pair.getLeft().length() > pair2.getLeft().length()) {
				return -1;
			}else if(pair.getLeft().length() < pair2.getLeft().length()) {
				return 1;
			}
			
			List<IArgument<?>> arguments = command.getArguments(), arguments2 = command2.getArguments();
			int argumentCount = arguments.size(), argumentCount2 = arguments2.size();
			
			if(argumentCount > 0 && argumentCount2 > 0) {
				IArgument<?> lastArgument = arguments.get(argumentCount - 1), lastArgument2 = arguments2.get(argumentCount2 - 1);
				
				boolean endless = false, endless2 = false;
				boolean endlessArguments = false, endlessArguments2 = false;
				
				/* Update argument count and check for endless arguments */
				if(lastArgument.isEndless()) {
					if(lastArgument instanceof IEndlessArgument) {
						int max = ((IEndlessArgument<?>) lastArgument).getMaxArguments();
						
						if(max != -1) {
							argumentCount += (max - 1);
						}else{
							endlessArguments = true;
						}
					}
					
					endless = true;
				}
				
				/* Update argument count and check for endless arguments */
				if(lastArgument2.isEndless()) {
					if(lastArgument2 instanceof IEndlessArgument) {
						int max = ((IEndlessArgument<?>) lastArgument2).getMaxArguments();
						
						if(max != -1) {
							argumentCount2 += (max - 1);
						}else{
							endlessArguments2 = true;
						}
					}
					
					endless2 = true;
				}
				
				/* Check if the last argument contains an endless amount of arguments */
				if(!endlessArguments && endlessArguments2) {
					return -1;
				}else if(endlessArguments && !endlessArguments2) {
					return 1;
				}
				
				/**
				 * Check how many arguments the command has, the more arguments the more specific it is
				 * and should therefore be closer.
				 */
				if(argumentCount > argumentCount2) {
					return -1;
				}else if(argumentCount < argumentCount2) {
					return 1;
				}
				
				/* 
				 * Check if the last argument is endless, if it is it will simply accept all the remaining content 
				 * which means it is less specific and should therefore be further back.
				 */
				if(!endless && endless2) {
					return -1;
				}else if(endless && !endless2) {
					return 1;
				}
				
				/* 
				 * Check the order of the argument parsers. This was mostly introduced to combat an issue where
				 * due to the fact that Class#getDeclaredMethods doesn't return the methods in the order they were
				 * specified in, let alone any order at all, the order of the commands would sometimes be different
				 * and could cause weird behaviour.
				 * 
				 * One example of a weird behaviour is if you have a command, "prune", which takes one argument, amount (Integer),
				 * and then you have an alternate command implementation which takes a keyword argument of the type String.
				 * Now if the first method loads first everything works correctly as the argument can be checked if it is an integer 
				 * and then move to the String variant.
				 * If the second version is instead loaded first it will always take it as a keyword as the String parser just accepts any content given to it
				 * and this causes the first version to become effectively inaccessible. 
				 */
				if(argumentCount == argumentCount2) {
					for(int i = 0; i < argumentCount; i++) {
						int parsePriority = arguments.get(i).getParser().getPriority();
						int parsePriority2 = arguments2.get(i).getParser().getPriority();
						
						if(parsePriority != parsePriority2) {
							if(parsePriority > parsePriority2) {
								return 1;
							}else if(parsePriority < parsePriority2) {
								return -1;
							}
						}
					}
				}
			}else if(argumentCount == 0 && argumentCount2 > 0) {
				return 1;
			}else if(argumentCount > 0 && argumentCount2 == 0) {
				return -1;
			}
			
			/* 
			 * Check for case sensitivity, if it is case sensitive it is more specific and therefore goes first.
			 * 
			 * This could be useful if you, for instance, had a command called "ban" and then a case sensitive command called "Ban" 
			 * to fake ban people.
			 */
			if(command.isCaseSensitive() && !command2.isCaseSensitive()) {
				return -1;
			}else if(!command.isCaseSensitive() && command2.isCaseSensitive()) {
				return 1;
			}
			
			/* 
			 * TODO: This is hacky solution to fix the problem with inconsistent order of optional arguments.
			 * 
			 * This works by sorting it by the "distance" the arguments is from their original position, for instance
			 * let's say the arguments are [optional, optional2 and optional3] and this DummyCommand has optional3 as its argument,
			 * that would mean that the distance is 2 (2 - 0).
			 * 
			 * Why 2 - 0?
			 * This is because optional3 is at index 2 for the original arguments and index 0 for the DummyCommand.
			 * 
			 * This works, unsure of how well it works but it has been tested with
			 * [optional, optional2, optional3, optional4] and [optional, required, optional2, required2]
			 */
			if(command instanceof DummyCommand && command2 instanceof DummyCommand) {
				ICommand actualCommand = command.getParent();
				
				if(actualCommand.equals(command2.getParent())) {
					List<IArgument<?>> actualArguments = actualCommand.getArguments();
					
					int distance = 0, distance2 = 0;
					for(int i = 0; i < arguments.size(); i++) {
						distance += actualArguments.indexOf(arguments.get(i)) - i;
					}
					
					for(int i = 0; i < arguments2.size(); i++) {
						distance2 += actualArguments.indexOf(arguments2.get(i)) - i;
					}
					
					return Integer.compare(distance, distance2);
				}
			}
			
			return 0;
		}
	};
	
	public static final BiConsumer<CommandEvent, List<Permission>> DEFAULT_MISSING_PERMISSION_FUNCTION = (event, permissions) -> {
		StringBuilder missingPermissions = new StringBuilder();
		for(Permission permission : permissions) {
			missingPermissions.append(permission.getName() + "\n");
		}
		
		StringBuilder message = new StringBuilder()
			.append("Missing permission" + (missingPermissions.length() > 1 ? "s" : "") + " to execute **")
			.append(event.getCommandTrigger()).append("** in ")
			.append(event.getChannel().getName())
			.append(", ")
			.append(event.getGuild().getName())
			.append("\n```")
			.append(missingPermissions)
			.append("```");
		
		if(!event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE)) {
			event.getAuthor().openPrivateChannel().queue(channel -> {
				channel.sendMessage(message).queue();
			});
		}else{
			event.getChannel().sendMessage(message).queue();
		}
	};
	
	public static final BiConsumer<CommandEvent, Permission> DEFAULT_MISSING_PERMISSION_EXCEPTION_FUNCTION = (event, permission) -> {
		CommandListener.DEFAULT_MISSING_PERMISSION_FUNCTION.accept(event, List.of(permission));
	};
	
	public static final BiConsumer<CommandEvent, List<Permission>> DEFAULT_MISSING_AUTHOR_PERMISSION_FUNCTION = (event, permissions) -> {
		StringBuilder message = new StringBuilder()
			.append("You are missing the");
		
		for(int i = 0; i < permissions.size(); i++) {
			message.append(" **" + permissions.get(i).getName() + "**");
			
			if(i != (permissions.size() - 1)) {
				if(i == (permissions.size() - 2)) {
					message.append(" and");
				}else{
					message.append(",");
				}
			}
		}
		
		message.append(" permission" + (permissions.size() == 1 ? "" : "s") + " to execute that command");
		
		event.reply(message).queue();
	};
	
	public static final BiConsumer<CommandEvent, ICooldown> DEFAULT_COOLDOWN_FUNCTION = (event, cooldown) -> {
		event.reply("Slow down, try again in " + ((double) cooldown.getTimeRemainingMillis()/1000) + " seconds").queue();
	};
	
	public static final Consumer<CommandEvent> DEFAULT_NSFW_FUNCTION = (event) -> {
		event.reply("NSFW commands are not allowed in non-NSFW channels!").queue();
	};
	
	public static final TriConsumer<Message, String, List<Failure>> DEFAULT_HELP_FUNCTION = (message, prefix, failures) -> {
		if(message.getChannelType().isGuild()) {
			Member bot = message.getGuild().getSelfMember();
			
			if(!bot.hasPermission(Permission.MESSAGE_WRITE)) {
				message.getAuthor().openPrivateChannel().queue(channel -> {
					channel.sendMessage("Missing permission **" + Permission.MESSAGE_WRITE.getName() + "** in " + message.getChannel().getName() + ", " + message.getGuild().getName()).queue();
				});
				
				return;
			}else if(!bot.hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				message.getChannel().sendMessage("Missing permission **" + Permission.MESSAGE_EMBED_LINKS.getName() + "** in " + message.getChannel().getName() + ", " + message.getGuild().getName()).queue();
				
				return;
			}
		}
		
		List<ICommand> commands = failures.stream()
			.map(failure -> {
				ICommand command = failure.getCommand();
				if(command instanceof DummyCommand) {
					return command.getParent();
				}
				
				return command;
			})
			.distinct()
			.collect(Collectors.toList());
		
		StringBuilder description = new StringBuilder();
		for(int i = 0; i < commands.size(); i++) {
			ICommand command = commands.get(i);
			
			description.append(command.getCommandTrigger())
				.append(" ")
				.append(command.getArgumentInfo());
			
			if(i < failures.size() - 1) {
				description.append("\n");
			}
		}
		
		MessageEmbed embedMessage = new EmbedBuilder().setDescription(description.toString())
			.setFooter("* means required. [] means multiple arguments of that type.", null)
			.setAuthor("Help", null, message.getJDA().getSelfUser().getEffectiveAvatarUrl())
			.build();
		
		message.getChannel().sendMessage(embedMessage).queue();
	};
	
	public final BiPredicate<CommandEvent, ICommand> defaultBotPermissionCheck = (event, command) -> {
		if(event.getChannelType().isGuild()) {
			long neededPermissions = Permission.getRaw(command.getBotDiscordPermissions()) | Permission.MESSAGE_WRITE.getRawValue();
			long currentPermissions = Permission.getRaw(event.getSelfMember().getPermissions(event.getTextChannel()));
			
			long permissions = (neededPermissions & ~currentPermissions);
			
			if(permissions != 0) {
				if(this.missingPermissionFunction != null) {
					this.missingPermissionFunction.accept(event, Permission.getPermissions(permissions));
				}
				
				return false;
			}
		}
		
		return true;
	};
	
	public final BiPredicate<CommandEvent, ICommand> defaultAuthorPermissionCheck = (event, command) -> {
		if(event.getChannelType().isGuild()) {
			List<Permission> authorPermissions = command.getAuthorDiscordPermissions();
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
		}
		
		return true;
	};
	
	public final BiPredicate<CommandEvent, ICommand> defaultNsfwCheck = (event, command) -> {
		if(event.getChannelType().isGuild()) {
			if(command.isNSFW() && !event.getTextChannel().isNSFW()) {
				if(this.nsfwFunction != null) {
					this.nsfwFunction.accept(event);
				}
				
				return false;
			}
		}
		
		return true;
	};
	
	protected BiConsumer<CommandEvent, Permission> missingPermissionExceptionFunction = DEFAULT_MISSING_PERMISSION_EXCEPTION_FUNCTION;
	protected BiConsumer<CommandEvent, List<Permission>> missingPermissionFunction = DEFAULT_MISSING_PERMISSION_FUNCTION;
	protected BiConsumer<CommandEvent, List<Permission>> missingAuthorPermissionFunction = DEFAULT_MISSING_AUTHOR_PERMISSION_FUNCTION;
	
	protected BiConsumer<CommandEvent, ICooldown> cooldownFunction = DEFAULT_COOLDOWN_FUNCTION;
	protected Consumer<CommandEvent> nsfwFunction = DEFAULT_NSFW_FUNCTION;
	protected TriConsumer<Message, String, List<Failure>> helpFunction = DEFAULT_HELP_FUNCTION;
	
	protected Set<Long> developers = new LinkedHashSet<>();
	
	protected Set<CommandStore> commandStores = new LinkedHashSet<>();
	
	protected Set<CommandEventListener> commandEventListeners = new LinkedHashSet<>();
	
	protected ExecutorService commandExecutor = Executors.newCachedThreadPool();
	
	protected ICooldownManager cooldownManager = new CooldownManagerImpl();
	
	protected IReturnManager returnManager = new ReturnManagerImpl();
	
	protected ICommandParser commandParser = new CommandParserImpl();
	
	protected Set<Predicate<Message>> preParseChecks = new LinkedHashSet<>();
	protected Set<BiPredicate<CommandEvent, ICommand>> preExecuteChecks = new LinkedHashSet<>();
	
	protected List<String> defaultPrefixes = List.of("!");
	
	protected Function<Message, List<String>> prefixFunction;
	
	protected boolean allowMentionPrefix = true;
	
	protected boolean filterStackTrace = true;
	
	public CommandListener() {
		this.addDefaultPreExecuteChecks();
	}
	
	/**
	 * Add command event listeners
	 * 
	 * @param commandEventListeners the command event listeners to register
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener addCommandEventListener(CommandEventListener... commandEventListeners) {
		for(CommandEventListener commandEventListener : commandEventListeners) {
			if(!this.commandEventListeners.contains(commandEventListener)) {
				this.commandEventListeners.add(commandEventListener);
			}
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
	public CommandListener removeCommandEventListener(CommandEventListener... commandEventListeners) {
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
	public Set<CommandEventListener> getCommandEventListeners() {
		return Collections.unmodifiableSet(this.commandEventListeners);
	}
	
	/**
	 * @return a list of all registered commands, does not include developer ({@link ICommand#isDeveloperCommand()}) or hidden ({@link ICommand#isHidden()}) commands
	 */
	public List<ICommand> getAllCommands() {
		return this.getAllCommands(false, false);
	}
	
	/**
	 * @param includeDeveloper whether or not commands that match {@link ICommand#isDeveloperCommand()} should be returned
	 * @param includeHidden whether or not commands that match {@link ICommand#isHidden()} should be returned
	 * 
	 * @return a list of all registered commands
	 */
	public List<ICommand> getAllCommands(boolean includeDeveloper, boolean includeHidden) {
		return this.getCommandStores().stream()
			.map(CommandStore::getCommands)
			.flatMap(Set::stream)
			.map(command -> command.getAllCommandsRecursive(false))
			.flatMap(List::stream)
			.filter(command -> !command.isPassive())
			.filter(command -> !(!includeDeveloper && command.isDeveloperCommand()))
			.filter(command -> !(!includeHidden && command.isHidden()))
			.collect(Collectors.toList());
	}
	
	/**
	 * @param message the event which will be used to verify the commands
	 * 
	 * @return a list of all registered commands verified ({@link ICommand#verify(Message, CommandListener)}) with the current event, 
	 * this does not include hidden ({@link ICommand#isHidden()}) commands
	 */
	public List<ICommand> getAllCommands(Message message) {
		return this.getAllCommands(message, false);
	}
	
	/**
	 * @param message the event which will be used to verify the commands
	 * @param includeHidden whether or not commands that match {@link ICommand#isHidden()} should be returned
	 * 
	 * @return a list of all registered commands verified ({@link ICommand#verify(Message, CommandListener)}) with the current event
	 */
	public List<ICommand> getAllCommands(Message message, boolean includeHidden) {
		return this.getCommandStores().stream()
			.map(CommandStore::getCommands)
			.flatMap(Set::stream)
			.map(command -> command.getAllCommandsRecursive(false))
			.flatMap(List::stream)
			.filter(command -> !command.isPassive())
			.filter(command -> !(!includeHidden && command.isHidden()))
			.filter(command -> command.verify(message, this))
			.collect(Collectors.toList());
	}
	
	/**
	 * @param commandClass the class of the command instance to get
	 * 
	 * @return the registered command instance of the provided class
	 */
	@SuppressWarnings("unchecked")
	public <T extends ICommand> T getCommand(Class<T> commandClass) {
		return (T) this.getCommandStores().stream()
			.map(CommandStore::getCommands)
			.flatMap(Set::stream)
			.filter(command -> command.getClass().equals(commandClass))
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
	public CommandListener addCommandStore(CommandStore... commandStores) {
		for(CommandStore commandStore : commandStores) {
			if(!this.commandStores.contains(commandStore)) {
				this.commandStores.add(commandStore);
			}
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
	public CommandListener removeCommandStore(CommandStore... commandStores) {
		for(CommandStore commandStore : commandStores) {
			this.commandStores.remove(commandStore);
		}
		
		return this;
	}
	
	/**
	 * @return an unmodifiable set of command stores
	 */
	public Set<CommandStore> getCommandStores() {
		return Collections.unmodifiableSet(this.commandStores);
	}
	
	/**
	 * @param prefixes the prefixes to set as default
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener setDefaultPrefixes(String... prefixes) {
		Checks.notNull(prefixes, "Prefixes");
		
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
	public List<String> getDefaultPrefixes() {
		return Collections.unmodifiableList(this.defaultPrefixes);
	}
	
	/**
	 * This is true by default and should most of the times be enabled as it is a pretty good thing to have, a bot's mention is defined as <@{@link User#getId()}>
	 * 
	 * @param allowMentionPrefix a boolean that will determine whether or not the bot's tag can be used as a prefix
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
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
	 * @param filter a boolean that will determine whether or not command exceptions should be filtered to only show the command's stack trace
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
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
	public CommandListener addDevelopers(long... ids) {
		for(long id : ids) {
			this.developers.add(id);
		}
		
		return this;
	}
	
	/**
	 * Register an array of ids (as a Snowflake) as developers
	 * 
	 * @param ids the ids (as a Snowflake) of the developers to register
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener addDevelopers(ISnowflake... ids) {
		for(ISnowflake id : ids) {
			this.developers.add(id.getIdLong());
		}
		
		return this;
	}
	
	/**
	 * Register an id as a developer
	 * 
	 * @param id the id of the developer to register
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener addDeveloper(long id) {
		return this.addDevelopers(id);
	}
	
	/**
	 * Register an id (as a Snowflake) as a developer
	 * 
	 * @param id the id (as a Snowflake) of the developer to register
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener addDeveloper(ISnowflake id) {
		return this.addDevelopers(id);
	}
	
	/**
	 * Unregister an array of ids from the developers
	 * 
	 * @param ids the ids of the developers to unregister
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener removeDevelopers(long... ids) {
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
	public CommandListener removeDevelopers(ISnowflake... ids) {
		for(ISnowflake id : ids) {
			this.developers.remove(id.getIdLong());
		}
		
		return this;
	}
	
	/**
	 * Unregister an id from the developers
	 * 
	 * @param ids the id of the developer to unregister
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener removeDeveloper(long id) {
		return this.removeDevelopers(id);
	}
	
	/**
	 * Unregister an id (as a Snowflake) from the developers
	 * 
	 * @param ids the id (as a Snowflake) of the developer to unregister
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener removeDeveloper(ISnowflake id) {
		return this.removeDevelopers(id);
	}
	
	/**
	 * @return the developers which should be checked for in {@link ICommand#verify(Message, CommandListener)} if the command has {@link ICommand#isDeveloperCommand()}
	 */
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
	 * @return whether or not the provided id (as a Snowflake) is the id of a developer
	 */
	public boolean isDeveloper(ISnowflake id) {
		return this.developers.contains(id.getIdLong());
	}
	
	/**
	 * @param function the function which will return a set amount of prefixes for the specific context,
	 * for instance you can return guild or user specific prefixes
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 * 
	 * @see {@link #getPrefixes(Message)}
	 */
	public CommandListener setPrefixesFunction(Function<Message, List<String>> function) {
		Checks.notNull(function, "Function");
		
		this.prefixFunction = function;
		
		return this;
	}
	
	/**
	 * @return the current prefix function
	 * 
	 * @see {@link #setPrefixesFunction(Function)}
	 */
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
	public List<String> getPrefixes(Message message) {
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
				System.err.println("The prefix function returned a null object, returning the default prefixes instead");
			}
		}
		
		return Collections.unmodifiableList(this.getDefaultPrefixes());
	}
	
	/**
	 * @param consumer the function which will be called when the command failed due to missing permission 
	 * ({@link net.dv8tion.jda.core.exceptions.PermissionException PermissionException} being thrown)
	 * </br></br>
	 * <b>Parameter type definitions:</b>
	 * </br><b>CommandEvent</b> - The command which was triggered's event
	 * </br><b>Permission</b> - The missing permission which was acquired through {@link PermissionException#getPermission()}
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener setMissingPermissionExceptionFunction(BiConsumer<CommandEvent, Permission> consumer) {
		this.missingPermissionExceptionFunction = consumer;
		
		return this;
	}
	
	/**
	 * @return the current missing permission exception function
	 * 
	 * @see {@link #setMissingPermissionExceptionFunction(BiConsumer)}
	 */
	public BiConsumer<CommandEvent, Permission> getMissingPermissionExceptionFunction() {
		return this.missingPermissionExceptionFunction;
	}
	
	/**
	 * @param consumer the function which will be called when the bot does not have the required permissions to execute the command, 
	 * gotten from {@link ICommand#getBotDiscordPermissions()}
	 * </br></br>
	 * <b>Parameter type definitions:</b>
	 * </br><b>CommandEvent</b> - The command which was triggered's event
	 * </br><b>List&#60;Permission&#62;</b> - The missing permission which was acquired through {@link PermissionException#getPermission()}
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener setMissingPermissionFunction(BiConsumer<CommandEvent, List<Permission>> consumer) {
		this.missingPermissionFunction = consumer;
		
		return this;
	}
	
	/**
	 * @return the current missing permission function
	 * 
	 * @see {@link #setMissingPermissionFunction(BiConsumer)}
	 */
	public BiConsumer<CommandEvent, List<Permission>> getMissingPermissionFunction() {
		return this.missingPermissionFunction;
	}
	
	/**
	 * @param consumer the function which will be called when the author does not have the required permissions to execute the command, 
	 * gotten from {@link ICommand#getAuthorDiscordPermissions()}
	 * </br></br>
	 * <b>Parameter type definitions:</b>
	 * </br><b>CommandEvent</b> - The command which was triggered's event
	 * </br><b>List&#60;Permission&#62;</b> - The missing permission which was acquired through {@link PermissionException#getPermission()}
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener setMissingAuthorPermissionFunction(BiConsumer<CommandEvent, List<Permission>> consumer) {
		this.missingAuthorPermissionFunction = consumer;
		
		return this;
	}
	
	/**
	 * @return the current missing author permission function
	 * 
	 * @see {@link #setMissingAuthorPermissionFunction(BiConsumer)}
	 */
	public BiConsumer<CommandEvent, List<Permission>> getMissingAuthorPermissionFunction() {
		return this.missingAuthorPermissionFunction;
	}
	
	/**
	 * @param consumer the function which will be called if a command (for the current context) is on cooldown
	 * </br></br>
	 * <b>Parameter type definitions:</b>
	 * </br><b>CommandEvent</b> - The command which was triggered's event
	 * </br><b>ICooldown</b> - The cooldown which was hindering the command from being executed
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener setCooldownFunction(BiConsumer<CommandEvent, ICooldown> consumer) {
		this.cooldownFunction = consumer;
		
		return this;
	}
	
	/**
	 * @return the current cooldown function
	 * 
	 * @see {@link #setCooldownFunction(BiConsumer)}
	 */
	public BiConsumer<CommandEvent, ICooldown> getCooldownFunction() {
		return this.cooldownFunction;
	}
	
	/**
	 * @param consumer the function which will be called if a command is NSFW and the channel which it was triggered in is not an NSFW channel
	 * </br></br>
	 * <b>Parameter type definitions:</b>
	 * </br><b>CommandEvent</b> - The command which was triggered's event
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener setNSFWFunction(Consumer<CommandEvent> consumer) {
		this.nsfwFunction = consumer;
		
		return this;
	}
	
	/**
	 * @return the current nsfw function
	 * 
	 * @see {@link #setNSFWFunction(Consumer)}
	 */
	public Consumer<CommandEvent> getNSFWFunction() {
		return this.nsfwFunction;
	}
	
	/**
	 * @param consumer the function that will be called when a command had the wrong arguments.
	 * </br></br>
	 * <b>Parameter type definitions:</b>
	 * </br><b>Message</b> - The message that triggered this
	 * </br><b>String</b> - The prefix used to trigger this command
	 * </br><b>List&#60;Failure&#62;</b> - A list of all failures which happened throughout the parsing of the message
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener setHelpFunction(TriConsumer<Message, String, List<Failure>> consumer) {
		this.helpFunction = consumer;
		
		return this;
	}
	
	/**
	 * @return the current help function
	 * 
	 * @see {@link #setHelpFunction(TriConsumer)}
	 */
	public TriConsumer<Message, String, List<Failure>> getHelpFunction() {
		return this.helpFunction;
	}
	
	/**
	 * Set the executor service which will be used in executing async commands
	 * 
	 * @param service the executor service
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener setCommandExecutor(ExecutorService executorService) {
		Checks.notNull(executorService, "ExecutorService");
		
		this.commandExecutor = executorService;
		
		return this;
	}
	
	/**
	 * @return the {@link ExecutorService} instance, useful for chaining
	 */
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
	public CommandListener setCooldownManager(ICooldownManager cooldownManager) {
		Checks.notNull(cooldownManager, "ICooldownManager");
		
		this.cooldownManager = cooldownManager;
		
		return this;
	}
	
	/**
	 * @return the {@link ICooldownManager} which is handling the command cooldowns
	 */
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
	public CommandListener setReturnManager(IReturnManager returnManager) {
		Checks.notNull(returnManager, "IReturnManager");
		
		this.returnManager = returnManager;
		
		return this;
	}
	
	/**
	 * @return the {@link IReturnManager} which will be used to handle what
	 * the command returns
	 */
	public IReturnManager getReturnManager() {
		return this.returnManager;
	}
	
	/**
	 * Set the command parser which will be used to parse the commands 
	 * when {@link #handleMessage(Message)} is called
	 * 
	 * @param commandParser the command parser
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener setCommandParser(ICommandParser commandParser) {
		Checks.notNull(commandParser, "ICommandParser");
		
		this.commandParser = commandParser;
		
		return this;
	}
	
	/**
	 * @return the {@link ICommandParser} which will be used to parse commands
	 * when {@link #handleMessage(Message)} is called
	 */
	public ICommandParser getCommandParser() {
		return this.commandParser;
	}
	
	/**
	 * Add a pre-parse check which will determine whether or not the message should be parsed, 
	 * this could be useful if you for instance blacklist a user or server
	 * 
	 * @param predicate the predicate to register
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener addPreParseCheck(Predicate<Message> predicate) {
		Checks.notNull(predicate, "Predicate");
		
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
	 * @see {@link #addPreParseCheck(Predicate)}
	 */
	public CommandListener removePreParseCheck(Predicate<Message> predicate) {
		this.preParseChecks.remove(predicate);
		
		return this;
	}
	
	/**
	 * @return an unmodifiable set of all the registered pre-parse checks
	 */
	public Set<Predicate<Message>> getPreParseChecks() {
		return Collections.unmodifiableSet(this.preParseChecks);
	}
	
	/**
	 * Add a pre-command execution check which will determine whether or not the command should be executed, 
	 * this could be useful if you for instance have disabled commands
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
	public CommandListener addPreExecuteCheck(BiPredicate<CommandEvent, ICommand> predicate) {
		Checks.notNull(predicate, "Predicate");
		
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
	 * @see {@link #addPreExecuteCheck(BiPredicate)}
	 */
	public CommandListener removePreExecuteCheck(BiPredicate<CommandEvent, ICommand> predicate) {
		this.preExecuteChecks.remove(predicate);
		
		return this;
	}
	
	/** 
	 * Remove the default registered pre-execute checks, these include bot and author permission checks as well as NSFW
	 * 
	 * @return the {@link CommandListener} instance, useful for chaining
	 */
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
	public CommandListener addDefaultPreExecuteChecks() {
		return this.addPreExecuteCheck(this.defaultBotPermissionCheck)
			.addPreExecuteCheck(this.defaultAuthorPermissionCheck)
			.addPreExecuteCheck(this.defaultNsfwCheck);
	}
	
	/**
	 * @return an unmodifiable set of all the registered pre-execute checks
	 */
	public Set<BiPredicate<CommandEvent, ICommand>> getPreExecuteChecks() {
		return Collections.unmodifiableSet(this.preExecuteChecks);
	}
	
	public void onEvent(Event event) {
		if(event instanceof MessageReceivedEvent) {
			this.parse(((MessageReceivedEvent) event).getMessage());
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
	
	protected String extractPrefix(Message message) {
		String contentRaw = message.getContentRaw();
		String prefix = null;
		
		/* Needs to work for both non-nicked mentions and nicked mentions */
		long botId = message.getJDA().getSelfUser().getIdLong();
		if(this.allowMentionPrefix && (contentRaw.startsWith("<@" + botId + "> ") || contentRaw.startsWith("<@!" + botId + "> "))) {
			prefix = contentRaw.substring(0, contentRaw.indexOf(" ") + 1);
		}else{
			for(String p : this.getPrefixes(message)) {
				if(contentRaw.startsWith(p)) {
					prefix = p;
					
					break;
				}
			}
		}
		
		return prefix;
	}
	
	protected Map<Object, Object> orderingKeys = new HashMap<Object, Object>();
	
	/**
	 * Parse a message
	 * 
	 * @param message the message to parse
	 * 
	 * @return the {@link CommandEvent} which was parsed, may be null if no command could be found
	 */
	public CommandEvent parse(Message message) {
		long timeStarted = System.nanoTime();
		
		for(Predicate<Message> predicate : this.preParseChecks) {
			try {
				if(!predicate.test(message)) {
					return null;
				}
			}catch(Exception e) {
				throw new IllegalStateException("Failed on pre-parse check", e);
			}
		}
		
		String contentRaw = message.getContentRaw();
		
		String prefix = this.extractPrefix(message);
		if(prefix == null) {
			return null;
		}
		
		for(CommandEventListener listener : this.commandEventListeners) {
			try {
				listener.onPrefixedMessage(message, prefix);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		contentRaw = contentRaw.substring(prefix.length());
		
		List<Failure> possibleCommands = new ArrayList<>();
		
		List<Pair<String, ICommand>> commands = this.getCommandStores().stream()
			.map(CommandStore::getCommands)
			.flatMap(Set::stream)
			.map(command -> command.getAllCommandsRecursiveWithTriggers(message, ""))
			.flatMap(List::stream)
			.filter(pair -> pair.getRight().verify(message, this))
			.filter(pair -> !pair.getRight().isPassive())
			.sorted(CommandListener.COMMAND_COMPARATOR)
			.collect(Collectors.toList());
		
		for(Pair<String, ICommand> pair : commands) {
			ICommand command = pair.getRight();
			
			String contentToParse = contentRaw;
			String trigger = pair.getLeft();
			
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
				commandEvent = this.commandParser.parse(this, command, trigger, message, prefix, contentToParse, timeStarted);
				
				if(commandEvent == null) {
					continue;
				}
			}catch(ParseException e) {
				possibleCommands.add(new Failure(command, e));
				
				continue;
			}
			
			Object[] arguments = commandEvent.getArguments();
			
			if(command.isExecuteAsync()) {
				this.commandExecutor.submit(() -> {
					Object orderingKey = command.getAsyncOrderingKey(commandEvent);
					if(orderingKey != null) {
						if(orderingKey.getClass().isPrimitive() || orderingKey instanceof String) {
							Object key = this.orderingKeys.get(orderingKey);
							if(key == null) {
								key = new Object();
								
								this.orderingKeys.put(orderingKey, key);
							}
							
							orderingKey = key;
						}
						
						/* TODO: Implement a sort of OrderedExecutor, similar to 
						 * https://github.com/sedmelluq/lavaplayer/blob/master/main/src/main/java/com/sedmelluq/discord/lavaplayer/tools/OrderedExecutor.java 
						 * to prevent the need of creating a new task for every command which has to wait
						 */
						synchronized(orderingKey) {
							this.execute(command, commandEvent, timeStarted, arguments);
						}
					}else{
						this.execute(command, commandEvent, timeStarted, arguments);
					}
				});
			}else{
				this.execute(command, commandEvent, timeStarted, arguments);
			}
			
			return commandEvent;
		}
		
		if(possibleCommands.size() > 0) {
			if(this.helpFunction != null) {
				this.helpFunction.accept(message, prefix, possibleCommands);
			}
		}else{
			for(CommandEventListener listener : this.commandEventListeners) {
				try {
					listener.onUnknownCommand(message, prefix);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Execute a command, used internally. Use at your own risk
	 * 
	 * @param command the command to execute
	 * @param event the context
	 * @param timeStarted the time as {@link System#nanoTime()} when this started parsing
	 * @param arguments the arguments to execute the provided command with
	 */
	public void execute(ICommand command, CommandEvent event, long timeStarted, Object... arguments) {
		ICommand actualCommand = (command instanceof DummyCommand) ? command.getParent() : command;
		
		for(BiPredicate<CommandEvent, ICommand> predicate : this.preExecuteChecks) {
			try {
				if(!predicate.test(event, actualCommand)) {
					return;
				}
			}catch(Exception e) {
				for(CommandEventListener listener : this.commandEventListeners) {
					/* Wrapped in a try catch because we don't want the execution of this to fail just because we couldn't rely on an event handler not to throw an exception */
					try {
						listener.onCommandExecutionException(command, event, e);
					}catch(Exception e1) {
						e1.printStackTrace();
					}
				}
				
				new CommandExecutionException(event, e).printStackTrace();
				
				/* Better to return if a pre-execute check fails than to continue to the command */
				return;
			}
		}
		
		try {
			/* TODO: Should this also be added to the pre-execute predicates? */
			if(command.getCooldownDuration() > 0) {
				ICooldown cooldown = this.cooldownManager.getCooldown(actualCommand, event.getMessage());
				long timeRemaining = cooldown != null ? cooldown.getTimeRemainingMillis() : -1;
				
				if(timeRemaining > 0) {
					if(this.cooldownFunction != null) {
						this.cooldownFunction.accept(event, cooldown);
					}
					
					return;
				}
				
				/* Add the cooldown before the command has executed so that in case the command has a long execution time it will not get there */
				this.cooldownManager.applyCooldown(actualCommand, event.getMessage());
			}
			
			command.execute(event, arguments);
			
			for(CommandEventListener listener : this.commandEventListeners) {
				/* Wrapped in a try catch because we don't want the execution of this to fail just because we couldn't rely on an event handler not to throw an exception */
				try {
					listener.onCommandExecuted(command, event);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}catch(Throwable e) {
			if(command.getCooldownDuration() > 0) {
				/* If the command execution fails then no cooldown should be applied */
				this.cooldownManager.removeCooldown(actualCommand, event.getMessage());
			}
			
			if(e instanceof CancelException) {
				return;
			}
			
			if(e instanceof PermissionException) {
				System.err.println("Attempted to execute command (" + event.getCommandTrigger() + ") with arguments " + Arrays.deepToString(arguments) + 
					", though it failed due to missing permissions, time elapsed " + (System.nanoTime() - timeStarted) + 
					", error message (" + e.getMessage() + ")");
				
				for(CommandEventListener listener : this.commandEventListeners) {
					/* Wrapped in a try catch because we don't want the execution of this to fail just because we couldn't rely on an event handler not to throw an exception */
					try {
						listener.onCommandMissingPermissions(command, event, (PermissionException) e);
					}catch(Exception e1) {
						e1.printStackTrace();
					}
				}
				
				if(this.missingPermissionExceptionFunction != null) {
					this.missingPermissionExceptionFunction.accept(event, ((PermissionException) e).getPermission());
				}
				
				return;
			}
			
			for(CommandEventListener listener : this.commandEventListeners) {
				/* Wrapped in a try catch because we don't want the execution of this to fail just because we couldn't rely on an event handler not to throw an exception */
				try {
					listener.onCommandExecutionException(command, event, e);
				}catch(Exception e1) {
					e1.printStackTrace();
				}
			}
			
			new CommandExecutionException(event, e).printStackTrace();
			
			return;
		}
		
		System.out.println("Executed command (" + event.getCommandTrigger() + ") with the arguments " + Arrays.deepToString(arguments) + ", time elapsed " + (System.nanoTime() - timeStarted));
	}
}