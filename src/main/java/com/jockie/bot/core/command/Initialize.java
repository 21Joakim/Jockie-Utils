package com.jockie.bot.core.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a method to be used as a initialization method when 
 * loading a command module.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
/* TODO: The behaviour of the value is inconsistent with @SubCommand, consider changing? */
public @interface Initialize {
	
	/**
	 * @return a list of commands (by the method or class name) which are going to initialized
	 */
	public String[] value() default {};
	
	/**
	 * @return whether or not it should initialize all commands
	 */
	public boolean all() default false;
	
	/**
	 * @return whether or not this should include sub-commands, if true
	 * all sub-commands of the commands initializing will be included
	 */
	public boolean subCommands() default false;
	
}