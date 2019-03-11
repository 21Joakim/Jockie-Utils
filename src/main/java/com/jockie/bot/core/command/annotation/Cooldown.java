package com.jockie.bot.core.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import com.jockie.bot.core.cooldown.ICooldown.Scope;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cooldown {
	
	public long cooldown() default 0;
	
	public TimeUnit cooldownUnit() default TimeUnit.SECONDS;
	
	public Scope cooldownScope() default Scope.USER;
	
}