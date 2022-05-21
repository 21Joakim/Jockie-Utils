package com.jockie.bot.core.parser.impl;

import java.util.Set;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IAfterParser;
import com.jockie.bot.core.parser.IBeforeParser;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class DelegateParser<T, Component> implements IParser<T, Component> {
	
	protected final IParser<T, Component> parser;
	
	protected final Set<IBeforeParser<Component>> beforeParsers;
	protected final Set<IAfterParser<T, Component>> afterParsers;
	
	public DelegateParser(IParser<T, Component> parser, Set<IBeforeParser<Component>> beforeParsers, Set<IAfterParser<T, Component>> afterParsers) {
		this.parser = parser;
		this.beforeParsers = beforeParsers;
		this.afterParsers = afterParsers;
	}
	
	@Override
	@Nonnull
	public ParsedResult<T> parse(@Nonnull ParseContext context, @Nonnull Component argument, @Nonnull String content) {
		for(IBeforeParser<Component> parser : this.beforeParsers) {
			ParsedResult<String> parsed = parser.parse(context, argument, content);
			if(!parsed.isValid()) {
				return new ParsedResult<T>(false, null);
			}
			
			String value = parsed.getObject();
			if(value == null) {
				throw new IllegalStateException();
			}
			
			content = value;
		}
		
		ParsedResult<T> parsed = this.parser.parse(context, argument, content);
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
		
		return new ParsedResult<T>(true, object, parsed.getContentLeft());
	}
	
	@Override
	public boolean isHandleAll() {
		return this.parser.isHandleAll();
	}
}