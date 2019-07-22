package com.jockie.bot.core.command.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.category.ICategory;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.factory.IComponentFactory;
import com.jockie.bot.core.command.factory.impl.ComponentFactory;
import com.jockie.bot.core.cooldown.ICooldown;
import com.jockie.bot.core.option.IOption;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.utils.tuple.Pair;

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
		
		public List<IOption<?>> getOptions() {
			return this.options;
		}
		
		public String getArgumentInfo() {
			return ICommand.getArgumentInfo(this);
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
	public DummyCommand(ICommand command, IArgument<?>... arguments) {
		this.command = command;
		
		List<IArgument<?>> commandArguments = command.getArguments();
		List<IArgument<?>> requiredArguments = new ArrayList<>(commandArguments.size());
		
		ARGUMENTS:
		for(int i = 0; i < commandArguments.size(); i++) {
			IArgument<?> argument = commandArguments.get(i);
			if(!argument.hasDefault()) {
				requiredArguments.add(argument);
			}else{
				for(int j = 0; j < arguments.length; j++) {
					if(arguments[j].equals(argument)) {
						this.optionalArguments.put(i, argument);
						
						continue ARGUMENTS;
					}
				}
				
				requiredArguments.add(argument);
			}
		}
		
		this.arguments = requiredArguments;
	}
	
	public void execute(CommandEvent event, Object... arguments) throws Throwable {
		Object[] args = new Object[this.command.getArguments().size()];
		
		for(int i = 0, offset = 0; i < args.length; i++) {
			if(this.optionalArguments.get(i) != null) {
				args[i] = this.optionalArguments.get(i).getDefault(event);
				
				/* TODO: Not entirely sure if this is the right place to handle this */
				if(args[i] == null) {
					Class<?> type = this.optionalArguments.get(i).getType();
					
					if(type.equals(boolean.class)) {
						args[i] = false;
					}else if(type.equals(byte.class)) {
						args[i] = (byte) 0;
					}else if(type.equals(short.class)) {
						args[i] = (short) 0;
					}else if(type.equals(int.class)) {
						args[i] = 0;
					}else if(type.equals(long.class)) {
						args[i] = 0L;
					}else if(type.equals(float.class)) {
						args[i] = 0.0F;
					}else if(type.equals(double.class)) {
						args[i] = 0.0D;
					}else if(type.equals(char.class)) {
						args[i] = '\u0000';
					}
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
	public ICommand getActualCommand() {
		return this.command;
	}
	
	public boolean verify(Message message, CommandListener commandListener) {
		return this.command.verify(message, commandListener);
	}
	
	public List<String> getAliases() {
		return this.command.getAliases();
	}
	
	public long getCooldownDuration() {
		return this.command.getCooldownDuration();
	}
	
	public ICooldown.Scope getCooldownScope() {
		return this.command.getCooldownScope();
	}
	
	public boolean isExecuteAsync() {
		return this.command.isExecuteAsync();
	}
	
	public boolean isBotTriggerable() {
		return this.command.isBotTriggerable();
	}
	
	public boolean isCaseSensitive() {
		return this.command.isCaseSensitive();
	}
	
	public boolean isDeveloperCommand() {
		return this.command.isDeveloperCommand();
	}
	
	public boolean isGuildTriggerable() {
		return this.command.isGuildTriggerable();
	}
	
	public boolean isPrivateTriggerable() {
		return this.command.isPrivateTriggerable();
	}
	
	public boolean isHidden() {
		return this.command.isHidden();
	}
	
	/** A DummyCommand should never be passive */
	public boolean isPassive() {
		return false;
	}
	
	public String getDescription() {
		return this.command.getDescription();
	}
	
	public String getShortDescription() {
		return this.command.getShortDescription();
	}
	
	public String getArgumentInfo() {
		return this.command.getArgumentInfo();
	}
	
	public Set<Permission> getAuthorDiscordPermissions() {
		return this.command.getAuthorDiscordPermissions();
	}
	
	public Set<Permission> getBotDiscordPermissions() {
		return this.command.getBotDiscordPermissions();
	}
	
	public String getCommand() {
		return this.command.getCommand();
	}
	
	public ICommand getParent() {
		return this.command.getParent();
	}
	
	public List<IArgument<?>> getArguments() {
		return Collections.unmodifiableList(this.arguments);
	}
	
	public List<ICommand> getSubCommands() {
		return Collections.emptyList();
	}
	
	public List<Pair<String, ICommand>> getAllCommandsRecursiveWithTriggers(Message message, String prefix) {
		return Collections.emptyList();
	}
	
	public List<ICommand> getAllCommandsRecursive(boolean includeDummyCommands) {
		return Collections.emptyList();
	}
	
	public String getCommandTrigger() {
		return this.command.getCommandTrigger();
	}
	
	public List<IOption<?>> getOptions() {
		return this.command.getOptions();
	}
	
	public InvalidOptionPolicy getInvalidOptionPolicy() {
		return this.command.getInvalidOptionPolicy();
	}
	
	public ContentOverflowPolicy getContentOverflowPolicy() {
		return this.command.getContentOverflowPolicy();
	}
	
	public Set<ArgumentParsingType> getAllowedArgumentParsingTypes() {
		return this.command.getAllowedArgumentParsingTypes();
	}
	
	public ArgumentTrimType getArgumentTrimType() {
		return this.command.getArgumentTrimType();
	}
	
	public ICategory getCategory() {
		return this.command.getCategory();
	}
	
	public boolean isNSFW() {
		return this.command.isNSFW();
	}
	
	public Object getAsyncOrderingKey(CommandEvent event) {
		return this.command.getAsyncOrderingKey(event);
	}
}