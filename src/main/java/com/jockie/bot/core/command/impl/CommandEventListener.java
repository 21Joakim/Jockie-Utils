package com.jockie.bot.core.command.impl;

import com.jockie.bot.core.command.ICommand;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.exceptions.PermissionException;

/* Add more events? */
public class CommandEventListener {
	
	/** 
	 * This will be sent after the command has been called. Depending on how the executed command is made, 
	 * async or blocking, the command might not have finished executing when this is called 
	 * 
	 * @param command the command which was executed
	 * @param event the context of what was executed
	 */
	public void onCommandExecuted(ICommand command, CommandEvent event) {}
	
	/** 
	 * This will be sent after an exception passed by uncaught 
	 * 
	 * @param command the command which caused the exception
	 * @param event the context of what was executed
	 * @param throwable the exception which was thrown
	 */
	public void onCommandExecutionException(ICommand command, CommandEvent event, Throwable throwable) {}
	
	/** 
	 * This is only called if a PermissionException was thrown.
	 * </br></br>
	 * <b>NOTE:</b>
	 * This means that this will not be called if it was handled correctly through the handlers of
	 * {@link ICommand#getAuthorDiscordPermissions()} and {@link ICommand#getBotDiscordPermissions()}
	 * 
	 * @param command the command which caused the exception
	 * @param event the context of what was executed
	 * @param exception the exception which was thrown
	 */
	public void onCommandMissingPermissions(ICommand command, CommandEvent event, PermissionException exception) {}
	
	/** 
	 * This will be sent if a message starts with a registered prefix, 
	 * this is called no matter if a command was executed or not
	 * 
	 * @param message the message which triggered this
	 * @param prefix the prefix which was used to trigger this
	 */
	public void onPrefixedMessage(Message message, String prefix) {}
	
	/** 
	 * This will be sent if a message starts with a registered prefix and passed by without any matches 
	 * 
	 * @param message the message which triggered this
	 * @param prefix the prefix which was used to trigger this
	 */
	public void onUnknownCommand(Message message, String prefix) {}
	
}