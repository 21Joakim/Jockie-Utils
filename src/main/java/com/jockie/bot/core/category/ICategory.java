package com.jockie.bot.core.category;

import java.util.Set;

import com.jockie.bot.core.command.ICommand;

public interface ICategory {
	
	public String getName();
	
	public String getDescription();
	
	public ICategory getParent();
	
	public ICategory addSubCategory(ICategory category);
	public ICategory removeSubCategory(ICategory category);
	
	public Set<ICategory> getSubCategories();
	
	public ICategory addCommand(ICommand command);
	public ICategory removeCommand(ICommand command);
	
	public Set<ICommand> getCommands();
	
}