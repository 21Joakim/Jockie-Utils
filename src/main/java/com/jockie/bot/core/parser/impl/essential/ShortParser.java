package com.jockie.bot.core.parser.impl.essential;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class ShortParser<Component> implements IParser<Short, Component> {
	
	@Override
	@Nonnull
	public ParsedResult<Short> parse(@Nonnull ParseContext context, @Nonnull Component component, @Nonnull String content) {
		try {
			return ParsedResult.valid(Short.parseShort(content));
		}catch(NumberFormatException e) {
			return ParsedResult.invalid();
		}
	}
}