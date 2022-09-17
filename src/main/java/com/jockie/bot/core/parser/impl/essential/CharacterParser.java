package com.jockie.bot.core.parser.impl.essential;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class CharacterParser<Component> implements IParser<Character, Component> {
	
	@Override
	@Nonnull
	public ParsedResult<Character> parse(@Nonnull ParseContext context, @Nonnull Component component, @Nonnull String content) {
		if(content.length() == 1) {
			return ParsedResult.valid(content.charAt(0));
		}
		
		return ParsedResult.invalid();
	}
}