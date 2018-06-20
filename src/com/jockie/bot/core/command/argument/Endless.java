package com.jockie.bot.core.command.argument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Endless {
	
	public int minArguments() default 1;
	public int maxArguments() default 0;
	
	public boolean endless() default true;
	
}