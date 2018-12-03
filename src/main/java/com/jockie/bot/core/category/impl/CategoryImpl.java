package com.jockie.bot.core.category.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.jockie.bot.core.category.ICategory;
import com.jockie.bot.core.command.ICommand;

public class CategoryImpl implements ICategory {
	
	private final String name;
	private final String description;
	
	private final ICategory parent;
	
	private final Set<ICategory> subCategories = new HashSet<>();
	
	private final Set<ICommand> commands = new HashSet<>();
	
	private CategoryImpl(String name, String description, ICategory parent) {
		this.name = name;
		this.description = description;
		this.parent = parent;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public ICategory getParent() {
		return this.parent;
	}
	
	public ICategory addSubCategory(ICategory category) {
		if(!this.equals(category.getParent())) {
			throw new IllegalArgumentException("The category's parent is not equal to this");
		}
		
		this.subCategories.add(category);
		
		return this;
	}
	
	public ICategory removeSubCategory(ICategory category) {
		this.subCategories.remove(category);
		
		return this;
	}
	
	public Set<ICategory> getSubCategories() {
		return Collections.unmodifiableSet(this.subCategories);
	}
	
	public ICategory addCommand(ICommand command) {
		if(!this.equals(command.getCategory())) {
			throw new IllegalArgumentException("The command's category is not equal to this");
		}
		
		this.commands.add(command);
		
		return this;
	}
	
	public ICategory removeCommand(ICommand command) {
		if(this.equals(command.getCategory())) {
			throw new IllegalArgumentException("The command's category is still equal to this");
		}
		
		this.commands.remove(command);
		
		return this;
	}
	
	public Set<ICommand> getCommands() {
		return Collections.unmodifiableSet(this.commands);
	}
}