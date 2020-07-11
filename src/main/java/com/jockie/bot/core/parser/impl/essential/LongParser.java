package com.jockie.bot.core.parser.impl.essential;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class LongParser<Component> implements IParser<Long, Component> {

	@Override
	public ParsedResult<Long> parse(ParseContext context, Component component, String content) {
		try {
			return new ParsedResult<>(true, Long.parseLong(content));
		}catch(NumberFormatException e) {
			return new ParsedResult<>(false, null);
		}
	}
}