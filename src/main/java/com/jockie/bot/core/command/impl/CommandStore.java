package com.jockie.bot.core.command.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.utility.LoaderUtility;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandStore {
	
	public static CommandStore of(String packagePath) {
		return new CommandStore().loadFrom(packagePath);
	}
	
	private List<ICommand> commands = new ArrayList<ICommand>();
	
	public CommandStore loadFrom(String packagePath) {
		return this.loadFrom(packagePath, true);
	}
	
	public CommandStore loadFrom(String packagePath, boolean subPackages) {
		List<ICommand> commands = LoaderUtility.loadFrom(packagePath, ICommand.class);
		
		this.addCommands(commands);
		
		return this;
	}
	
	public CommandStore addCommands(ICommand... commands) {
		for(ICommand command : commands) {
			if(!this.commands.contains(command)) {
				this.commands.add(command);
			}
		}
		
		return this;
	}
	
	public CommandStore addCommands(Collection<ICommand> commands)  {
		return this.addCommands(commands.toArray(new ICommand[0]));
	}
	
	public CommandStore addCommands(Category category) {
		return this.addCommands(category.addCommandStores(this).getCommands());
	}
	
	public CommandStore removeCommands(ICommand... commands) {
		for(ICommand command : commands) {
			if(this.commands.contains(command)) {
				this.commands.remove(command);
				
				for(int i = 0; i < this.commands.size(); i++) {
					if(this.commands.get(i) instanceof DummyCommand) {
						if(this.commands.get(i).getParent().equals(command)) {
							this.commands.remove(i--);
						}
					}
				}
			}
		}
		
		return this;
	}
	
	public CommandStore removeCommands(Collection<ICommand> commands) {
		return this.removeCommands(commands.toArray(new ICommand[0]));
	}
	
	public CommandStore removeCommands(Category category) {
		return this.removeCommands(category.removeCommandStores(this).getCommands());
	}
	
	public List<ICommand> getCommands() {
		return Collections.unmodifiableList(this.commands);
	}
	
	public List<ICommand> getCommandsAuthorized(MessageReceivedEvent event, CommandListener commandListener) {
		return Collections.unmodifiableList(this.commands.stream().filter(c -> c.verify(event, commandListener)).collect(Collectors.toList()));
	}
}