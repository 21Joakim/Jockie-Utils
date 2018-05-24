package com.jockie.bot.core.command.impl;

import com.jockie.bot.core.command.ICommand;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/* Add more events? */
public class CommandEventListener {
	
	public void onCommandExecuted(ICommand command, MessageReceivedEvent event, CommandEvent commandEvent) {}
	
	public void onCommandExecutionException(ICommand command, MessageReceivedEvent event, CommandEvent commandEvent, Exception e) {}
	
}