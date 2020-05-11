package example.core.command;

import java.util.Map;

import com.jockie.bot.core.category.ICategory;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.ICommand.ArgumentParsingType;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandListener;

import net.dv8tion.jda.api.entities.Message;

public class ExtendedCommandEvent extends CommandEvent {

	public ExtendedCommandEvent(Message message, CommandListener listener, ICommand command, Object[] arguments,
		String[] rawArguments, String prefix, String commandTrigger, Map<String, Object> options,
		ArgumentParsingType parsingType, String contentOverflow, long timeStarted) {
		
		super(message, listener, command, arguments, rawArguments, prefix, commandTrigger, options, parsingType,
			contentOverflow, timeStarted);
	}
	
	public ICategory getCommandCategory() {
		return this.command.getCategory();
	}
}