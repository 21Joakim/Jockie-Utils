package com.jockie.bot.core.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used on commands which should be ignored when using 
 * any automatic method of adding commands.
 * <br><br>
 * <b>NOTE:</b>
 * If used on a command class place it on the class and not the command 
 * method. This can also be placed on modules
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Ignore {}