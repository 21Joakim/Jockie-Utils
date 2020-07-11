package com.jockie.bot.core.parser.impl.essential;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class FloatParser<Component> implements IParser<Float, Component> {

	@Override
	public ParsedResult<Float> parse(ParseContext context, Component component, String content) {
		try {
			return new ParsedResult<>(true, Float.parseFloat(content));
		}catch(NumberFormatException e) {
			return new ParsedResult<>(false, null);
		}
	}
}