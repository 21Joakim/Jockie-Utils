package com.jockie.bot.core.argument.impl;

import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.component.impl.AbstractComponent;

import net.dv8tion.jda.api.entities.Message;

public class ArgumentImpl<Type> extends AbstractComponent<Type, IArgument<Type>> implements IArgument<Type> {
	
	public static class Builder<Type> extends IArgument.Builder<Type, ArgumentImpl<Type>, Builder<Type>> {
		
		public Builder(Class<Type> type) {
			super(type);
		}
		
		@Nonnull
		public Builder<Type> self() {
			return this;
		}
		
		@Nonnull
		public ArgumentImpl<Type> build() {
			return new ArgumentImpl<>(this);
		}
	}
	
	protected final boolean endless, empty, quote;
	protected final BiConsumer<Message, String> errorConsumer;
	
	protected <BuilderType extends IArgument.Builder<Type, ?, BuilderType>> ArgumentImpl(BuilderType builder) {
		super(builder);
		
		this.endless = builder.isEndless();
		this.empty = builder.isAcceptEmpty();
		this.quote = builder.isAcceptQuote();
		this.errorConsumer = builder.getErrorConsumer();
	}
	
	@Override
	public boolean isEndless() {
		return this.endless;
	}
	
	@Override
	public boolean acceptQuote() {
		return this.quote;
	}
	
	@Override
	public boolean acceptEmpty() {
		return this.empty;
	}
	
	@Override
	public BiConsumer<Message, String> getErrorConsumer() {
		return this.errorConsumer;
	}
}