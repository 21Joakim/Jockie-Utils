package com.jockie.bot.core.option.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.jockie.bot.core.component.impl.AbstractComponent;
import com.jockie.bot.core.option.IOption;

public class OptionImpl<Type> extends AbstractComponent<Type, IOption<Type>> implements IOption<Type> {
	
	public static class Builder<Type> extends IOption.Builder<Type, OptionImpl<Type>, Builder<Type>> {
		
		public Builder(Class<Type> type) {
			super(type);
		}
		
		@Override
		@Nonnull
		public Builder<Type> self() {
			return this;
		}
		
		@Override
		@Nonnull
		public OptionImpl<Type> build() {
			return new OptionImpl<>(this);
		}
	}
	
	protected final List<String> aliases;
	
	protected final boolean hidden;
	protected final boolean developer;
	
	protected <BuilderType extends IOption.Builder<Type, ?, BuilderType>> OptionImpl(BuilderType builder) {
		super(builder);
		
		this.aliases = Collections.unmodifiableList(new ArrayList<>(builder.getAliases()));
		this.hidden = builder.isHidden();
		this.developer = builder.isDeveloper();
	}
	
	@Override
	@Nonnull
	public List<String> getAliases() {
		return this.aliases;
	}
	
	@Override
	public boolean isHidden() {
		return this.hidden;
	}
	
	@Override
	public boolean isDeveloper() {
		return this.developer;
	}
}