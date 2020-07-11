package com.jockie.bot.core.parser.impl.essential;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class BooleanParser<Component> implements IParser<Boolean, Component> {

	@Override
	public ParsedResult<Boolean> parse(ParseContext context, Component component, String content) {
		if(content.equalsIgnoreCase("true") || content.equalsIgnoreCase("false")) {
			return new ParsedResult<>(true, Boolean.parseBoolean(content));
		}
		
		return new ParsedResult<>(false, null);
	}
}