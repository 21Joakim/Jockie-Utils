package example.command.info;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

public class CommandColour extends CommandImpl {
	
	public CommandColour() {
		super("colour");
	}
	
	private String getColourHex(int colourRaw) {
		String colour = Integer.toHexString(colourRaw);
		colour = colour.substring(2, colour.length());
		
		return "#" + colour;
	}

	public String onCommand(CommandEvent event, @Argument("role") Role role) {
		return String.format("The colour of the role %s is %s", role.getName(), this.getColourHex(role.getColorRaw()));
	}
	
	public String onCommand(CommandEvent event, @Argument("member") Member member) {
		return String.format("The colour of the member %s is %s", member.getEffectiveName(), this.getColourHex(member.getColorRaw()));
	}
}