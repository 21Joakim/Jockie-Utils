package com.jockie.bot.core.parser.impl.discord;

import java.util.List;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.utility.ArgumentUtility;

import net.dv8tion.jda.api.entities.TextChannel;

public class TextChannelParser<Component> implements IParser<TextChannel, Component> {

	@Override
	public ParsedResult<TextChannel> parse(ParseContext context, Component component, String content) {
		List<TextChannel> channels = ArgumentUtility.getTextChannelsByIdOrName(context.getMessage().getGuild(), content, true);
		
		if(channels.size() == 1) {
			return new ParsedResult<>(true, channels.get(0));
		}else{
			return new ParsedResult<>(false, null);
		}
	}	
}