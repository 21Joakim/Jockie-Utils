package com.jockie.bot.core.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.category.ICategory;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.cooldown.ICooldown;
import com.jockie.bot.core.cooldown.ICooldown.Scope;
import com.jockie.bot.core.option.IOption;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.internal.utils.Checks;

public abstract class AbstractCommand implements ICommand {
	
	protected String command;
	
	protected String description, shortDescription;
	
	protected String argumentInfo;
	
	protected List<String> aliases = Collections.emptyList();
	
	protected List<IArgument<?>> arguments = Collections.emptyList();
	protected List<IOption<?>> options = Collections.emptyList();
	
	protected UnknownOptionPolicy unknownOptionPolicy = UnknownOptionPolicy.INCLUDE;
	
	protected DuplicateOptionPolicy duplicateOptionPolicy = DuplicateOptionPolicy.USE_LAST;
	
	protected OptionParsingFailurePolicy optionParsingFailurePolicy = OptionParsingFailurePolicy.IGNORE;
	
	protected ContentOverflowPolicy overflowPolicy = ContentOverflowPolicy.FAIL;
	
	protected EnumSet<ArgumentParsingType> allowedArgumentParsingTypes = EnumSet.of(ArgumentParsingType.POSITIONAL);
	
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
	
	protected Map<String, Object> properties = new HashMap<>();
	
	public AbstractCommand(String command) {
		this.command = command;
	}
	
	public AbstractCommand() {}
	
	@Override
	@Nonnull
	public String getCommand() {
		return this.command;
	}
	
	@Override
	@Nullable
	public String getShortDescription() {
		return this.shortDescription;
	}
	
	@Override
	@Nullable
	public String getDescription() {
		return this.description;
	}
	
	@Override
	@Nonnull
	public String getArgumentInfo() {
		if(this.argumentInfo == null || this.argumentInfo.length() == 0) {
			return ICommand.super.getArgumentInfo();
		}
		
		return this.argumentInfo;
	}
	
	@Override
	@Nonnull
	public List<String> getAliases() {
		return Collections.unmodifiableList(this.aliases);
	}
	
	@Override
	@Nonnull
	public List<IArgument<?>> getArguments() {
		return Collections.unmodifiableList(this.arguments);
	}
	
	@Override
	@Nonnull
	public List<IOption<?>> getOptions() {
		return Collections.unmodifiableList(this.options);
	}
	
	@Override
	@Nonnull
	public UnknownOptionPolicy getUnknownOptionPolicy() {
		return this.unknownOptionPolicy;
	}
	
	@Override
	@Nonnull
	public DuplicateOptionPolicy getDuplicateOptionPolicy() {
		return this.duplicateOptionPolicy;
	}

	@Override
	@Nonnull
	public OptionParsingFailurePolicy getOptionParsingFailurePolicy() {
		return this.optionParsingFailurePolicy;
	}
	
	@Override
	@Nonnull
	public ContentOverflowPolicy getContentOverflowPolicy() {
		return this.overflowPolicy;
	}
	
	@Override
	@Nonnull
	public EnumSet<ArgumentParsingType> getAllowedArgumentParsingTypes() {
		return this.allowedArgumentParsingTypes;
	}
	
	@Override
	@Nonnull
	public ArgumentTrimType getArgumentTrimType() {
		return this.argumentTrimType;
	}
	
	@Override
	@Nonnull
	public Set<Permission> getBotDiscordPermissions() {
		return Collections.unmodifiableSet(this.botDiscordPermissions);
	}
	
	@Override
	@Nonnull
	public Set<Permission> getAuthorDiscordPermissions() {
		return Collections.unmodifiableSet(this.authorDiscordPermissions);
	}
	
	@Override
	public boolean isGuildTriggerable() {
		return this.guildTriggerable;
	}
	
	@Override
	public boolean isPrivateTriggerable() {
		return this.privateTriggerable;
	}
	
	@Override
	public boolean isCaseSensitive() {
		return this.caseSensitive;
	}
	
	@Override
	public boolean isBotTriggerable() {
		return this.botTriggerable;
	}
	
	@Override
	public boolean isDeveloperCommand() {
		return this.developerCommand;
	}
	
	@Override
	public boolean isHidden() {
		return this.hidden;
	}
	
	@Override
	public boolean isNSFW() {
		return this.nsfw;
	}
	
	@Override
	public long getCooldownDuration() {
		return this.cooldownDuration;
	}
	
	@Override
	@Nonnull
	public ICooldown.Scope getCooldownScope() {
		return this.cooldownScope;
	}
	
	@Override
	public boolean isExecuteAsync() {
		return this.executeAsync;
	}
	
	@Override
	@Nullable
	public Object getAsyncOrderingKey(CommandEvent event) {
		if(this.asyncOrderingKey != null) {
			return this.asyncOrderingKey.apply(event);
		}
		
		return null;
	}
	
	@Override
	@Nullable
	public ICommand getParent() {
		return this.parent;
	}
	
	@Override
	@Nullable
	public ICategory getCategory() {
		return this.category;
	}
	
	@Override
	public boolean isPassive() {
		return this.passive;
	}
	
	@Override
	@Nonnull
	public List<ICommand> getSubCommands() {
		return this.subCommands;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T> T getProperty(@Nonnull String name, @Nullable T defaultValue) {
		Checks.notNull(name, "name");
		
		return (T) this.properties.getOrDefault(name, defaultValue);
	}
	
	@Override
	@Nonnull
	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(this.properties);
	}
	
	@Nonnull
	public AbstractCommand setCommand(@Nullable String command) {
		/* TODO: Can this be null? What happens if it is */
		this.command = command;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setDeveloper(boolean developerCommand) {
		this.developerCommand = developerCommand;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setBotTriggerable(boolean botTriggerable) {
		this.botTriggerable = botTriggerable;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setBotDiscordPermissions(@Nonnull Permission... permissions) {
		this.botDiscordPermissions.clear();
		
		for(Permission type : permissions) {
			this.botDiscordPermissions.add(type);
		}
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setAuthorDiscordPermissions(@Nonnull Permission... permissions) {
		this.authorDiscordPermissions.clear();
		
		for(Permission type : permissions) {
			this.authorDiscordPermissions.add(type);
		}
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setDescription(@Nullable String description) {
		this.description = description;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setShortDescription(@Nullable String shortDescription) {
		this.shortDescription = shortDescription;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setArgumentInfo(@Nullable String argumentInfo) {
		this.argumentInfo = argumentInfo;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setAliases(@Nonnull String... aliases) {
		Checks.noneNull(aliases, "aliases");
		
		/* 
		 * From the longest alias to the shortest so that if the command for instance has two aliases one being "hello" 
		 * and the other being "hello there" it would recognize that the command is "hello there" instead of it thinking that
		 * "hello" is the command and "there" being the argument.
		 */
		Arrays.sort(aliases, (a, b) -> Integer.compare(b.length(), a.length()));
		
		this.aliases = List.of(aliases);
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setArguments(@Nonnull IArgument<?>... arguments) {
		Checks.noneNull(arguments, "arguments");
		this.arguments = List.of(arguments);
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setOptions(@Nonnull IOption<?>... options) {
		Checks.noneNull(options, "options");
		this.options = List.of(options);
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setUnknownOptionPolicy(@Nonnull UnknownOptionPolicy unknownOptionPolicy) {
		Checks.notNull(unknownOptionPolicy, "unknownOptionPolicy");
		this.unknownOptionPolicy = unknownOptionPolicy;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setDuplicateOptionPolicy(@Nonnull DuplicateOptionPolicy duplicateOptionPolicy) {
		Checks.notNull(duplicateOptionPolicy, "duplicateOptionPolicy");
		this.duplicateOptionPolicy = duplicateOptionPolicy;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setOptionParsingFailureType(@Nonnull OptionParsingFailurePolicy optionParsingFailurePolicy) {
		Checks.notNull(optionParsingFailurePolicy, "optionParsingFailurePolicy");
		this.optionParsingFailurePolicy = optionParsingFailurePolicy;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setContentOverflowPolicy(@Nonnull ContentOverflowPolicy contentOverflowPolicy) {
		Checks.notNull(contentOverflowPolicy, "contentOverflowPolicy");
		this.overflowPolicy = contentOverflowPolicy;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setAllowedArgumentParsingTypes(@Nonnull ArgumentParsingType... argumentParsingTypes) {
		this.allowedArgumentParsingTypes.clear();
		
		for(ArgumentParsingType type : argumentParsingTypes) {
			this.allowedArgumentParsingTypes.add(type);
		}
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setArgumentTrimType(@Nonnull ArgumentTrimType argumentTrimType) {
		Checks.notNull(argumentTrimType, "argumentTrimType");
		this.argumentTrimType = argumentTrimType;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setGuildTriggerable(boolean triggerable) {
		this.guildTriggerable = triggerable;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setPrivateTriggerable(boolean triggerable) {
		this.privateTriggerable = triggerable;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setHidden(boolean hidden) {
		this.hidden = hidden;
		
		return this;
	}
	
	@Nonnull
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
	@Nonnull
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
	@Nonnull
	public AbstractCommand setCooldownDuration(long duration, @Nonnull TimeUnit unit) {
		return this.setCooldownDuration(unit.toMillis(duration));
	}
	
	@Nonnull
	public AbstractCommand setCooldownScope(@Nonnull Scope scope) {
		Checks.notNull(scope, "scope");
		this.cooldownScope = scope;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setExecuteAsync(boolean executeAsync) {
		this.executeAsync = executeAsync;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setAsyncOrderingKey(@Nullable Function<CommandEvent, Object> function) {
		this.asyncOrderingKey = function;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setAsyncOrderingKey(@Nullable Object key) {
		return this.setAsyncOrderingKey(($) -> key);
	}
	
	@Nonnull
	public AbstractCommand setParent(@Nullable ICommand parent) {
		this.parent = parent;
		
		return this;
	}
	
	/* TODO: The whole category implementation is weird and should be re-considered */
	@Nonnull
	public AbstractCommand setCategory(@Nullable ICategory category) {
		ICategory old = this.category;
		
		this.category = category;
		
		if(old != null) {
			old.removeCommand(this);
		}
		
		if(this.category != null) {
			this.category.addCommand(this);
		}
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand setPassive(boolean passive) {
		this.passive = passive;
		
		return this;
	}
	
	@Nonnull
	public AbstractCommand addSubCommand(@Nonnull ICommand command) {
		Checks.notNull(command, "command");
		this.subCommands.add(command);
		
		if(command instanceof AbstractCommand) {
			((AbstractCommand) command).setParent(this);
		}
		
		return this;
	}
	
	@Nonnull
	public <T> AbstractCommand setProperty(@Nonnull String name, T value) {
		Checks.notNull(name, "name");
		this.properties.put(name, value);
		
		return this;
	}
	
	@Override
	public String toString() {
		return String.format("%s{usage=%s}", this.getClass().getSimpleName(), this.getUsage().trim());
	}
}