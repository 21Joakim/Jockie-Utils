package com.jockie.bot.core.category;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.ICommand;

public interface ICategory {
	
	/**
	 * @return the name of this category
	 */
	@Nonnull
	public String getName();
	
	/**
	 * @return the description of this category
	 */
	@Nullable
	public String getDescription();
	
	/**
	 * @return the parent of this category
	 */
	@Nullable
	public ICategory getParent();
	
	/**
	 * @param category the category to add as a sub-category
	 * 
	 * @return the {@link ICategory} instance, useful for chaining
	 */
	@Nonnull
	public ICategory addSubCategory(@Nonnull ICategory category);
	
	/**
	 * @param category the category to remove from the sub-categories
	 * 
	 * @return the {@link ICategory} instance, useful for chaining
	 */
	@Nonnull
	public ICategory removeSubCategory(@Nonnull ICategory category);
	
	/**
	 * @return an unmodifiable list of the sub-categories
	 * of this category
	 */
	@Nonnull
	public Set<ICategory> getSubCategories();
	
	/**
	 * @param command the command to add to this category
	 * 
	 * @return the {@link ICategory} instance, useful for chaining
	 */
	@Nonnull
	public ICategory addCommand(@Nonnull ICommand command);
	
	/**
	 * @param command the command to remove from this category
	 * 
	 * @return the {@link ICategory} instance, useful for chaining
	 */
	@Nonnull
	public ICategory removeCommand(@Nonnull ICommand command);
	
	/**
	 * @return an unmodifiable list of the commands in
	 * this category
	 */
	@Nonnull
	public Set<ICommand> getCommands();
	
}