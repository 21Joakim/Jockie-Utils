package com.jockie.bot.core.command.annotation;

import net.dv8tion.jda.core.Permission;

public @interface Command {
	
	public String command() default "";
	public String description() default "";
	
	public String[] aliases() default {};
	
	public boolean hidden() default false;
	public boolean developerOnly() default false;
	
	public Permission[] botPermissionsRequired() default {};
	public Permission[] authorPermissionsRequired() default {};
	
}