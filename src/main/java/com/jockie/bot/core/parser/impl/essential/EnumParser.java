package com.jockie.bot.core.parser.impl.essential;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IGenericParser;
import com.jockie.bot.core.parser.IParsableComponent;
import com.jockie.bot.core.parser.ParsedResult;

public class EnumParser<Type extends Enum<?>, Component extends IParsableComponent<Type, Component>> implements IGenericParser<Type, Component> {
	
	@Override
	@Nonnull
	public ParsedResult<Type> parse(@Nonnull ParseContext context, @Nonnull Class<Type> type, @Nonnull Component component, @Nonnull String content) {
		for(Type enumEntry : type.getEnumConstants()) {
			String name = enumEntry.name();
			if(name.equalsIgnoreCase(content) || name.replace("_", " ").equalsIgnoreCase(content)) {
				return ParsedResult.valid(enumEntry);
			}
		}
		
		return ParsedResult.invalid();
	}
}