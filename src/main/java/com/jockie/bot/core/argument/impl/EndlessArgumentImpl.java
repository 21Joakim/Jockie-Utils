package com.jockie.bot.core.argument.impl;

import java.lang.reflect.Array;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.IEndlessArgument;
import com.jockie.bot.core.argument.VerifiedArgument;
import com.jockie.bot.core.argument.VerifiedArgument.VerifiedType;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class EndlessArgumentImpl<Type> extends ArgumentImpl<Type[]> implements IEndlessArgument<Type> {
	
	private final IArgument<Type> argument;
	
	private final int minArguments, maxArguments;
	
	private final Class<Type> clazz;
	
	public static class Builder<Type> extends IEndlessArgument.Builder<Type, IEndlessArgument<Type>, Builder<Type>> {
		
		private IArgument<Type> argument;
		
		private Class<Type> clazz;
		
		public Builder(Class<Type> clazz) {
			if(clazz.isPrimitive()) {
				throw new IllegalArgumentException("Primitve types are currently not supported for endless arguments");
			}
			
			this.clazz = clazz;
		}
		
		public Builder<Type> setArgument(IArgument<Type> argument) {
			if(argument instanceof IEndlessArgument || argument.isEndless()) {
				throw new IllegalArgumentException("Not a valid candidate, candidate may not be endless");
			}
			
			this.argument = argument;
			this.name = argument.getName();
			
			return this.self();
		}
		
		public IArgument<Type> getArgument() {
			return this.argument;
		}
		
		public Class<Type> getType() {
			return this.clazz;
		}
		
		public Builder<Type> self() {
			return this;
		}
		
		public IEndlessArgument<Type> build() {
			return new EndlessArgumentImpl<>(this);
		}
	}
	
	private EndlessArgumentImpl(Builder<Type> builder) {
		super(builder);
		
		this.clazz = builder.getType();
		this.argument = builder.getArgument();
		this.minArguments = builder.getMinArguments();
		this.maxArguments = builder.getMaxArguments();
	}
	
	public IArgument<Type> getArgument() {
		return this.argument;
	}
	
	public int getMinArguments() {
		return this.minArguments;
	}
	
	public int getMaxArguments() {
		return this.maxArguments;
	}
	
	@SuppressWarnings("unchecked")
	public VerifiedArgument<Type[]> verify(MessageReceivedEvent event, String value) {
		int args = 0;
		
		Type[] arguments = (Type[]) Array.newInstance(this.clazz, (this.maxArguments > 0) ? this.maxArguments : (int) value.codePoints().filter(c2 -> c2 == ' ').count() + 1);
		
		ARGUMENTS:
		for(int i = 0; i < arguments.length; i++) {
			if(value.trim().length() == 0) {
				break;
			}
			
			if(i != 0 && value.length() > 0) {
				if(value.startsWith(" ")) {
					value = value.substring(1);
				}else{
					/* When does this happen? */
					
					return new VerifiedArgument<>(VerifiedType.INVALID, null);
				}
			}
			
			String content = null;
			if(value.length() > 0) {
				if(this.argument.acceptQuote()) {
					if(value.charAt(0) == '"') {
						int nextQuote = 0;
						while((nextQuote = value.indexOf('"', nextQuote + 1)) != -1 && value.charAt(nextQuote - 1) == '\\');
						
						if(nextQuote != -1) {
							content = value.substring(1, nextQuote);
							
							value = value.substring(content.length() + 2);
							
							content = content.replace("\\\"", "\"");
						}
					}
				}
				
				if(content == null) {
					content = value.substring(0, (value.contains(" ")) ? value.indexOf(" ") : value.length());
					value = value.substring(content.length());
				}
			}else{
				content = "";
			}
			
			if(content.length() == 0 && !this.argument.acceptEmpty()) {
				/* Content may not be empty */
				return new VerifiedArgument<>(VerifiedType.INVALID, null);
			}
			
			VerifiedArgument<Type> verified = this.argument.verify(event, content);
			switch(verified.getVerifiedType()) {
				case INVALID: {
					
					/* "is invalid, argument at index " + (i + 1) + " is not valid" */
					return new VerifiedArgument<>(VerifiedType.INVALID, null);
				}
				case VALID: {
					arguments[args++] = (Type) verified.getObject();
					
					break;
				}
				case VALID_END_NOW: {
					arguments[args++] = (Type) verified.getObject();
					
					break ARGUMENTS;
				}
			}
		}
		
		if(value.length() > 0) {
			/* Content overflow, when does this happen? */
			
			return new VerifiedArgument<>(VerifiedType.INVALID, null);
		}
		
		if(args < this.minArguments || ((this.maxArguments > 0) ? args > this.maxArguments : false)) {
			return new VerifiedArgument<>(VerifiedType.INVALID, null);
		}
		
		Type[] objects = (Type[]) Array.newInstance(this.clazz, args);
		for(int i2 = 0; i2 < objects.length; i2++) {
			objects[i2] = (Type) arguments[i2];
		}
		
		arguments = objects;
		
		if(this.isEndless()) {
			return new VerifiedArgument<>(VerifiedType.VALID_END_NOW, arguments);
		}else{
			return new VerifiedArgument<>(VerifiedType.VALID, arguments);
		}
	}
}