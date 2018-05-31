package com.jockie.bot.example.command.arguments;

import com.jockie.bot.core.command.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandAddTwo extends CommandImpl {

	public CommandAddTwo() {
		super("add two");
		
		super.setDescription("Add two integer numbers together and get the result");
		super.setAliases("addtwo", "at");
	}
	
	public void onCommand(MessageReceivedEvent event, CommandEvent commandEvent, @Argument(description="number 1") int num1, @Argument(description="number 2") int num2) {
		event.getChannel().sendMessage((num1 + num2) + "").queue();
	}
}