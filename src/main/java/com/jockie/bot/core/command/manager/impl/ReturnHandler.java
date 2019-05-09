package com.jockie.bot.core.command.manager.impl;

import java.util.function.BiConsumer;

import com.jockie.bot.core.command.impl.CommandEvent;

class ReturnHandler<T> {
	
	private Class<?> type;
	
	private BiConsumer<CommandEvent, T> returnHandler;
	
	private boolean handleInheritence;
	
	public ReturnHandler(Class<?> type, BiConsumer<CommandEvent, T> returnHandler) {
		this.type = type;
		this.returnHandler = returnHandler;
	}
	
	public Class<?> getType() {
		return this.type;
	}
	
	public BiConsumer<CommandEvent, T> getReturnHandler() {
		return this.returnHandler;
	}
	
	public void setReturnHandler(BiConsumer<CommandEvent, T> returnHandler) {
		this.returnHandler = returnHandler;
	}
	
	public boolean isHandleInheritence() {
		return this.handleInheritence;
	}
	
	public void setHandleInheritence(boolean handleInheritence) {
		this.handleInheritence = handleInheritence;
	}
}