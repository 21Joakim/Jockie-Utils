package com.jockie.bot.example.command;

import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandPing extends CommandImpl {

	public CommandPing() {
		super("ping");
		
		super.setDescription("Simple ping command");
	}
	
	public void onCommand(MessageReceivedEvent event, CommandEvent commandEvent) {
		event.getChannel().sendMessage(event.getJDA().getPing() + " ms").queue();
	}
}