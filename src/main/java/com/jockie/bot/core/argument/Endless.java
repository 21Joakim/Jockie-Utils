package com.jockie.bot.core.argument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Endless {
	
	/**
	 * @see {@link IEndlessArgument#getMinArguments()}
	 */
	public int minArguments() default 1;
	
	/**
	 * @see {@link IEndlessArgument#getMaxArguments()}
	 */
	public int maxArguments() default 0;
	
	/**
	 * @see {@link IArgument#isEndless()}
	 */
	public boolean endless() default true;
	
}