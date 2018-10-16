package com.jockie.bot.core.argument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Argument {
	
	public boolean endless() default false;
	public boolean acceptEmpty() default false;
	
	/* I see no reason not to allow quoted by default */
	public boolean acceptQuote() default true;
	
	public boolean nullDefault() default false;
	
	public String name();
	
}