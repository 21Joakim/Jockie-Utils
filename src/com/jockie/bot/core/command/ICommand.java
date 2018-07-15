package com.jockie.bot.core.command;

import java.util.List;

import com.jockie.bot.core.command.argument.IArgument;
import com.jockie.bot.core.command.argument.IEndlessArgument;
import com.jockie.bot.core.command.impl.Category;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandListener;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.tuple.Pair;

public interface ICommand {
	
	/**
	 * @return the command which the Listener should look for.
	 */
	public String getCommand();
	
	/**
	 * @return a boolean that will prove if this command should be able to be triggered by guild messages.
	 */
	public boolean isGuildTriggerable();
	
	/**
	 * @return a boolean that will prove if this command should be able to be triggered by private messages.
	 */
	public boolean isPrivateTriggerable();
	
	/**
	 * @return the argument arguments.
	 */
	public IArgument<?>[] getArguments();
	
	/**
	 * @return a boolean that will prove if this command is hidden and should therefore not be shown in help commands
	 */
	public boolean isHidden();
	
	/**
	 * @return a description of what this command does
	 */
	public String getDescription();
	
	/**
	 * @return all the possible aliases for this command
	 */
	public String[] getAliases();
	
	/**
	 * @return the discord permissions required for this command to function correctly.
	 */
	public Permission[] getBotDiscordPermissionsNeeded();
	
	/**
	 * @return the discord permissions the author is required to have to trigger this command, if the user does not have these permissions the command will not be visible to them.
	 */
	public Permission[] getAuthorDiscordPermissionsNeeded();
	
	/**
	 * @return a boolean that will prove if this command is a <strong>developer</strong> command, if it is a developer command it can only be triggered by developers/authorised users
	 */
	public boolean isDeveloperCommand();
	
	/**
	 * @return a boolean that will prove if this command can be triggered by a bot {@link net.dv8tion.jda.core.entities.User#isBot() User.isBot()}
	 */
	public boolean isBotTriggerable();
	
	/**
	 * @return a boolean that will prove if this command is case sensitive.<p>
	 * For instance if {@link com.jockie.bot.core.command.ICommand#getCommand() Command.getCommand()} 
	 * is equal to <strong>ping</strong> and {@link com.jockie.bot.core.command.ICommand#isCaseSensitive() Command.isCaseSensitive()} 
	 * is <strong>false</strong> then the command could be triggered by any message that {@link String#toLowerCase()} would be equal to <strong>ping</strong>.<br>
	 * On the other hand if {@link com.jockie.bot.core.command.ICommand#isCaseSensitive() Command.isCaseSensitive()} is <strong>true</strong> and
	 * {@link com.jockie.bot.core.command.ICommand#getCommand() Command.getCommand()} is equal to <strong>PiNg</strong> 
	 * then the command could only be triggered if the message is equal to <strong>PiNg</strong> 
	 */
	public boolean isCaseSensitive();
	
	/**
	 * @return the category this command is in
	 */
	public Category getCategory();
	
	/**
	 * @return the cooldown duration in milliseconds which will be applied to a user
	 * after they use this command. If the cooldown is less than or equal to 0 no
	 * cooldown will be applied
	 */
	public long getCooldownDuration();
	
	/**
	 * @return a boolean that will tell whether the command should be executed on a separate thread or not
	 */
	public boolean isExecuteAsync();
	
	public ICommand getParent();
	
	public default boolean hasParent() {
		return this.getParent() != null;
	}
	
	/**
	 * @return a boolean to prove whether this command is passive or not, a passive command will not have any executable method and might for instance only have sub-commands.
	 */
	public boolean isPassive();
	
	public List<ICommand> getSubCommands();
	
	/**
	 * Should only be used by the class that implements this and the class that verifies the commands
	 * 
	 * @return a boolean that will prove if the event has the correct properties for the command to be valid
	 */
	
	/* Should this be renamed to something else, such as isAccessible? Since it checks whether the user has access to the command or not, maybe canAccess? */
	public boolean verify(MessageReceivedEvent event, CommandListener commandListener);
	
	/**
	 * This is what should be executed when this command is considered to be valid.
	 * 
	 * @param event the event which triggered the command.
	 * @param arguments the arguments which triggered the command.
	 */
	public void execute(MessageReceivedEvent event, CommandEvent commandEvent, Object... arguments) throws Exception;
	
	/* I don't really like how this is but it works for now */
	public List<Pair<ICommand, List<?>>> getAllCommandsRecursive(String prefix);
	
	/**
	 * @return information about the arguments
	 */
	public default String getArgumentInfo() {
		StringBuilder builder = new StringBuilder();
		
		for(int i = 0; i < this.getArguments().length; i++) {
			IArgument<?> argument = this.getArguments()[i];
			
			if(argument.getName() != null) {
				builder.append("<").append(argument.getName()).append(">");
			}else{
				builder.append("<argument ").append(i + 1).append(">");
			}
			
			if(argument instanceof IEndlessArgument) {
				IEndlessArgument<?> endlessArgument = (IEndlessArgument<?>) argument;
				
				builder.append("[").append(endlessArgument.getMinArguments()).append("-").append((endlessArgument.getMaxArguments() != 0) ? endlessArgument.getMaxArguments() + "]" : "...]");
			}
			
			if(!argument.hasDefault()) {
				builder.append("*");			
			}
			
			if(i < this.getArguments().length - 1) {
				builder.append(" ");
			}
		}
		
		return builder.toString();
	}
	
	/**
	 * @return full usage information about the command, prefix, command and {@link #getArgumentInfo()}
	 */
	public default String getUsage(String prefix) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(prefix)
			.append(this.getCommandTrigger())
			.append(" ")
			.append(this.getArgumentInfo());
		
		return builder.toString();
	}
	
	public default String getUsage() {
		return this.getUsage("");
	}
	
	/**
	 * @return the actual trigger for this command which is created by recursively getting the parents of this command
	 */
	public default String getCommandTrigger() {
		String command = this.getCommand();
		
		ICommand parent = this;
		while((parent = parent.getParent()) != null) {
			command = (parent.getCommand() + " " + command).trim();
		}
		
		return command;
	}
	
	public default ICommand getTopParent() {
		if(this.hasParent()) {
			ICommand parent = this.getParent();
			while(parent.hasParent()) {
				parent = parent.getParent();
			}
			
			return parent;
		}
		
		return this;
	}
}