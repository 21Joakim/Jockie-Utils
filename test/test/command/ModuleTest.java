package test.command;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.argument.Endless;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.ICommand.ArgumentTrimType;
import com.jockie.bot.core.command.ICommand.ContentOverflowPolicy;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandListener.Failure;
import com.jockie.bot.core.module.Module;
import com.jockie.bot.core.option.Option;
import com.jockie.bot.core.utility.CommandUtility;
import com.jockie.bot.core.utility.TriConsumer;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.impl.AbstractMessage;
import net.dv8tion.jda.core.utils.tuple.Pair;
import test.annotation.TestRun;
import test.annotation.TestRuns;

@Module
public class ModuleTest {
	
	private Message modifyContent(AbstractMessage message, String newContent) throws Exception {
		Field field = AbstractMessage.class.getDeclaredField("content");
		field.setAccessible(true);
		
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		
		field.set(message, newContent);
		
		return message;
	}
	
	private Pair<Boolean, Double> doTest(CommandEvent event, String command, TestRun testRun) throws Exception{
		String prefix = event.getPrefix();
		String argument = testRun.argument().length() > 0 ? " " + testRun.argument() : "";
		String newMessage = prefix + command + argument;
		
		Message message = modifyContent((AbstractMessage) event.getMessage(), newMessage);
		
		long timeElapsed = System.nanoTime();
		CommandEvent commandEvent = event.getCommandListener().parse(message);
		timeElapsed = (System.nanoTime() - timeElapsed);
		
		boolean status;
		if(commandEvent != null) {
			if(!testRun.success()) {
				status = false;
			}else{
				String result = Arrays.deepToString(commandEvent.getArguments());
				
				if(testRun.result().length() > 0) {
					if(result.equals(testRun.result())) {
						status = true;
					}else{
						status = false;
					}
				}else{
					status = true;
				}
			}
		}else{
			status = !testRun.success();
		}
		
		return Pair.of(status, timeElapsed/(double) TimeUnit.MILLISECONDS.toNanos(1));
	}
	
	private void sendResult(CommandEvent event, StringBuilder result, long timeElapsed) {
		result = new StringBuilder(result);
		
		int maxLength = Message.MAX_CONTENT_LENGTH - 50;
		
		while(result.length() > maxLength) {
			int lastLine = result.substring(0, maxLength - 50).lastIndexOf("\n");
			String newResult = "```diff\n" + result.substring(0, lastLine) + "```";
			
			event.reply(newResult).queue();
			
			result.delete(0, lastLine);
		}
		
		if(result.length() > 0) {
			result.insert(0, "```diff\n").append("```\n");
		}
		
		event.reply(result.append(":stopwatch: **" + timeElapsed + "**ms")).queue();
	}
	
	@Command.Developer
	@Command("run tests")
	public void runTests(CommandEvent event, @Option("failed") boolean failed, @Option("time") boolean time) throws Exception {
		TriConsumer<Message, String, List<Failure>> tempHelpFunction = event.getCommandListener().getHelpFunction();
		
		event.getCommandListener().setHelpFunction(null);
		
		int total = 0, successful = 0;
		StringBuilder successfulBuilder = new StringBuilder(), unsuccessfulBuilder = new StringBuilder();
		
		for(Method commandMethod : CommandUtility.getCommandMethods(ModuleTest.class.getDeclaredMethods())) {
			if(commandMethod.isAnnotationPresent(TestRuns.class)) {
				for(TestRun testRun : commandMethod.getAnnotation(TestRuns.class).value()) {
					String commandName = commandMethod.getName();
					
					Pair<Boolean, Double> status = doTest(event, commandName, testRun);
					
					if(status.getLeft()) {
						successfulBuilder.append("+ [Passed] ");
						
						if(time) {
							successfulBuilder.append("[" + String.format("%.2f", status.getRight()) + "ms] ");
						}
						
						successfulBuilder.append(commandName + " " + testRun.argument() + "\n");
						
						successful += 1;
					}else{
						unsuccessfulBuilder.append("- [Failed] ");
						
						if(time) {
							unsuccessfulBuilder.append("[" + String.format("%.2f", status.getRight()) + "ms] ");
						}
						
						unsuccessfulBuilder.append(commandName + " " + testRun.argument() + "\n");
					}
					
					total += 1;
				}
			}
		}
		
		/*
		for(Class<ICommand> commandClass : CommandUtility.getClassesImplementing(ModuleTest.class.getDeclaredClasses(), ICommand.class)) {
			
		}
		*/
		
		long timeSinceStarted = TimeUnit.NANOSECONDS.toMillis(event.getTimeSinceStarted());
		
		StringBuilder result = new StringBuilder();
		result.append("----------------------------\n");
		
		if(!failed) {
			if(successfulBuilder.length() > 0) {
				result.append(successfulBuilder);
				result.append("\n");
			}
		}
		
		if(unsuccessfulBuilder.length() > 0) {
			result.append(unsuccessfulBuilder);
			result.append("\n");
		}
		
		result.append((total - successful == 0 ? "+" : "-") + " Result [" + successful + "/" + total + "]");
		result.append("\n----------------------------");
		
		event.getCommandListener().setHelpFunction(tempHelpFunction);
		
		System.out.println(result.toString());
		
		this.sendResult(event, result, timeSinceStarted);
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
	
}