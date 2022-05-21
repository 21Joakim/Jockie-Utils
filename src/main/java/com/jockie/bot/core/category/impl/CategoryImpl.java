package com.jockie.bot.core.category.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.category.ICategory;
import com.jockie.bot.core.command.ICommand;

import net.dv8tion.jda.internal.utils.Checks;

public class CategoryImpl implements ICategory {
	
	private final String name;
	private final String description;
	
	private final ICategory parent;
	
	private final Set<ICategory> subCategories = new HashSet<>();
	
	private final Set<ICommand> commands = new HashSet<>();
	
	public CategoryImpl(String name, String description) {
		this(name, description, null);
	}
	
	public CategoryImpl(String name, String description, ICategory parent) {
		Checks.notNull(name, "name");
		
		this.name = name;
		this.description = description;
		this.parent = parent;
	}
	
	@Nonnull
	public String getName() {
		return this.name;
	}
	
	@Nullable
	public String getDescription() {
		return this.description;
	}
	
	@Nullable
	public ICategory getParent() {
		return this.parent;
	}
	
	@Nonnull
	public ICategory addSubCategory(ICategory category) {
		Checks.notNull(category, "category");
		
		if(!this.equals(category.getParent())) {
			throw new IllegalArgumentException("The category's parent is not equal to this");
		}
		
		this.subCategories.add(category);
		
		return this;
	}
	
	@Nonnull
	public ICategory removeSubCategory(ICategory category) {
		Checks.notNull(category, "category");
		
		this.subCategories.remove(category);
		
		return this;
	}
	
	@Nonnull
	public Set<ICategory> getSubCategories() {
		return Collections.unmodifiableSet(this.subCategories);
	}
	
	@Nonnull
	public ICategory addCommand(ICommand command) {
		Checks.notNull(command, "command");
		
		if(!this.equals(command.getCategory())) {
			throw new IllegalArgumentException("The command's category is not equal to this");
		}
		
		this.commands.add(command);
		
		return this;
	}
	
	@Nonnull
	public ICategory removeCommand(ICommand command) {
		Checks.notNull(command, "command");
		
		if(this.equals(command.getCategory())) {
			throw new IllegalArgumentException("The command's category is still equal to this");
		}
		
		this.commands.remove(command);
		
		return this;
	}
	
	@Nonnull
	public Set<ICommand> getCommands() {
		return Collections.unmodifiableSet(this.commands);
	}
}