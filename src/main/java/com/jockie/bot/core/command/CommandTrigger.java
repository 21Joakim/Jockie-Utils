package com.jockie.bot.core.command;

public class CommandTrigger {
	
	private final String trigger;
	private final ICommand command;
	
	public CommandTrigger(String trigger, ICommand command) {
		this.trigger = trigger;
		this.command = command;
	}
	
	public String getTrigger() {
		return this.trigger;
	}
	
	public ICommand getCommand() {
		return this.command;
	}
	
	@Override
	public String toString() {
		return String.format("CommandTrigger{trigger=%s, command=%s}", this.trigger, this.command);
	}
}