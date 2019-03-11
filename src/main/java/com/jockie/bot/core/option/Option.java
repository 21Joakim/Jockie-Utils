package com.jockie.bot.core.option;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Option {
	
	public String value();
	
	public String description() default "";
	
	public String[] aliases() default {};
	
	public boolean hidden() default false;
	
	public boolean developer() default false;
	
}