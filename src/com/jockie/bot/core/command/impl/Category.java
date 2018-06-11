package com.jockie.bot.core.command.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jockie.bot.core.command.ICommand;

/* Implemented in a hacky way and I am not sure how people want it to work. */
public class Category {
	
	private String name;
	
	private List<CommandImpl> commands = new ArrayList<>();
	
	private List<CommandStore> commandStores = new ArrayList<>();
	
	public Category(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public List<ICommand> getCommands() {
		return Collections.unmodifiableList(this.commands);
	}
	
	public Category setName(String name) {
		this.name = name;
		
		return this;
	}
	
	public Category addCommands(CommandImpl... commands) {
		for(CommandImpl command : commands) {
			if(command.getCategory() != null) {
				command.getCategory().removeCommands(command);
			}
			
			this.commands.add(command);
		}
		
		for(CommandStore store : this.commandStores) {
			store.addCommands(commands);
		}
		
		return this;
	}
	
	public Category removeCommands(CommandImpl... commands) {
		for(CommandImpl command : commands) {
			if(command.getCategory() != null && command.getCategory().equals(this)) {
				command.setCategory(null);
			}
			
			this.commands.remove(command);
		}
		
		for(CommandStore store : this.commandStores) {
			store.removeCommands(commands);
		}
		
		return this;
	}
	
	Category addCommandStores(CommandStore... stores) {
		for(CommandStore store : stores) {
			this.commandStores.add(store);
		}
		
		return this;
	}
	
	Category removeCommandStores(CommandStore... stores) {
		for(CommandStore store : stores) {
			this.commandStores.remove(store);
		}
		
		return this;
	}
}