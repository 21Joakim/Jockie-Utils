package com.jockie.bot.core.parser.impl.discord;

import java.util.List;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.utility.ArgumentUtility;

import net.dv8tion.jda.api.entities.VoiceChannel;

public class VoiceChannelParser<Component> implements IParser<VoiceChannel, Component> {

	@Override
	public ParsedResult<VoiceChannel> parse(ParseContext context, Component component, String content) {
		List<VoiceChannel> channels = ArgumentUtility.getVoiceChannelsByIdOrName(context.getMessage().getGuild(), content, true);
		
		if(channels.size() == 1) {
			return new ParsedResult<>(true, channels.get(0));
		}else{
			return new ParsedResult<>(false, null);
		}
	}	
}