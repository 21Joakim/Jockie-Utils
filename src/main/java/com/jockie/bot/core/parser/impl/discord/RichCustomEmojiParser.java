package com.jockie.bot.core.parser.impl.discord;

import java.util.List;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.utility.ArgumentUtility;

import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

public class RichCustomEmojiParser<Component> implements IParser<RichCustomEmoji, Component> {
	
	@Override
	@Nonnull
	public ParsedResult<RichCustomEmoji> parse(@Nonnull ParseContext context, @Nonnull Component component, @Nonnull String content) {
		List<RichCustomEmoji> emojis = ArgumentUtility.getRichEmojisByIdOrName(context.getMessage().getGuild(), content, true);
		if(emojis.size() == 1) {
			return new ParsedResult<>(true, emojis.get(0));
		}
		
		return new ParsedResult<>(false, null);
	}
}