package com.jockie.bot.core.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jockie.bot.core.command.ICommand.ContentOverflowPolicy;
import com.jockie.bot.core.command.ICommand.InvalidOptionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Policy {
	
	public ContentOverflowPolicy contentOverflow() default ContentOverflowPolicy.FAIL;
	
	public InvalidOptionPolicy invalidOption() default InvalidOptionPolicy.INCLUDE;
	
}