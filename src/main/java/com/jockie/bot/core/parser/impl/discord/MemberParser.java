package com.jockie.bot.core.parser.impl.discord;

import java.util.List;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.utility.ArgumentUtility;

import net.dv8tion.jda.api.entities.Member;

public class MemberParser<Component> implements IParser<Member, Component> {

	@Override
	public ParsedResult<Member> parse(ParseContext context, Component component, String content) {
		List<Member> members = ArgumentUtility.getMembersByIdOrName(context.getMessage().getGuild(), content, true);
		
		if(members.size() == 1) {
			return new ParsedResult<>(true, members.get(0));
		}else{
			return new ParsedResult<>(false, null);
		}
	}	
}