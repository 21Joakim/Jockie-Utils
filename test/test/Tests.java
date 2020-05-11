package test;

import java.io.File;
import java.io.FileInputStream;

import com.jockie.bot.core.command.impl.CommandListener;
import com.jockie.bot.core.command.impl.CommandStore;

import net.dv8tion.jda.api.JDABuilder;
import test.command.CommandRunTests;

public class Tests {
	
	public static void main(String[] args) throws Exception {
		String token;
		try(FileInputStream stream = new FileInputStream(new File("./example.token"))) {
			token = new String(stream.readAllBytes());
		}
		
		CommandListener listener = new CommandListener()
			.addCommandStores(new CommandStore().addCommands(new CommandRunTests()))
			.addDevelopers(190551803669118976L)
			.setDefaultPrefixes("!");
		
		JDABuilder.createDefault(token)
			.addEventListeners(listener)
			.build()
			.awaitReady();
	}
}