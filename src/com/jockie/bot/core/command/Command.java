package com.jockie.bot.core.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.dv8tion.jda.core.Permission;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
	
	public String command() default "";
	
	public String[] aliases() default {};
	
	public boolean guildTriggerable() default true;
	public boolean privateTriggerable() default false;
	
	public String description() default "";
	
	public Permission[] botPermissionsNeeded() default {};
	public Permission[] authorPermissionsNeeded() default {};
	
	public boolean caseSensitive() default false;
	public boolean developerCommand() default false;
	public boolean botTriggerable() default false;
	public boolean hidden() default false;
	public long cooldown() default 0;
	public boolean async() default false;
	
}