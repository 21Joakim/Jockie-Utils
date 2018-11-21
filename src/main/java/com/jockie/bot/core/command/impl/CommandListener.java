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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.IEndlessArgument;
import com.jockie.bot.core.argument.VerifiedArgument;
import com.jockie.bot.core.command.ICommand;
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
import net.dv8tion.jda.core.MessageBuilder.Formatting;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.tuple.Pair;

public class CommandListener implements EventListener {
	
	/* More specific goes first */
	private static final Comparator<Pair<String, ICommand>> COMMAND_COMPARATOR = new Comparator<Pair<String, ICommand>>() {
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
	
	private String[] defaultPrefixes = {"!"};
	
	private Function<MessageReceivedEvent, String[]> prefixFunction;
	
	private TriFunction<MessageReceivedEvent, String, List<Failure>, MessageBuilder> helperFunction;
	
	private boolean helpEnabled = true;
	private boolean missingPermissionEnabled = true;
	
	private Set<Long> developers = new HashSet<>();
	
	private List<CommandStore> commandStores = new ArrayList<>();
	
	private List<CommandEventListener> commandEventListeners = new ArrayList<>();
	
	private ExecutorService commandExecutor = Executors.newCachedThreadPool();
	
	private ICooldownManager cooldownManager = new CooldownManager();
	
	private List<Predicate<MessageReceivedEvent>> preParseChecks = new ArrayList<>();
	
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
	 * See {@link #getDevelopers()}
	 */
	public CommandListener addDeveloper(long id) {
		this.developers.add(id);
		
		return this;
	}
	
	/**
	 * See {@link #getDevelopers()}
	 */
	public CommandListener removeDeveloper(long id) {
		this.developers.remove(id);
		
		return this;
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
	 * See {@link #isMissingPermissionsEnabled()}
	 */
	public CommandListener setMissingPermissionsEnabled(boolean enabled) {
		this.missingPermissionEnabled = enabled;
		
		return this;
	}
	
	/**
	 * Whether or not the bot should return a message when the command failed due to missing permissions 
	 * ({@link net.dv8tion.jda.core.exceptions.PermissionException PermissionException} being thrown)
	 */
	public boolean isMissingPermissionsEnabled() {
		return this.missingPermissionEnabled;
	}
	
	/**
	 * @param function the function that will be called when a command had the wrong arguments. 
	 * </br></br>Parameters for the function:
	 * </br><b>MessageReceivedEvent</b> - The event that triggered this
	 * </br><b>String</b> - Prefix
	 * </br><b>List&#60;ICommand&#62;</b> - The possible commands which the message could be referring to
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
		
		failures = failures.stream()
			.filter(failure -> !(failure.getCommand() instanceof DummyCommand))
			.collect(Collectors.toList());
		
		StringBuilder description = new StringBuilder();
		for(int i = 0; i < failures.size(); i++) {
			ICommand command = failures.get(i).getCommand();
			
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
	
	/* Would it be possible to split this event in to different steps, opinions? */
	public void onMessageReceived(MessageReceivedEvent event) {
		for(Predicate<MessageReceivedEvent> predicate : this.preParseChecks) {
			try {
				if(!predicate.test(event)) {
					return;
				}
			}catch(Exception e) {}
		}
		
		String[] prefixes = this.getPrefixes(event);
		
		String message = event.getMessage().getContentRaw(), prefix = null;
		
		/* Needs to work for both non-nicked mention and nicked mention */
		if(message.startsWith("<@" + event.getJDA().getSelfUser().getId() + "> ") || message.startsWith("<@!" + event.getJDA().getSelfUser().getId() + "> ")) {
			/* I want every bot to have this feature therefore it will be a hard coded one, arguments against it? */
			prefix = message.substring(0, message.indexOf(" ") + 1);
			
			if(message.equals(prefix + "prefix") || message.equals(prefix + "prefixes")) {
				String allPrefixes = Arrays.deepToString(prefixes);
				allPrefixes = allPrefixes.substring(1, allPrefixes.length() - 1);
				
				event.getChannel().sendMessage(new MessageBuilder()
					.append("My prefix")
					.append(prefixes.length > 1 ? "es are " : " is ")
					.append(allPrefixes, Formatting.BOLD)
					.build()).queue();
				
				return;
			}
		}else{
			for(String p : prefixes) {
				if(message.startsWith(p)) {
					prefix = p;
					
					break;
				}
			}
		}
		
		if(prefix != null) {
			long commandStarted = System.nanoTime();
			
			message = message.substring(prefix.length());
			
			List<Failure> possibleCommands = new ArrayList<>();
			
			List<Pair<String, ICommand>> commands = this.getCommandStores().stream()
				.map(CommandStore::getCommands)
				.flatMap(List::stream)
				.map(command -> command.getAllCommandsRecursive(event, ""))
				.flatMap(List::stream)
				.filter(pair -> pair.getRight().verify(event, this))
				.filter(pair -> !pair.getRight().isPassive())
				.sorted(COMMAND_COMPARATOR)
				.collect(Collectors.toList());
			
			COMMANDS :
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
				
				if(msg.length() > 0 && msg.charAt(0) != ' ') {
					/* Can it even get to this? */
					
					continue COMMANDS;
				}
				
				int argumentCount = 0;
				
				Object[] arguments = new Object[command.getArguments().length];
				
				IArgument<?>[] args = command.getArguments();
				
				boolean developer = this.isDeveloper(event.getAuthor().getIdLong());
				
				/* Creates a map of all the options which can be used by this user */
				Map<String, IOption> optionMap = new HashMap<>();
				for(IOption option : command.getOptions()) {
					if(option.isDeveloperOption() && !developer) {
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
					if(msg.charAt(i) == ' ' && msg.length() - i > 3 && (msg.charAt(i + 1) == '-' && msg.charAt(i + 2) == '-') && msg.charAt(i + 3) != ' ') {
						String optionStr = msg.substring(i + 1);
						optionStr = optionStr.substring(2, (optionStr.contains(" ")) ? optionStr.indexOf(" ") : optionStr.length()).toLowerCase();
						
						IOption option = optionMap.get(optionStr);
						if(option != null) {
							options.add(optionStr);
							
							i += (optionStr.length() + 2);
							
							continue;
						}else{
							if(command.getInvalidOptionPolicy().equals(InvalidOptionPolicy.ADD)) {
								options.add(optionStr);
								
								i += (optionStr.length() + 2);
								
								continue;
							}else if(command.getInvalidOptionPolicy().equals(InvalidOptionPolicy.IGNORE)) {
								i += (optionStr.length() + 2);
								
								continue;
							}else if(command.getInvalidOptionPolicy().equals(InvalidOptionPolicy.FAIL)) {
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
				
				/* Handle command as key-value (This needs to be updated) */
				Map<String, String> map = new HashMap<>();
				
				String tempMsg = msg;
				while(map != null && tempMsg.length() > 0) {
					if(tempMsg.startsWith(" ")) {
						tempMsg = tempMsg.substring(1);
					}
					
					if(tempMsg.contains("=")) {
						String key = tempMsg.substring(0, tempMsg.indexOf("="));
						tempMsg = tempMsg.substring(key.length() + 1);
						
						key = key.trim();
						
						if(!key.contains(" ")) {
							if(tempMsg.startsWith(" ")) {
								tempMsg = tempMsg.substring(1);
							}
							
							String value = null;
							if(tempMsg.charAt(0) == '"') {
								int nextQuote = 0;
								while((nextQuote = tempMsg.indexOf('"', nextQuote + 1)) != -1 && tempMsg.charAt(nextQuote - 1) == '\\');
								
								if(nextQuote != -1) {
									value = tempMsg.substring(1, nextQuote);
									
									tempMsg = tempMsg.substring(value.length() + 2);
									
									value = value.replace("\\\"", "\"");
								}
							}
							
							if(value == null) {
								value = tempMsg.substring(0, (tempMsg.contains(" ")) ? tempMsg.indexOf(" ") : tempMsg.length());
								tempMsg = tempMsg.substring(value.length());
							}
							
							map.put(key, value);
						}else{
							map = null;
						}
					}else{
						map = null;
					}
				}
				
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
				}else{
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
					
					/* There is more content than the arguments handled */
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
				}
				
				CommandEvent commandEvent = new CommandEvent(event, this, command, arguments, prefix, pair.getLeft(), options);
				if(command.isExecuteAsync()) {
					this.commandExecutor.submit(() -> {
						this.execute(command, event, commandEvent, commandStarted, arguments);
					});
				}else{
					this.execute(command, event, commandEvent, commandStarted, arguments);
				}
				
				return;
			}
			
			if(this.helpEnabled && possibleCommands.size() > 0) {
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
		}
	}
	
	private boolean checkPermissions(MessageReceivedEvent event, CommandEvent commandEvent, ICommand command) {
		if(event.getChannelType().isGuild()) {
			Member bot = event.getGuild().getMember(event.getJDA().getSelfUser());
			
			long permissionsNeeded = Permission.getRaw(command.getBotDiscordPermissionsNeeded());
			
			StringBuilder missingPermissions = new StringBuilder();
			for(Permission permission : Permission.getPermissions(permissionsNeeded)) {
				if(!bot.hasPermission(event.getTextChannel(), permission)) {
					missingPermissions.append(permission.getName() + "\n");
				}
			}
			
			if(missingPermissions.length() > 0) {
				StringBuilder message = new StringBuilder()
					.append("Missing permission" + (missingPermissions.length() > 1 ? "s" : "") + " to execute **")
					.append(commandEvent.getCommandTrigger()).append("** in ")
					.append(event.getChannel().getName())
					.append(", ")
					.append(event.getGuild().getName())
					.append("\n```")
					.append(missingPermissions)
					.append("```");
				
				MessageChannel channel;
				if(!bot.hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE)) {
					channel = event.getAuthor().openPrivateChannel().complete();
				}else{
					channel = event.getChannel();
				}
				
				channel.sendMessage(message).queue();
				
				return false;
			}
		}
		
		return true;
	}
	
	private void execute(ICommand command, MessageReceivedEvent event, CommandEvent commandEvent, long timeStarted, Object... arguments) {
		if(this.checkPermissions(event, commandEvent, command)) {
			ICommand actualCommand = (command instanceof DummyCommand) ? command.getParent() : command;
			
			try {
				if(command.getCooldownDuration() > 0) {
					ICooldown cooldown = this.cooldownManager.getCooldown(actualCommand, event);
					long timeRemaining = cooldown != null ? cooldown.getTimeRemainingMillis() : -1;
					
					if(timeRemaining <= 0) {
						/* Add the cooldown before the command has executed so that in case the command has a long execution time it will not get there */
						this.cooldownManager.createCooldown(actualCommand, event);
						
						/* Additional features surrounding this will come in the future */
						for(Function<CommandEvent, Object> function : command.getBeforeExecuteFunctions()) {
							try {
								function.apply(commandEvent);
							}catch(CancelException e) {
								if(command.getCooldownDuration() > 0) {
									this.cooldownManager.removeCooldown(actualCommand, event);
								}
								
								return;
							}catch(Exception e) {
								e.printStackTrace();
							}
						}
						
						command.execute(event, commandEvent, arguments);
						
						for(Function<CommandEvent, Object> function : command.getAfterExecuteFunctions()) {
							try {
								function.apply(commandEvent);
							}catch(Exception e) {
								e.printStackTrace();
							}
						}
					}else{
						event.getChannel().sendMessage("Slow down, try again in " + ((double) timeRemaining/1000) + " seconds").queue();
					}
				}else{
					command.execute(event, commandEvent, arguments);
				}
				
				for(CommandEventListener listener : this.commandEventListeners) {
					/* Wrapped in a try catch because we don't want the execution of this to fail just because we couldn't rely on an event handler not to throw an exception */
					try {
						listener.onCommandExecuted(command, event, commandEvent);
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			}catch(Throwable e) {
				if(command.getCooldownDuration() > 0) {
					/* If the command execution fails then no cooldown should be applied */
					this.cooldownManager.removeCooldown(actualCommand, event);
				}
				
				if(e instanceof CancelException) {
					return;
				}
				
				if(e instanceof PermissionException) {
					System.err.println("Attempted to execute command (" + commandEvent.getCommandTrigger() + ") with arguments " + Arrays.deepToString(arguments) + 
						", though it failed due to missing permissions, time elapsed " + (System.nanoTime() - timeStarted) + 
						", error message (" + e.getMessage() + ")");
					
					for(CommandEventListener listener : this.commandEventListeners) {
						/* Wrapped in a try catch because we don't want the execution of this to fail just because we couldn't rely on an event handler not to throw an exception */
						try {
							listener.onCommandMissingPermissions(command, event, commandEvent, (PermissionException) e);
						}catch(Exception e1) {
							e1.printStackTrace();
						}
					}
					
					if(this.missingPermissionEnabled) {
						event.getChannel().sendMessage("Missing permission **" + ((PermissionException) e).getPermission().getName() + "**").queue();
					}
					
					return;
				}
				
				for(CommandEventListener listener : this.commandEventListeners) {
					/* Wrapped in a try catch because we don't want the execution of this to fail just because we couldn't rely on an event handler not to throw an exception */
					try {
						listener.onCommandExecutionException(command, event, commandEvent, e);
					}catch(Exception e1) {
						e1.printStackTrace();
					}
				}
				
				try {
					/* This should probably be changed due to illegal access */
					Field field = Throwable.class.getDeclaredField("detailMessage");
					field.setAccessible(true);
					field.set(e, "Attempted to execute command (" + commandEvent.getCommandTrigger() + ") with arguments " + Arrays.deepToString(arguments) + " but failed" + ((e.getMessage() != null) ? " with the message \"" + e.getMessage() + "\""  : ""));
				}catch(Exception e1) {
					e1.printStackTrace();
				}
				
				e.printStackTrace();
				
				return;
			}
			
			System.out.println("Executed command (" + commandEvent.getCommandTrigger() + ") with the arguments " + Arrays.deepToString(arguments) + ", time elapsed " + (System.nanoTime() - timeStarted));
		}
	}
}