package com.jockie.bot.core.parser.impl.discord;

import java.util.List;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.utility.ArgumentUtility;

import net.dv8tion.jda.api.entities.emoji.CustomEmoji;

public class CustomEmojiParser<Component> implements IParser<CustomEmoji, Component> {
	
	@Override
	@Nonnull
	public ParsedResult<CustomEmoji> parse(@Nonnull ParseContext context, @Nonnull Component component, @Nonnull String content) {
		List<CustomEmoji> emojis = ArgumentUtility.getEmojisByIdOrName(context.getMessage().getGuild(), content, true);
		if(emojis.size() == 1) {
			return ParsedResult.valid(emojis.get(0));
		}
		
		return ParsedResult.invalid();
	}
}