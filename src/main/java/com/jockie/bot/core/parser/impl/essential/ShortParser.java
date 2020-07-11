package com.jockie.bot.core.parser.impl.essential;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class ShortParser<Component> implements IParser<Short, Component> {

	@Override
	public ParsedResult<Short> parse(ParseContext context, Component component, String content) {
		try {
			return new ParsedResult<>(true, Short.parseShort(content));
		}catch(NumberFormatException e) {
			return new ParsedResult<>(false, null);
		}
	}
}