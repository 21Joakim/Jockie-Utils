package com.jockie.bot.core.command.argument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Argument {
	
	public boolean endless() default false;
	public boolean acceptEmpty() default false;
	public boolean acceptQuote() default false;
	
	public boolean nullDefault() default false;
	
	public String description();
	
}