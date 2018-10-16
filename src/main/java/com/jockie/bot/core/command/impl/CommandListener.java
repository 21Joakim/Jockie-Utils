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
import java.util.stream.Collectors;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.IEndlessArgument;
import com.jockie.bot.core.argument.VerifiedArgument;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.ICommand.OptionPolicy;
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
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.tuple.Pair;

public class CommandListener implements EventListener {
	
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
			}
			
			return 0;
		}
	};
	
	private Permission[] genericPermissions = {};
	
	private String[] defaultPrefixes = {"!"};
	
	private Function<MessageReceivedEvent, String[]> prefixFunction;
	
	private TriFunction<MessageReceivedEvent, CommandEvent, List<ICommand>, MessageBuilder> helperFunction;
	
	private boolean helpEnabled = true;
	private boolean missingPermissionEnabled = true;
	
	private List<Long> developers = new ArrayList<>();
	
	private List<CommandStore> commandStores = new ArrayList<>();
	
	private List<CommandEventListener> commandEventListeners = new ArrayList<>();
	
	private ExecutorService commandExecutor = Executors.newCachedThreadPool();
	
	private ICooldownManager cooldownManager = new CooldownManager();
	
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
	 * See {@link #getGenericPermissions()}
	 */
	public CommandListener setGenericPermissions(Permission... permissions) {
		this.genericPermissions = permissions;
		
		return this;
	}
	
	/**
	 * @return a set of permissions which will always be checked for no matter the properties of the command. If the bot does not have these permissions the commands will not work
	 */
	public Permission[] getGenericPermissions() {
		return this.genericPermissions;
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
	public List<Long> getDevelopers() {
		return Collections.unmodifiableList(this.developers);
	}
	
	/**
	 * See {@link #getPrefixes(MessageReceivedEvent)}
	 * 
	 * @param function the function which will return a set amount of prefixes for the specific context,
	 * for instance you can return guild or user specific prefixes
	 */
	public CommandListener setPrefixesFunction(Function<MessageReceivedEvent, String[]> function) {
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
				System.err.println("The prefix function returned a null object, I will return the default prefixes instead");
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
	 * </br><b>CommandEvent</b> - Information about the command and context
	 * </br><b>List&#60;ICommand&#62;</b> - The possible commands which the message could be referring to
	 */
	public CommandListener setHelpFunction(TriFunction<MessageReceivedEvent, CommandEvent, List<ICommand>, MessageBuilder> function) {
		this.helperFunction = function;
		
		return this;
	}
	
	public MessageBuilder getHelp(MessageReceivedEvent event, CommandEvent commandEvent, List<ICommand> commands) {
		if(this.helperFunction != null) {
			MessageBuilder builder = this.helperFunction.apply(event, commandEvent, commands);
			
			if(builder != null) {
				return builder;
			}else{
				System.err.println("The help function returned a null object, I will return the default help instead");
			}
		}
		
		StringBuilder description = new StringBuilder();
		for(int i = 0; i < commands.size(); i++) {
			ICommand command = commands.get(i);
			
			description.append(command.getCommandTrigger())
				.append(" ")
				.append(command.getArgumentInfo());
			
			if(i < commands.size() - 1) {
				description.append("\n");
			}
		}
		
		return new MessageBuilder().setEmbed(new EmbedBuilder().setDescription(description.toString())
			.setFooter("* means required. [] means multiple arguments of that type.", null)
			.setAuthor("Help", null, event.getJDA().getSelfUser().getEffectiveAvatarUrl()).build());
	}
	
	public CommandListener setCooldownManager(ICooldownManager cooldownHandler) {
		Checks.notNull(cooldownHandler, "ICooldownManager");
		
		this.cooldownManager = cooldownHandler;
		
		return this;
	}
	
	public ICooldownManager getCoooldownManager() {
		return this.cooldownManager;
	}
	
	public void onEvent(Event event) {
		if(event instanceof MessageReceivedEvent) {
			this.onMessageReceived((MessageReceivedEvent) event);
		}
	}
	
	/* Would it be possible to split this event in to different steps, opinions? */
	public void onMessageReceived(MessageReceivedEvent event) {
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
			
			Set<ICommand> possibleCommands = new HashSet<>();
			
			/* This is probably not the best but it works */
			List<Pair<ICommand, List<?>>> allCommands = this.getCommandStores().stream()
				.map(store -> store.getCommands())
				.flatMap(List::stream)
				.map(command -> command.getAllCommandsRecursive(event, ""))
				.flatMap(List::stream)
				.filter(pair -> pair.getLeft().verify(event, this))
				.filter(pair -> !pair.getLeft().isPassive())
				.collect(Collectors.toList());
			
			List<Pair<String, ICommand>> commands = new ArrayList<>();
			for(Pair<ICommand, List<?>> pair : allCommands) {
				for(Object obj : pair.getRight()) {
					if(obj instanceof String) {
						commands.add(Pair.of((String) obj, pair.getLeft()));
					}else if(obj instanceof Pair) {
						@SuppressWarnings("unchecked")
						Pair<ICommand, List<String>> pairs = (Pair<ICommand, List<String>>) obj;
						
						for(String trigger : pairs.getRight()) {
							commands.add(Pair.of(trigger, pairs.getLeft()));
						}
					}
				}
			}
			
			commands.sort(COMMAND_COMPARATOR);
			
			COMMANDS :
			for(Pair<String, ICommand> pair : commands) {
				ICommand command = pair.getRight();
				
				boolean developer = this.developers.contains(event.getAuthor().getIdLong());
				
				Map<String, IOption> optionMap = new HashMap<>();
				for(IOption option : command.getOptions()) {
					if(option.isDeveloperOption() && developer) {
						optionMap.put(option.getName(), option);
						for(String alias : option.getAliases()) {
							optionMap.put(alias, option);
						}
					}
				}
				
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
				
				List<String> options = new ArrayList<>();
				
				IArgument<?>[] args = command.getArguments();
				
				/* Pre-processing */
				StringBuilder builder = new StringBuilder();
				
				for(int i = 0; i < msg.length(); i++) {
					if(msg.charAt(i) == ' ') {
						if(msg.length() - i > 3) {
							if(msg.charAt(i + 1) == '-' && msg.charAt(i + 2) == '-') {
								if(msg.charAt(i + 3) != ' ') {
									String optionStr = msg.substring(i + 1);
									optionStr = optionStr.substring(2, (optionStr.contains(" ")) ? optionStr.indexOf(" ") : optionStr.length()).toLowerCase();
									
									IOption option = optionMap.get(optionStr);
									if(option != null) {
										options.add(optionStr);
										
										i += (optionStr.length() + 2);
										
										continue;
									}else{
										if(command.getOptionPolicy().equals(OptionPolicy.IGNORE)) {
											i += (optionStr.length() + 2);
											
											continue;
										}else if(command.getOptionPolicy().equals(OptionPolicy.FAIL)) {
											possibleCommands.add((command instanceof DummyCommand) ? command.getParent() : command);
											
											continue COMMANDS;
										}
									}
								}
							}
						}
					}
					
					builder.append(msg.charAt(i));
				}
				
				msg = builder.toString();
				/* End pre-processing */
				
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
							VerifiedArgument<?> verified = argument.verify(event, map.get(argument.getName()));
							switch(verified.getVerifiedType()) {
								case INVALID: {
									possibleCommands.add((command instanceof DummyCommand) ? command.getParent() : command);
									
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
							possibleCommands.add((command instanceof DummyCommand) ? command.getParent() : command);
							
							continue COMMANDS;
						}
					}
				}else{
					ARGUMENTS:
					for(int i = 0; i < arguments.length; i++) {
						if(msg.length() > 0) {
							if(msg.startsWith(" ")) {
								msg = msg.substring(1);
							}else{
								/* When does it get here? */
								
								continue COMMANDS;
							}
						}
						
						IArgument<?> argument = args[i];
						
						VerifiedArgument<?> verified;
						if(argument.isEndless()) {
							if(msg.length() == 0 && !argument.acceptEmpty()) {
								possibleCommands.add((command instanceof DummyCommand) ? command.getParent() : command);
								
								continue COMMANDS;
							}
							
							verified = argument.verify(event, msg);
							msg = "";
						}else{
							String content = null;
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
							
							if(content.length() == 0 && !argument.acceptEmpty()) {
								possibleCommands.add((command instanceof DummyCommand) ? command.getParent() : command);
								
								continue COMMANDS;
							}
							
							verified = argument.verify(event, content);
						}
						
						switch(verified.getVerifiedType()) {
							case INVALID: {
								possibleCommands.add((command instanceof DummyCommand) ? command.getParent() : command);
								
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
						continue COMMANDS;
					}
					
					/* Not the correct amount of arguments for the command */
					if(command.getArguments().length != argumentCount) {
						continue COMMANDS;
					}
				}
				
				CommandEvent commandEvent = new CommandEvent(event, this, prefix, cmd, pair.getLeft(), options);
				if(command.isExecuteAsync()) {
					this.commandExecutor.submit(() -> {
						this.executeCommand(command, event, commandEvent, commandStarted, arguments);
					});
				}else{
					this.executeCommand(command, event, commandEvent, commandStarted, arguments);
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
				
				/* The alias for the CommandEvent is just everything after the prefix since there is no way to do it other than having a list of CommandEvent or aliases */
				event.getChannel().sendMessage(this.getHelp(event, new CommandEvent(event, this, prefix, message, null, null), new ArrayList<>(possibleCommands)).build()).queue();
			}
		}
	}
	
	private boolean checkPermissions(MessageReceivedEvent event, CommandEvent commandEvent, ICommand command) {
		if(event.getChannelType().isGuild()) {
			Member bot = event.getGuild().getMember(event.getJDA().getSelfUser());
			
			long permissionsNeeded = Permission.getRaw(this.genericPermissions) | Permission.getRaw(command.getBotDiscordPermissionsNeeded());
			
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
	
	private void executeCommand(ICommand command, MessageReceivedEvent event, CommandEvent commandEvent, long timeStarted, Object... arguments) {
		if(this.checkPermissions(event, commandEvent, command)) {
			ICommand actualCommand = (command instanceof DummyCommand) ? command.getParent() : command;
			
			try {
				if(command.getCooldownDuration() > 0) {
					ICooldown cooldown = this.cooldownManager.getCooldown(actualCommand, event);
					long timeRemaining = cooldown != null ? cooldown.getTimeRemainingMillis() : -1;
					
					if(timeRemaining <= 0) {
						/* Add the cooldown before the command has executed so that in case the command has a long execution time it will not get there */
						this.cooldownManager.createCooldown(actualCommand, event);
						
						command.execute(event, commandEvent, arguments);
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
						event.getChannel().sendMessage("Missing permission **" + ((InsufficientPermissionException) e).getPermission().getName() + "**").queue();
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
					Field field = Throwable.class.getDeclaredField("detailMessage");
					if(!field.canAccess(e)) {
						field.setAccessible(true);
					}
					
					field.set(e, "Attempted to execute command (" + commandEvent.getCommandTrigger() + ") with arguments " + Arrays.deepToString(arguments) + " but failed" + ((e.getMessage() != null) ? " with the message \"" + e.getMessage() + "\""  : ""));
				}catch(Exception e1) {
					e1.printStackTrace();
				}
				
				e.printStackTrace();
			}
			
			System.out.println("Executed command (" + commandEvent.getCommandTrigger() + ") with the arguments " + Arrays.deepToString(arguments) + ", time elapsed " + (System.nanoTime() - timeStarted));
		}
	}
}