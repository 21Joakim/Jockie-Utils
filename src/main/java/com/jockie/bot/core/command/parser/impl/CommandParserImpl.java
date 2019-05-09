package com.jockie.bot.core.command.parser.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.IEndlessArgument;
import com.jockie.bot.core.argument.parser.ParsedArgument;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.ICommand.ArgumentParsingType;
import com.jockie.bot.core.command.ICommand.ContentOverflowPolicy;
import com.jockie.bot.core.command.ICommand.InvalidOptionPolicy;
import com.jockie.bot.core.command.exception.parser.ArgumentParseException;
import com.jockie.bot.core.command.exception.parser.ContentOverflowException;
import com.jockie.bot.core.command.exception.parser.InvalidArgumentCountException;
import com.jockie.bot.core.command.exception.parser.MissingRequiredArgumentException;
import com.jockie.bot.core.command.exception.parser.OutOfContentException;
import com.jockie.bot.core.command.exception.parser.ParseException;
import com.jockie.bot.core.command.exception.parser.UnknownOptionException;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandListener;
import com.jockie.bot.core.command.parser.ICommandParser;
import com.jockie.bot.core.option.IOption;

import net.dv8tion.jda.core.entities.Message;

public class CommandParserImpl implements ICommandParser {
	
	public CommandEvent parse(CommandListener listener, ICommand command, String trigger, Message message, String prefix, String contentToParse, long timeStarted) throws ParseException {
		String messageContent = contentToParse;
		
		int argumentCount = 0;
		
		List<IArgument<?>> arguments = command.getArguments();
		
		Object[] parsedArguments = new Object[arguments.size()];
		String[] parsedArgumentsAsString = new String[parsedArguments.length];
		
		boolean developer = listener.isDeveloper(message.getAuthor());
		
		/* Creates a map of all the options which can be used by this user */
		Map<String, IOption> optionMap = new HashMap<>();
		for(IOption option : command.getOptions()) {
			if(option.isDeveloper() && !developer) {
				continue;
			}
			
			optionMap.put(option.getName(), option);
			for(String alias : option.getAliases()) {
				optionMap.put(alias, option);
			}
		}
		
		/* Pre-processing */
		StringBuilder builder = new StringBuilder();
		
		List<String> options = new ArrayList<>();
		for(int i = 0; i < messageContent.length(); i++) {
			if(messageContent.startsWith(" --", i) && messageContent.length() - i > 3 && messageContent.charAt(i + 3) != ' ') {
				String optionStr = messageContent.substring(i + 1);
				optionStr = optionStr.substring(2, (optionStr.contains(" ")) ? optionStr.indexOf(" ") : optionStr.length()).toLowerCase();
				
				IOption option = optionMap.get(optionStr);
				if(option != null) {
					options.add(optionStr);
					
					i += (optionStr.length() + 2);
					
					continue;
				}else{
					InvalidOptionPolicy optionPolicy = command.getInvalidOptionPolicy();
					if(optionPolicy.equals(InvalidOptionPolicy.ADD)) {
						options.add(optionStr);
						
						i += (optionStr.length() + 2);
						
						continue;
					}else if(optionPolicy.equals(InvalidOptionPolicy.IGNORE)) {
						i += (optionStr.length() + 2);
						
						continue;
					}else if(optionPolicy.equals(InvalidOptionPolicy.FAIL)) {
						/* The specified option does not exist */
						throw new UnknownOptionException(optionStr);
					}
				}
			}
			
			builder.append(messageContent.charAt(i));
		}
		
		messageContent = builder.toString();
		/* End pre-processing */
		
		ArgumentParsingType parsingType;
		ARGUMENT_PARSING:
		{
			List<ArgumentParsingType> argumentParsingTypes = command.getAllowedArgumentParsingTypes();
			
			if(argumentParsingTypes.contains(ArgumentParsingType.NAMED)) {
				if(messageContent.length() > 0) {
					/* Handle command as key-value */
					Map<String, String> map = this.asMap(messageContent);
					
					if(map != null) {
						for(int i = 0; i < arguments.size(); i++) {
							IArgument<?> argument = arguments.get(i);
							if(map.containsKey(argument.getName())) {
								String value = map.get(argument.getName());
								
								ParsedArgument<?> parsedArgument = argument.parse(message, value);
								if(parsedArgument.isValid() && (parsedArgument.getContentLeft() == null || parsedArgument.getContentLeft().isEmpty())) {
									parsedArguments[argumentCount] = parsedArgument.getObject();
									parsedArgumentsAsString[argumentCount] = value;
									
									argumentCount += 1;
								}else{
									/* The content does not make for a valid argument */
									throw new ArgumentParseException(argument, value);
								}
								
								map.remove(argument.getName());
							}else{
								/* Missing argument */
								throw new MissingRequiredArgumentException(argument);
							}
						}
						
						/* If it does not contain any invalid keys */
						if(map.size() == 0) {
							parsingType = ArgumentParsingType.NAMED;
							
							break ARGUMENT_PARSING;
						}
					}
				}
			}
			
			if(argumentParsingTypes.contains(ArgumentParsingType.POSITIONAL)) {
				for(int i = 0; i < parsedArguments.length; i++) {
					if(messageContent.length() > 0) {
						if(messageContent.startsWith(" ")) {
							messageContent = messageContent.substring(1);
						}else{
							/* When does it get here? */
							
							/* The argument for some reason does not start with a space */
							throw new ArgumentParseException(null, messageContent);
						}
					}
					
					IArgument<?> argument = arguments.get(i);
					
					ParsedArgument<?> parsedArgument;
					String content = null;
					if(argument.getParser().handleAll()) {
						parsedArgument = argument.parse(message, content = messageContent);
						
						if(parsedArgument.getContentLeft() != null) {
							messageContent = parsedArgument.getContentLeft();
						}else{
							messageContent = "";
						}
					}else if(argument.isEndless()) {
						if(messageContent.length() == 0 && !argument.acceptEmpty()) {
							/* There is no more content and the argument does not accept no content */
							throw new OutOfContentException(argument);
						}
						
						parsedArgument = argument.parse(message, content = messageContent);
						messageContent = "";
					}else{
						if(messageContent.length() > 0) {
							/* Is this even worth having? Not quite sure if I like the implementation */
							if(argument instanceof IEndlessArgument) {
								content = this.parseWrapped(messageContent, '[', ']');
								if(content != null) {
									messageContent = messageContent.substring(content.length());
									
									content = this.updateWrapped(content, '[', ']');
								}
							}else if(argument.acceptQuote()) {
								content = this.parseWrapped(messageContent, '"');
								if(content != null) {
									messageContent = messageContent.substring(content.length());
									
									content = this.updateWrapped(content, '"');
								}
							}
							
							if(content == null) {
								content = messageContent.substring(0, (messageContent.contains(" ")) ? messageContent.indexOf(" ") : messageContent.length());
								messageContent = messageContent.substring(content.length());
							}
						}else{
							content = "";
						}
						
						/* There is no more content and the argument does not accept no content */
						if(content.length() == 0 && !argument.acceptEmpty()) {
							throw new OutOfContentException(argument);
						}
						
						parsedArgument = argument.parse(message, content);
					}
					
					if(parsedArgument.isValid()) {
						parsedArguments[argumentCount] = parsedArgument.getObject();
						parsedArgumentsAsString[argumentCount] = content;
						
						argumentCount += 1;
					}else{
						/* The content does not make for a valid argument */
						throw new ArgumentParseException(argument, content);
					}
				}
				
				/* There is more content than the arguments could handle */
				if(messageContent.length() > 0) {
					if(command.getContentOverflowPolicy().equals(ContentOverflowPolicy.FAIL)) {
						throw new ContentOverflowException(messageContent);
					}
				}
				
				/* Not the correct amount of arguments for the command */
				if(arguments.size() != argumentCount) {
					Object[] temp = new Object[argumentCount];
					
					System.arraycopy(parsedArguments, 0, temp, 0, temp.length);
					
					throw new InvalidArgumentCountException(command.getArguments().toArray(new IArgument<?>[0]), temp);
				}
				
				parsingType = ArgumentParsingType.POSITIONAL;
				
				break ARGUMENT_PARSING;
			}
			
			/* If the command for some reason does not have any allowed parsing types */
			return null;
		}
		
		return new CommandEvent(message, listener, command, parsedArguments, parsedArgumentsAsString, prefix, trigger, options, parsingType, messageContent, timeStarted);
	}
	
	protected String updateWrapped(String wrapped, char wrapping) {
		return wrapped.substring(1, wrapped.length() - 1)
			.replace("\\" + wrapping, String.valueOf(wrapping));
	}

	protected String updateWrapped(String wrapped, char wrapStart, char wrapEnd) {
		return wrapped.substring(1, wrapped.length() - 1)
			.replace("\\" + wrapStart, String.valueOf(wrapStart))
			.replace("\\" + wrapEnd, String.valueOf(wrapEnd));
	}

	protected String parseWrapped(String wrapped, char wrapping) {
		return this.parseWrapped(wrapped, wrapping, wrapping);
	}

	protected String parseWrapped(String wrapped, char wrapStart, char wrapEnd) {
		if(wrapped.charAt(0) == wrapStart) {
			int nextWrap = 0;
			while((nextWrap = wrapped.indexOf(wrapEnd, nextWrap + 1)) != -1 && wrapped.charAt(nextWrap - 1) == '\\');
			
			if(nextWrap != -1) {
				return wrapped.substring(0, nextWrap + 1);
			}
		}
		
		return null;
	}

	/** Method used to convert a command to a map, for instance 
	 * </br><b>color=#00FFFF name="a cyan role" permissions=8</b>
	 * </br>would be parsed to a map with all the values, like this
	 * </br><b>{color="#00FFFF", name="a cyan role", permissions="8"}</b>
	 */
	protected Map<String, String> asMap(String command) {
		Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		
		String message = command;
		while(message.length() > 0) {
			int index = message.indexOf("=");
			if(index == -1) {
				return null;
			}
			
			String key = message.substring(0, index);
			message = message.substring(key.length() + 1);
			
			/* Trim to ignore any spaces between the end of the key and the = */
			key = key.trim();
			
			/* Trim to ignore any spaces between the = and the start of the value */
			message = message.trim();
			
			String value = this.parseWrapped(message, '"');
			if(value != null) {
				message = message.substring(value.length());
				value = this.updateWrapped(value, '"');
			}else{
				value = message.substring(0, (index = message.indexOf(" ")) != -1 ? index : message.length());
				message = message.substring(value.length());
			}
			
			String quotedKey = this.parseWrapped(key, '"');
			if(quotedKey != null) {
				key = this.updateWrapped(quotedKey, '"');
			}else{
				if(key.contains(" ")) {
					return null;
				}
			}
			
			map.put(key, value);
		}
		
		return map;
	}
}