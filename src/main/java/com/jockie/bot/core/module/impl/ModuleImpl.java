package com.jockie.bot.core.module.impl;

import java.lang.reflect.Method;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.impl.MethodCommand;
import com.jockie.bot.core.module.IModule;

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
	 * @param name the suggested name for this command, by default this is <b>{@code method.getName().replace("_", " ")}</b>
	 * 
	 * @return the loaded command or null if the default loader 
	 * 		({@link com.jockie.bot.core.command.factory.impl.MethodCommandFactory#getDefault() MethodCommandFactory#getDefaultFactory()}) 
	 * 		should be used
	 */
	public MethodCommand createCommand(Method method, String name) {
		return null;
	}
	
	/**
	 * This is called every time a command of this module has loaded
	 * 
	 * @param command the command which was loaded
	 */
	public void onCommandLoad(ICommand command) {}
	
	/**
	 * This is called when the module (all the commands) has loaded
	 */
	public void onModuleLoad() {}
	
}