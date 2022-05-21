package example.command.fun;

import java.util.concurrent.ThreadLocalRandom;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandImpl;

public class CommandCoinFlip extends CommandImpl {
	
	public CommandCoinFlip() {
		super("coinflip");
		
		super.setDescription("Flip a coin to see if your statement or question is true or false");
	}
	
	/* If the argument is not endless it will only take the first word as the argument */
	public String onCommand(@Argument(value="question", endless=true) String question) {
		return String.format("That is %s", ThreadLocalRandom.current().nextBoolean() ? " true!" : " not true!");
	}
}