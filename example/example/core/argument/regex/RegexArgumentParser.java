package example.core.argument.regex;

import java.util.regex.Matcher;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.parser.IArgumentParser;
import com.jockie.bot.core.argument.parser.ParsedArgument;
import com.jockie.bot.core.command.parser.ParseContext;

public class RegexArgumentParser implements IArgumentParser<String> {
	
	public static final RegexArgumentParser INSTANCE = new RegexArgumentParser();
	
	private RegexArgumentParser() {}
	
	public ParsedArgument<String> parse(ParseContext context, IArgument<String> argument, String content) {
		if(!(argument instanceof RegexArgument)) {
			throw new IllegalStateException("This parser can only be used for RegexArguments");
		}
		
		RegexArgument regexArgument = (RegexArgument) argument;
		
		Matcher mathcer = regexArgument.getPattern().matcher(content);
		if(mathcer.matches()) {
			return new ParsedArgument<>(content);
		}
		
		return new ParsedArgument<>();
	}
}