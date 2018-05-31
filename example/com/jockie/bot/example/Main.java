package com.jockie.bot.example;

import com.jockie.bot.core.command.impl.CommandListener;
import com.jockie.bot.core.command.impl.CommandStore;
import com.jockie.bot.core.command.impl.command.CommandHelp;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;

public class Main {
	
	public static void main(String[] args) throws Exception {
		CommandListener listener = new CommandListener()							/* Built in help command */
			.addCommandStore(CommandStore.of("com.jockie.bot.example.command").addCommands(new CommandHelp()))
			.setDefaultPrefixes("?");
		
		new JDABuilder(AccountType.BOT).setToken(Safe.TEST_TOKEN)
			.addEventListener(listener)
			.buildBlocking();
	}
}