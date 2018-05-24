package com.jockie.bot.core.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.jockie.bot.core.await.AwaitManager;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.argument.IArgument;
import com.jockie.bot.core.command.argument.IArgument.VerifiedArgument;
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
	
	private TriFunction<MessageReceivedEvent, CommandEvent, List<ICommand>, EmbedBuilder> helperFunction;
	
	private List<Long> developers = new ArrayList<>();
	
	private List<CommandStore> commandStores = new ArrayList<>();
	
	private List<CommandEventListener> commandEventListeners = new ArrayList<>();
	
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
	
	public List<CommandStore> getCommandStores() {
		return Collections.unmodifiableList(this.commandStores);
	}
	
	public CommandListener addCommandStore(CommandStore... commandStores) {
		for(CommandStore commandStore : commandStores) {
			if(!this.commandStores.contains(commandStore)) {
				this.commandStores.add(commandStore);
			}
		}
		
		return this;
	}
	
	public CommandListener removeCommandStore(CommandStore... commandStores) {
		for(CommandStore commandStore : commandStores) {
			this.commandStores.remove(commandStore);
		}
		
		return this;
	}
	
	public CommandListener setDefaultPrefixes(String... prefixes) {
		this.defaultPrefixes = prefixes;
		
		return this;
	}
	
	public String[] getDefaultPrefixes() {
		return this.defaultPrefixes;
	}
	
	public CommandListener setGenericPermissions(Permission... permissions) {
		this.genericPermissions = permissions;
		
		return this;
	}
	
	public Permission[] getGenericPermissions() {
		return this.genericPermissions;
	}
	
	public CommandListener addDeveloper(long id) {
		this.developers.add(id);
		
		return this;
	}
	
	public CommandListener removeDeveloper(long id) {
		this.developers.remove(id);
		
		return this;
	}
	
	public List<Long> getDevelopers() {
		return this.developers;
	}
	
	public CommandListener setPrefixesFunction(Function<MessageReceivedEvent, String[]> function) {
		this.prefixFunction = function;
		
		return this;
	}
	
	public String[] getPrefixes(MessageReceivedEvent event) {
		if(this.prefixFunction != null) {
			return this.prefixFunction.apply(event);
		}
		
		return this.getDefaultPrefixes();
	}
	
	public CommandListener setHelpFunction(TriFunction<MessageReceivedEvent, CommandEvent, List<ICommand>, EmbedBuilder> function) {
		this.helperFunction = function;
		
		return this;
	}
	
	public EmbedBuilder getHelp(MessageReceivedEvent event, CommandEvent commandEvent, List<ICommand> commands) {
		if(this.helperFunction != null) {
			return this.helperFunction.apply(event, commandEvent, commands);
		}
		
		StringBuilder description = new StringBuilder();
		for(int i = 0; i < commands.size(); i++) {
			ICommand command = commands.get(i);
			
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
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setDescription(description);
		embedBuilder.setAuthor("Help", null, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
		
		return embedBuilder;
	}
	
	public void onEvent(Event event) {
		if(event instanceof MessageReceivedEvent) {
			this.onMessageReceived((MessageReceivedEvent) event);
		}
		
		AwaitManager.handleAwait(event);
	}
	
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getChannelType().isGuild()) {
			if(PagedManager.handlePagedResults(event)) {
				return;
			}
		}
		
		String[] prefixes = this.getPrefixes(event);
		String message = event.getMessage().getContentRaw(), prefix = null, selfMention;
		
		if(event.getChannelType().isGuild()) {
			selfMention = event.getGuild().getSelfMember().getAsMention() + " ";
		}else{
			selfMention = event.getJDA().getSelfUser().getAsMention() + " ";
		}
		
		if(message.startsWith(selfMention)) {
			if(message.equals(selfMention + "prefix") || message.equals(selfMention + "prefixes")) {
				String allPrefixes = Arrays.deepToString(prefixes);
				allPrefixes = allPrefixes.substring(1, allPrefixes.length() - 1);
				
				event.getChannel().sendMessage(new MessageBuilder()
					.append("My prefix")
					.append(prefixes.length > 1 ? "es are " : " is ")
					.append(allPrefixes, Formatting.BOLD)
					.build()).queue();
				
				return;
			}else{
				/* I want every bot to have this feature therefore it will be a hard coded one, arguments against it? */
				prefix = selfMention;
			}
		}
		
		if(prefix == null) {
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
			
			List<ICommand> possibleCommands = new ArrayList<>();
			
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
						if(msg.startsWith((!command.isCaseSensitive() ? a.toLowerCase() : alias) + " ") || msg.equals(!command.isCaseSensitive() ? a.toLowerCase() : alias)) {
							alias = a;
							
							cmd = !command.isCaseSensitive() ? a.toLowerCase() : a;
							
							break VERIFY;
						}
					}
					
					continue COMMANDS;
				}
				
				/* Optional? */
				if(!(command instanceof DummyCommand)) {
					possibleCommands.add(command);
				}
				
				msg = message.substring(cmd.length());
				
				if(msg.length() >= 0) {
					if(msg.length() > 0 && msg.charAt(0) != ' ') {
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
								continue COMMANDS;
							}
						}
						
						IArgument<?> argument = command.getArguments()[i];
						
						VerifiedArgument<?> verified;
						if(argument.isEndless()) {
							if(msg.length() == 0 && !argument.acceptEmpty()) {
								continue COMMANDS;
							}
							
							verified = argument.verify(event, msg);
							msg = "";
						}else{
							String content = null;
							if(msg.length() > 0) {
								if(argument.acceptQuote()) {
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
								continue COMMANDS;
							}
							
							verified = argument.verify(event, content);
						}
						
						switch(verified.getVerifiedType()) {
							case INVALID: {
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
					
					if(msg.length() > 0) {
						continue COMMANDS;
					}
					
					if(command.getArguments().length != args) {
						continue COMMANDS;
					}
					
					this.executeCommand(command, event, new CommandEvent(event, this, prefix, alias), commandStarted, arguments);
					
					return;
				}
			}
			
			if(possibleCommands.size() > 0) {
				if(event.getChannelType().isGuild()) {
					Member bot = event.getGuild().getMember(event.getJDA().getSelfUser());
					
					if(!bot.hasPermission(Permission.MESSAGE_WRITE)) {
						event.getAuthor().openPrivateChannel().queue(channel -> {
							channel.sendMessage("Missing permission **" + Permission.MESSAGE_WRITE.getName() + "** in " + event.getChannel().getName() + ", " + event.getGuild().getName()).queue();
						});
						
						return;
					}else if(!bot.hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
						event.getChannel().sendMessage("Missing permission **" + Permission.MESSAGE_WRITE.getName() + "** in " + event.getChannel().getName() + ", " + event.getGuild().getName()).queue();
						
						return;
					}
				}
				
				event.getChannel().sendMessage(this.getHelp(event, new CommandEvent(event, this, prefix, message), possibleCommands).build()).queue();
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
				StringBuilder message = new StringBuilder();
				
				message.append("Missing permission" + (missingPermissions.length() > 1 ? "s" : "") + " to execute **")
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
					command.execute(event, commandEvent, arguments);
					
					for(CommandEventListener listener : this.commandEventListeners) {
						listener.onCommandExecuted(command, event, commandEvent);
					}
				}catch(Exception e) {
					for(CommandEventListener listener : this.commandEventListeners) {
						listener.onCommandExecutionException(command, event, commandEvent, e);
					}
					
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