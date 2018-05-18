package com.jockie.bot.core.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message.MentionType;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.RestAction.EmptyRestAction;

public class ArgumentUtility {
	
	public static final Pattern USER_NAME_PATTERN = Pattern.compile(".{1,}#[0-9]{4}");
	
	private static String getGroup(Pattern pattern, int group, String value) {
		Matcher matcher = pattern.matcher(value);
		if(matcher.find()) {
			return matcher.group(group);
		}
		
		return null;
	}
	
	public static Role getRole(Guild guild, String value) {
		String id = ArgumentUtility.getGroup(MentionType.ROLE.getPattern(), 1, value);
		
		if(id != null) {
			return guild.getRoleById(id);
		}
		
		return null;
	}
	
	public static Member getMember(Guild guild, String value) {
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		
		if(id != null) {
			return guild.getMemberById(id);
		}
		
		return null;
	}
	
	public static TextChannel getTextChannel(Guild guild, String value) {
		String id = ArgumentUtility.getGroup(MentionType.CHANNEL.getPattern(), 1, value);
		
		if(id != null) {
			return guild.getTextChannelById(id);
		}
		
		return null;
	}
	
	public static Emote getEmote(Guild guild, String value) {
		String id = ArgumentUtility.getGroup(MentionType.EMOTE.getPattern(), 2, value);
		
		if(id != null) {
			return guild.getEmoteById(id);
		}
		
		return null;
	}
	
	public static User getUser(JDA jda, String value) {
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		
		if(id != null) {
			return jda.getUserById(id);
		}
		
		return null;
	}
	
	public static RestAction<User> retrieveUser(JDA jda, String value) {
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		
		if(id != null) {
			return jda.retrieveUserById(id);
		}
		
		return new EmptyRestAction<User>(jda, null);
	}
	
	public static Member getMemberByIdOrName(Guild guild, String value, boolean ignoreCase) {
		String processed = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		
		if(processed != null) {
			return guild.getMemberById(processed);
		}
		
		if(USER_NAME_PATTERN.matcher(value).matches()) {
			for(Member member : guild.getMembers()) {
				if(ignoreCase) {
					if((member.getUser().getName() + "#" + member.getUser().getDiscriminator()).equalsIgnoreCase(value)) {
						return member;
					}
				}else{
					if((member.getUser().getName() + "#" + member.getUser().getDiscriminator()).equals(value)) {
						return member;
					}
				}
			}
		}
		
		return null;
	}
}