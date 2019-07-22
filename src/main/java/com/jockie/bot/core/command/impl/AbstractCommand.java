package com.jockie.bot.core.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.category.ICategory;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.cooldown.ICooldown;
import com.jockie.bot.core.cooldown.ICooldown.Scope;
import com.jockie.bot.core.option.IOption;

import net.dv8tion.jda.api.Permission;

public abstract class AbstractCommand implements ICommand {
	
	protected String command;
	
	protected String description, shortDescription;
	
	protected String argumentInfo;
	
	protected List<String> aliases = Collections.emptyList();
	
	protected List<IArgument<?>> arguments = Collections.emptyList();
	protected List<IOption<?>> options = Collections.emptyList();
	
	protected InvalidOptionPolicy invalidOptionPolicy = InvalidOptionPolicy.INCLUDE;
	
	protected ContentOverflowPolicy overflowPolicy = ContentOverflowPolicy.FAIL;
	
	protected EnumSet<ArgumentParsingType> allowedArgumentParsingTypes = EnumSet.of(ArgumentParsingType.POSITIONAL, ArgumentParsingType.NAMED);
	
	protected ArgumentTrimType argumentTrimType = ArgumentTrimType.LENIENT;
	
	protected EnumSet<Permission> botDiscordPermissions = EnumSet.noneOf(Permission.class);
	protected EnumSet<Permission> authorDiscordPermissions = EnumSet.noneOf(Permission.class);
	
	protected boolean guildTriggerable = true;
	protected boolean privateTriggerable;
	
	protected boolean caseSensitive;
	
	protected boolean botTriggerable;
	
	protected boolean developerCommand;
	
	protected boolean hidden;
	protected boolean nsfw = false;
	
	protected boolean executeAsync;
	
	protected Function<CommandEvent, Object> asyncOrderingKey;
	
	protected long cooldownDuration = 0;
	protected Scope cooldownScope = Scope.USER;
	
	protected ICommand parent;
	protected ICategory category;
	
	protected List<ICommand> subCommands = new ArrayList<>();
	
	protected boolean passive = false;
	
	public AbstractCommand(String command) {
		this.command = command;
	}
	
	public AbstractCommand() {}
	
	public String getCommand() {
		return this.command;
	}
	
	public String getShortDescription() {
		return this.shortDescription;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getArgumentInfo() {
		if(this.argumentInfo == null || this.argumentInfo.length() == 0) {
			return ICommand.super.getArgumentInfo();
		}
		
		return this.argumentInfo;
	}
	
	public List<String> getAliases() {
		return Collections.unmodifiableList(this.aliases);
	}
	
	public List<IArgument<?>> getArguments() {
		return Collections.unmodifiableList(this.arguments);
	}
	
	public List<IOption<?>> getOptions() {
		return Collections.unmodifiableList(this.options);
	}
	
	public InvalidOptionPolicy getInvalidOptionPolicy() {
		return this.invalidOptionPolicy;
	}
	
	public ContentOverflowPolicy getContentOverflowPolicy() {
		return this.overflowPolicy;
	}
	
	public EnumSet<ArgumentParsingType> getAllowedArgumentParsingTypes() {
		return this.allowedArgumentParsingTypes;
	}
	
	public ArgumentTrimType getArgumentTrimType() {
		return this.argumentTrimType;
	}
	
	public Set<Permission> getBotDiscordPermissions() {
		return Collections.unmodifiableSet(this.botDiscordPermissions);
	}
	
	public Set<Permission> getAuthorDiscordPermissions() {
		return Collections.unmodifiableSet(this.authorDiscordPermissions);
	}
	
	public boolean isGuildTriggerable() {
		return this.guildTriggerable;
	}
	
	public boolean isPrivateTriggerable() {
		return this.privateTriggerable;
	}
	
	public boolean isCaseSensitive() {
		return this.caseSensitive;
	}
	
	public boolean isBotTriggerable() {
		return this.botTriggerable;
	}
	
	public boolean isDeveloperCommand() {
		return this.developerCommand;
	}
	
	public boolean isHidden() {
		return this.hidden;
	}
	
	public boolean isNSFW() {
		return this.nsfw;
	}
	
	public long getCooldownDuration() {
		return this.cooldownDuration;
	}
	
	public ICooldown.Scope getCooldownScope() {
		return this.cooldownScope;
	}
	
	public boolean isExecuteAsync() {
		return this.executeAsync;
	}
	
	public Object getAsyncOrderingKey(CommandEvent event) {
		if(this.asyncOrderingKey != null) {
			return this.asyncOrderingKey.apply(event);
		}
		
		return null;
	}
	
	public ICommand getParent() {
		return this.parent;
	}
	
	public ICategory getCategory() {
		return this.category;
	}
	
	public boolean isPassive() {
		return this.passive;
	}
	
	public List<ICommand> getSubCommands() {
		return this.subCommands;
	}
	
	public AbstractCommand setCommand(String command) {
		this.command = command;
		
		return this;
	}
	
	public AbstractCommand setDeveloper(boolean developerCommand) {
		this.developerCommand = developerCommand;
		
		return this;
	}
	
	public AbstractCommand setBotTriggerable(boolean botTriggerable) {
		this.botTriggerable = botTriggerable;
		
		return this;
	}
	
	public AbstractCommand setBotDiscordPermissions(Permission... permissions) {
		this.botDiscordPermissions.clear();
		
		for(Permission type : permissions) {
			this.botDiscordPermissions.add(type);
		}
		
		return this;
	}
	
	public AbstractCommand setAuthorDiscordPermissions(Permission... permissions) {
		this.authorDiscordPermissions.clear();
		
		for(Permission type : permissions) {
			this.authorDiscordPermissions.add(type);
		}
		
		return this;
	}
	
	public AbstractCommand setDescription(String description) {
		this.description = description;
		
		return this;
	}
	
	public AbstractCommand setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
		
		return this;
	}
	
	public AbstractCommand setArgumentInfo(String argumentInfo) {
		this.argumentInfo = argumentInfo;
		
		return this;
	}
	
	public AbstractCommand setAliases(String... aliases) {
		/* 
		 * From the longest alias to the shortest so that if the command for instance has two aliases one being "hello" 
		 * and the other being "hello there" it would recognize that the command is "hello there" instead of it thinking that
		 * "hello" is the command and "there" being the argument.
		 */
		Arrays.sort(aliases, (a, b) -> Integer.compare(b.length(), a.length()));
		
		this.aliases = List.of(aliases);
		
		return this;
	}
	
	public AbstractCommand setArguments(IArgument<?>... arguments) {
		this.arguments = List.of(arguments);
		
		return this;
	}
	
	public AbstractCommand setOptions(IOption<?>... options) {
		this.options = List.of(options);
		
		return this;
	}
	
	public AbstractCommand setInvalidOptionPolicy(InvalidOptionPolicy optionPolicy) {
		this.invalidOptionPolicy = optionPolicy;
		
		return this;
	}
	
	public AbstractCommand setContentOverflowPolicy(ContentOverflowPolicy overflowPolicy) {
		this.overflowPolicy = overflowPolicy;
		
		return this;
	}
	
	public AbstractCommand setAllowedArgumentParsingTypes(ArgumentParsingType... argumentParsingTypes) {
		this.allowedArgumentParsingTypes.clear();
		
		for(ArgumentParsingType type : argumentParsingTypes) {
			this.allowedArgumentParsingTypes.add(type);
		}
		
		return this;
	}
	
	public AbstractCommand setArgumentTrimType(ArgumentTrimType trimType) {
		this.argumentTrimType = trimType;
		
		return this;
	}
	
	public AbstractCommand setGuildTriggerable(boolean triggerable) {
		this.guildTriggerable = triggerable;
		
		return this;
	}
	
	public AbstractCommand setPrivateTriggerable(boolean triggerable) {
		this.privateTriggerable = triggerable;
		
		return this;
	}
	
	public AbstractCommand setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		
		return this;
	}
	
	public AbstractCommand setHidden(boolean hidden) {
		this.hidden = hidden;
		
		return this;
	}
	
	public AbstractCommand setNSFW(boolean nsfw) {
		this.nsfw = nsfw;
		
		return this;
	}
	
	/**
	 * @param duration in milliseconds
	 * 
	 * @return the {@link AbstractCommand} instance, useful for chaining
	 * 
	 * @see #getCooldownDuration()
	 */
	public AbstractCommand setCooldownDuration(long duration) {
		this.cooldownDuration = duration;
		
		return this;
	}
	
	/**
	 * @param duration in the specified unit
	 * @param unit the unit of the duration
	 * 
	 * @return the {@link AbstractCommand} instance, useful for chaining
	 * 
	 * @see #getCooldownDuration()
	 */
	public AbstractCommand setCooldownDuration(long duration, TimeUnit unit) {
		return this.setCooldownDuration(unit.toMillis(duration));
	}
	
	public AbstractCommand setCooldownScope(Scope scope) {
		this.cooldownScope = scope;
		
		return this;
	}
	
	public AbstractCommand setExecuteAsync(boolean executeAsync) {
		this.executeAsync = executeAsync;
		
		return this;
	}
	
	public AbstractCommand setAsyncOrderingKey(Function<CommandEvent, Object> function) {
		this.asyncOrderingKey = function;
		
		return this;
	}
	
	public AbstractCommand setAsyncOrderingKey(Object key) {
		return this.setAsyncOrderingKey(($) -> key);
	}
	
	public AbstractCommand setParent(ICommand parent) {
		this.parent = parent;
		
		return this;
	}
	
	public AbstractCommand setCategory(ICategory category) {
		ICategory old = this.category;
		
		this.category = category;
		
		if(old != null) {
			this.category.removeCommand(this);
		}
		
		if(this.category != null) {
			this.category.addCommand(this);
		}
		
		return this;
	}
	
	public AbstractCommand setPassive(boolean passive) {
		this.passive = passive;
		
		return this;
	}
	
	public AbstractCommand addSubCommand(ICommand command) {
		this.subCommands.add(command);
		
		if(command instanceof AbstractCommand) {
			((AbstractCommand) command).setParent(this);
		}
		
		return this;
	}
	
	public String toString() {
		return (this.getCommandTrigger() + " " + this.getArgumentInfo()).trim();
	}
}