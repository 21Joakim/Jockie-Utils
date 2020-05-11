package com.jockie.bot.core.command.impl;

import java.util.Comparator;
import java.util.List;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.IEndlessArgument;
import com.jockie.bot.core.command.CommandTrigger;
import com.jockie.bot.core.command.ICommand;

/* The more specific the command is (more arguments, for instance) the sooner it will be returned */
public class CommandTriggerComparator implements Comparator<CommandTrigger> {
	
	public static final CommandTriggerComparator INSTANCE = new CommandTriggerComparator();
	
	private CommandTriggerComparator() {}
	
	@Override
	public int compare(CommandTrigger commandTrigger, CommandTrigger commandTrigger2) {
		ICommand command = commandTrigger.getCommand(), command2 = commandTrigger2.getCommand();
		
		/* Check the trigger length, the longer the more specific so it goes first */
		if(commandTrigger.getTrigger().length() > commandTrigger2.getTrigger().length()) {
			return -1;
		}else if(commandTrigger.getTrigger().length() < commandTrigger2.getTrigger().length()) {
			return 1;
		}
		
		List<IArgument<?>> arguments = command.getArguments(), arguments2 = command2.getArguments();
		int argumentCount = arguments.size(), argumentCount2 = arguments2.size();
		
		if(argumentCount > 0 && argumentCount2 > 0) {
			IArgument<?> lastArgument = arguments.get(argumentCount - 1), lastArgument2 = arguments2.get(argumentCount2 - 1);
			
			boolean endless = false, endless2 = false;
			boolean endlessArguments = false, endlessArguments2 = false;
			
			/* Update argument count and check for endless arguments */
			if(lastArgument.isEndless()) {
				if(lastArgument instanceof IEndlessArgument) {
					int max = ((IEndlessArgument<?>) lastArgument).getMaxArguments();
					
					if(max != -1) {
						argumentCount += (max - 1);
					}else{
						endlessArguments = true;
					}
				}
				
				endless = true;
			}
			
			/* Update argument count and check for endless arguments */
			if(lastArgument2.isEndless()) {
				if(lastArgument2 instanceof IEndlessArgument) {
					int max = ((IEndlessArgument<?>) lastArgument2).getMaxArguments();
					
					if(max != -1) {
						argumentCount2 += (max - 1);
					}else{
						endlessArguments2 = true;
					}
				}
				
				endless2 = true;
			}
			
			/* Check if the last argument contains an endless amount of arguments */
			if(!endlessArguments && endlessArguments2) {
				return -1;
			}else if(endlessArguments && !endlessArguments2) {
				return 1;
			}
			
			/**
			 * Check how many arguments the command has, the more arguments the more specific it is
			 * and should therefore be closer.
			 */
			if(argumentCount > argumentCount2) {
				return -1;
			}else if(argumentCount < argumentCount2) {
				return 1;
			}
			
			/* 
			 * Check if the last argument is endless, if it is it will simply accept all the remaining content 
			 * which means it is less specific and should therefore be further back.
			 */
			if(!endless && endless2) {
				return -1;
			}else if(endless && !endless2) {
				return 1;
			}
			
			/* 
			 * Check the order of the argument parsers. This was mostly introduced to combat an issue where
			 * due to the fact that Class#getDeclaredMethods doesn't return the methods in the order they were
			 * specified in, let alone any order at all, the order of the commands would sometimes be different
			 * and could cause weird behaviour.
			 * 
			 * One example of a weird behaviour is if you have a command, "prune", which takes one argument, amount (Integer),
			 * and then you have an alternate command implementation which takes a keyword argument of the type String.
			 * Now if the first method loads first everything works correctly as the argument can be checked if it is an integer 
			 * and then move to the String variant.
			 * If the second version is instead loaded first it will always take it as a keyword as the String parser just accepts any content given to it
			 * and this causes the first version to become effectively inaccessible. 
			 */
			if(argumentCount == argumentCount2) {
				for(int i = 0; i < argumentCount; i++) {
					IArgument<?> argument;
					if(arguments.size() > i) {
						argument = arguments.get(i);
					}else{
						argument = arguments.get(arguments.size() - 1);
					}
					
					IArgument<?> argument2;
					if(arguments2.size() > i) {
						argument2 = arguments2.get(i);
					}else{
						argument2 = arguments2.get(arguments2.size() - 1);
					}
					
					int parsePriority = argument.getParser().getPriority();
					int parsePriority2 = argument2.getParser().getPriority();
					
					if(parsePriority != parsePriority2) {
						if(parsePriority > parsePriority2) {
							return 1;
						}else if(parsePriority < parsePriority2) {
							return -1;
						}
					}
				}
			}
		}else if(argumentCount == 0 && argumentCount2 > 0) {
			return 1;
		}else if(argumentCount > 0 && argumentCount2 == 0) {
			return -1;
		}
		
		/* 
		 * Check for case sensitivity, if it is case sensitive it is more specific and therefore goes first.
		 * 
		 * This could be useful if you, for instance, had a command called "ban" and then a case sensitive command called "Ban" 
		 * to fake ban people.
		 */
		if(command.isCaseSensitive() && !command2.isCaseSensitive()) {
			return -1;
		}else if(!command.isCaseSensitive() && command2.isCaseSensitive()) {
			return 1;
		}
		
		/* 
		 * TODO: This is hacky solution to fix the problem with inconsistent order of optional arguments.
		 * 
		 * This works by sorting it by the "distance" the arguments is from their original position, for instance
		 * let's say the arguments are [optional, optional2 and optional3] and this DummyCommand has optional3 as its argument,
		 * that would mean that the distance is 2 (2 - 0).
		 * 
		 * Why 2 - 0?
		 * This is because optional3 is at index 2 for the original arguments and index 0 for the DummyCommand.
		 * 
		 * This works, unsure of how well it works but it has been tested with
		 * [optional, optional2, optional3, optional4] and [optional, required, optional2, required2]
		 */
		if(command instanceof DummyCommand && command2 instanceof DummyCommand) {
			ICommand actualCommand = ((DummyCommand) command).getActualCommand();
			ICommand actualCommand2 = ((DummyCommand) command2).getActualCommand();
			
			if(actualCommand.equals(actualCommand2)) {
				List<IArgument<?>> actualArguments = actualCommand.getArguments();
				
				int distance = 0, distance2 = 0;
				for(int i = 0; i < arguments.size(); i++) {
					distance += actualArguments.indexOf(arguments.get(i)) - i;
				}
				
				for(int i = 0; i < arguments2.size(); i++) {
					distance2 += actualArguments.indexOf(arguments2.get(i)) - i;
				}
				
				return Integer.compare(distance, distance2);
			}
		}
		
		return 0;
	}
}