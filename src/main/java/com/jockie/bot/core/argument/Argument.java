package com.jockie.bot.core.argument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Argument {
	
	/**
	 * @see IArgument#isEndless()
	 */
	public boolean endless() default false;
	
	/**
	 * @see IArgument#acceptEmpty()
	 */
	public boolean acceptEmpty() default false;
	
	/**
	 * @see IArgument#acceptQuote()
	 */
	public boolean acceptQuote() default true;
	
	/**
	 * This will make the default a null value
	 * 
	 * @see IArgument#getDefault(com.jockie.bot.core.command.impl.CommandEvent)
	 */
	public boolean nullDefault() default false;
	
	/**
	 * @see IArgument#getName()
	 */
	public String value() default "";
	
	/**
	 * @see IArgument#getDescription()
	 */
	public String description() default "";
	
}