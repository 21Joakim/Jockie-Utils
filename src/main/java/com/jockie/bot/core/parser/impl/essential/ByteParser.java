package com.jockie.bot.core.parser.impl.essential;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class ByteParser<Component> implements IParser<Byte, Component> {
	
	@Override
	@Nonnull
	public ParsedResult<Byte> parse(@Nonnull ParseContext context, @Nonnull Component component, @Nonnull String content) {
		try {
			return ParsedResult.valid(Byte.parseByte(content));
		}catch(NumberFormatException e) {
			return ParsedResult.invalid();
		}
	}
}