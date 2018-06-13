package com.jockie.bot.core.command.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.argument.IArgument;
import com.jockie.bot.core.command.argument.IEndlessArgument;
import com.jockie.bot.core.utility.LoaderUtility;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandStore {
	
	public static CommandStore of(String packagePath) {
		return new CommandStore().loadFrom(packagePath);
	}
	
	private static List<ICommand> sortCommands(List<ICommand> commands) {
		return commands.stream().sorted(new Comparator<ICommand>() {
			public int compare(ICommand command, ICommand command2) {
				if(command.getCommand().length() > command2.getCommand().length()) {
					return -1;
				}else if(command.getCommand().length() < command2.getCommand().length()) {
					return 1;
				}
				
				int arguments = command.getArguments().length, arguments2 = command2.getArguments().length;
				
				if(arguments > 0 && arguments2 > 0) {
					IArgument<?> lastArgument = command.getArguments()[arguments - 1], lastArgument2 = command2.getArguments()[arguments2 - 1];
					
					if(lastArgument.isEndless()) {
						if(lastArgument instanceof IEndlessArgument<?>) {
							int max = ((IEndlessArgument<?>) lastArgument).getMaxArguments();
							
							if(max != -1) {
								arguments += (max - 1);
							}else{
								arguments = -1;
							}
						}else{
							arguments = -1;
						}
					}
					
					if(lastArgument2.isEndless()) {
						if(lastArgument2 instanceof IEndlessArgument<?>) {
							int max = ((IEndlessArgument<?>) lastArgument2).getMaxArguments();
							
							if(max != -1) {
								arguments2 += (max - 1);
							}else{
								arguments2 = -1;
							}
						}else{
							arguments2 = -1;
						}
					}
					
					if(arguments == -1 && arguments2 == -1) {
						return 0;
					}else if(arguments == -1 && arguments2 != -1) {
						return 1;
					}else if(arguments != -1 && arguments == -1) {
						return -1;
					}
					
					if(arguments > arguments2) {
						return 1;
					}else if(arguments < arguments2) {
						return -1;
					}
				}else if(arguments == 0 && arguments2 > 0) {
					return -1;
				}else if(arguments > 0 && arguments2 == 0) {
					return 1;
				}
				
				return 0;
			}
		}).collect(Collectors.toList());
	}
	
	private List<ICommand> commands = new ArrayList<ICommand>();
	
	public CommandStore loadFrom(String packagePath) {
		return this.loadFrom(packagePath, true);
	}
	
	public CommandStore loadFrom(String packagePath, boolean subPackages) {
		List<ICommand> commands = LoaderUtility.loadFrom(packagePath, ICommand.class);
		
		this.addCommands(commands);
		this.commands = CommandStore.sortCommands(this.commands);
		
		return this;
	}
	
	public CommandStore addCommands(ICommand... commands) {
		for(ICommand command : commands) {
			if(!this.commands.contains(command)) {
				if(!(command instanceof DummyCommand)) {
					List<IArgument<?>> arguments = new ArrayList<>();
					if(command.getArguments().length > 0) {
						for(int i = 0; i < command.getArguments().length; i++) {
							IArgument<?> argument = command.getArguments()[i];
							if(argument.hasDefault()) {
								arguments.add(argument);
							}
						}
						
						if(arguments.size() > 0) {
							List<IArgument<?>> args = new ArrayList<>();
					    	for(int i = 1, max = 1 << arguments.size(); i < max; ++i) {
					    	    for(int j = 0, k = 1; j < arguments.size(); ++j, k <<= 1) {
					    	        if((k & i) != 0) {
					    	        	args.add(arguments.get(j));
					    	        }
					    	    }
					    	    
					    	    DummyCommand dummy = new DummyCommand(command, args.toArray(new IArgument[0]));
								this.commands.add(dummy);
								
								args.clear();
					    	}
						}
					}
				}
				
				this.commands.add(command);
			}
		}
		
		this.commands = CommandStore.sortCommands(this.commands);
		
		return this;
	}
	
	public CommandStore addCommands(Collection<ICommand> commands)  {
		return this.addCommands(commands.toArray(new ICommand[0]));
	}
	
	public CommandStore addCommands(Category category) {
		return this.addCommands(category.addCommandStores(this).getCommands());
	}
	
	public CommandStore removeCommands(ICommand... commands) {
		for(ICommand command : commands) {
			if(this.commands.contains(command)) {
				this.commands.remove(command);
				
				for(int i = 0; i < this.commands.size(); i++) {
					if(this.commands.get(i) instanceof DummyCommand) {
						if(((DummyCommand) this.commands.get(i)).getDummiedCommand().equals(command)) {
							this.commands.remove(i--);
						}
					}
				}
			}
		}
		
		return this;
	}
	
	public CommandStore removeCommands(Collection<ICommand> commands) {
		return this.removeCommands(commands.toArray(new ICommand[0]));
	}
	
	public CommandStore removeCommands(Category category) {
		return this.removeCommands(category.removeCommandStores(this).getCommands());
	}
	
	public List<ICommand> getCommands() {
		return Collections.unmodifiableList(this.commands);
	}
	
	public List<ICommand> getCommandsAuthorized(MessageReceivedEvent event, CommandListener commandListener) {
		return Collections.unmodifiableList(this.commands.stream().filter(c -> c.verify(event, commandListener)).collect(Collectors.toList()));
	}
}