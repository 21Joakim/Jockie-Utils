package com.jockie.bot.core.parser.impl.discord;

import java.util.List;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.utility.ArgumentUtility;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class TextChannelParser<Component> implements IParser<TextChannel, Component> {
	
	@Override
	@Nonnull
	public ParsedResult<TextChannel> parse(@Nonnull ParseContext context, @Nonnull Component component, @Nonnull String content) {
		List<TextChannel> channels = ArgumentUtility.getTextChannelsByIdOrName(context.getMessage().getGuild(), content, true);
		if(channels.size() == 1) {
			return ParsedResult.valid(channels.get(0));
		}
		
		return ParsedResult.invalid();
	}
}