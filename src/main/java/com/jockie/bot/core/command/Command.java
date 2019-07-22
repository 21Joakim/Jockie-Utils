package com.jockie.bot.core.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import com.jockie.bot.core.command.ICommand.ArgumentParsingType;
import com.jockie.bot.core.command.ICommand.ArgumentTrimType;
import com.jockie.bot.core.command.ICommand.ContentOverflowPolicy;
import com.jockie.bot.core.command.ICommand.InvalidOptionPolicy;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.cooldown.ICooldown.Scope;

import net.dv8tion.jda.core.Permission;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
/* TODO: Might be worth copying the documentation from ICommand here */
public @interface Command {
	
	/**
	 * @see ICommand#getCommand()
	 */
	public String value() default "";
	
	/**
	 * @see ICommand#getAliases()
	 */
	public String[] aliases() default {};
	
	/**
	 * @see ICommand#isGuildTriggerable()
	 */
	public boolean guildTriggerable() default true;
	
	/**
	 * @see ICommand#isPrivateTriggerable()
	 */
	public boolean privateTriggerable() default false;
	
	/**
	 * @see ICommand#getDescription()
	 */
	public String description() default "";
	
	/**
	 * @see ICommand#getShortDescription()
	 */
	public String shortDescription() default "";
	
	/**
	 * @see ICommand#getArgumentInfo()
	 */
	public String argumentInfo() default "";
	
	/**
	 * @see ICommand#getBotDiscordPermissions()
	 */
	public Permission[] botPermissions() default {};
	
	/**
	 * @see ICommand#getAuthorDiscordPermissions()
	 */
	public Permission[] authorPermissions() default {};
	
	/**
	 * @see ICommand#isCaseSensitive()
	 */
	public boolean caseSensitive() default false;
	
	/**
	 * @see ICommand#isDeveloperCommand()
	 */
	public boolean developer() default false;
	
	/**
	 * @see ICommand#isBotTriggerable()
	 */
	public boolean botTriggerable() default false;
	
	/**
	 * @see ICommand#isHidden()
	 */
	public boolean hidden() default false;
	
	/**
	 * @see ICommand#getCooldownDuration()
	 */
	public long cooldown() default 0;
	
	/**
	 * @return the time unit to use for the cooldown duration
	 */
	public TimeUnit cooldownUnit() default TimeUnit.SECONDS;
	
	/**
	 * @see ICommand#getCooldownScope()
	 */
	public Scope cooldownScope() default Scope.USER;
	
	/**
	 * @see ICommand#isExecuteAsync()
	 */
	public boolean async() default false;
	
	/**
	 * @see ICommand#getAsyncOrderingKey(CommandEvent)
	 */
	public String orderingKey() default "";
	
	/**
	 * @see ICommand#isNSFW()
	 */
	public boolean nsfw() default false;
	
	/**
	 * @see ICommand#getContentOverflowPolicy()
	 */
	public ContentOverflowPolicy contentOverflowPolicy() default ContentOverflowPolicy.FAIL;
	
	/**
	 * @see ICommand#getInvalidOptionPolicy()
	 */
	public InvalidOptionPolicy invalidOptionPolicy() default InvalidOptionPolicy.INCLUDE;
	
	/**
	 * @see ICommand#getAllowedArgumentParsingTypes()
	 */
	public ArgumentParsingType[] allowedArgumentParsingTypes() default { ArgumentParsingType.POSITIONAL, ArgumentParsingType.NAMED };
	
	/**
	 * @see ICommand#getArgumentTrimType()
	 */
	public ArgumentTrimType argumentTrimType() default ArgumentTrimType.LENIENT;
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface Async {
		
		/**
		 * @see ICommand#isExecuteAsync()
		 */
		public boolean value() default true;
		
		/**
		 * @see ICommand#getAsyncOrderingKey(CommandEvent)
		 */
		public String orderingKey() default "";
		
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface AuthorPermissions {
		
		/**
		 * @see ICommand#getAuthorDiscordPermissions()
		 */
		public Permission[] value() default {};
		
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface BotPermissions {
		
		/**
		 * @see ICommand#getBotDiscordPermissions()
		 */
		public Permission[] value() default {};
		
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface Cooldown {
		
		/**
		 * @see ICommand#getCooldownDuration()
		 */
		public long value() default 0;
		
		/**
		 * @return the time unit to use for the cooldown duration
		 */
		public TimeUnit cooldownUnit() default TimeUnit.SECONDS;
		
		/**
		 * @see ICommand#getCooldownScope()
		 */
		public Scope cooldownScope() default Scope.USER;
		
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface Developer {
		
		/**
		 * @see ICommand#isDeveloperCommand()
		 */
		public boolean value() default true;
		
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface Hidden {
		
		/**
		 * @see ICommand#isHidden()
		 */
		public boolean value() default true;
		
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface Nsfw {
		
		/**
		 * @see ICommand#isNSFW()
		 */
		public boolean value() default true;
		
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface Policy {
		
		/**
		 * @see ICommand#getContentOverflowPolicy()
		 */
		public ContentOverflowPolicy contentOverflow() default ContentOverflowPolicy.FAIL;
		
		/**
		 * @see ICommand#getInvalidOptionPolicy()
		 */
		public InvalidOptionPolicy invalidOption() default InvalidOptionPolicy.INCLUDE;
		
	}
}