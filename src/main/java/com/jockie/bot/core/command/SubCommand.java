package com.jockie.bot.core.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a command as a sub-command of the command matching the
 * specified path ({@link SubCommand#value()}).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
/* TODO: The behaviour of the value is inconsistent with @Initialize, consider changing? */
public @interface SubCommand {
	
	/**
	 * @return the path for this sub-command, a list of command names.
	 * <br><br>
	 * For example, let's say this command is "add blacklist server", "add" being the top parent,
	 * "blacklist" being the parent of this and "server" being this command, the path would then be
	 * {"add", "blacklist"}
	 */
	public String[] value() default {};
	
}