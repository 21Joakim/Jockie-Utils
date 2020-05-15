package com.jockie.bot.core.command.manager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.utility.function.TriConsumer;
import com.jockie.bot.core.utility.function.TriFunction;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.internal.entities.GuildImpl;

public interface IErrorManager {
	
	/**
	 * @param argument the argument which was parsed incorrectly
	 * @param message the message which was parsed
	 * @param content the content which was parsed as the argument incorrectly
	 * 
	 * @return whether or not it was handled
	 */
	public boolean handle(@Nonnull IArgument<?> argument, @Nonnull Message message, @Nonnull String content);
	
	/**
	 * Register a response for the error handler
	 * 
	 * @param type the type to handle
	 * @param consumer the handler
	 * 
	 * @return the {@link IErrorManager} instance, useful for chaining
	 */
	@Nonnull
	public <T> IErrorManager registerResponse(@Nonnull Class<T> type, @Nonnull TriConsumer<IArgument<T>, Message, String> consumer);
	
	/**
	 * Unregister a previously registered error response
	 * 
	 * @param type the type to remove a response for
	 * 
	 * @return the {@link IErrorManager} instance, useful for chaining
	 */
	public IErrorManager unregisterResponse(@Nullable Class<?> type);
	
	/**
	 * Register a response for the error handler, this uses String.format on the error message
	 * with the content which was parsed incorrectly, this means that you could, for instance,
	 * do <b>registerResponse(User.class, "`%s` is not a valid user");</b>
	 * 
	 * @param type the type to handle
	 * @param errorMessage the error message to reply with
	 * 
	 * @return the {@link IErrorManager} instance, useful for chaining
	 */
	@Nonnull
	public default IErrorManager registerResponse(@Nonnull Class<?> type, @Nonnull String errorMessage) {
		this.registerResponse(type, (argument, message, content) -> {
			message.getChannel().sendMessage(String.format(errorMessage, content)).queue();
		});
		
		return this;
	}
	
	/**
	 * Register a response for the error handler
	 * 
	 * @param type the type to handle
	 * @param function the function which will return the error message
	 * 
	 * @return the {@link IErrorManager} instance, useful for chaining
	 */
	@Nonnull
	public default <T> IErrorManager registerResponse(@Nonnull Class<T> type, @Nonnull TriFunction<IArgument<T>, Message, String, String> function) {
		this.registerResponse(type, (argument, message, content) -> {
			message.getChannel().sendMessage(function.apply(argument, message, content)).queue();
		});
		
		return this;
	}
	
	/**
	 * @param type the type of the handler
	 * 
	 * @return whether or not the specified type should be handled with inheritance, 
	 * this means that it will, for instance, handle {@link GuildImpl} if {@link Guild}
	 * was registered
	 */
	@Nonnull
	public boolean isHandleInheritance(@Nonnull Class<?> type);
	
	/**
	 * @param type the type of the handler
	 * @param handle whether or not the specified type should be handled with inheritance, 
	 * this means that it will, for instance, handle {@link GuildImpl} if {@link Guild}
	 * was registered
	 * 
	 * @return the {@link IErrorManager} instance, useful for chaining
	 */
	@Nonnull
	public IErrorManager setHandleInheritance(@Nonnull Class<?> type, boolean handle);
	
}