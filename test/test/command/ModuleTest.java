package test.command;

import java.lang.reflect.Method;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.argument.Endless;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.ICommand.ArgumentTrimType;
import com.jockie.bot.core.command.ICommand.ContentOverflowPolicy;
import com.jockie.bot.core.command.IMethodCommand;
import com.jockie.bot.core.command.factory.impl.MethodCommandFactory;
import com.jockie.bot.core.module.Module;
import com.jockie.bot.core.module.impl.ModuleImpl;

import test.annotation.TestRun;

@Module
public class ModuleTest extends ModuleImpl {
	
	@Override
	public IMethodCommand createCommand(Method method, String name) {
		return MethodCommandFactory.getDefault().create(method, method.getName(), this);
	}
	
	@Command
	@TestRun(success=false)
	@TestRun(success=true, argument="hello", result="[hello]")
	public void testSingleRequired(@Argument String required) {}
	
	@Command
	@TestRun(success=true, result="[null]")
	@TestRun(success=true, argument="hello", result="[hello]")
	public void testSingleOptional(@Argument(nullDefault=true) String optional) {}
	
	@Command
	@TestRun(success=false)
	@TestRun(success=true, argument="hello", result="[hello, null]")
	@TestRun(success=true, argument="hello hello2", result="[hello, hello2]")
	public void testRequiredAndOptional(@Argument String required, @Argument(nullDefault=true) String optional) {}
	
	@Command
	@TestRun(success=false)
	@TestRun(success=false, argument="hello")
	@TestRun(success=true, argument="hello hello2", result="[hello, hello2]")
	public void testRequiredAndRequired(@Argument String required, @Argument String secondRequired) {}
	
	@Command
	@TestRun(success=false)
	@TestRun(success=true, argument="hello", result="[null, hello]")
	@TestRun(success=true, argument="hello hello2", result="[hello, hello2]")
	public void testOptionalAndRequired(@Argument(nullDefault=true) String optional, @Argument String required) {}
	
	@Command
	@TestRun(success=true, result="[null, null]")
	@TestRun(success=true, argument="hello", result="[hello, null]")
	@TestRun(success=true, argument="hello hello2", result="[hello, hello2]")
	public void testOptionalAndOptional(@Argument(nullDefault=true) String optional, @Argument(nullDefault=true) String secondOptional) {}
	
	@Command
	@TestRun(success=true, result="[null, null, null, null]")
	@TestRun(success=true, argument="hello", result="[hello, null, null, null]")
	@TestRun(success=true, argument="hello hello2", result="[hello, hello2, null, null]")
	@TestRun(success=true, argument="hello hello2 hello3", result="[hello, hello2, hello3, null]")
	@TestRun(success=true, argument="hello hello2 hello3 hello4", result="[hello, hello2, hello3, hello4]")
	public void testFourOptionals(@Argument(nullDefault=true) String optional, @Argument(nullDefault=true) String secondOptional, 
			@Argument(nullDefault=true) String thirdOptional, @Argument(nullDefault=true) String fourthOptional) {}
	
	@Command
	@TestRun(success=false)
	@TestRun(success=false, argument="hello")
	@TestRun(success=true, argument="hello hello2", result="[null, hello, null, hello2]")
	@TestRun(success=true, argument="hello hello2 hello3", result="[hello, hello2, null, hello3]")
	@TestRun(success=true, argument="hello hello2 hello3 hello4", result="[hello, hello2, hello3, hello4]")
	public void testOptionalAndRequiredTwice(@Argument(nullDefault=true) String optional, @Argument String required,
			@Argument(nullDefault=true) String secondOptional, @Argument String secondRequired) {}
	
	@Command
	@TestRun(success=false)
	@TestRun(success=true, argument="hello", result="[[hello]]")
	@TestRun(success=true, argument="hello hello2", result="[[hello, hello2]]")
	@TestRun(success=false, argument="hello hello2 hello3")
	public void testEndlessArguments(@Argument @Endless(maxArguments=2) String[] endless) {}
	
	@Command
	@TestRun(success=true)
	@TestRun(success=true, argument="hello", result="[hello, []]")
	@TestRun(success=true, argument="hello hello2", result="[hello, [hello2]]")
	@TestRun(success=true, argument="hello hello2 hello3", result="[hello, [hello2, hello3]]")
	@TestRun(success=false, argument="hello hello2 hello3 hello4")
	public void testOptionalAndEndlessArguments(@Argument(nullDefault=true) String optional, @Argument @Endless(minArguments=0, maxArguments=2) String[] endless) {}
	
	@Command
	@TestRun(success=false)
	@TestRun(success=true, argument="hello", result="[null, [hello]]")
	@TestRun(success=true, argument="hello hello2", result="[hello, [hello2]]")
	@TestRun(success=true, argument="hello hello2 hello3", result="[hello, [hello2, hello3]]")
	@TestRun(success=false, argument="hello hello2 hello3 hello4")
	public void testOptionalAndEndlessArguments2(@Argument(nullDefault=true) String optional, @Argument @Endless(minArguments=1, maxArguments=2) String[] endless) {}
	
	@Command(argumentTrimType=ArgumentTrimType.NONE)
	@TestRun(success=false, argument="   hello")
	@TestRun(success=true, argument="hello", result="[hello]")
	@TestRun(success=true, argument="\"   hello\"", result="[   hello]")
	public void testNoneArgumentTrimType(@Argument(acceptQuote=true) String argument) {}
	
	@Command(argumentTrimType=ArgumentTrimType.LENIENT)
	@TestRun(success=true, argument="hello", result="[hello]")
	@TestRun(success=true, argument="   hello", result="[hello]")
	@TestRun(success=true, argument="\"   hello\"", result="[   hello]")
	public void testLeninetArgumentTrimType(@Argument(acceptQuote=true) String argument) {}
	
	@Command(argumentTrimType=ArgumentTrimType.STRICT)
	@TestRun(success=true, argument="hello", result="[hello]")
	@TestRun(success=true, argument="   hello", result="[hello]")
	@TestRun(success=true, argument="\"   hello\"", result="[hello]")
	public void testStrictArgumentTrimType(@Argument(acceptQuote=true) String argument) {}
	
	@Command
	@TestRun(success=true, argument="\"hello\"", result="[hello]")
	@TestRun(success=true, argument="\"hello there\"", result="[hello there]")
	@TestRun(success=false, argument="\"hello\" there\"")
	@TestRun(success=true, argument="\"hello\\\" there\"", result="[hello\" there]")
	public void testAcceptQuote(@Argument(acceptQuote=true) String argument) {}
	
	@Command(contentOverflowPolicy=ContentOverflowPolicy.FAIL)
	@TestRun(success=true, argument="hello", result="[hello]")
	@TestRun(success=false, argument="hello hello")
	public void testFailOverflowPolicy(@Argument String argument) {}
	
	@Command(contentOverflowPolicy=ContentOverflowPolicy.IGNORE)
	@TestRun(success=true, argument="hello", result="[hello]")
	@TestRun(success=true, argument="hello hello", result="[hello]")
	public void testIgnoreOverflowPolicy(@Argument String argument) {}
	
	@Command(argumentTrimType=ArgumentTrimType.NONE)
	@TestRun(success=true, argument="hello there", result="[hello there]")
	@TestRun(success=true, argument="   hello there", result="[   hello there]")
	public void testEndlessWithSpacingNoneArgumentTrimType(@Argument(endless=true) String argument) {}
	
	@Command(argumentTrimType=ArgumentTrimType.LENIENT)
	@TestRun(success=true, argument="hello there", result="[hello there]")
	@TestRun(success=true, argument="   hello there", result="[   hello there]")
	public void testEndlessWithSpacingLenientArgumentTrimType(@Argument(endless=true) String argument) {}
	
	@Command(argumentTrimType=ArgumentTrimType.STRICT)
	@TestRun(success=true, argument="hello there", result="[hello there]")
	@TestRun(success=true, argument="   hello there", result="[hello there]")
	public void testEndlessWithSpacingStrictArgumentTrimType(@Argument(endless=true) String argument) {}
	
	@Command(argumentTrimType=ArgumentTrimType.NONE)
	@TestRun(success=true, argument="hello there there", result="[[hello, there, there]]")
	
	/* Not entirely sure why this fails */
	@TestRun(success=true, argument="hello    there    there")
	public void testEndlessArgumentsWithSpacingNoneArgumentTrimType(@Argument @Endless String argument[]) {}
	
	@Command(argumentTrimType=ArgumentTrimType.LENIENT)
	@TestRun(success=true, argument="hello there there", result="[[hello, there, there]]")
	@TestRun(success=true, argument="hello    there    there", result="[[hello, there, there]]")
	public void testEndlessArgumentsWithSpacingLenientArgumentTrimType(@Argument String argument[]) {}
	
	@Command(argumentTrimType=ArgumentTrimType.STRICT)
	@TestRun(success=true, argument="hello there there", result="[[hello, there, there]]")
	@TestRun(success=true, argument="hello    there    there", result="[[hello, there, there]]")
	public void testEndlessArgumentsWithSpacingStrictArgumentTrimType(@Argument String argument[]) {}
	
}