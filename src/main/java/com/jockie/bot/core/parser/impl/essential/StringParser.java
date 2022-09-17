package com.jockie.bot.core.parser.impl.essential;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class StringParser<Component> implements IParser<String, Component> {
	
	@Override
	@Nonnull
	public ParsedResult<String> parse(@Nonnull ParseContext context, @Nonnull Component component, @Nonnull String content) {
		return ParsedResult.valid(content);
	}
}