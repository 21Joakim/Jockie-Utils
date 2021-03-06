package com.jockie.bot.core.cooldown;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import net.dv8tion.jda.api.entities.Message;

public interface ICooldown {
	
	public static enum Scope {
		/** This scope applies to the current user, the command can be used by any other user */
		USER(message -> String.format("u:%s", message.getAuthor().getId())),
		/** This scope applies to the current channel for the user, the command can be used in another channel by the same user */
		USER_CHANNEL(message -> String.format("u:%s,c:%s", message.getAuthor().getId(), message.getChannel().getId())),
		/** This scope applies to the current guild for the user, the command can be used in another guild by the same user */
		USER_GUILD(message -> message.getChannelType().isGuild() ? String.format("u:%s,g:%s", message.getAuthor().getId(), message.getGuild().getId()) : USER_CHANNEL.getContextKey(message)),
		/** This scope applies to the current shard for the user, the command can be used in another shard by the same user */
		USER_SHARD(message -> message.getJDA().getShardInfo() != null ? String.format("u:%s,s:%s", message.getAuthor().getId(), message.getJDA().getShardInfo()) : USER.getContextKey(message)),
		/** This scope applies to the current channel, the command can be used in any other channel */
		CHANNEL(message -> String.format("c:%s", message.getChannel().getId())),
		/** This scope applies to the entire guild, the command can be used in any other guild */
		GUILD(message -> String.format("g:%s", message.getGuild().getId())),
		/** This scope applies to the current shard, the command can be used in any other shard */
		SHARD(message -> message.getJDA().getShardInfo() != null ? String.format("s:%s", message.getJDA().getShardInfo()) : ""),
		/** This scope applies to everything, the command can not be used anywhere else */
		GLOBAL(message -> "");
		
		private Function<Message, String> keyFunction;
		
		private Scope(Function<Message, String> function) {
			this.keyFunction = function;
		}
		
		public String getContextKey(Message message) {
			return this.keyFunction.apply(message);
		}
	}
	
	/**
	 * Apply a context to this cooldown
	 * 
	 * @param message the context to apply to this cooldown
	 */
	public void applyContext(Message message);
	
	/**
	 * @return the scope this cooldown is applied to
	 */
	public Scope getScope();
	
	/**
	 * @return the context this cooldown is applied to
	 */
	public String getContextKey();
	
	/**
	 * @return the Instant this cooldown was started
	 */
	public Instant getTimeStarted();
	
	/**
	 * @return the time unit used for this cooldown
	 */
	public TimeUnit getDurationUnit();
	
	/**
	 * @return the full duration of the cooldown
	 */
	public long getDuration();
	
	/**
	 * @param unit the time unit to return the cooldown duration in
	 * 
	 * @return the full duration of the cooldown in the specified time unit
	 */
	public long getDuration(TimeUnit unit);
	
	/**
	 * @return the time remaining of the cooldown in milliseconds
	 */
	public long getTimeRemainingMillis();
	
	/**
	 * @return the time remaining of the cooldown as a Duration
	 */
	public Duration getTimeRemaining();
	
	/**
	 * @return whether or not this cooldown has expired or not
	 */
	public boolean hasExpired();
	
	/**
	 * Update the time of the cooldown, giving it a positive value will increase the cooldown 
	 * and giving it a negative value will decrease the cooldown
	 * 
	 * @param duration the duration to update the cooldown by, the time unit is milliseconds
	 */
	public void updateDuration(long duration);
	
	/**
	 * Update the time of the cooldown, giving it a positive value will increase the cooldown 
	 * and giving it a negative value will decrease the cooldown
	 * 
	 * @param duration the duration to update the cooldown by
	 * @param unit the time unit to the update the cooldown by
	 */
	public void updateDuration(long duration, TimeUnit unit);
	
	/**
	 * Increase the time of the cooldown
	 * 
	 * @param duration the duration to increase the cooldown by, the time unit is milliseconds
	 */
	public void increase(long duration);
	
	/**
	 * Increase the time of the cooldown
	 * 
	 * @param duration the duration to increase the cooldown by
	 * @param unit the time unit to the increase the cooldown by
	 */
	public void increase(long duration, TimeUnit unit);
	
	/**
	 * Decrease the time of the cooldown
	 * 
	 * @param duration the duration to decrease the cooldown by, the time unit is milliseconds
	 */
	public void decrease(long duration);
	
	/**
	 * Decrease the time of the cooldown
	 * 
	 * @param duration the duration to decrease the cooldown by
	 * @param unit the time unit to the decrease the cooldown by
	 */
	public void decrease(long duration, TimeUnit unit);
	
	/**
	 * Start the cooldown
	 */
	public void start();
	
	/**
	 * Reset the cooldown
	 */
	public void reset();
	
	/**
	 * Cancel the cooldown
	 */
	public void cancel();
	
}