package example.command.fun;

import java.util.Random;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandCoinFlip extends CommandImpl {
	
	/* No need to create a new one each time someone uses it */
	private Random random = new Random();
	
	public CommandCoinFlip() {
		super("coinflip");
		
		super.setDescription("Flip a coin to see if your statement or question is true or false");
	}
	
	/* If the argument is not endless it will only take the first word as the argument */
	public String onCommand(MessageReceivedEvent event, @Argument(value="question", endless=true) String question) {
		return "That is " + (this.random.nextBoolean() ? " true!" : " not true!");
	}
}