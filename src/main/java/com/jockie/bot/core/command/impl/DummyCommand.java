package com.jockie.bot.core.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.cooldown.ICooldown.Scope;
import com.jockie.bot.core.option.IOption;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.tuple.Pair;

/* This was basically the easiest way I could figure out how to make optional arguments */
public class DummyCommand implements ICommand {
	
	private ICommand command;
	
	private Map<Integer, IArgument<?>> indexes = new HashMap<>();
	
	private IArgument<?>[] arguments;
	
	public DummyCommand(ICommand command, IArgument<?>... arguments) {
		this.command = command;
		
		this.arguments = Arrays.asList(command.getArguments()).stream().filter(new Predicate<IArgument<?>>() {
			public boolean test(IArgument<?> argument) {
				if(!argument.hasDefault())
					return true;
				
				for(int i = 0; i < command.getArguments().length; i++) {
					if(command.getArguments()[i].equals(argument)) {
						for(int j = 0; j < arguments.length; j++) {
							if(arguments[j].equals(argument)) {
								DummyCommand.this.indexes.put(i, argument);
								
								return false;
							}
						}
					}
				}
				
				return true;
			}
		}).toArray(IArgument[]::new);
	}
	
	public void execute(MessageReceivedEvent event, CommandEvent commandEvent, Object... arguments) throws Throwable {
		Object[] args = new Object[this.command.getArguments().length];
		
		for(int i = 0, offset = 0; i < args.length; i++) {
			if(this.indexes.get(i) != null) {
				args[i] = this.indexes.get(i).getDefault(event, commandEvent);
			}else{
				args[i] = arguments[offset];
				offset = offset + 1;
			}
		}
		
		this.command.execute(event, commandEvent, args);
	}
	
	public boolean verify(MessageReceivedEvent event, CommandListener commandListener) {
		return this.command.verify(event, commandListener);
	}
	
	public String[] getAliases() {
		return this.command.getAliases();
	}
	
	public long getCooldownDuration() {
		return this.command.getCooldownDuration();
	}
	
	public Scope getCooldownScope() {
		return this.command.getCooldownScope();
	}
	
	public boolean isExecuteAsync() {
		return this.command.isExecuteAsync();
	}
	
	public boolean isBotTriggerable() {
		return this.command.isBotTriggerable();
	}
	
	public boolean isCaseSensitive() {
		return this.command.isCaseSensitive();
	}
	
	public boolean isDeveloperCommand() {
		return this.command.isDeveloperCommand();
	}
	
	public boolean isGuildTriggerable() {
		return this.command.isGuildTriggerable();
	}
	
	public boolean isPrivateTriggerable() {
		return this.command.isPrivateTriggerable();
	}
	
	public boolean isHidden() {
		return this.command.isHidden();
	}
	
	public boolean isPassive() {
		return this.command.isPassive();
	}
	
	public String getDescription() {
		return this.command.getDescription();
	}
	
	public String getShortDescription() {
		return this.command.getShortDescription();
	}
	
	public String[] getExamples() {
		return this.command.getExamples();
	}
	
	public Permission[] getAuthorDiscordPermissionsNeeded() {
		return this.command.getAuthorDiscordPermissionsNeeded();
	}
	
	public Permission[] getBotDiscordPermissionsNeeded() {
		return this.command.getBotDiscordPermissionsNeeded();
	}
	
	public String getCommand() {
		return this.command.getCommand();
	}
	
	public ICommand getParent() {
		return this.command;
	}
	
	public IArgument<?>[] getArguments() {
		return this.arguments;
	}
	
	public Category getCategory() {
		return this.command.getCategory();
	}
	
	public List<ICommand> getSubCommands() {
		return new ArrayList<>();
	}
	
	public List<Pair<ICommand, List<?>>> getAllCommandsRecursive(MessageReceivedEvent event, String prefix) {
		return Collections.emptyList();
	}
	
	public String getCommandTrigger() {
		String command = this.getCommand();
		
		ICommand parent = this.getParent();
		while((parent = parent.getParent()) != null) {
			command = (parent.getCommand() + " " + command).trim();
		}
		
		return command;
	}
	
	public IOption[] getOptions() {
		return this.command.getOptions();
	}
	
	public OptionPolicy getOptionPolicy() {
		return this.command.getOptionPolicy();
	}
}