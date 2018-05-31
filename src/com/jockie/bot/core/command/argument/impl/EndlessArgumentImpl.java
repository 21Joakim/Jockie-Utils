package com.jockie.bot.core.command.argument.impl;

import java.lang.reflect.Array;

import com.jockie.bot.core.command.argument.IArgument;
import com.jockie.bot.core.command.argument.IArgument.VerifiedArgument.VerifiedType;
import com.jockie.bot.core.command.argument.IEndlessArgument;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class EndlessArgumentImpl<Type> extends ArgumentImpl<Type[]> implements IEndlessArgument<Type> {
	
	private IArgument<Type> argument;
	
	private int minArguments, maxArguments;
	
	private Class<Type> clazz;
	
	public static class Builder<Type> extends IEndlessArgument.Builder<Type, IEndlessArgument<Type>, Builder<Type>> {
		
		private IArgument<Type> argument;
		
		private Class<Type> clazz;
		
		public Builder(Class<Type> clazz) {			
			if(clazz.isPrimitive()) {
				throw new IllegalArgumentException("Primitve types are currently not supported");
			}
			
			this.clazz = clazz;
		}
		
		public Builder<Type> setArgument(IArgument<Type> argument) {
			if(argument instanceof IEndlessArgument || argument.isEndless()) {
				throw new IllegalArgumentException("Not a valid candidate");
			}
			
			this.argument = argument;
			
			this.description = argument.getDescription();
			this.empty = argument.acceptEmpty();
			this.quote = argument.acceptQuote();
			
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
					return new VerifiedArgument<Type[]>(VerifiedType.INVALID, null);
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
				return new VerifiedArgument<Type[]>(VerifiedType.INVALID, null);
			}
			
			VerifiedArgument<Type> verified = this.argument.verify(event, content);
			
			switch(verified.getVerifiedType()) {
				case INVALID: {
					return new VerifiedArgument<Type[]>(VerifiedType.INVALID, null);
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
			return new VerifiedArgument<Type[]>(VerifiedType.INVALID, null);
		}
		
		if(args < this.minArguments || ((this.maxArguments > 0) ? args > this.maxArguments : false)) {
			return new VerifiedArgument<Type[]>(VerifiedType.INVALID, null);
		}
		
		Type[] objects = (Type[]) Array.newInstance(this.clazz, args);
		for(int i2 = 0; i2 < objects.length; i2++) {
			objects[i2] = (Type) arguments[i2];
		}
		
		arguments = objects;
		
		return new VerifiedArgument<Type[]>(VerifiedType.VALID_END_NOW, objects);
	}
}