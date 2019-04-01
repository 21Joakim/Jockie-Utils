package example;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jockie.bot.core.argument.impl.ArgumentFactory;
import com.jockie.bot.core.argument.parser.ParsedArgument;
import com.jockie.bot.core.command.impl.CommandListener;
import com.jockie.bot.core.command.impl.CommandStore;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;

public class Main {
	
	public static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE;
	
	public static void main(String[] args) throws Exception {
		String token;
		try(FileInputStream stream = new FileInputStream(new File("./example.token"))) {
			token = new String(stream.readAllBytes());
		}
		
		final Pattern HEX_PATTERN = Pattern.compile("(#|)(([0-9]|(?i)[A-F]){6})");
		
		/* Register the class Color so that it can be used as an argument */
		ArgumentFactory.registerArgument(Color.class, (event, argument, value) -> {
			Matcher matcher = HEX_PATTERN.matcher(value);
			if(matcher.matches()) {
				String content = matcher.group(2);
				
				return new ParsedArgument<>(true, Color.decode("#" + content));
			}
			
			return new ParsedArgument<>(false, null);
		});
		
		/* Register the class URL so that it can be used as an argument */
		ArgumentFactory.registerArgument(URL.class, (event, argument, value) -> {
			try {
				/* Preferably you would add an extra check to not allow for local files (server files) to be used, such as file:///C:/Users/Joakim/Desktop/my%20nudes.png */
				return new ParsedArgument<>(true, new URL(value));
			}catch(MalformedURLException e) {
				return new ParsedArgument<>(false, null);
			}
		});
		
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
			throwable.printStackTrace();
		});

		CommandListener listener = new CommandListener()
			.addCommandStore(CommandStore.of("example.command"))
			.addDeveloper(190551803669118976L)
			.setDefaultPrefixes("§");
		
		new JDABuilder(AccountType.BOT).setToken(token)
			.addEventListener(listener)
			.build()
			.awaitReady();
	}
}