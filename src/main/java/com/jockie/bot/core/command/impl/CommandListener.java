package com.jockie.bot.core.command.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import com.jockie.bot.core.argument.VerifiedArgument;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.ICommand.ArgumentParsingType;
import com.jockie.bot.core.command.ICommand.ContentOverflowPolicy;
import com.jockie.bot.core.command.ICommand.InvalidOptionPolicy;
import com.jockie.bot.core.command.exception.CancelException;
import com.jockie.bot.core.command.exception.parser.ArgumentParseException;
import com.jockie.bot.core.command.exception.parser.ContentOverflowException;
import com.jockie.bot.core.command.exception.parser.InvalidArgumentCountException;
import com.jockie.bot.core.command.exception.parser.MissingRequiredArgumentException;
import com.jockie.bot.core.command.exception.parser.OutOfContentException;
import com.jockie.bot.core.command.exception.parser.UnknownOptionException;
import com.jockie.bot.core.cooldown.ICooldown;
import com.jockie.bot.core.cooldown.ICooldownManager;
import com.jockie.bot.core.cooldown.impl.CooldownManager;
import com.jockie.bot.core.option.IOption;
import com.jockie.bot.core.utility.TriFunction;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
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
			
			if(pair.getLeft().length() > pair2.getLeft().length()) {
				return -1;
			}else if(pair.getLeft().length() < pair2.getLeft().length()) {
				return 1;
			}
			
			int arguments = command.getArguments().length, arguments2 = command2.getArguments().length;
			
			if(arguments > 0 && arguments2 > 0) {
				IArgument<?> lastArgument = command.getArguments()[arguments - 1], lastArgument2 = command2.getArguments()[arguments2 - 1];
				
				boolean endless = false, endless2 = false;
				boolean endlessArguments = false, endlessArguments2 = false;
				
				if(lastArgument.isEndless()) {
					if(lastArgument instanceof IEndlessArgument<?>) {
						int max = ((IEndlessArgument<?>) lastArgument).getMaxArguments();
						
						if(max != -1) {
							arguments += (max - 1);
						}else{
							endlessArguments = true;
						}
					}
					
					endless = true;
				}
				
				if(lastArgument2.isEndless()) {
					if(lastArgument2 instanceof IEndlessArgument<?>) {
						int max = ((IEndlessArgument<?>) lastArgument2).getMaxArguments();
						
						if(max != -1) {
							arguments2 += (max - 1);
						}else{
							endlessArguments2 = true;
						}
					}
					
					endless2 = true;
				}
				
				if(!endlessArguments && endlessArguments2) {
					return -1;
				}else if(endlessArguments && !endlessArguments2) {
					return 1;
				}
				
				if(arguments > arguments2) {
					return -1;
				}else if(arguments < arguments2) {
					return 1;
				}
				
				if(!endless && endless2) {
					return -1;
				}else if(endless && !endless2) {
					return 1;
				}
			}else if(arguments == 0 && arguments2 > 0) {
				return 1;
			}else if(arguments > 0 && arguments2 == 0) {
				return -1;
			}else if(command.isCaseSensitive() && !command2.isCaseSensitive()) {
				return -1;
			}else if(!command.isCaseSensitive() && command2.isCaseSensitive()) {
				return 1;
			}
			
			return 0;
		}
	};
	
	protected String[] defaultPrefixes = {"!"};
	
	protected Function<MessageReceivedEvent, String[]> prefixFunction;
	
	protected TriFunction<MessageReceivedEvent, String, List<Failure>, MessageBuilder> helperFunction;
	
	protected boolean helpEnabled = true;
	
	protected boolean allowMentionPrefix = true;
	
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
		
		MessageChannel channel;
		if(!event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE)) {
			channel = event.getAuthor().openPrivateChannel().complete();
		}else{
			channel = event.getChannel();
		}
		
		channel.sendMessage(message).queue();
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
	
	protected BiConsumer<CommandEvent, Permission> missingPermissionExceptionFunction = DEFAULT_MISSING_PERMISSION_EXCEPTION_FUNCTION;
	protected BiConsumer<CommandEvent, List<Permission>> missingPermissionFunction = DEFAULT_MISSING_PERMISSION_FUNCTION;
	protected BiConsumer<CommandEvent, List<Permission>> missingAuthorPermissionFunction = DEFAULT_MISSING_AUTHOR_PERMISSION_FUNCTION;
	
	public static final BiConsumer<CommandEvent, ICooldown> DEFAULT_COOLDOWN_FUNCTION = (event, cooldown) -> {
		event.reply("Slow down, try again in " + ((double) cooldown.getTimeRemainingMillis()/1000) + " seconds").queue();
	};
	
	protected BiConsumer<CommandEvent, ICooldown> cooldownFunction = DEFAULT_COOLDOWN_FUNCTION;
	
	public static final Consumer<CommandEvent> DEFAULT_NSFW_FUNCTION = (event) -> {
		event.reply("NSFW commands are not allowed in non-NSFW channels!").queue();
	};
	
	protected Consumer<CommandEvent> nsfwFunction = DEFAULT_NSFW_FUNCTION;
	
	protected Set<Long> developers = new HashSet<>();
	
	protected List<CommandStore> commandStores = new ArrayList<>();
	
	protected List<CommandEventListener> commandEventListeners = new ArrayList<>();
	
	protected ExecutorService commandExecutor = Executors.newCachedThreadPool();
	
	protected ICooldownManager cooldownManager = new CooldownManager();
	
	protected List<Predicate<MessageReceivedEvent>> preParseChecks = new ArrayList<>();
	protected List<BiPredicate<ICommand, CommandEvent>> preExecuteChecks = new ArrayList<>();
	
	public CommandListener addCommandEventListener(CommandEventListener... commandEventListeners) {
		for(CommandEventListener commandEventListener : commandEventListeners) {
			if(!this.commandEventListeners.contains(commandEventListener)) {
				this.commandEventListeners.add(commandEventListener);
			}
		}
		
		return this;
	}
	
	public CommandListener removeCommandEventListener(CommandEventListener... commandEventListeners) {
		for(CommandEventListener commandEventListener : commandEventListeners) {
			this.commandEventListeners.remove(commandEventListener);
		}
		
		return this;
	}
	
	public List<CommandEventListener> getCommandEventListeners() {
		return Collections.unmodifiableList(this.commandEventListeners);
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
	 * @param event the event which will be used to verify the commands
	 * 
	 * @return a list of all registered commands verified ({@link ICommand#verify(MessageReceivedEvent, CommandListener)}) with the current event, 
	 * this does not include hidden ({@link ICommand#isHidden()}) commands
	 */
	public List<ICommand> getAllCommands(MessageReceivedEvent event) {
		return this.getAllCommands(event, false);
	}
	
	/**
	 * @param event the event which will be used to verify the commands
	 * @param includeHidden whether or not commands that match {@link ICommand#isHidden()} should be returned
	 * 
	 * @return a list of all registered commands verified ({@link ICommand#verify(MessageReceivedEvent, CommandListener)}) with the current event
	 */
	public List<ICommand> getAllCommands(MessageReceivedEvent event, boolean includeHidden) {
		return this.getCommandStores().stream()
			.map(CommandStore::getCommands)
			.flatMap(Set::stream)
			.map(command -> command.getAllCommandsRecursive(false))
			.flatMap(List::stream)
			.filter(command -> !command.isPassive())
			.filter(command -> !(!includeHidden && command.isHidden()))
			.filter(command -> command.verify(event, this))
			.collect(Collectors.toList());
	}
	
	/**
	 * See {@link #getCommandStores()}
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
	 * See {@link #getCommandStores()}
	 */
	public CommandListener removeCommandStore(CommandStore... commandStores) {
		for(CommandStore commandStore : commandStores) {
			this.commandStores.remove(commandStore);
		}
		
		return this;
	}
	
	/**
	 * @return a list of CommandStores which are basically like command containers holding all the commands
	 */
	public List<CommandStore> getCommandStores() {
		return Collections.unmodifiableList(this.commandStores);
	}
	
	/**
	 * See {@link #getDefaultPrefixes()}
	 */
	public CommandListener setDefaultPrefixes(String... prefixes) {
		Checks.notNull(prefixes, "Prefixes");
		
		/* 
		 * From the longest prefix to the shortest so that if the bot for instance has two prefixes one being "hello" 
		 * and the other being "hello there" it would recognize that the prefix is "hello there" instead of it thinking that
		 * "hello" is the prefix and "there" being the command.
		 */
		Arrays.sort(prefixes, (a, b) -> Integer.compare(b.length(), a.length()));
		
		this.defaultPrefixes = prefixes;
		
		return this;
	}
	
	/**
	 * @return a set of default prefixes which will be checked for when the bot receives a MessageReceivedEvent, 
	 * additionally the mention of the bot is a hard-coded prefix which can not be removed
	 */
	public String[] getDefaultPrefixes() {
		return this.defaultPrefixes;
	}
	
	/**
	 * This is true by default and should most of the times be enabled as it is a pretty good thing to have, a bot's tag is defined as <@{@link User#getId()}>
	 * 
	 * @param allowMentionPrefix a boolean that will determine whether or not the bot's tag can be used as a prefix
	 * 
	 * @return the object which this was invoked on
	 */
	public CommandListener setAllowMentionPrefix(boolean allowMentionPrefix) {
		this.allowMentionPrefix = allowMentionPrefix;
		
		return this;
	}
	
	/**
	 * @return a boolean that determines whether or not the bot's tag can be used as a prefix
	 */
	public boolean isAllowMentionPrefix() {
		return this.allowMentionPrefix;
	}
	
	/**
	 * See {@link #getDevelopers()}
	 */
	public CommandListener addDevelopers(long... ids) {
		for(long id : ids) {
			this.developers.add(id);
		}
		
		return this;
	}
	
	public CommandListener addDeveloper(long id) {
		return this.addDevelopers(id);
	}
	
	/**
	 * See {@link #getDevelopers()}
	 */
	public CommandListener removeDevelopers(long... ids) {
		for(long id : ids) {
			this.developers.remove(id);
		}
		
		return this;
	}
	
	public CommandListener removeDeveloper(long id) {
		return this.removeDevelopers(id);
	}
	
	/**
	 * @return the developers which should be checked for in {@link ICommand#verify(MessageReceivedEvent, CommandListener)} if the command has {@link ICommand#isDeveloperCommand()}
	 */
	public Set<Long> getDevelopers() {
		return Collections.unmodifiableSet(this.developers);
	}
	
	/**
	 * @return a boolean that will prove if the provided user id is the id of a developer
	 */
	public boolean isDeveloper(long id) {
		return this.developers.contains(id);
	}
	
	/**
	 * See {@link #getPrefixes(MessageReceivedEvent)}
	 * 
	 * @param function the function which will return a set amount of prefixes for the specific context,
	 * for instance you can return guild or user specific prefixes
	 */
	public CommandListener setPrefixesFunction(Function<MessageReceivedEvent, String[]> function) {
		Checks.notNull(function, "Function");
		
		this.prefixFunction = function;
		
		return this;
	}
	
	/**
	 * @param event the context of the message
	 * 
	 * @return this will return a set of prefixes for the specific context,
	 * if a function was not set through {@link #setPrefixesFunction(Function)}
	 * the default function, {@link #getDefaultPrefixes()}, will instead be used
	 */
	public String[] getPrefixes(MessageReceivedEvent event) {
		if(this.prefixFunction != null) {
			String[] prefixes = this.prefixFunction.apply(event);
			
			/* 
			 * Should we also check if the length of the array is greater than 0 or
			 * can we justify giving the user the freedom of not returning any prefixes at all? 
			 * After all the mention prefix is hard-coded 
			 */
			if(prefixes != null /* && prefixes.length > 0 */) {
				/* 
				 * From the longest prefix to the shortest so that if the bot for instance has two prefixes one being "hello" 
				 * and the other being "hello there" it would recognize that the prefix is "hello there" instead of it thinking that
				 * "hello" is the prefix and "there" being the command.
				 */
				Arrays.sort(prefixes, (a, b) -> Integer.compare(b.length(), a.length()));
				
				return prefixes;
			}else{
				System.err.println("The prefix function returned a null object, returning the default prefixes instead");
			}
		}
		
		return this.getDefaultPrefixes();
	}
	
	/**
	 * See {@link #isHelpEnabled()}
	 */
	public CommandListener setHelpEnabled(boolean enabled) {
		this.helpEnabled = enabled;
		
		return this;
	}
	
	/**
	 * Whether or not the bot should return a message when the wrong arguments were given
	 */
	public boolean isHelpEnabled() {
		return this.helpEnabled;
	}
	
	/**
	 * @param consumer
	 * The function which will be called when the command failed due to missing permission 
	 * ({@link net.dv8tion.jda.core.exceptions.PermissionException PermissionException} being thrown)
	 * </br></br>
	 * <b>Parameter type definitions:</b>
	 * </br><b>CommandEvent</b> - The command which was triggered's event
	 * </br><b>Permission</b> - The missing permission which was acquired through {@link PermissionException#getPermission()}
	 */
	public CommandListener setMissingPermissionExceptionFunction(BiConsumer<CommandEvent, Permission> consumer) {
		this.missingPermissionExceptionFunction = consumer;
		
		return this;
	}
	
	/**
	 * @param consumer
	 * The function which will be called when the bot does not have the required permissions to execute the command, gotten from {@link ICommand#getBotDiscordPermissions()}
	 * </br></br>
	 * <b>Parameter type definitions:</b>
	 * </br><b>CommandEvent</b> - The command which was triggered's event
	 * </br><b>List&#60;Permission&#62;</b> - The missing permission which was acquired through {@link PermissionException#getPermission()}
	 */
	public CommandListener setMissingPermissionFunction(BiConsumer<CommandEvent, List<Permission>> consumer) {
		this.missingPermissionFunction = consumer;
		
		return this;
	}
	
	/**
	 * @param consumer
	 * The function which will be called when the author does not have the required permissions to execute the command, gotten from {@link ICommand#getAuthorDiscordPermissions()}
	 * </br></br>
	 * <b>Parameter type definitions:</b>
	 * </br><b>CommandEvent</b> - The command which was triggered's event
	 * </br><b>List&#60;Permission&#62;</b> - The missing permission which was acquired through {@link PermissionException#getPermission()}
	 */
	public CommandListener setMissingAuthorPermissionFunction(BiConsumer<CommandEvent, List<Permission>> consumer) {
		this.missingAuthorPermissionFunction = consumer;
		
		return this;
	}
	
	/**
	 * @param consumer
	 * The function which will be called if a command (for the current context) is on cooldown
	 * </br></br>
	 * <b>Parameter type definitions:</b>
	 * </br><b>CommandEvent</b> - The command which was triggered's event
	 * </br><b>ICooldown</b> - The cooldown which was hindering the command from being executed
	 */
	public CommandListener setCooldownFunction(BiConsumer<CommandEvent, ICooldown> consumer) {
		this.cooldownFunction = consumer;
		
		return this;
	}
	
	/**
	 * @param consumer
	 * The function which will be called if a command is NSFW and the channel which it was triggered in is not an NSFW channel
	 * </br></br>
	 * <b>Parameter type definitions:</b>
	 * </br><b>CommandEvent</b> - The command which was triggered's event
	 */
	public CommandListener setNSFWFunction(Consumer<CommandEvent> consumer) {
		this.nsfwFunction = consumer;
		
		return this;
	}
	
	/**
	 * @param function the function that will be called when a command had the wrong arguments.
	 * </br></br>
	 * <b>Parameter type definitions:</b>
	 * </br><b>MessageReceivedEvent</b> - The event that triggered this
	 * </br><b>String</b> - The prefix used to trigger this command
	 * </br><b>List&#60;Failure&#62;</b> - A list of all failures which happened throughout the parsing of the message
	 */
	public CommandListener setHelpFunction(TriFunction<MessageReceivedEvent, String, List<Failure>, MessageBuilder> function) {
		this.helperFunction = function;
		
		return this;
	}
	
	public MessageBuilder getHelp(MessageReceivedEvent event, String prefix, List<Failure> failures) {
		if(this.helperFunction != null) {
			MessageBuilder builder = this.helperFunction.apply(event, prefix, failures);
			
			if(builder != null) {
				return builder;
			}else{
				System.err.println("The help function returned a null object, I will return the default help instead");
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
		
		return new MessageBuilder().setEmbed(new EmbedBuilder().setDescription(description.toString())
			.setFooter("* means required. [] means multiple arguments of that type.", null)
			.setAuthor("Help", null, event.getJDA().getSelfUser().getEffectiveAvatarUrl()).build());
	}
	
	/**
	 * Set the cooldown manager which will be used to handle cooldowns on commands
	 */
	public CommandListener setCooldownManager(ICooldownManager cooldownHandler) {
		Checks.notNull(cooldownHandler, "ICooldownManager");
		
		this.cooldownManager = cooldownHandler;
		
		return this;
	}
	
	/**
	 * @return the {@link ICooldownManager} which is handling the command cooldowns
	 */
	public ICooldownManager getCoooldownManager() {
		return this.cooldownManager;
	}
	
	/**
	 * Adds a pre-parse check which will determine whether or not the message should be parsed, this could be useful if you for instance blacklist a user or server
	 */
	public CommandListener addPreParseCheck(Predicate<MessageReceivedEvent> predicate) {
		Checks.notNull(predicate, "Predicate");
		
		this.preParseChecks.add(predicate);
		
		return this;
	}
	
	public CommandListener removePreParseCheck(Predicate<MessageReceivedEvent> predicate) {
		this.preParseChecks.remove(predicate);
		
		return this;
	}
	
	public List<Predicate<MessageReceivedEvent>> getPreParseChecks() {
		return Collections.unmodifiableList(this.preParseChecks);
	}
	
	/**
	 * Adds a pre command execution check which will determine whether or not the command should be executed, this could be useful if you for instance have disabled commands
	 * </br></br>
	 * This is checked before anything else in {@link CommandListener#execute(ICommand, CommandEvent, long, Object...)}, such as permission and cooldown checks
	 */
	public CommandListener addPreExecuteCheck(BiPredicate<ICommand, CommandEvent> predicate) {
		Checks.notNull(predicate, "Predicate");
		
		this.preExecuteChecks.add(predicate);
		
		return this;
	}
	
	public CommandListener removePreExecuteCheck(BiPredicate<ICommand, CommandEvent> predicate) {
		this.preExecuteChecks.remove(predicate);
		
		return this;
	}
	
	public List<BiPredicate<ICommand, CommandEvent>> getPreExecuteChecks() {
		return Collections.unmodifiableList(this.preExecuteChecks);
	}
	
	public void onEvent(Event event) {
		if(event instanceof MessageReceivedEvent) {
			this.onMessageReceived((MessageReceivedEvent) event);
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
	
	protected String extractPrefix(MessageReceivedEvent event) {
		String message = event.getMessage().getContentRaw();
		String prefix = null;
		
		/* Needs to work for both non-nicked mention and nicked mention */
		long botId = event.getJDA().getSelfUser().getIdLong();
		if(this.allowMentionPrefix && (message.startsWith("<@" + botId + "> ") || message.startsWith("<@!" + botId + "> "))) {
			prefix = message.substring(0, message.indexOf(" ") + 1);
		}else{
			for(String p : this.getPrefixes(event)) {
				if(message.startsWith(p)) {
					prefix = p;
					
					break;
				}
			}
		}
		
		return prefix;
	}
	
	protected Map<Object, Object> orderingKeys = new HashMap<Object, Object>();
	
	/* Would it be possible to split this event in to different steps, opinions? */
	protected void onMessageReceived(MessageReceivedEvent event) {
		long timeStarted = System.nanoTime();
		
		for(Predicate<MessageReceivedEvent> predicate : this.preParseChecks) {
			try {
				if(!predicate.test(event)) {
					return;
				}
			}catch(Exception e) {}
		}
		
		String message = event.getMessage().getContentRaw();
		
		String prefix = this.extractPrefix(event);
		if(prefix == null) {
			return;
		}
		
		for(CommandEventListener listener : this.commandEventListeners) {
			try {
				listener.onPrefixedMessage(event, prefix);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		message = message.substring(prefix.length());
		
		List<Failure> possibleCommands = new ArrayList<>();
		
		List<Pair<String, ICommand>> commands = this.getCommandStores().stream()
			.map(CommandStore::getCommands)
			.flatMap(Set::stream)
			.map(command -> command.getAllCommandsRecursiveWithTriggers(event, ""))
			.flatMap(List::stream)
			.filter(pair -> pair.getRight().verify(event, this))
			.filter(pair -> !pair.getRight().isPassive())
			.sorted(CommandListener.COMMAND_COMPARATOR)
			.collect(Collectors.toList());
		
		COMMANDS:
		for(Pair<String, ICommand> pair : commands) {
			ICommand command = pair.getRight();
			
			String msg = message, cmd = pair.getLeft();
			
			if(!command.isCaseSensitive()) {
				msg = msg.toLowerCase();
				cmd = cmd.toLowerCase();
			}
			
			if(!msg.startsWith(cmd)) {
				continue COMMANDS;
			}
			
			msg = message.substring(cmd.length());
			
			/* Happens if the command for instance would be "ping" and the content is "pingasd"*/
			if(msg.length() > 0 && msg.charAt(0) != ' ') {
				continue COMMANDS;
			}
			
			int argumentCount = 0;
			
			Object[] arguments = new Object[command.getArguments().length];
			
			IArgument<?>[] args = command.getArguments();
			
			boolean developer = this.isDeveloper(event.getAuthor().getIdLong());
			
			/* Creates a map of all the options which can be used by this user */
			Map<String, IOption> optionMap = new HashMap<>();
			for(IOption option : command.getOptions()) {
				if(option.isDeveloper() && !developer) {
					continue;
				}
				
				optionMap.put(option.getName(), option);
				for(String alias : option.getAliases()) {
					optionMap.put(alias, option);
				}
			}
			
			/* Pre-processing */
			StringBuilder builder = new StringBuilder();
			
			List<String> options = new ArrayList<>();
			for(int i = 0; i < msg.length(); i++) {
				if(msg.startsWith(" --", i) && msg.length() - i > 3 && msg.charAt(i + 3) != ' ') {
					String optionStr = msg.substring(i + 1);
					optionStr = optionStr.substring(2, (optionStr.contains(" ")) ? optionStr.indexOf(" ") : optionStr.length()).toLowerCase();
					
					IOption option = optionMap.get(optionStr);
					if(option != null) {
						options.add(optionStr);
						
						i += (optionStr.length() + 2);
						
						continue;
					}else{
						InvalidOptionPolicy optionPolicy = command.getInvalidOptionPolicy();
						if(optionPolicy.equals(InvalidOptionPolicy.ADD)) {
							options.add(optionStr);
							
							i += (optionStr.length() + 2);
							
							continue;
						}else if(optionPolicy.equals(InvalidOptionPolicy.IGNORE)) {
							i += (optionStr.length() + 2);
							
							continue;
						}else if(optionPolicy.equals(InvalidOptionPolicy.FAIL)) {
							/* The specified option does not exist */
							possibleCommands.add(new Failure(command, new UnknownOptionException(optionStr)));
							
							continue COMMANDS;
						}
					}
				}
				
				builder.append(msg.charAt(i));
			}
			
			msg = builder.toString();
			/* End pre-processing */
			
			ArgumentParsingType parsingType;
			ARGUMENT_PARSING:
			{
				List<ArgumentParsingType> argumentParsingTypes = Arrays.asList(command.getAllowedArgumentParsingTypes());
				
				if(argumentParsingTypes.contains(ArgumentParsingType.NAMED)) {
					/* Handle command as key-value */
					Map<String, String> map = this.asMap(msg);
					
					if(map != null) {
						for(int i = 0; i < args.length; i++) {
							IArgument<?> argument = args[i];
							if(map.containsKey(argument.getName())) {
								String value = map.get(argument.getName());
								
								VerifiedArgument<?> verified = argument.verify(event, value);
								switch(verified.getVerifiedType()) {
									case INVALID: {
										/* The content does not make for a valid argument */
										possibleCommands.add(new Failure(command, new ArgumentParseException(argument, value)));
										
										continue COMMANDS;
									}
									case VALID:
									case VALID_END_NOW: {
										arguments[argumentCount++] = verified.getObject();
										
										break;
									}
								}
								
								arguments[i] = verified.getObject();
							}else{
								/* Missing argument */
								possibleCommands.add(new Failure(command, new MissingRequiredArgumentException(argument)));
								
								continue COMMANDS;
							}
						}
						
						parsingType = ArgumentParsingType.NAMED;
						
						break ARGUMENT_PARSING;
					}
				}
				
				if(argumentParsingTypes.contains(ArgumentParsingType.POSITIONAL)) {
					ARGUMENTS:
					for(int i = 0; i < arguments.length; i++) {
						if(msg.length() > 0) {
							if(msg.startsWith(" ")) {
								msg = msg.substring(1);
							}else{ /* When does it get here? */
								/* The argument for some reason does not start with a space */
								possibleCommands.add(new Failure(command, new ArgumentParseException(null, msg)));
								
								continue COMMANDS;
							}
						}
						
						IArgument<?> argument = args[i];
						
						VerifiedArgument<?> verified;
						String content = null;
						if(argument.isEndless()) {
							if(msg.length() == 0 && !argument.acceptEmpty()) {
								/* There is no more content and the argument does not accept no content */
								possibleCommands.add(new Failure(command, new OutOfContentException(argument)));
								
								continue COMMANDS;
							}
							
							verified = argument.verify(event, content = msg);
							msg = "";
						}else{
							if(msg.length() > 0) {
								/* Is this even worth having? Not quite sure if I like the implementation */
								if(argument instanceof IEndlessArgument) {
									if(msg.charAt(0) == '[') {
										int endBracket = 0;
										while((endBracket = msg.indexOf(']', endBracket + 1)) != -1 && msg.charAt(endBracket - 1) == '\\');
										
										if(endBracket != -1) {
											content = msg.substring(1, endBracket);
											
											msg = msg.substring(content.length() + 2);
											
											content = content.replace("\\[", "[").replace("\\]", "]");
										}
									}
								}else if(argument.acceptQuote()) {
									if(msg.charAt(0) == '"') {
										int nextQuote = 0;
										while((nextQuote = msg.indexOf('"', nextQuote + 1)) != -1 && msg.charAt(nextQuote - 1) == '\\');
										
										if(nextQuote != -1) {
											content = msg.substring(1, nextQuote);
											
											msg = msg.substring(content.length() + 2);
											
											content = content.replace("\\\"", "\"");
										}
									}
								}
								
								if(content == null) {
									content = msg.substring(0, (msg.contains(" ")) ? msg.indexOf(" ") : msg.length());
									msg = msg.substring(content.length());
								}
							}else{
								content = "";
							}
							
							/* There is no more content and the argument does not accept no content */
							if(content.length() == 0 && !argument.acceptEmpty()) {
								possibleCommands.add(new Failure(command, new OutOfContentException(argument)));
								
								continue COMMANDS;
							}
							
							verified = argument.verify(event, content);
						}
						
						switch(verified.getVerifiedType()) {
							/* The content does not make for a valid argument */
							case INVALID: {
								possibleCommands.add(new Failure(command, new ArgumentParseException(argument, content)));
								
								continue COMMANDS;
							}
							case VALID: {
								arguments[argumentCount++] = verified.getObject();
								
								break;
							}
							case VALID_END_NOW: {
								arguments[argumentCount++] = verified.getObject();
								
								break ARGUMENTS;
							}
						}
					}
					
					/* There is more content than the arguments could handle */
					if(msg.length() > 0) {
						if(command.getContentOverflowPolicy().equals(ContentOverflowPolicy.FAIL)) {
							possibleCommands.add(new Failure(command, new ContentOverflowException(msg)));
							
							continue COMMANDS;
						}
					}
					
					/* Not the correct amount of arguments for the command */
					if(command.getArguments().length != argumentCount) {
						Object[] temp = new Object[argumentCount];
						for(int i = 0; i < temp.length; i++) {
							temp[i] = arguments[i];
						}
						
						possibleCommands.add(new Failure(command, new InvalidArgumentCountException(command.getArguments(), temp)));
						
						continue COMMANDS;
					}
					
					parsingType = ArgumentParsingType.POSITIONAL;
					
					break ARGUMENT_PARSING;
				}
				
				/* If the command for some reason does not have any allowed parsing type */
				continue COMMANDS;
			}
			
			CommandEvent commandEvent = new CommandEvent(event, this, command, arguments, prefix, pair.getLeft(), options, parsingType, msg);
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
					}
				});
			}else{
				this.execute(command, commandEvent, timeStarted, arguments);
			}
			
			return;
		}
		
		if(possibleCommands.size() > 0) {
			if(this.helpEnabled) {
				if(event.getChannelType().isGuild()) {
					Member bot = event.getGuild().getSelfMember();
					
					if(!bot.hasPermission(Permission.MESSAGE_WRITE)) {
						event.getAuthor().openPrivateChannel().queue(channel -> {
							channel.sendMessage("Missing permission **" + Permission.MESSAGE_WRITE.getName() + "** in " + event.getChannel().getName() + ", " + event.getGuild().getName()).queue();
						});
						
						return;
					}else if(!bot.hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
						event.getChannel().sendMessage("Missing permission **" + Permission.MESSAGE_EMBED_LINKS.getName() + "** in " + event.getChannel().getName() + ", " + event.getGuild().getName()).queue();
						
						return;
					}
				}
				
				event.getChannel().sendMessage(this.getHelp(event, prefix, new ArrayList<>(possibleCommands)).build()).queue();
			}
		}else{
			for(CommandEventListener listener : this.commandEventListeners) {
				try {
					listener.onUnknownCommand(event, prefix);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/** Method used to convert a command to a map, for instance 
	 * </br><b>color=#00FFFF name="a cyan role" permissions=8</b>
	 * </br>would be parsed to a map with all the values, like this
	 * </br><b>{color="#00FFFF", name="a cyan role", permissions="8"}</b>
	 */
	/* This should probably be re-worked */
	protected Map<String, String> asMap(String command) {
		Map<String, String> map = new HashMap<>();
		
		String message = command;
		while(map != null && message.length() > 0) {
			if(message.startsWith(" ")) {
				message = message.substring(1);
			}
			
			if(message.contains("=")) {
				String key = message.substring(0, message.indexOf("="));
				message = message.substring(key.length() + 1);
				
				key = key.trim();
				
				if(!key.contains(" ")) {
					if(message.startsWith(" ")) {
						message = message.substring(1);
					}
					
					String value = null;
					if(message.charAt(0) == '"') {
						int nextQuote = 0;
						while((nextQuote = message.indexOf('"', nextQuote + 1)) != -1 && message.charAt(nextQuote - 1) == '\\');
						
						if(nextQuote != -1) {
							value = message.substring(1, nextQuote);
							
							message = message.substring(value.length() + 2);
							
							value = value.replace("\\\"", "\"");
						}
					}
					
					if(value == null) {
						value = message.substring(0, (message.contains(" ")) ? message.indexOf(" ") : message.length());
						message = message.substring(value.length());
					}
					
					map.put(key, value);
				}else{
					map = null;
				}
			}else{
				map = null;
			}
		}
		
		return map;
	}
	
	protected boolean canExecute(CommandEvent event, ICommand command) {
		if(event.getChannelType().isGuild()) {
			{
				long neededPermissions = Permission.getRaw(command.getBotDiscordPermissions()) | Permission.MESSAGE_WRITE.getRawValue();
				long currentPermissions = Permission.getRaw(event.getMember().getPermissions(event.getTextChannel()));
				
				long permissions = (neededPermissions & ~currentPermissions);
				
				if(permissions != 0) {
					if(this.missingPermissionFunction != null) {
						this.missingPermissionFunction.accept(event, Permission.getPermissions(permissions));
					}
					
					return false;
				}
			}
			
			if(command.getAuthorDiscordPermissions().length > 0) {
				long neededPermissions = Permission.getRaw(command.getAuthorDiscordPermissions());
				long currentPermissions = Permission.getRaw(event.getMember().getPermissions(event.getTextChannel()));
				
				long permissions = (neededPermissions & ~currentPermissions);
				
				if(permissions != 0) {
					if(this.missingAuthorPermissionFunction != null) {
						this.missingAuthorPermissionFunction.accept(event, Permission.getPermissions(permissions));
					}
					
					return false;
				}
			}
			
			if(command.isNSFW() && !event.getTextChannel().isNSFW()) {
				if(this.nsfwFunction != null) {
					this.nsfwFunction.accept(event);
				}
				
				return false;
			}
		}
		
		return true;
	}
	
	protected void execute(ICommand command, CommandEvent event, long timeStarted, Object... arguments) {
		for(BiPredicate<ICommand, CommandEvent> predicate : this.preExecuteChecks) {
			try {
				if(!predicate.test(command, event)) {
					return;
				}
			}catch(Exception e) {}
		}
		
		ICommand actualCommand = (command instanceof DummyCommand) ? command.getParent() : command;
		if(!this.canExecute(event, actualCommand)) {
			return;
		}
		
		try {
			if(command.getCooldownDuration() > 0) {
				ICooldown cooldown = this.cooldownManager.getCooldown(actualCommand, event.getEvent());
				long timeRemaining = cooldown != null ? cooldown.getTimeRemainingMillis() : -1;
				
				if(timeRemaining > 0) {
					if(this.cooldownFunction != null) {
						this.cooldownFunction.accept(event, cooldown);
					}
					
					return;
				}
				
				/* Add the cooldown before the command has executed so that in case the command has a long execution time it will not get there */
				this.cooldownManager.createCooldown(actualCommand, event.getEvent());
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
				this.cooldownManager.removeCooldown(actualCommand, event.getEvent());
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
			
			try {
				/* This should probably be changed due to illegal access */
				Field field = Throwable.class.getDeclaredField("detailMessage");
				field.setAccessible(true);
				field.set(e, "Attempted to execute command (" + event.getCommandTrigger() + ") with arguments " + Arrays.deepToString(arguments) + " but failed" + ((e.getMessage() != null) ? " with the message \"" + e.getMessage() + "\""  : ""));
			}catch(Exception e1) {
				e1.printStackTrace();
			}
			
			e.printStackTrace();
			
			return;
		}
		
		System.out.println("Executed command (" + event.getCommandTrigger() + ") with the arguments " + Arrays.deepToString(arguments) + ", time elapsed " + (System.nanoTime() - timeStarted));
	}
}