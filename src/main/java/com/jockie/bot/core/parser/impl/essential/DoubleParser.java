package com.jockie.bot.core.parser.impl.essential;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class DoubleParser<Component> implements IParser<Double, Component> {

	@Override
	public ParsedResult<Double> parse(ParseContext context, Component component, String content) {
		try {
			return new ParsedResult<>(true, Double.parseDouble(content));
		}catch(NumberFormatException e) {
			return new ParsedResult<>(false, null);
		}
	}
}