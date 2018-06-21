package com.jockie.bot.core.utility;

import java.util.List;
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
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.RestAction.EmptyRestAction;

public class ArgumentUtility {
	
	public static final Pattern USER_NAME_PATTERN = Pattern.compile(".{2,32}#[0-9]{4}");
	public static final Pattern ID_PATTERN = Pattern.compile("\\d+");
	
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
		}else if(ID_PATTERN.matcher(value).matches()) {
			return guild.getRoleById(value);
		}
		
		return null;
	}
	
	public static Member getMember(Guild guild, String value) {
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		
		if(id != null) {
			return guild.getMemberById(id);
		}else if(ID_PATTERN.matcher(value).matches()) {
			return guild.getMemberById(value);
		}
		
		return null;
	}
	
	public static TextChannel getTextChannel(Guild guild, String value) {
		String id = ArgumentUtility.getGroup(MentionType.CHANNEL.getPattern(), 1, value);
		
		if(id != null) {
			return guild.getTextChannelById(id);
		}else if(ID_PATTERN.matcher(value).matches()) {
			return guild.getTextChannelById(value);
		}
		
		return null;
	}
	
	public static Emote getEmote(Guild guild, String value) {
		String id = ArgumentUtility.getGroup(MentionType.EMOTE.getPattern(), 2, value);
		
		if(id != null) {
			return guild.getEmoteById(id);
		}else if(ID_PATTERN.matcher(value).matches()) {
			return guild.getEmoteById(value);
		}
		
		return null;
	}
	
	public static User getUser(JDA jda, String value) {
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		
		if(id != null) {
			return jda.getUserById(id);
		}else if(ID_PATTERN.matcher(value).matches()) {
			return jda.getUserById(value);
		}
		
		return null;
	}
	
	public static RestAction<User> retrieveUser(JDA jda, String value) {
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		
		if(id != null) {
			return jda.retrieveUserById(id);
		}else if(ID_PATTERN.matcher(value).matches()) {
			return jda.retrieveUserById(value);
		}
		
		return new EmptyRestAction<User>(jda, null);
	}
	
	public static Member getMemberByIdOrName(Guild guild, String value, boolean ignoreCase) {
		String processed = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		
		if(processed != null) {
			return guild.getMemberById(processed);
		}else if(ID_PATTERN.matcher(value).matches()) {
			return guild.getMemberById(value);
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
		
		List<Member> members = guild.getMembersByEffectiveName(value, ignoreCase);
		if(members.size() == 1) {
			return members.get(0);
		}
		
		return null;
	}
	
	public static Role getRoleByIdOrName(Guild guild, String value, boolean ignoreCase) {
		String processed = ArgumentUtility.getGroup(MentionType.ROLE.getPattern(), 1, value);
		
		if(processed != null) {
			return guild.getRoleById(processed);
		}else if(ID_PATTERN.matcher(value).matches()) {
			return guild.getRoleById(value);
		}
		
		List<Role> roles = guild.getRolesByName(value, ignoreCase);
		if(roles.size() == 1) {
			return roles.get(0);
		}
		
		return null;
	}
	
	public static Emote getEmoteByIdOrName(Guild guild, String value, boolean ignoreCase) {
		String processed = ArgumentUtility.getGroup(MentionType.EMOTE.getPattern(), 2, value);
		
		if(processed != null) {
			return guild.getEmoteById(processed);
		}else if(ID_PATTERN.matcher(value).matches()) {
			return guild.getEmoteById(value);
		}
		
		List<Emote> emote = guild.getEmotesByName(value, ignoreCase);
		if(emote.size() == 1) {
			return emote.get(0);
		}
		
		return null;
	}
	
	public static TextChannel getTextChannelByIdOrName(Guild guild, String value, boolean ignoreCase) {
		String processed = ArgumentUtility.getGroup(MentionType.CHANNEL.getPattern(), 1, value);
		
		if(processed != null) {
			return guild.getTextChannelById(processed);
		}else if(ID_PATTERN.matcher(value).matches()) {
			return guild.getTextChannelById(value);
		}
		
		List<TextChannel> channels = guild.getTextChannelsByName(value, ignoreCase);
		if(channels.size() == 1) {
			return channels.get(0);
		}
		
		return null;
	}
	
	public static VoiceChannel getVoiceChannelByIdOrName(Guild guild, String value, boolean ignoreCase) {
		if(ID_PATTERN.matcher(value).matches()) {
			return guild.getVoiceChannelById(value);
		}
		
		List<VoiceChannel> channels = guild.getVoiceChannelsByName(value, ignoreCase);
		if(channels.size() == 1) {
			return channels.get(0);
		}
		
		return null;
	}
}