package com.jockie.bot.core.argument;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.component.IComponent;

import net.dv8tion.jda.api.entities.Message;

public interface IArgument<Type> extends IComponent<Type, IArgument<Type>> {
	
	/**
	 * @return whether or not this argument should take all the remaining content
	 * when parsing the command and pass it to the parser
	 */
	public boolean isEndless();
	
	/**
	 * @return whether or not this argument should allow for quoted content to be 
	 * passed when parsing the command, quoted content is content which is within two 
	 * quotes, like so "hello there"
	 */
	public boolean acceptQuote();
	
	/**
	 * @return whether or not this argument should allow for empty content to be 
	 * passed when parsing the command, empty content could either be nothing at all
	 * or two quotes with nothing inside it if this accepts quoted content ({@link #acceptQuote()})
	 */
	public boolean acceptEmpty();
	
	/**
	 * Used to give the user information about what happened or give them an error response,
	 * the consumer which this returns will be called when this argument is incorrectly parsed
	 * 
	 * @return an error consumer
	 */
	@Nullable
	public BiConsumer<Message, String> getErrorConsumer();
	
	public abstract class Builder<Type, ReturnType extends IArgument<Type>, BuilderType extends Builder<Type, ReturnType, BuilderType>> extends IComponent.Builder<Type, IArgument<Type>, ReturnType, BuilderType> {
		
		protected boolean endless, empty, quote = true;
		
		protected BiConsumer<Message, String> errorConsumer;
		
		protected Builder(@Nonnull Class<Type> type) {
			super(type);
		}
		
		@Nonnull
		public BuilderType setEndless(boolean endless) {
			this.endless = endless;
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setAcceptEmpty(boolean empty) {
			this.empty = empty;
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setAcceptQuote(boolean quote) {
			this.quote = quote;
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setErrorConsumer(@Nullable BiConsumer<Message, String> consumer) {
			this.errorConsumer = consumer;
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setErrorFunction(@Nullable BiFunction<Message, String, String> function) {
			if(function != null) {
				this.errorConsumer = (message, content) -> message.getChannel().sendMessage(function.apply(message, content)).queue();
			}else{
				this.errorConsumer = null;
			}
			
			return this.self();
		}
		
		@Nonnull
		public BuilderType setErrorMessage(@Nullable String errorMessage) {
			if(errorMessage != null) {
				this.errorConsumer = (message, content) -> message.getChannel().sendMessage(String.format(errorMessage, content)).queue();
			}else{
				this.errorConsumer = null;
			}
			
			return this.self();
		}
		
		public boolean isEndless() {
			return this.endless;
		}
		
		public boolean isAcceptEmpty() {
			return this.empty;
		}
		
		public boolean isAcceptQuote() {
			return this.quote;
		}
		
		@Nullable
		public BiConsumer<Message, String> getErrorConsumer() {
			return this.errorConsumer;
		}
	}
}