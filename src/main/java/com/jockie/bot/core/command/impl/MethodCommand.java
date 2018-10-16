package com.jockie.bot.core.command.impl;

import java.lang.reflect.Method;
import java.util.Objects;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class MethodCommand extends CommandImpl {
	
	private Object invoker;
	private Method method;

	public MethodCommand(String command, Object invoker, Method method) {
		super(command, false, CommandImpl.generateDefaultArguments(method));
		
		this.invoker = invoker;
		
		Objects.requireNonNull(method);
		
		this.method = method;
		
		super.setOptions(CommandImpl.generateOptions(method));
	}
	
	public void execute(MessageReceivedEvent event, CommandEvent commandEvent, Object... args) throws Throwable {
		CommandImpl.executeMethodCommand(this.invoker, this.method, event, commandEvent, args);
	}
}