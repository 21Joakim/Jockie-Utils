package com.jockie.bot.core.parser.impl.essential;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class CharacterParser<Component> implements IParser<Character, Component> {

	@Override
	public ParsedResult<Character> parse(ParseContext context, Component component, String content) {
		if(content.length() == 1) {
			return new ParsedResult<>(true, content.charAt(0));
		}else{
			return new ParsedResult<>(false, null);
		}
	}
}