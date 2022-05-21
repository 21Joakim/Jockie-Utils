package example;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jockie.bot.core.argument.factory.impl.ArgumentFactory;
import com.jockie.bot.core.argument.factory.impl.ArgumentFactoryImpl;
import com.jockie.bot.core.category.ICategory;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.ICommand.ArgumentParsingType;
import com.jockie.bot.core.command.factory.ICommandEventFactory;
import com.jockie.bot.core.command.factory.IMethodCommandFactory;
import com.jockie.bot.core.command.factory.impl.MethodCommandFactory;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandListener;
import com.jockie.bot.core.command.impl.CommandStore;
import com.jockie.bot.core.command.manager.impl.ContextManagerFactory;
import com.jockie.bot.core.parser.ParsedResult;

import example.core.argument.number.Max;
import example.core.argument.number.Min;
import example.core.argument.regex.Regex;
import example.core.command.ExtendedCommand;
import example.core.command.ExtendedCommandEvent;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class Main {
	
	public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE;
	
	public static void registerCustomParsers(ArgumentFactoryImpl argumentFactory) {
		/* A regex hex pattern to match hex colours */
		Pattern hexPattern = Pattern.compile("(#|)(([0-9]|(?i)[A-F]){6})");
		
		/* Register the class Color so that it can be used as an argument */
		argumentFactory.registerParser(Color.class, (event, argument, value) -> {
			Matcher matcher = hexPattern.matcher(value);
			if(matcher.matches()) {
				return new ParsedResult<>(true, Color.decode("#" + matcher.group(2)));
			}
			
			return new ParsedResult<>(false, null);
		});
		
		/* Register the class URL so that it can be used as an argument */
		argumentFactory.registerParser(URL.class, (event, argument, value) -> {
			try {
				/* 
				 * Preferably you would add an extra check to not allow for local files (server files) to be used,
				 * such as file:///C:/Users/Joakim/Desktop/my%20nudes.png 
				 */
				return new ParsedResult<>(true, new URL(value));
			}catch(MalformedURLException e) {
				return new ParsedResult<>(false, null);
			}
		});
	}
	
	public static void registerRegexAnnotation(ArgumentFactoryImpl argumentFactory) {
		argumentFactory.addGenericParserBefore(Object.class, (context, argument, value) -> {
			Pattern pattern = argument.getProperty("pattern");
			if(pattern != null && !pattern.matcher(value).matches()) {
				return new ParsedResult<>(false, value);
			}
			
			return new ParsedResult<>(true, value);
		});
		
		/* 
		 * You would use this by, for instance, doing 
		 * "@Argument("name") @Regex("[a-z0-9]+") String name"
		 */
		argumentFactory.addGenericBuilderConfigureFunction(Object.class, (parameter, builder) -> {
			Regex regex = parameter.getAnnotation(Regex.class);
			if(regex != null && !regex.value().isEmpty()) {
				builder.setProperty("pattern", Pattern.compile(regex.value()));
			}
			
			return builder;
		});
	}
	
	public static void registerMinMaxAnnotation(ArgumentFactoryImpl argumentFactory) {
		argumentFactory.addParserAfter(Long.class, (context, argument, value) -> {
			Long max = argument.getProperty("max");
			if(max != null) {
				value = Math.min(max, value);
			}
			
			Long min = argument.getProperty("min");
			if(min != null) {
				value = Math.max(min, value);
			}
			
			return new ParsedResult<>(value);
		});
		
		/* 
		 * You would use this by, for instance, doing 
		 * "@Argument("count") @Max(100) long count"
		 */
		argumentFactory.addBuilderConfigureFunction(Long.class, (parameter, builder) -> {
			Max max = parameter.getAnnotation(Max.class);
			if(max != null) {
				builder.setProperty("max", max.value());
			}
			
			Min min = parameter.getAnnotation(Min.class);
			if(min != null) {
				builder.setProperty("min", min.value());
			}
			
			return builder;
		});
	}
	
	public static void main(String[] args) throws Exception {
		/* Load the token from a file */
		String token;
		try(FileInputStream stream = new FileInputStream(new File("./example.token"))) {
			token = new String(stream.readAllBytes());
		}
		
		/* 
		 * Get the default argument factory
		 * NOTE: You do not have to cast this to register parsers
		 * but for stuff like the builder function which are not in
		 * the IArgumentParser interface it is required
		 */
		ArgumentFactoryImpl argumentFactory = (ArgumentFactoryImpl) ArgumentFactory.getDefault();
		
		/* Register custom argument components */
		Main.registerCustomParsers(argumentFactory);
		Main.registerRegexAnnotation(argumentFactory);
		Main.registerMinMaxAnnotation(argumentFactory);
		
		/* Set the default exception handler to make sure you don't miss out on them exception */
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
			throwable.printStackTrace();
		});
		
		/* 
		 * Set the default MethodCommandFactory, this is used when creating commands
		 * from methods, so the commands which are specified by @Command would
		 * most likely go through this factory.
		 * 
		 * NOTE: This needs to be registered before you load any commands (Like CommandStore.of)
		 */
		MethodCommandFactory.setDefault(new IMethodCommandFactory<>() {
			@Override
			public ExtendedCommand create(Method method, String name, Object invoker) {
				return new ExtendedCommand(IMethodCommandFactory.getName(name, method), method, invoker);
			}
		});
		
		/* 
		 * Register contexts for commands, for instance, if a command has
		 * "@Context ICategory category" as a parameter it will call this
		 * function and get the command category which will then be in
		 * the parameter, this example may not be very helpful as you
		 * usually don't need access to the category but you get the point!
		 */
		ContextManagerFactory.getDefault()
			.registerContext(ICategory.class, (event, type) -> event.getCommand().getCategory());
		
		/* Create the CommandListener */
		CommandListener listener = new CommandListener()
			/* Add the developers of this bot, */
			.addDevelopers(190551803669118976L)
			/* Set the default prefix of this bot */
			.setDefaultPrefixes("!");
		
		/*
		 * Set the CommandEvent factory, this can be used to have the parser
		 * create your custom CommandEvent class with cool methods
		 */
		listener.setCommandEventFactory(new ICommandEventFactory() {
			@Override
			public CommandEvent create(Message message, CommandListener listener, ICommand command, Object[] arguments,
					String[] rawArguments, String prefix, String commandTrigger, Map<String, Object> options,
					ArgumentParsingType parsingType, String contentOverflow, long timeStarted) {
				
				return new ExtendedCommandEvent(message, listener, command, arguments, rawArguments, prefix, commandTrigger, options, 
					parsingType, contentOverflow, timeStarted);
			}
		});
		
		/*
		 * If you want to use ExtendedCommandEvent instead of CommandEvent
		 * you must use this too. This will allow you to replace CommandEvent
		 * with ExtendedCommandEvent in command method.
		 * 
		 * This would not be required if you only wanted to change the
		 * behaviour of the methods in CommandEvent.
		 */
		ContextManagerFactory.getDefault()
			.registerContext(ExtendedCommandEvent.class, (event, type) -> (ExtendedCommandEvent) event)
			.setEnforcedContext(ExtendedCommandEvent.class, true);
		
		/* Register all the commands by the package example.command */
		listener.addCommandStores(CommandStore.of("example.command"));
		
		/* 
		 * Register error responses for when an argument fails to parse the content correctly,
		 * for instance, if I were to execute a command, let's say ban, and the user does not exist
		 * "!ban hello" it would then reply with "@Joakim, `hello` is not a valid user"
		 */
		listener.getErrorManager()
			.registerResponse(User.class, (argument, message, content) -> {
				return String.format("%s, `%s` is not a valid user", message.getAuthor().getAsMention(), content);
			})
			.registerResponse(Member.class, (argument, message, content) -> {
				return String.format("%s, `%s` is not a valid member", message.getAuthor().getAsMention(), content);
			});
		
		/* 
		 * Register return handlers for when something is returned from a command,
		 * for instance, if a command returned any type of object it would now 
		 * convert it to a string and send it
		 */
		listener.getReturnManager()
			.registerHandler(Object.class, (event, object) -> event.reply(object.toString()).queue())
			/* 
			 * This makes it so anything which extends Object (everything)
			 * will be handled as an Object, unless their type explicitly 
			 * registered
			 */
			.setHandleInheritance(Object.class, true);
		
		
		/* Create the JDA instance */
		JDABuilder.createDefault(token)
			/* Register the CommandListener, this is an important step */
			.addEventListeners(listener)
			.build()
			.awaitReady();
	}
}