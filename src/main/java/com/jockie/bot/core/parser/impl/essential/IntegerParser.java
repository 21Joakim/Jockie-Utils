package com.jockie.bot.core.parser.impl.essential;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class IntegerParser<Component> implements IParser<Integer, Component> {
	
	@Override
	@Nonnull
	public ParsedResult<Integer> parse(@Nonnull ParseContext context, @Nonnull Component component, @Nonnull String content) {
		try {
			return new ParsedResult<>(true, Integer.parseInt(content));
		}catch(NumberFormatException e) {
			return new ParsedResult<>(false, null);
		}
	}
}