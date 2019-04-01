package com.jockie.bot.core.command.exception.load;

public class CommandLoadException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private Class<?> commandClass;
	
    public CommandLoadException(Class<?> commandClass, Throwable cause) {
        super("Failed to load command " + commandClass.getName(), cause);
        
        this.commandClass = commandClass;
    }
    
    public Class<?> getCommandClass() {
    	return this.commandClass;
    }
}