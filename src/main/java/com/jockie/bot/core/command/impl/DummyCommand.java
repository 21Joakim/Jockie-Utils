package com.jockie.bot.core.command.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.category.ICategory;
import com.jockie.bot.core.command.CommandTrigger;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.factory.IComponentFactory;
import com.jockie.bot.core.command.factory.impl.ComponentFactory;
import com.jockie.bot.core.cooldown.ICooldown;
import com.jockie.bot.core.option.IOption;
import com.jockie.bot.core.utility.CommandUtility;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.internal.utils.Checks;

/**
 * The DummyCommand is a Command which replicates any ICommand but with different arguments, 
 * this is used for creating optional arguments. 
 * <br>
 * <br>
 * <b>It is not recommended to use the DummyCommand for anything as it is most likely going to be 
 * replaced with a more elegant solution in the future.</b>
 */
public class DummyCommand implements ICommand {
	
	public static class AlternativeCommand extends DummyCommand {
		
		protected List<IOption<?>> options;
		
		protected Method method;
		protected Object invoker;
		
		public AlternativeCommand(ICommand command, Method method, Object invoker) {
			IComponentFactory componentFactory = ComponentFactory.getDefault();
			
			this.arguments = Arrays.asList(componentFactory.createArguments(method));
			this.options = Arrays.asList(componentFactory.createOptions(method));
			
			this.command = command;
			
			this.method = method;
			this.invoker = invoker;
		}
		
		public void execute(CommandEvent event, Object... arguments) throws Throwable {
			MethodCommandImpl.executeMethodCommand(this, this.invoker, this.method, event, arguments);
		}
		
		@Nonnull
		public List<IOption<?>> getOptions() {
			return this.options;
		}
		
		@Nonnull
		public String getArgumentInfo() {
			return ICommand.getArgumentInfo(this);
		}
		
		@Override
		public String toString() {
			return String.format("AlternativeCommand<%s>{usage=%s}", this.command.getClass().getSimpleName(), this.getUsage().trim());
		}
	}
	
	protected ICommand command;
	
	protected Map<Integer, IArgument<?>> optionalArguments = new HashMap<>();
	
	protected List<IArgument<?>> arguments;
	
	private DummyCommand() {}
	
	/**
	 * @param command the command which this DummyCommand should replicate
	 * @param arguments the arguments which this DummyCommand should have, 
	 * these are different from the command's arguments.
	 */
	public DummyCommand(@Nonnull ICommand command, @Nonnull IArgument<?>... arguments) {
		Checks.notNull(command, "command");
		Checks.notNull(arguments, "arguments");
		
		this.command = command;
		
		List<IArgument<?>> commandArguments = command.getArguments();
		List<IArgument<?>> requiredArguments = new ArrayList<>(commandArguments.size());
		
		ARGUMENTS:
		for(int i = 0; i < commandArguments.size(); i++) {
			IArgument<?> argument = commandArguments.get(i);
			if(argument.hasDefault()) {
				for(int j = 0; j < arguments.length; j++) {
					if(arguments[j].equals(argument)) {
						this.optionalArguments.put(i, argument);
						
						continue ARGUMENTS;
					}
				}
			}
			
			requiredArguments.add(argument);
		}
		
		this.arguments = requiredArguments;
	}
	
	@Override
	public void execute(CommandEvent event, Object... arguments) throws Throwable {
		Object[] args = new Object[this.command.getArguments().size()];
		
		for(int i = 0, offset = 0; i < args.length; i++) {
			if(this.optionalArguments.get(i) != null) {
				args[i] = this.optionalArguments.get(i).getDefault(event);
				
				/* TODO: Not entirely sure if this is the right place to handle this */
				if(args[i] == null) {
					Class<?> type = this.optionalArguments.get(i).getType();
					args[i] = CommandUtility.getDefaultValue(type);
				}
			}else{
				args[i] = arguments[offset++];
			}
		}
		
		this.command.execute(event, event.arguments = args);
	}
	
	/**
	 * @return the actual command; the command this DummyCommand is replicating
	 */
	@Nonnull
	public ICommand getActualCommand() {
		return this.command;
	}
	
	@Override
	public boolean isAccessible(@Nonnull Message message, @Nonnull CommandListener commandListener) {
		return this.command.isAccessible(message, commandListener);
	}
	
	@Override
	@Nonnull
	public List<String> getAliases() {
		return this.command.getAliases();
	}
	
	@Override
	public long getCooldownDuration() {
		return this.command.getCooldownDuration();
	}
	
	@Override
	@Nonnull
	public ICooldown.Scope getCooldownScope() {
		return this.command.getCooldownScope();
	}
	
	@Override
	public boolean isExecuteAsync() {
		return this.command.isExecuteAsync();
	}
	
	@Override
	public boolean isBotTriggerable() {
		return this.command.isBotTriggerable();
	}
	
	@Override
	public boolean isCaseSensitive() {
		return this.command.isCaseSensitive();
	}
	
	@Override
	public boolean isDeveloperCommand() {
		return this.command.isDeveloperCommand();
	}
	
	@Override
	public boolean isGuildTriggerable() {
		return this.command.isGuildTriggerable();
	}
	
	@Override
	public boolean isPrivateTriggerable() {
		return this.command.isPrivateTriggerable();
	}
	
	@Override
	public boolean isHidden() {
		return this.command.isHidden();
	}
	
	/** A DummyCommand should never be passive */
	@Override
	public boolean isPassive() {
		return false;
	}
	
	@Override
	@Nullable
	public String getDescription() {
		return this.command.getDescription();
	}
	
	@Override
	@Nullable
	public String getShortDescription() {
		return this.command.getShortDescription();
	}
	
	@Override
	@Nonnull
	public String getArgumentInfo() {
		return this.command.getArgumentInfo();
	}
	
	@Override
	@Nonnull
	public Set<Permission> getAuthorDiscordPermissions() {
		return this.command.getAuthorDiscordPermissions();
	}
	
	@Override
	@Nonnull
	public Set<Permission> getBotDiscordPermissions() {
		return this.command.getBotDiscordPermissions();
	}
	
	@Override
	@Nonnull
	public String getCommand() {
		return this.command.getCommand();
	}
	
	@Override
	@Nullable
	public ICommand getParent() {
		return this.command.getParent();
	}
	
	@Override
	@Nonnull
	public List<IArgument<?>> getArguments() {
		return Collections.unmodifiableList(this.arguments);
	}
	
	@Override
	@Nonnull
	public List<ICommand> getSubCommands() {
		return Collections.emptyList();
	}
	
	@Override
	@Nonnull
	public List<CommandTrigger> getAllCommandsRecursiveWithTriggers(@Nonnull Message message, @Nonnull String prefix) {
		return Collections.emptyList();
	}
	
	@Override
	@Nonnull
	public List<ICommand> getAllCommandsRecursive(boolean includeDummyCommands) {
		return Collections.emptyList();
	}
	
	@Override
	@Nonnull
	public String getCommandTrigger() {
		return this.command.getCommandTrigger();
	}
	
	@Override
	@Nonnull
	public List<IOption<?>> getOptions() {
		return this.command.getOptions();
	}
	
	@Override
	@Nonnull
	public UnknownOptionPolicy getUnknownOptionPolicy() {
		return this.command.getUnknownOptionPolicy();
	}
	
	@Override
	@Nonnull
	public DuplicateOptionPolicy getDuplicateOptionPolicy() {
		return this.command.getDuplicateOptionPolicy();
	}

	@Override
	@Nonnull
	public OptionParsingFailurePolicy getOptionParsingFailurePolicy() {
		return this.command.getOptionParsingFailurePolicy();
	}
	
	@Override
	@Nonnull
	public ContentOverflowPolicy getContentOverflowPolicy() {
		return this.command.getContentOverflowPolicy();
	}
	
	@Override
	@Nonnull
	public Set<ArgumentParsingType> getAllowedArgumentParsingTypes() {
		return this.command.getAllowedArgumentParsingTypes();
	}
	
	@Override
	@Nonnull
	public ArgumentTrimType getArgumentTrimType() {
		return this.command.getArgumentTrimType();
	}
	
	@Override
	@Nullable
	public ICategory getCategory() {
		return this.command.getCategory();
	}
	
	@Override
	public boolean isNSFW() {
		return this.command.isNSFW();
	}
	
	@Override
	@Nullable
	public Object getAsyncOrderingKey(@Nonnull CommandEvent event) {
		return this.command.getAsyncOrderingKey(event);
	}
	
	@Override
	@Nullable
	public <T> T getProperty(@Nonnull String name, @Nullable T defaultValue) {
		return this.command.getProperty(name, defaultValue);
	}
	
	@Override
	@Nonnull
	public Map<String, Object> getProperties() {
		return this.command.getProperties();
	}
	
	@Override
	public String toString() {
		return String.format("DummyCommand<%s>{usage=%s}", this.command.getClass().getSimpleName(), (this.getCommandTrigger() + " " + ICommand.getArgumentInfo(this)).trim());
	}
}