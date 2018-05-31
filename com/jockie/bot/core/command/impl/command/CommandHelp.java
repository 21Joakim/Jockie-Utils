package com.jockie.bot.core.command.impl.command;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.argument.impl.ArgumentFactory;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;
import com.jockie.bot.core.command.impl.DummyCommand;
import com.jockie.bot.core.paged.impl.PagedManager;
import com.jockie.bot.core.paged.impl.PagedResult;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandHelp extends CommandImpl {

	public CommandHelp() {
		super("help");
		
		super.setArguments(ArgumentFactory.of(String.class).setDefaultValue("").setEndless(true).build());
		super.setDescription("Help command");
	}
	
	public void onCommand(MessageReceivedEvent event, CommandEvent commandEvent, String commandRaw) {
		if(commandRaw.length() > 0) {
			if(commandRaw.equals("all")) {
				List<ICommand> commands = commandEvent.getCommandListener().getCommandStores()
					.stream()
					.map(store -> store.getCommandsAuthorized(event, commandEvent.getCommandListener()))
					.flatMap(List::stream)
					.filter(c -> !(c instanceof DummyCommand))
					.filter(c -> !c.isHidden())
					.sorted((first, second) -> first.getCommand().compareToIgnoreCase(second.getCommand())) 
					.collect(Collectors.toList());
				
				PagedResult<ICommand> pagedResult = new PagedResult<>(commands, c -> "");
				
				pagedResult.setEntriesPerPage(1);
				pagedResult.setListIndexes(false);
				
				pagedResult.onUpdate(e -> {
					EmbedBuilder builder = e.pagedResult.getEmbedBuilder();
					builder.clearFields();
					
					ICommand command = e.pagedResult.getCurrentPageEntries().get(0);
					
					getHelp(commandEvent, command, builder);
				});
				
				pagedResult.setPage(commands.size());
				pagedResult.setPage(1);
				
				PagedManager.addPagedResult(event, pagedResult);
			}else{
				List<ICommand> commands = commandEvent.getCommandListener().getCommandStores()
					.stream()
					.map(store -> store.getCommandsAuthorized(event, commandEvent.getCommandListener()))
					.flatMap(List::stream)
					.filter(c -> !(c instanceof DummyCommand))
					.filter(c -> {						
						if(c.isCaseSensitive()) {
							if(c.getCommand().contains(commandRaw)) {
								return true;
							}
						}else{
							if(c.getCommand().toLowerCase().contains(commandRaw.toLowerCase())) {
								return true;
							}
						}
						
						for(String alias : c.getAliases()) {
							if(c.isCaseSensitive()) {
								if(alias.toLowerCase().contains(commandRaw)) {
									return true;
								}
							}else{
								if(alias.contains(commandRaw)) {
									return true;
								}
							}
						}
						
						return false;
					})
					.filter(c -> !c.isHidden())
					.sorted((first, second) -> first.getCommand().compareToIgnoreCase(second.getCommand())) 
					.collect(Collectors.toList());
					
				if(commands.size() > 0) {
					if(commands.size() == 1) {
						EmbedBuilder builder = new EmbedBuilder();
						
						getHelp(commandEvent, commands.get(0), builder);
						
						event.getChannel().sendMessage(builder.build()).queue();
						
						return;
					}
					
					PagedResult<ICommand> pagedResult = new PagedResult<>(commands, c -> c.getUsage(), e -> {
						EmbedBuilder builder = new EmbedBuilder();
						
						getHelp(commandEvent, e.entry, builder);
						
						event.getChannel().sendMessage(builder.build()).queue();
					});
					
					PagedManager.addPagedResult(event, pagedResult);
				}else{
					event.getChannel().sendMessage("No command found").queue();
				}
			}
		}else{
			List<ICommand> commands = commandEvent.getCommandListener().getCommandStores()
				.stream()
				.map(store -> store.getCommandsAuthorized(event, commandEvent.getCommandListener()))
				.flatMap(List::stream)
				.filter(c -> !(c instanceof DummyCommand))
				.filter(c -> !c.isHidden())
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
			/* Maybe? pagedResult.setTimeout(3, TimeUnit.MINUTES); */
			
			/* Not mobile friendly
			StringBuilder stringBuilder = new StringBuilder();
			for(int i = 0; i < commands.size(); i++) {
				stringBuilder.append("**").append(commands.get(i).getCommand()).append("**");
				
				String description = commands.get(i).getDescription();
				if(description != null && description.length() > 0) {
					stringBuilder.append(" - *").append(description).append("*");
				}
				
				if(i != commands.size() - 1) {
					stringBuilder.append("\n");
				}
			}
			
			PagedResultText pagedResult = new PagedResultText(stringBuilder.toString());
			*/
			
			PagedManager.addPagedResult(event, pagedResult);
		}
	}
	
	private static void getHelp(CommandEvent event, ICommand command, EmbedBuilder builder) {
		builder.addField("Command", command.getCommand(), true);
		
		builder.addField("Usage", command.getUsage(event.getPrefix()), true);
		
		if(command.getAliases().length > 0) {
			builder.addField("Aliases", Arrays.toString(command.getAliases()).replace("[", "").replace("]", ""), false);
		}
		
		if(command.getDescription() != null && command.getDescription().length() > 0) {
			builder.addField("Description", command.getDescription(), false);
		}
		
		builder.setFooter("\"*\" means required. \"[]\" means multiple arguments of that type.", null);
	}
}