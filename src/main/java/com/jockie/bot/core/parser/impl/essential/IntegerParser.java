package com.jockie.bot.core.parser.impl.essential;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class IntegerParser<Component> implements IParser<Integer, Component> {

	@Override
	public ParsedResult<Integer> parse(ParseContext context, Component component, String content) {
		try {
			return new ParsedResult<>(true, Integer.parseInt(content));
		}catch(NumberFormatException e) {
			return new ParsedResult<>(false, null);
		}
	}
}