package com.jockie.bot.core.parser.impl;

import java.util.Set;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IAfterParser;
import com.jockie.bot.core.parser.IBeforeParser;
import com.jockie.bot.core.parser.IGenericParser;
import com.jockie.bot.core.parser.IParsableComponent;
import com.jockie.bot.core.parser.ParsedResult;

public class DelegateGenericParser<T, Component extends IParsableComponent<T, Component>> implements IGenericParser<T, Component> {
	
	protected final IGenericParser<T, Component> parser;
	
	protected final Set<IBeforeParser<Component>> beforeParsers;
	protected final Set<IAfterParser<T, Component>> afterParsers;
	
	public DelegateGenericParser(IGenericParser<T, Component> parser, Set<IBeforeParser<Component>> beforeParsers, Set<IAfterParser<T, Component>> afterParsers) {
		this.parser = parser;
		this.beforeParsers = beforeParsers;
		this.afterParsers = afterParsers;
	}
	
	@Override
	@Nonnull
	public ParsedResult<T> parse(@Nonnull ParseContext context, @Nonnull Class<T> type, @Nonnull Component argument, @Nonnull String content) {
		for(IBeforeParser<Component> parser : this.beforeParsers) {
			ParsedResult<String> parsed = parser.parse(context, argument, content);
			if(!parsed.isValid()) {
				return ParsedResult.invalid();
			}
			
			String value = parsed.getObject();
			if(value == null) {
				throw new IllegalStateException();
			}
			
			content = value;
		}
		
		ParsedResult<T> parsed = this.parser.parse(context, type, argument, content);
		if(!parsed.isValid()) {
			return parsed;
		}
		
		T object = parsed.getObject();
		for(IAfterParser<T, Component> parser : this.afterParsers) {
			ParsedResult<T> newResult = parser.parse(context, argument, object);
			if(!newResult.isValid()) {
				return newResult;
			}
			
			object = newResult.getObject();
		}
		
		return ParsedResult.valid(object, parsed.getContentLeft());
	}
	
	@Override
	public boolean isHandleAll() {
		return this.parser.isHandleAll();
	}
}