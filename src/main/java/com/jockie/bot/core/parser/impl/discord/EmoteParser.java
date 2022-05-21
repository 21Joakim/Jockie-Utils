package com.jockie.bot.core.parser.impl.discord;

import java.util.List;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.utility.ArgumentUtility;

import net.dv8tion.jda.api.entities.Emote;

public class EmoteParser<Component> implements IParser<Emote, Component> {
	
	@Override
	@Nonnull
	public ParsedResult<Emote> parse(@Nonnull ParseContext context, @Nonnull Component component, @Nonnull String content) {
		List<Emote> emotes = ArgumentUtility.getEmotesByIdOrName(context.getMessage().getGuild(), content, true);
		if(emotes.size() == 1) {
			return new ParsedResult<>(true, emotes.get(0));
		}
		
		return new ParsedResult<>(false, null);
	}
}