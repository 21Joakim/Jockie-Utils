package com.jockie.bot.core.command.impl.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;
import com.jockie.bot.core.paged.impl.PagedManager;
import com.jockie.bot.core.paged.impl.PagedResult;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.tuple.Pair;

/* This will do as a temporary solution */
public class CommandHelp extends CommandImpl {
	
	public CommandHelp() {
		super("help");
	}
	
	public void onCommand(MessageReceivedEvent event, CommandEvent commandEvent, @Argument(name="command", nullDefault=true) String commandStr) {
		Stream<ICommand> stream = commandEvent.getCommandListener().getCommandStores().stream()
			.map(store -> store.getCommandsAuthorized(event, commandEvent.getCommandListener()))
			.flatMap(List::stream)
			.filter(command -> !command.isHidden());
		
		if(commandStr != null) {
			List<Pair<String, ICommand>> commands = stream
				.map(command -> CommandHelp.getSubCommands(command, ""))
				.flatMap(List::stream)
				.filter(pair -> pair.getLeft().toLowerCase().contains(commandStr.toLowerCase()))
				.sorted((first, second) -> first.getLeft().compareToIgnoreCase(second.getLeft()))
				.collect(Collectors.toList());
			
			if(commands.size() > 0) {
				if(commands.size() == 1) {
					EmbedBuilder builder = new EmbedBuilder();
					CommandHelp.setHelp(builder, commandEvent, commands.get(0).getRight());
					
					event.getChannel().sendMessage(builder.build()).queue();
				}else{
					PagedResult<ICommand> pagedResult = new PagedResult<>(commands.stream().map(Pair::getRight).distinct().collect(Collectors.toList()), c -> c.getUsage(), e -> {
						EmbedBuilder builder = new EmbedBuilder();
						CommandHelp.setHelp(builder, commandEvent, e.entry);
						
						event.getChannel().sendMessage(builder.build()).queue();
					});
					
					PagedManager.addPagedResult(event, pagedResult);
				}
			}else{
				event.getChannel().sendMessage("No command found").queue();
			}
		}else{
			List<ICommand> commands = stream
				.sorted((first, second) -> first.getCommand().compareToIgnoreCase(second.getCommand()))
				.collect(Collectors.toList());
			
			PagedResult<ICommand> pagedResult = new PagedResult<>(commands, command -> {
				String info = "**" + command.getCommand() + "**";
				
				String description = command.getDescription();
				if(description != null && description.length() > 0) {
					info += ": *" + description + "*";
				}
				
				return info;
			});
			
			pagedResult.setListIndexes(false);
			
			PagedManager.addPagedResult(event, pagedResult);
		}
	}
	
	private static void setHelp(EmbedBuilder builder, CommandEvent event, ICommand command) {
		builder.addField("Command", command.getCommandTrigger(), true);
		
		builder.addField("Usage", command.getUsage(event.getPrefix()), true);
		
		if(command.getAliases().length > 0) {
			builder.addField("Aliases", Arrays.toString(command.getAliases()).replace("[", "").replace("]", ""), false);
		}
		
		if(command.getDescription() != null && command.getDescription().length() > 0) {
			builder.addField("Description", command.getDescription(), false);
		}
		
		StringBuilder subCommands = new StringBuilder();
		for(ICommand subCommand : command.getSubCommands()) {
			subCommands.append(subCommand.getUsage(event.getPrefix()) + "\n");
		}
		
		if(subCommands.length() > 0) {
			builder.addBlankField(false);
			
			builder.addField("Sub-Commands", subCommands.substring(0, subCommands.length() - 1), true);
		}
		
		builder.setFooter("* means required. [] means multiple arguments of that type.", null);
	}
	
	private static List<Pair<String, ICommand>> getSubCommands(ICommand command, String prefix) {
		List<Pair<String, ICommand>> commands = new ArrayList<>();
		
 		String prefixDefault = (prefix + " " + command.getCommand()).trim();
		
		commands.add(Pair.of(prefixDefault, command));
		
		for(ICommand subCommand : command.getSubCommands()) {
			commands.addAll(CommandHelp.getSubCommands(subCommand, prefixDefault));
		}
		
		for(String alias : command.getAliases()) {
			String aliasPrefix = (prefix + " " + alias).trim();
			
			commands.add(Pair.of(aliasPrefix, command));
			
			for(ICommand subCommand : command.getSubCommands()) {
				commands.addAll(CommandHelp.getSubCommands(subCommand, aliasPrefix));
			}
		}
		
		return commands;
	}
}