package example.command.math;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.argument.Endless;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.Initialize;
import com.jockie.bot.core.command.SubCommand;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;
import com.jockie.bot.core.module.Module;

import example.Categories;

@Module
public class ModuleMath {
	
	//@Initialize({"multiply", "remainder", "add", "addAll"})	
	@Initialize(all=true)
	public void init(CommandImpl command) {
		command.setCategory(Categories.MATH);
	}
	
	@Command(description="Multiply two numbers")
	public void multiply(CommandEvent event, @Argument("first") double first, @Argument("second") double second) {
		event.reply(String.valueOf(first * second)).queue();
	}
	
	@SubCommand("multiply")
	@Command(value="all", description="Mutliply multiple numbers")
	public void multiplyAll(CommandEvent event, @Argument("numbers") @Endless(minArguments=2, maxArguments=10) Double[] numbers) {
		double sum = numbers[0];
		for(int i = 1; i < numbers.length; i++) {
			sum *= numbers[i];
		}
		
		event.reply(String.valueOf(sum)).queue();
	}
	
	@Command(description="Get the remainder of a divison")
	public void remainder(CommandEvent event, @Argument("number") double number, @Argument("modulo") double modulo) {
		event.reply(String.valueOf(number % modulo)).queue();
	}
	
	@Command(description="Add two numbers together")
	public void add(CommandEvent event, @Argument("first") double first, @Argument("second") double second) {
		event.reply(String.valueOf(first + second)).queue();
	}
	
	@Command(description="Add mutliple numbers together", aliases="add all")
	public void addAll(CommandEvent event, @Argument("numbers") @Endless(minArguments=2, maxArguments=10) /* Primitive arrays aren't supported */ Double[] numbers) {
		double sum = 0;
		for(int i = 0; i < numbers.length; i++) {
			sum += numbers[i];
		}
		
		event.reply(String.valueOf(sum)).queue();
	}
}