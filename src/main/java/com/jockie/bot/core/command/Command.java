package com.jockie.bot.core.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import com.jockie.bot.core.cooldown.ICooldown.Scope;

import net.dv8tion.jda.core.Permission;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
	
	public String value() default "";
	
	public String[] aliases() default {};
	
	public boolean guildTriggerable() default true;
	public boolean privateTriggerable() default false;
	
	public String description() default "";
	public String shortDescription() default "";
	
	public String[] examples() default {};
	
	public Permission[] botPermissions() default {};
	public Permission[] authorPermissions() default {};
	
	public boolean caseSensitive() default false;
	public boolean developer() default false;
	public boolean botTriggerable() default false;
	public boolean hidden() default false;
	
	public long cooldown() default 0;
	public TimeUnit cooldownUnit() default TimeUnit.SECONDS;
	public Scope cooldownScope() default Scope.USER;
	
	public boolean async() default false;
	public String orderingKey() default "";
	
	public boolean nsfw() default false;
	
}