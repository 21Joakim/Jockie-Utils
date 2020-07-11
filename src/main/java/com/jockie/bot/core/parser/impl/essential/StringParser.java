package com.jockie.bot.core.parser.impl.essential;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class StringParser<Component> implements IParser<String, Component> {

	@Override
	public ParsedResult<String> parse(ParseContext context, Component component, String content) {
		return new ParsedResult<>(true, content);
	}
}