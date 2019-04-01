package com.jockie.bot.core.argument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Repeatable(Arguments.class)
public @interface Argument {
	
	/**
	 * @see {@link IArgument#isEndless()}
	 */
	public boolean endless() default false;
	
	/**
	 * @see {@link IArgument#acceptEmpty()}
	 */
	public boolean acceptEmpty() default false;
	
	/**
	 * @see {@link IArgument#acceptQuote()}
	 */
	/* TODO: I see no reason not to allow quoted by default */
	public boolean acceptQuote() default true;
	
	/**
	 * This will make the default a null value
	 * 
	 * @see {@link IArgument#getDefault(com.jockie.bot.core.command.impl.CommandEvent)}
	 */
	public boolean nullDefault() default false;
	
	/**
	 * @see {@link IArgument#getName()}
	 */
	public String value() default "";
	
}