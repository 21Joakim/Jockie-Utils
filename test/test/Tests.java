package test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import com.jockie.bot.core.command.impl.CommandListener;
import com.jockie.bot.core.command.impl.CommandStore;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import test.command.CommandRunTests;

public class Tests {
	
	public static void main(String[] args) throws Exception {
		String token;
		try(FileInputStream stream = new FileInputStream(new File("./example.token"))) {
			token = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
		
		CommandListener listener = new CommandListener()
			.addCommandStores(new CommandStore().addCommands(new CommandRunTests()))
			.setDefaultPrefixes("!");
		
		JDABuilder.createDefault(token)
			.addEventListeners(listener)
			.enableIntents(GatewayIntent.MESSAGE_CONTENT)
			.build()
			.awaitReady();
	}
}