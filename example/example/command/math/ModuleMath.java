package example.command.math;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.argument.Endless;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.module.Module;

@Module
public class ModuleMath {
	
	@Command(description="Multiply two numbers")
	public void multiply(CommandEvent event, @Argument(name="first") double first, @Argument(name="second") double second) {
		event.reply(String.valueOf(first * second)).queue();
	}
	
	@Command(description="Get the remainder of a divison")
	public void remainder(CommandEvent event, @Argument(name="number") double number, @Argument(name="modulo") double modulo) {
		event.reply(String.valueOf(number % modulo)).queue();
	}
	
	@Command(description="Add two numbers together")
	public void add(CommandEvent event, @Argument(name="first") double first, @Argument(name="second") double second) {
		event.reply(String.valueOf(first + second)).queue();
	}
	
	@Command(description="Add mutliple numbers together", aliases="add all")
	public void addAll(CommandEvent event, @Argument(name="numbers") @Endless(minArguments=2, maxArguments=10) /* Primitive arrays aren't supported */ Double[] numbers) {
		double sum = 0;
		for(int i = 0; i < numbers.length; i++) {
			sum += numbers[i];
		}
		
		event.reply(String.valueOf(sum)).queue();
	}
}