package com.jockie.bot.core.command.parser.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.IEndlessArgument;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.ICommand.ArgumentParsingType;
import com.jockie.bot.core.command.ICommand.ArgumentTrimType;
import com.jockie.bot.core.command.ICommand.ContentOverflowPolicy;
import com.jockie.bot.core.command.exception.parser.ArgumentParseException;
import com.jockie.bot.core.command.exception.parser.ContentOverflowException;
import com.jockie.bot.core.command.exception.parser.DuplicateOptionException;
import com.jockie.bot.core.command.exception.parser.InvalidArgumentCountException;
import com.jockie.bot.core.command.exception.parser.MissingRequiredArgumentException;
import com.jockie.bot.core.command.exception.parser.OptionParseException;
import com.jockie.bot.core.command.exception.parser.OutOfContentException;
import com.jockie.bot.core.command.exception.parser.ParseException;
import com.jockie.bot.core.command.exception.parser.PassiveCommandException;
import com.jockie.bot.core.command.exception.parser.UnknownOptionException;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandListener;
import com.jockie.bot.core.command.parser.ICommandParser;
import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.option.IOption;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.utility.StringUtility;
import com.jockie.bot.core.utility.StringUtility.QuoteCharacter;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.internal.utils.Checks;

public class CommandParserImpl implements ICommandParser {
	
	/**
	 * A bunch of different quote characters, these were gotten from the 
	 * source code of <a href="https://github.com/Rapptz/discord.py/blob/fc5a2936dd9456f1489dc1125c12448a2af23e15/discord/ext/commands/view.py#L30-L48">discord.py</a> 
	 * as they already had a list of quotes
	 */
	public static final List<QuoteCharacter> DEFAULT_QUOTE_CHARACTERS;
	
	static {
		List<QuoteCharacter> defaultQuoteCharacters = new ArrayList<>();
		defaultQuoteCharacters.add(new QuoteCharacter('"'));
		defaultQuoteCharacters.add(new QuoteCharacter('‘', '’'));
		defaultQuoteCharacters.add(new QuoteCharacter('‚', '‛'));
		defaultQuoteCharacters.add(new QuoteCharacter('“', '”'));
		defaultQuoteCharacters.add(new QuoteCharacter('„', '‟'));
		defaultQuoteCharacters.add(new QuoteCharacter('「', '」'));
		defaultQuoteCharacters.add(new QuoteCharacter('『', '』'));
		defaultQuoteCharacters.add(new QuoteCharacter('〝', '〞'));
		defaultQuoteCharacters.add(new QuoteCharacter('﹁', '﹂'));
		defaultQuoteCharacters.add(new QuoteCharacter('﹃', '﹄'));
		defaultQuoteCharacters.add(new QuoteCharacter('＂'));
		defaultQuoteCharacters.add(new QuoteCharacter('｢', '｣'));
		defaultQuoteCharacters.add(new QuoteCharacter('«', '»'));
		defaultQuoteCharacters.add(new QuoteCharacter('‹', '›'));
		defaultQuoteCharacters.add(new QuoteCharacter('《', '》'));
		defaultQuoteCharacters.add(new QuoteCharacter('〈', '〉'));
		
		DEFAULT_QUOTE_CHARACTERS = Collections.unmodifiableList(defaultQuoteCharacters);
	}
	
	public static final List<String> DEFAULT_OPTION_PREFIXES;
	
	static {
		List<String> defaultOptionPrefixes = new ArrayList<>();
		defaultOptionPrefixes.add("--");
		/* 
		 * "--" automatically gets converted to "—" on apple devices,
		 * or something like that, I don't have one myself so can't
		 * confirm.
		 */
		defaultOptionPrefixes.add("—");
		
		DEFAULT_OPTION_PREFIXES = Collections.unmodifiableList(defaultOptionPrefixes);
	}
	
	protected Set<QuoteCharacter> quoteCharacters = new LinkedHashSet<>();
	protected Set<String> optionPrefixes = new LinkedHashSet<>();
	
	public CommandParserImpl() {
		DEFAULT_QUOTE_CHARACTERS.forEach(this::addQuoteCharacter);
		DEFAULT_OPTION_PREFIXES.forEach(this::addOptionPrefix);
	}
	
	/**
	 * @param prefixes the option prefixes to allow
	 * 
	 * @return the {@link CommandParserImpl} instance, useful for chaining
	 */
	@Nonnull
	public CommandParserImpl setOptionPrefixes(@Nonnull Collection<String> prefixes) {
		Checks.noneNull(prefixes, "prefixes");
		
		this.optionPrefixes = new LinkedHashSet<>(prefixes);
		
		return this;
	}
	
	/**
	 * @param prefix the option prefix to allow
	 * 
	 * @return the {@link CommandParserImpl} instance, useful for chaining
	 */
	@Nonnull
	public CommandParserImpl addOptionPrefix(@Nonnull String prefix) {
		Checks.notNull(prefix, "prefix");
		
		this.optionPrefixes.add(prefix);
		
		return this;
	}
	
	/**
	 * @param prefix the allowed option prefix to remove
	 * 
	 * @return the {@link CommandParserImpl} instance, useful for chaining
	 */
	@Nonnull
	public CommandParserImpl removeOptionPrefix(@Nullable String prefix) {
		this.optionPrefixes.remove(prefix);
		
		return this;
	}
	
	/**
	 * @return the allowed option prefixes, these will be used to
	 * determine whether a sequence of characters is an option or not
	 */
	@Nonnull
	public Set<String> getOptionPrefixes() {
		return Collections.unmodifiableSet(this.optionPrefixes);
	}
	
	/**
	 * Set the characters which should be allowed to be used as quotes.
	 * 
	 * @param quoteCharacters the characters which are allowed to be used as quotes
	 * 
	 * @return the {@link CommandParserImpl} instance, useful for chaining
	 */
	@Nonnull
	public CommandParserImpl setQuoteCharacters(@Nonnull Collection<QuoteCharacter> quoteCharacters) {
		Checks.noneNull(quoteCharacters, "quoteCharacters");
		
		this.quoteCharacters = new LinkedHashSet<>(quoteCharacters);
		
		return this;
	}
	
	/**
	 * @param character the quote character to allow
	 * 
	 * @return the {@link CommandParserImpl} instance, useful for chaining
	 */
	@Nonnull
	public CommandParserImpl addQuoteCharacter(char character) {
		return this.addQuoteCharacter(character, character);
	}
	
	/**
	 * @param start the character used to start the quote
	 * @param end the character used to end the quote
	 * 
	 * @return the {@link CommandParserImpl} instance, useful for chaining
	 */
	@Nonnull
	public CommandParserImpl addQuoteCharacter(char start, char end) {
		return this.addQuoteCharacter(new QuoteCharacter(start, end));
	}
	
	/**
	 * @param quoteCharacter the quote character to allow
	 * 
	 * @return the {@link CommandParserImpl} instance, useful for chaining
	 */
	@Nonnull
	public CommandParserImpl addQuoteCharacter(@Nonnull QuoteCharacter quoteCharacter) {
		Checks.notNull(quoteCharacter, "quote");
		
		this.quoteCharacters.add(quoteCharacter);
		
		return this;
	}
	
	/**
	 * @param character the allowed quote character to remove
	 * 
	 * @return the {@link CommandParserImpl} instance, useful for chaining
	 */
	@Nonnull
	public CommandParserImpl removeQuoteCharacter(char character) {
		return this.removeQuoteCharacter(character, character);
	}
	
	/**
	 * @param start the character used to start the quote
	 * @param end the character used to end the quote
	 * 
	 * @return the {@link CommandParserImpl} instance, useful for chaining
	 */
	@Nonnull
	public CommandParserImpl removeQuoteCharacter(char start, char end) {
		this.quoteCharacters.remove(new QuoteCharacter(start, end));
		
		return this;
	}
	
	/**
	 * @param quoteCharacter the allowed quote character to remove
	 * 
	 * @return the {@link CommandParserImpl} instance, useful for chaining
	 */
	@Nonnull
	public CommandParserImpl removeQuoteCharacter(@Nullable QuoteCharacter quoteCharacter) {
		this.quoteCharacters.remove(quoteCharacter);
		
		return this;
	}
	
	/**
	 * @return the characters which are allowed to be used as quotes
	 */
	@Nonnull
	public Set<QuoteCharacter> getQuoteCharacters() {
		return Collections.unmodifiableSet(this.quoteCharacters);
	}
	
	/**
	 * @return a map of all the options which can be used by the author of the message
	 */
	@Nonnull
	protected Map<String, IOption<?>> getValidOptions(@Nonnull CommandListener listener, @Nonnull ICommand command, @Nonnull Message message) {
		boolean developer = listener.isDeveloper(message.getAuthor());
		
		Map<String, IOption<?>> validOptions = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		for(IOption<?> option : command.getOptions()) {
			if(option.isDeveloper() && !developer) {
				continue;
			}
			
			validOptions.put(option.getName(), option);
			for(String alias : option.getAliases()) {
				validOptions.put(alias, option);
			}
		}
		
		return validOptions;
	}
	
	private String findOptionPrefix(String messageContent, int index) {
		if(messageContent.charAt(index) != ' ') {
			return null;
		}
		
		for(String prefix : this.optionPrefixes) {
			int characterAfter = index + prefix.length() + 1;
			if(characterAfter >= messageContent.length()) {
				continue;
			}
			
			if(messageContent.startsWith(prefix, index + 1) && messageContent.charAt(characterAfter) != ' ') {
				return prefix;
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private void combineOption(Map<String, Object> options, String option, Object value) {
		Object previousValue = options.get(option);
		
		List<Object> values;
		if(previousValue instanceof List) {
			values = (List<Object>) previousValue;
		}else{
			values = new ArrayList<>();
			values.add(previousValue);
		}
		
		values.add(value);
		options.put(option, values);
	}
	
	/**
	 * @return a map of all the parsed options and the their values
	 */
	@Nonnull
	protected Map<String, Object> parseOptions(@Nonnull ParseContext context, @Nonnull CommandListener listener, @Nonnull ICommand command, @Nonnull Message message, @Nonnull String messageContent, @Nonnull StringBuilder builder) throws ParseException {
		Map<String, IOption<?>> validOptions = this.getValidOptions(listener, command, message);
		Map<String, Object> options = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		
		for(int i = 0; i < messageContent.length(); i++) {			
			String prefix = this.findOptionPrefix(messageContent, i);
			if(prefix == null) {
				builder.append(messageContent.charAt(i));
				
				continue;
			}
			
			String content = messageContent.substring(i + prefix.length() + 1);
			
			String stringOption = null;
			String stringValue = null;
			
			int spaceIndex = content.indexOf(' ');
			int equalIndex = content.indexOf('=');
			int length = prefix.length();
			
			if(equalIndex != -1 && (spaceIndex == -1 || equalIndex < spaceIndex)) {
				String optionContent = content.substring(0, equalIndex);
				String valueContent = content.substring(equalIndex + 1, content.length());
				
				String temp = null;
				for(QuoteCharacter quote : this.quoteCharacters) {
					temp = StringUtility.parseWrapped(valueContent, quote.start, quote.end);
					if(temp != null) {
						length += equalIndex + 1 + temp.length();
						
						stringOption = optionContent;
						stringValue = StringUtility.unwrap(temp, quote.start, quote.end);
						
						break;
					}
				}
			}
			
			if(stringOption == null) {
				content = content.substring(0, spaceIndex != -1 ? spaceIndex : content.length());
				length += content.length();
				
				equalIndex = content.indexOf('=');
				
				if(equalIndex != -1) {
					stringValue = content.substring(equalIndex + 1);
					stringOption = content.substring(0, equalIndex);
				}else{
					stringOption = content;
				}
			}
			
			IOption<?> option = validOptions.get(stringOption);
			Object value = null;
			
			PARSE_OPTION:
			if(option != null) {
				Class<?> optionType = option.getType();
				if(optionType.equals(boolean.class) || optionType.equals(Boolean.class)) {
					if(stringValue == null || stringValue.isEmpty()) {
						value = true;
						break PARSE_OPTION;
					}
				}
				
				/* Don't parse if no content was given */
				if(stringValue == null) {
					break PARSE_OPTION;
				}
				
				ParsedResult<?> parsedArgument = option.parse(context, stringValue);
				if(!parsedArgument.isValid()) {
					switch(command.getOptionParsingFailurePolicy()) {
						case FAIL: throw new OptionParseException(context, option, stringValue);
						case INCLUDE: {
							builder.append(messageContent.charAt(i));
							
							continue;
						}
						case IGNORE: {
							i += length;
							
							continue;
						}
					}
				}
				
				value = parsedArgument.getObject();
			}
			
			if(option == null) {
				value = stringValue;
				
				switch(command.getUnknownOptionPolicy()) {
					case ADD: break;
					case IGNORE: {
						i += length;
						
						continue;
					}
					/* The specified option does not exist */
					case FAIL: throw new UnknownOptionException(context, content);
					case INCLUDE: {
						builder.append(messageContent.charAt(i));
						
						continue;
					}
				}
			}else{
				/* 
				 * This removes the ability to determine which trigger (alias)
				 * the option was provided by, however, this is probably better
				 * for convience as you wouldn't have to check each of the option's
				 * triggers to determine whether it was provided or not
				 */
				stringOption = option.getName();
			}
			
			if(options.containsKey(stringOption)) {
				switch(command.getDuplicateOptionPolicy()) {
					case COMBINE: {
						this.combineOption(options, stringOption, value);
						
						break;
					}
					case FAIL: throw new DuplicateOptionException(context, stringOption, stringValue);
					/* Do nothing since it's already the first one */
					case USE_FIRST: break;
					case USE_LAST: {
						options.put(stringOption, value);
						
						break;
					}
				}
			}else{
				options.put(stringOption, value);
			}
			
			i += length;
		}
		
		return options;
	}
	
	@Nullable
	protected CommandEvent parseNamed(ICommand command, List<IArgument<?>> arguments, ParseContext context, String messageContent, Map<String, Object> options) throws ParseException {
		if(messageContent.isEmpty()) {
			return null;
		}
		
		/* 
		 * Handle command as key-value, this is null if it could not parse the entire
		 * string as a key-value map
		 */
		Map<String, String> map = StringUtility.asMap(messageContent, this.quoteCharacters);
		if(map == null) {
			return null;
		}
		
		Object[] parsedArguments = new Object[arguments.size()];
		String[] parsedArgumentsAsString = new String[parsedArguments.length];
		
		int argumentCount = 0;
		
		for(int i = 0; i < arguments.size(); i++) {
			IArgument<?> argument = arguments.get(i);
			
			/* Missing argument */
			if(!map.containsKey(argument.getName())) {
				throw new MissingRequiredArgumentException(context, argument);
			}
			
			String value = map.get(argument.getName());
			
			ParsedResult<?> parsedArgument = argument.parse(context, value);
			if(!parsedArgument.isValid()) {
				/* The content does not make for a valid argument */
				throw new ArgumentParseException(context, argument, value);
			}
			
			String contentLeft = parsedArgument.getContentLeft();
			if(contentLeft != null && !contentLeft.isEmpty()) {
				/* When would this happen? */
				throw new ArgumentParseException(context, argument, value);
			}
			
			parsedArguments[argumentCount] = parsedArgument.getObject();
			parsedArgumentsAsString[argumentCount] = value;
			
			argumentCount += 1;
			
			map.remove(argument.getName());
		}
		
		/* Contains keys which could not be mapped to arguments */
		if(!map.isEmpty()) {
			return null;
		}
		
		return this.createCommandEvent(context, options, parsedArguments, parsedArgumentsAsString, ArgumentParsingType.NAMED, messageContent);
	}
	
	@Nullable
	protected CommandEvent parsePositional(ICommand command, List<IArgument<?>> arguments, ParseContext context, String messageContent, Map<String, Object> options) throws ParseException {
		Object[] parsedArguments = new Object[arguments.size()];
		String[] parsedArgumentsAsString = new String[parsedArguments.length];
		
		int argumentCount = 0;
		
		for(int i = 0; i < arguments.size(); i++) {
			IArgument<?> argument = arguments.get(i);
			
			if(!messageContent.isEmpty()) {
				if(messageContent.charAt(0) == ' ') {
					ArgumentTrimType trimType = command.getArgumentTrimType();
					if(trimType != ArgumentTrimType.NONE && !(argument.isEndless() && trimType != ArgumentTrimType.STRICT)) {
						messageContent = StringUtility.stripLeading(messageContent);
					}else{
						messageContent = messageContent.substring(1);
					}
				}else{
					/* 
					 * It gets here if an argument is parsed with quotes and there is a 
					 * value directly after the quotes without any spacing, like !add "15"5
					 */
					
					/* The argument for some reason does not start with a space */
					throw new ArgumentParseException(context, argument, messageContent);
				}
			}
			
			ParsedResult<?> parsedArgument;
			String content = null;
			if(argument.getParser().isHandleAll()) {
				parsedArgument = argument.parse(context, content = messageContent);
				
				String contentLeft = parsedArgument.getContentLeft();
				if(contentLeft != null) {
					messageContent = contentLeft;
				}else{
					messageContent = "";
				}
			}else if(argument.isEndless()) {
				if(messageContent.length() == 0 && !argument.acceptEmpty()) {
					/* There is no more content and the argument does not accept no content */
					throw new OutOfContentException(context, argument);
				}
				
				parsedArgument = argument.parse(context, content = messageContent);
				messageContent = "";
			}else{
				if(!messageContent.isEmpty()) {
					/* TODO: Is this even worth having? Not quite sure if I like the implementation */
					if(argument instanceof IEndlessArgument) {
						content = StringUtility.parseWrapped(messageContent, '[', ']');
						if(content != null) {
							messageContent = messageContent.substring(content.length());
							content = StringUtility.unwrap(content, '[', ']');
							
							if(command.getArgumentTrimType() == ArgumentTrimType.STRICT) {
								content = StringUtility.strip(content);
							}
						}
					}else if(argument.acceptQuote()) {
						for(QuoteCharacter quote : this.quoteCharacters) {
							content = StringUtility.parseWrapped(messageContent, quote.start, quote.end);
							if(content != null) {
								messageContent = messageContent.substring(content.length());
								content = StringUtility.unwrap(content, quote.start, quote.end);
								
								if(command.getArgumentTrimType() == ArgumentTrimType.STRICT) {
									content = StringUtility.strip(content);
								}
								
								break;
							}
						}
					}
				}
				
				if(content == null) {
					int index = messageContent.indexOf(' ');
					content = messageContent.substring(0, index != -1 ? index : messageContent.length());
					
					messageContent = messageContent.substring(content.length());
				}
				
				/* There is no more content and the argument does not accept no content */
				if(content.isEmpty() && !argument.acceptEmpty()) {
					throw new OutOfContentException(context, argument);
				}
				
				parsedArgument = argument.parse(context, content);
			}
			
			if(parsedArgument.isValid()) {
				parsedArguments[argumentCount] = parsedArgument.getObject();
				parsedArgumentsAsString[argumentCount] = content;
				
				argumentCount += 1;
			}else{
				/* The content does not make for a valid argument */
				throw new ArgumentParseException(context, argument, content);
			}
		}
		
		/* There is more content than the arguments could handle */
		if(!messageContent.isEmpty()) {
			if(command.getContentOverflowPolicy() == ContentOverflowPolicy.FAIL) {
				throw new ContentOverflowException(context, messageContent);
			}
		}
		
		/* Not the correct amount of arguments for the command */
		if(arguments.size() != argumentCount) {
			Object[] temp = new Object[argumentCount];
			System.arraycopy(parsedArguments, 0, temp, 0, temp.length);
			
			throw new InvalidArgumentCountException(context, arguments.toArray(new IArgument<?>[0]), temp);
		}
		
		return this.createCommandEvent(context, options, parsedArguments, parsedArgumentsAsString, ArgumentParsingType.POSITIONAL, messageContent);
	}
	
	protected CommandEvent createCommandEvent(ParseContext context, Map<String, Object> options, Object[] parsedArguments, String[] parsedArgumentsAsString, ArgumentParsingType parsingType, String contentOverflow) {
		CommandListener listener = context.getCommandListener();
		Message message = context.getMessage();
		ICommand command = context.getCommand();
		String prefix = context.getPrefix();
		String trigger = context.getTrigger();
		long timeStarted = context.getTimeStarted();
		
		return listener.getCommandEventFactory()
			.create(message, listener, command, parsedArguments, parsedArgumentsAsString, prefix, trigger, options, parsingType, contentOverflow, timeStarted);
	}
	
	@Nullable
	public CommandEvent parse(CommandListener listener, ICommand command, Message message, String prefix, String trigger, String contentToParse, long timeStarted) throws ParseException {
		ParseContext context = new ParseContext(listener, this, command, message, prefix, trigger, contentToParse, timeStarted);
		
		if(command.isPassive()) {
			throw new PassiveCommandException(context);
		}
		
		String messageContent = contentToParse;
		
		/* Pre-processing */
		StringBuilder builder = new StringBuilder();
		
		Map<String, Object> options = this.parseOptions(context, listener, command, message, messageContent, builder);
		
		messageContent = builder.toString();
		/* End pre-processing */
		
		List<IArgument<?>> arguments = command.getArguments();
		
		Set<ArgumentParsingType> argumentParsingTypes = command.getAllowedArgumentParsingTypes();
		if(argumentParsingTypes.contains(ArgumentParsingType.NAMED)) {
			CommandEvent event = this.parseNamed(command, arguments, context, messageContent, options);
			if(event != null) {
				return event;
			}
		}
		
		if(argumentParsingTypes.contains(ArgumentParsingType.POSITIONAL)) {
			CommandEvent event = this.parsePositional(command, arguments, context, messageContent, options);
			if(event != null) {
				return event;
			}
		}
		
		/* If the command for some reason does not have any allowed parsing types */
		return null;
	}
}