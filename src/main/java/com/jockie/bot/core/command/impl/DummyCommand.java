package com.jockie.bot.core.command.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.category.ICategory;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.cooldown.ICooldown;
import com.jockie.bot.core.option.IOption;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.utils.tuple.Pair;

/**
 * The DummyCommand is a Command which replicates any ICommand but with different arguments, 
 * this is used for creating optional arguments. 
 * </br>
 * </br>
 * <b>It is not recommended to use the DummyCommand for anything as it is most likely going to be 
 * replaced with a more elegant solution in the future.</b>
 */
public class DummyCommand implements ICommand {
	
	private ICommand command;
	
	private Map<Integer, IArgument<?>> optionalArguments = new HashMap<>();
	
	private List<IArgument<?>> arguments;
	
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
			}else{
				args[i] = arguments[offset++];
			}
		}
		
		this.command.execute(event, event.arguments = args);
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
	
	public boolean isPassive() {
		return this.command.isPassive();
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
	
	public List<String> getExamples() {
		return this.command.getExamples();
	}
	
	public List<Permission> getAuthorDiscordPermissions() {
		return this.command.getAuthorDiscordPermissions();
	}
	
	public List<Permission> getBotDiscordPermissions() {
		return this.command.getBotDiscordPermissions();
	}
	
	public String getCommand() {
		return this.command.getCommand();
	}
	
	/**
	 * @return the actual command; the command this DummyCommand is replicating
	 */
	public ICommand getParent() {
		return this.command;
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
	
	public List<IOption> getOptions() {
		return this.command.getOptions();
	}
	
	public InvalidOptionPolicy getInvalidOptionPolicy() {
		return this.command.getInvalidOptionPolicy();
	}
	
	public ContentOverflowPolicy getContentOverflowPolicy() {
		return this.command.getContentOverflowPolicy();
	}
	
	public List<ArgumentParsingType> getAllowedArgumentParsingTypes() {
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