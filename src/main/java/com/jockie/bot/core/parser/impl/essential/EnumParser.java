package com.jockie.bot.core.parser.impl.essential;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IGenericParser;
import com.jockie.bot.core.parser.IParsableComponent;
import com.jockie.bot.core.parser.ParsedResult;

public class EnumParser<Type extends Enum<?>, Component extends IParsableComponent<Type, Component>> implements IGenericParser<Type, Component> {
	
	@Override
	public ParsedResult<Type> parse(ParseContext context, Class<Type> type, Component component, String content) {
		for(Type enumEntry : type.getEnumConstants()) {
			String name = enumEntry.name();
			if(name.equalsIgnoreCase(content) || name.replace("_", " ").equalsIgnoreCase(content)) {
				return new ParsedResult<>(true, enumEntry);
			}
		}
		
		return new ParsedResult<>(false, null);
	}
}