package com.jockie.bot.core.command.exception.load;

import java.lang.reflect.Method;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.IMethodCommand;
import com.jockie.bot.core.module.IModule;
import com.jockie.bot.core.module.Module;
import com.jockie.bot.core.utility.CommandUtility;

public class CommandLoadException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private Class<?> commandClass;
	
	private Method commandMethod;
	
	private ICommand command;
	
	public CommandLoadException(Class<?> commandClass, Throwable cause) {
		super(cause);
		
		this.commandClass = commandClass;
	}
	
	public CommandLoadException(Method method, Throwable cause) {
		super(cause);
		
		this.commandMethod = method;
		this.commandClass = method.getDeclaringClass();
	}
	
	public CommandLoadException(ICommand command, Throwable cause) {
		super(cause);
		
		this.command = command;
		
		if(command instanceof IMethodCommand) {
			IMethodCommand methodCommand = (IMethodCommand) command;
			
			this.commandMethod = methodCommand.getCommandMethod();
			this.commandClass = this.commandMethod.getDeclaringClass();
		}else{
			this.commandClass = command.getClass();
		}
	}
	
	public Class<?> getCommandClass() {
		return this.commandClass;
	}
	
	public Method getCommandMethod() {
		return this.commandMethod;
	}
	
	public ICommand getCommand() {
		return this.command;
	}
	
	public String getMessage() {
		String message = "Failed to load";
		
		if(this.commandMethod != null && this.commandClass != null) {
			message += " method command " + this.commandClass.getName() + "#" + this.commandMethod.getName();
		}else if(this.commandClass != null) {
			if(this.commandClass.isAnnotationPresent(Module.class) || CommandUtility.isInstanceOf(this.commandClass, IModule.class)) {
				message += " module " + this.commandClass.getName();
			}else{
				message += " command " + this.commandClass.getName();
			}
		}
		
		if(this.command != null) {
			message += " (" + this.command.getCommand() + ")";
		}
		
		return message;
	}
}