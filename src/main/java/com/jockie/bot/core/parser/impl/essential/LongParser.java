package com.jockie.bot.core.parser.impl.essential;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class LongParser<Component> implements IParser<Long, Component> {
	
	@Override
	@Nonnull
	public ParsedResult<Long> parse(@Nonnull ParseContext context, @Nonnull Component component, @Nonnull String content) {
		try {
			return ParsedResult.valid(Long.parseLong(content));
		}catch(NumberFormatException e) {
			return ParsedResult.invalid();
		}
	}
}