package com.jockie.bot.core.command.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.jockie.bot.core.await.AwaitManager;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.argument.IArgument;
import com.jockie.bot.core.command.argument.IEndlessArgument;
import com.jockie.bot.core.command.argument.VerifiedArgument;
import com.jockie.bot.core.paged.impl.PagedManager;
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
import net.dv8tion.jda.core.hooks.EventListener;

public class CommandListener implements EventListener {
	
	private Permission[] genericPermissions = {};
	
	private String[] defaultPrefixes = {"!"};
	
	private Function<MessageReceivedEvent, String[]> prefixFunction;
	
	private TriFunction<MessageReceivedEvent, CommandEvent, List<FailedExecution>, MessageBuilder> helperFunction;
	
	private boolean helpEnabled = true, asyncEnabled = false;
	
	private List<Long> developers = new ArrayList<>();
	
	private List<CommandStore> commandStores = new ArrayList<>();
	
	private List<CommandEventListener> commandEventListeners = new ArrayList<>();
	
	private ExecutorService commandExecutor = Executors.newCachedThreadPool();
	
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
	 * See {@link #isAsyncEnabled()}
	 */
	public CommandListener setAsyncEnabled(boolean enabled) {
		this.asyncEnabled = enabled;
		
		return this;
	}
	
	/**
	 * Whether or not each command execution will be created on a new thread or not
	 */
	public boolean isAsyncEnabled() {
		return this.asyncEnabled;
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
	 * @param function the function that will be called when a command had the wrong arguments. 
	 * </br></br>Parameters for the function:
	 * </br><b>MessageReceivedEvent</b> - The event that triggered this
	 * </br><b>CommandEvent</b> - Information about the command and context
	 * </br><b>List&#60;ICommand&#62;</b> - The possible commands which the message could be referring to
	 */
	public CommandListener setHelpFunction(TriFunction<MessageReceivedEvent, CommandEvent, List<FailedExecution>, MessageBuilder> function) {
		this.helperFunction = function;
		
		return this;
	}
	
	public MessageBuilder getHelp(MessageReceivedEvent event, CommandEvent commandEvent, List<FailedExecution> commands) {
		if(this.helperFunction != null) {
			MessageBuilder builder = this.helperFunction.apply(event, commandEvent, commands);
			
			if(builder != null) {
				return builder;
			}else{
				System.err.println("The help function returned a null object, I will return the default help instead");
			}
		}
		
		List<String> reasons = commands.stream().map(failed -> failed.reasons).flatMap(List::stream).distinct().collect(Collectors.toList());
		
		if(reasons.size() == 1) {
			return new MessageBuilder().setEmbed(new EmbedBuilder().setDescription(reasons.get(0)).setColor(Color.RED).build());
		}else{
			StringBuilder description = new StringBuilder();
			for(int i = 0; i < commands.size(); i++) {
				FailedExecution failed = commands.get(i);
				
				ICommand command = failed.command;
				
				description.append("**Usage:** ")
					.append(commandEvent.getPrefix())
					.append(command.getCommand())
					.append(" ")
					.append(command.getArgumentInfo());
				
				if(command.getDescription() != null) {
					description.append("\n**Command Description:** ").append(command.getDescription());
				}
				
				if(i < commands.size() - 1) {
					description.append("\n\n");
				}
			}
			
			return new MessageBuilder().setEmbed(new EmbedBuilder().setDescription(description)
				.setFooter("\"*\" means required. \"[]\" means multiple arguments of that type.", null)
				.setAuthor("Help", null, event.getJDA().getSelfUser().getEffectiveAvatarUrl()).build());
		}
	}
	
	public void onEvent(Event event) {
		if(event instanceof MessageReceivedEvent) {
			this.onMessageReceived((MessageReceivedEvent) event);
		}
		
		AwaitManager.handleAwait(event);
	}
	
	private class FailedExecution {
		
		public final ICommand command;
		
		/* The reason there are multiple reasons is because DummyCommands should be considered
		 * the original command rather than a separate one therefore there can be multiple reasons a command fail */
		public List<String> reasons = new ArrayList<>();
		
		public FailedExecution(ICommand command) {
			this.command = command;
		}
	}
	
	/* Would it be possible to split this event in to different steps, opinions? */
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getChannelType().isGuild()) {
			if(PagedManager.handlePagedResults(event)) {
				return;
			}
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
			
			Map<ICommand, FailedExecution> possibleCommands = new HashMap<>();
			
			COMMANDS :
			for(ICommand command : this.getCommandStores().stream().map(store -> store.getCommandsAuthorized(event, this)).flatMap(List::stream).collect(Collectors.toList())) {
				String msg = message, cmd = command.getCommand(), alias = cmd;
				
				if(!command.isCaseSensitive()) {
					msg = msg.toLowerCase();
					cmd = cmd.toLowerCase();
				}
				
				VERIFY:
				if(!msg.startsWith(cmd + " ") && !msg.equals(cmd)) {
					for(String a : command.getAliases()) {
						String temp = !command.isCaseSensitive() ? a.toLowerCase() : a;
						if(msg.startsWith(temp + " ") || msg.equals(temp)) {
							alias = a;
							cmd = temp;
							
							break VERIFY;
						}
					}
					
					continue COMMANDS;
				}
				
				FailedExecution failed;
				if(!(command instanceof DummyCommand)) {
					failed = possibleCommands.get(command);
					
					if(failed == null) {
						failed = new FailedExecution(command);
						
						possibleCommands.put(command, failed);
					}
				}else{
					ICommand original = ((DummyCommand) command).getDummiedCommand();
					
					failed = possibleCommands.get(original);
					
					if(failed == null) {
						failed = new FailedExecution(original);
						
						possibleCommands.put(original, failed);
					}
				}
				
				msg = message.substring(cmd.length());
				
				if(msg.length() > 0 && msg.charAt(0) != ' ') {
					/* Can it even get to this? */
					
					continue COMMANDS;
				}
				
				int args = 0;
				
				Object[] arguments = new Object[command.getArguments().length];
				
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
					
					IArgument<?> argument = command.getArguments()[i];
					
					VerifiedArgument<?> verified;
					if(argument.isEndless()) {
						if(msg.length() == 0 && !argument.acceptEmpty()) {
							failed.reasons.add("Argument **" + (argument.getName() != null ? argument.getName() : "argument " + (i + 1)) + "** can not be empty");
							
							continue COMMANDS;
						}
						
						verified = argument.verify(event, msg);
						msg = "";
					}else{
						String content = null;
						if(msg.length() > 0) {
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
							failed.reasons.add("Argument **" + (argument.getName() != null ? argument.getName() : "argument " + (i + 1)) + "** can not be empty");
							
							continue COMMANDS;
						}
						
						verified = argument.verify(event, content);
					}
					
					switch(verified.getVerifiedType()) {
						case INVALID: {
							String reason = argument.getError();
							if(reason == null) {
								reason = verified.getError();
								if(reason == null) {
									reason = "is invalid";
								}
							}
							
							failed.reasons.add("Argument **" + (argument.getName() != null ? argument.getName() : "argument " + (i + 1)) + "** " + reason);
							
							continue COMMANDS;
						}
						case VALID: {
							arguments[args++] = verified.getObject();
							
							break;
						}
						case VALID_END_NOW: {
							arguments[args++] = verified.getObject();
							
							break ARGUMENTS;
						}
					}
				}
				
				/* There is more content than the arguments handled */
				if(msg.length() > 0) {
					continue COMMANDS;
				}
				
				/* Not the correct amount of arguments for the command */
				if(command.getArguments().length != args) {
					continue COMMANDS;
				}
				
				CommandEvent commandEvent = new CommandEvent(event, this, prefix, alias);
				if(this.asyncEnabled) {
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
				event.getChannel().sendMessage(this.getHelp(event, new CommandEvent(event, this, prefix, message), new ArrayList<>(possibleCommands.values())).build()).queue();
			}
		}
	}
	
	private boolean checkPermissions(MessageReceivedEvent event, ICommand command) {
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
					.append(command.getCommand()).append("** in ")
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
		try {
			if(this.checkPermissions(event, command)) {
				try {
					/* Allow for a custom cooldown implementation? */
					/* Simple cooldown feature, not sure how scalable it is */
					if(command.getCooldownDuration() > 0) {
						/* Should a new manager be used for this or not? */
						long remaining = CooldownManager.getTimeRemaining(command, event.getAuthor().getIdLong());
						
						if(remaining == 0) {
							/* Add the cooldown before the command has executed so that in case the command has a long execution time it will not get there */
							CooldownManager.addCooldown(command, event.getAuthor().getIdLong());
							
							command.execute(event, commandEvent, arguments);
						}else{
							event.getChannel().sendMessage("This command has a cooldown, please try again in " + ((double) remaining/1000) + " seconds").queue();
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
				}catch(Exception e) {
					if(command.getCooldownDuration() > 0) {
						/* If the command execution fails then no cooldown should be added therefore this */
						CooldownManager.removeCooldown(command, event.getAuthor().getIdLong());
					}
					
					for(CommandEventListener listener : this.commandEventListeners) {
						/* Wrapped in a try catch because we don't want the execution of this to fail just because we couldn't rely on an event handler not to throw an exception */
						try {
							listener.onCommandExecutionException(command, event, commandEvent, e);
						}catch(Exception e1) {
							e1.printStackTrace();
						}
					}
					
					/* Should this still be thrown even if it sends to the listener? */
					try {
						Exception exception = e.getClass().getConstructor(String.class).newInstance("Attempted to execute command (" + command.getCommand() + ") with the arguments " +
							Arrays.deepToString(arguments) + " but it failed" + 
							((e.getMessage() != null) ? " with the message \"" + e.getMessage() + "\""  : ""));
						
						exception.setStackTrace(e.getStackTrace());
						exception.printStackTrace();
					}catch(Exception e2) {
						e2.printStackTrace();
					}
				}
				
				System.out.println("Executed command (" + command.getCommand() + ") with the arguments " 
					+ Arrays.deepToString(arguments) + ", time elapsed " + (System.nanoTime() - timeStarted));
			}
		}catch(InsufficientPermissionException e) {
			System.out.println("Attempted to execute command (" + command.getCommand() + ") with arguments " + Arrays.deepToString(arguments) + 
				", though it failed due to missing permissions, time elapsed " + (System.nanoTime() - timeStarted) + 
				", error message (" + e.getMessage() + ")");
			
			event.getChannel().sendMessage("Missing permissions").queue();
		}
	}
}