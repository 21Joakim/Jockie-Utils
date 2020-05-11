package com.jockie.bot.core.module.impl;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.IMethodCommand;
import com.jockie.bot.core.command.factory.impl.MethodCommandFactory;
import com.jockie.bot.core.module.IModule;
import com.jockie.bot.core.utility.CommandUtility;

/**
 * A helper implementation of the IModule to show which features can be used,
 * you do not need to extend this class to benefit from the methods in it,
 * these are gathered through reflection and can therefore be used even if the class just implements {@link IModule}
 * or has the {@link com.jockie.bot.core.module.Module Module} annotation.
 */
public class ModuleImpl implements IModule {
	
	/**
	 * This is called to load every command,
	 * this can be useful for having the commands extend a custom command implementation.
	 * 
	 * @param method the method which the command is based of, one could say the command itself
	 * @param name the suggested name for this command, by default this is {@link CommandUtility#getCommandName(Method)}
	 * 
	 * @return the loaded command or null if the default loader ({@link MethodCommandFactory#getDefault()}) should be used
	 */
	@Nullable
	public IMethodCommand createCommand(@Nonnull Method method, @Nonnull String name) {
		return null;
	}
	
	/**
	 * This is called every time a command of this module has loaded
	 * 
	 * @param command the command which was loaded
	 */
	public void onCommandLoad(@Nonnull ICommand command) {}
	
	/**
	 * This is called when the module (all the commands) has loaded
	 */
	public void onModuleLoad() {}
	
}