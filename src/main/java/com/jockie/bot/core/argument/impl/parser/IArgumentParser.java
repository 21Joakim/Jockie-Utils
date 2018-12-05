package com.jockie.bot.core.argument.impl.parser;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.VerifiedArgument;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

@FunctionalInterface
public interface IArgumentParser<Type> {
	
	public VerifiedArgument<Type> parse(MessageReceivedEvent event, IArgument<Type> argument, String content);
	
}