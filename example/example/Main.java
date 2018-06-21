package example;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jockie.bot.core.command.argument.VerifiedArgument;
import com.jockie.bot.core.command.argument.VerifiedArgument.VerifiedType;
import com.jockie.bot.core.command.argument.impl.ArgumentFactory;
import com.jockie.bot.core.command.impl.CommandListener;
import com.jockie.bot.core.command.impl.CommandStore;
import com.jockie.bot.core.command.impl.command.CommandHelp;
import com.jockie.bot.example.Safe;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;

public class Main {
	
	public static void main(String[] args) throws Exception {
		final Pattern HEX_PATTERN = Pattern.compile("(#|)(([0-9]|(?i)[A-F]){6})");
		
		/* Register the class Color so that it can be used as an argument  */
		ArgumentFactory.registerArgument(Color.class, (event, argument, value) -> {
			Matcher matcher = HEX_PATTERN.matcher(value);
			if(matcher.matches()) {
				String content = matcher.group(2);
				
				return new VerifiedArgument<Color>(VerifiedType.VALID, Color.decode("#" + content));
			}
			
			return new VerifiedArgument<Color>(null);
		});
		
		/* Register the class URL so that it can be used as an argument  */
		ArgumentFactory.registerArgument(URL.class, (event, argument, value) -> {
			try {
				/* Preferably you would add an extra check to not allow for local files (server files) to be used, such as file:///C:/Users/Joakim/Desktop/my%20nudes.png */
				return new VerifiedArgument<URL>(VerifiedType.VALID, new URL(value));
			}catch(MalformedURLException e) {
				return new VerifiedArgument<URL>(null);
			}
		});
		
		CommandListener listener = new CommandListener()
			.addCommandStore(CommandStore.of("example.command").addCommands(new CommandHelp() /* Built in help command */))
			.setDefaultPrefixes("?");
		
		new JDABuilder(AccountType.BOT).setToken(Safe.TEST_TOKEN)
			.addEventListener(listener)
			.buildBlocking();
	}
}