package com.jockie.bot.core.command.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.argument.IArgument;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/* This was basically the easiest way I could figure out how to make optional arguments */
public class DummyCommand extends CommandImpl {
	
	private ICommand command;
	
	private Map<Integer, IArgument<?>> indexes = new HashMap<>();
	
	public DummyCommand(ICommand command, IArgument<?>... arguments) {
		super(command.getCommand());
		
		this.command = command;
		
		super.setAliases(command.getAliases());
		super.setArguments(Arrays.asList(command.getArguments()).stream().filter(new Predicate<IArgument<?>>() {
			public boolean test(IArgument<?> argument) {
				if(!argument.hasDefault())
					return true;
				
				for(int i = 0; i < command.getArguments().length; i++) {
					if(command.getArguments()[i].equals(argument)) {
						for(int j = 0; j < arguments.length; j++) {
							if(arguments[j].equals(argument)) {
								indexes.put(i, argument);
								
								return false;
							}
						}
					}
				}
				
				return true;
			}
		}).toArray(IArgument[]::new));
		
		super.setAuthorDiscordPermissionsNeeded(command.getAuthorDiscordPermissionsNeeded());
		super.setBotDiscordPermissionsNeeded(command.getBotDiscordPermissionsNeeded());
		super.setBotTriggerable(command.isBotTriggerable());
		super.setDescription(command.getDescription());
		super.setDeveloperCommand(command.isDeveloperCommand());
		super.setGuildTriggerable(command.isGuildTriggerable());
		super.setHidden(command.isHidden());
		super.setPrivateTriggerable(command.isPrivateTriggerable());
	}
	
	public ICommand getDummiedCommand() {
		return this.command;
	}
	
	public void execute(MessageReceivedEvent event, CommandEvent commandEvent, Object... arguments) {
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
}