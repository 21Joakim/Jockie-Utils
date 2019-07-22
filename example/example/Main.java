package example;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jockie.bot.core.argument.factory.impl.ArgumentFactory;
import com.jockie.bot.core.argument.factory.impl.ArgumentFactoryImpl;
import com.jockie.bot.core.argument.parser.ParsedArgument;
import com.jockie.bot.core.category.ICategory;
import com.jockie.bot.core.command.factory.IMethodCommandFactory;
import com.jockie.bot.core.command.factory.impl.MethodCommandFactory;
import com.jockie.bot.core.command.impl.CommandListener;
import com.jockie.bot.core.command.impl.CommandStore;
import com.jockie.bot.core.command.manager.impl.ContextManagerFactory;

import example.core.argument.regex.Regex;
import example.core.argument.regex.RegexArgument;
import example.core.argument.regex.RegexArgumentParser;
import example.core.command.ExtendedCommand;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class Main {
	
	public static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE;
	
	public static void main(String[] args) throws Exception {
		/* Load the token from a file */
		String token;
		try(FileInputStream stream = new FileInputStream(new File("./example.token"))) {
			token = new String(stream.readAllBytes());
		}
		
		/* Get the default argument factory
		 * NOTE: You do not have to cast this to register parsers
		 * but for stuff like the builder function which are not in
		 * the IArgumentParser interface it is required
		 */
		ArgumentFactoryImpl argumentFactory = (ArgumentFactoryImpl) ArgumentFactory.getDefault();
		
		/* A regex hex pattern to match hex colours */
		final Pattern HEX_PATTERN = Pattern.compile("(#|)(([0-9]|(?i)[A-F]){6})");
		
		/* Register the class Color so that it can be used as an argument */
		argumentFactory.registerParser(Color.class, (event, argument, value) -> {
			Matcher matcher = HEX_PATTERN.matcher(value);
			if(matcher.matches()) {
				return new ParsedArgument<>(true, Color.decode("#" + matcher.group(2)));
			}
			
			return new ParsedArgument<>(false, null);
		});
		
		/* Register the class URL so that it can be used as an argument */
		argumentFactory.registerParser(URL.class, (event, argument, value) -> {
			try {
				/* Preferably you would add an extra check to not allow for local files (server files) to be used,
				 * such as file:///C:/Users/Joakim/Desktop/my%20nudes.png 
				 */
				return new ParsedArgument<>(true, new URL(value));
			}catch(MalformedURLException e) {
				return new ParsedArgument<>(false, null);
			}
		});
		
		/* Register a builder function to make a custom argument,
		 * for more information check the Regex, RegexArgument 
		 * and RegexArgumentParser in example.core.argument.regex
		 * 
		 * You would use this by, for instance, doing 
		 * "@Argument("name") @Regex("[a-z0-9]+") String name"
		 */
		argumentFactory.registerBuilderFunction((parameter) -> {
			if(parameter.getType().equals(String.class)) {
				Regex regex = parameter.getAnnotation(Regex.class);
				if(regex != null && !regex.value().isEmpty()) {
					return new RegexArgument.Builder()
						.setPattern(regex.value())
						.setParser(RegexArgumentParser.INSTANCE);
				}
			}
			
			return null;
		});
		
		/* Set the default exception handler to make sure you don't miss out on them exception */
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
			throwable.printStackTrace();
		});
		
		/* Set the default MethodCommandFactory, this is used when creating commands
		 * from methods, so the commands which are specified by @Command would
		 * most likely go through this factory.
		 * 
		 * NOTE: This needs to be registered before you load any commands (Like CommandStore.of)
		 */
		MethodCommandFactory.setDefault(new IMethodCommandFactory<>() {
			public ExtendedCommand create(Method method, String name, Object invoker) {
				return new ExtendedCommand(IMethodCommandFactory.getName(name, method), method, invoker);
			}
		});
		
		/** Register contexts for commands, for instance, if a command has
		 * "@Context ICategory category" as a parameter it will call this
		 * function and get the command category which will then be in
		 * the parameter, this example may not be very helpful as you
		 * usually don't need access to the category but you get the point!
		 */
		ContextManagerFactory.getDefault()
			.registerContext(ICategory.class, (event, type) -> {
				return event.getCommand().getCategory();
			});
		
		/* Create the CommandListener */
		CommandListener listener = new CommandListener()
			/* Register all the commands by the package example.command */
			.addCommandStores(CommandStore.of("example.command"))
			/* Add the developers of this bot, */
			.addDevelopers(190551803669118976L)
			/* Set the default prefix of this bot */
			.setDefaultPrefixes("!");
		
		/* Register error responses for when an argument fails to parse the content correctly,
		 * for instance, if I were to execute a command, let's say ban, and the user does not exist
		 * "!ban hello" it would then reply with "@Joakim, `hello` is not a valid user"
		 */
		listener.getErrorManager()
			.registerResponse(User.class, (argument, message, content) -> {
				return message.getAuthor().getAsMention() + ", `" + content + "` is not a valid user";
			})
			.registerResponse(Member.class, (argument, message, content) -> {
				return message.getAuthor().getAsMention() + ", `" + content + "` is not a valid member";
			});
		
		/* Register return handlers for when something is returned from a command,
		 * for instance, if a command returned any type of object it would now 
		 * convert it to a string and send it
		 */
		listener.getReturnManager()
			.registerHandler(Object.class, (event, object) -> {
				event.reply(object.toString()).queue();
			})
			/* This makes it so anything which extends Object (everything)
			 * will be handled as an Object, unless their type explicitly 
			 * registered
			 */
			.setHandleInheritance(Object.class, true);
		
		/* Create the JDA instance */
		new JDABuilder(AccountType.BOT).setToken(token)
			/* Register the CommandListener, this is an important step */
			.addEventListeners(listener)
			.build()
			.awaitReady();
	}
}