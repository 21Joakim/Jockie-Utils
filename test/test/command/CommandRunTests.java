package test.command;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;
import com.jockie.bot.core.command.impl.CommandListener;
import com.jockie.bot.core.command.impl.CommandStore;
import com.jockie.bot.core.option.Option;
import com.jockie.bot.core.utility.CommandUtility;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.internal.entities.AbstractMessage;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import test.annotation.TestRun;
import test.annotation.TestRuns;

public class CommandRunTests extends CommandImpl {
	
	public CommandRunTests() {
		super("run tests");
		
		super.setDeveloper(true);
	}
	
	private static Message modifyContent(AbstractMessage message, String newContent) throws Throwable {
		Field field = AbstractMessage.class.getDeclaredField("content");
		field.setAccessible(true);
		
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		
		field.set(message, newContent);
		
		return message;
	}
	
	private static Pair<Boolean, Double> doTest(CommandListener listener, Message message, String prefix, String command, TestRun testRun) throws Throwable {
		String argument = testRun.argument().length() > 0 ? " " + testRun.argument() : "";
		String newMessage = prefix + command + argument;
		
		message = CommandRunTests.modifyContent((AbstractMessage) message, newMessage);
		
		long timeElapsed = System.nanoTime();
		CommandEvent commandEvent = listener.parse(message);
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
	
	private static void sendResult(CommandEvent event, StringBuilder result, long timeElapsed) {
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
	
	private CommandListener listener = new CommandListener()
		.addCommandStores(new CommandStore().addCommands(ModuleTest.class))
		.setDefaultPrefixes("!")
		.setHelpFunction(null);
	
	public void onCommand(CommandEvent event, @Option("failed") boolean failed, @Option("time") boolean time) throws Throwable {
		int total = 0, successful = 0;
		StringBuilder successfulBuilder = new StringBuilder(), unsuccessfulBuilder = new StringBuilder();
		
		for(Method commandMethod : CommandUtility.getCommandMethods(ModuleTest.class.getDeclaredMethods())) {
			TestRun[] testRuns;
			if(commandMethod.isAnnotationPresent(TestRuns.class)) {
				testRuns = commandMethod.getAnnotation(TestRuns.class).value();
			}else if(commandMethod.isAnnotationPresent(TestRun.class)) {
				testRuns = new TestRun[] {
					commandMethod.getAnnotation(TestRun.class)
				};
			}else{
				continue;
			}
			
			for(TestRun testRun : testRuns) {
				String commandName = commandMethod.getName();
				
				Pair<Boolean, Double> status = CommandRunTests.doTest(this.listener, event.getMessage(), this.listener.getDefaultPrefixes().get(0), commandName, testRun);
				
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
		
		/* TODO: Implement tests for clas based commands as well
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
		
		System.out.println(result.toString());
		
		CommandRunTests.sendResult(event, result, timeSinceStarted);
	}
}