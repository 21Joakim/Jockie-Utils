package com.jockie.bot.core.utility;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EmoteImpl;
import net.dv8tion.jda.internal.requests.EmptyRestAction;
import net.dv8tion.jda.internal.utils.Checks;

public class ArgumentUtility {
	
	private ArgumentUtility() {}
	
	public static final Pattern USER_NAME_PATTERN = Pattern.compile("(.{2,32})#([0-9]{4})");
	public static final Pattern ID_PATTERN = Pattern.compile("\\d+");
	
	private static String getGroup(Pattern pattern, int group, String value) {
		Matcher matcher = pattern.matcher(value);
		if(matcher.find()) {
			return matcher.group(group);
		}
		
		return null;
	}
	
	/**
	 * Get a role by id or mention
	 * 
	 * @param guild the guild to search for the role in
	 * @param value the mention or id of the role
	 * 
	 * @return the found role, may be null
	 */
	public static Role getRoleById(Guild guild, String value) {
		String id = ArgumentUtility.getGroup(MentionType.ROLE.getPattern(), 1, value);
		
		if(id != null) {
			return guild.getRoleById(id);
		}else if(ID_PATTERN.matcher(value).matches()) {
			return guild.getRoleById(value);
		}
		
		return null;
	}
	
	/**
	 * Get a member by id or mention
	 * 
	 * @param guild the guild to search for the member in
	 * @param value the mention or id of the member
	 * 
	 * @return the found member, may be null
	 */
	public static Member getMemberById(Guild guild, String value) {
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		
		if(id != null) {
			return guild.getMemberById(id);
		}else if(ID_PATTERN.matcher(value).matches()) {
			return guild.getMemberById(value);
		}
		
		return null;
	}
	
	/**
	 * Get a text channel by id or mention
	 * 
	 * @param guild the guild to search for the text channel in
	 * @param value the mention or id of the text channel
	 * 
	 * @return the found text channel, may be null
	 */
	public static TextChannel getTextChannelById(Guild guild, String value) {
		String id = ArgumentUtility.getGroup(MentionType.CHANNEL.getPattern(), 1, value);
		
		if(id != null) {
			return guild.getTextChannelById(id);
		}else if(ID_PATTERN.matcher(value).matches()) {
			return guild.getTextChannelById(value);
		}
		
		return null;
	}
	
	/**
	 * Get an emote by id or mention
	 * 
	 * @param guild the guild to search for the emote in
	 * @param value the mention or id of the emote
	 * 
	 * @return the found emote, may be null
	 */
	public static Emote getEmoteById(Guild guild, String value) {
		Matcher matcher = MentionType.EMOTE.getPattern().matcher(value);
		
		Emote emote = null;
		if(matcher.matches()) {
			emote = guild.getEmoteById(matcher.group(2));
			
			if(emote == null) {
				emote = new EmoteImpl(Long.parseLong(matcher.group(2)), (JDAImpl) guild.getJDA())
					.setName(matcher.group(1))
					.setAnimated(value.startsWith("<a:"));
			}
		}else if(ID_PATTERN.matcher(value).matches()) {
			emote = guild.getEmoteById(value);
		}
		
		return emote;
	}
	
	/**
	 * Get an user by id or mention
	 * 
	 * @param jda the JDA instance to search for the user in
	 * @param value the mention or id of the user
	 * 
	 * @return the found user, may be null
	 */
	public static User getUserById(JDA jda, String value) {
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		
		if(id != null) {
			return jda.getUserById(id);
		}else if(ID_PATTERN.matcher(value).matches()) {
			return jda.getUserById(value);
		}
		
		return null;
	}
	
	/**
	 * Get an user by id or mention
	 * 
	 * @param shardManager the shard manager instance to search for the user in
	 * @param value the mention or id of the user
	 * 
	 * @return the found user, may be null
	 */
	public static User getUserById(ShardManager shardManager, String value) {
		Checks.notNull(shardManager, "ShardManager");
		
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		
		if(id != null) {
			return shardManager.getUserById(id);
		}else if(ID_PATTERN.matcher(value).matches()) {
			return shardManager.getUserById(value);
		}
		
		return null;
	}
	
	/**
	 * Retrieve an user by id or mention
	 * 
	 * @param jda the JDA instance to make the request from
	 * @param value the mention or id of the user
	 * 
	 * @return RestAction - Type: User<br>
	 * On request, gets the User with id matching provided id from Discord.
	 */
	public static RestAction<User> retrieveUserById(JDA jda, String value) {
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		
		if(id != null) {
			return jda.retrieveUserById(id);
		}else if(ID_PATTERN.matcher(value).matches()) {
			return jda.retrieveUserById(value);
		}
		
		return new EmptyRestAction<User>(jda, null);
	}
	
	/**
	 * Retrieve an user by id or mention
	 * 
	 * @param shardManager the shard manager instance to search for the user in
	 * @param value the mention or id of the user
	 * 
	 * @return RestAction - Type: User<br>
	 * On request, gets the User with id matching provided id from Discord.
	 */
	public static RestAction<User> retrieveUserById(ShardManager shardManager, String value) {
		Checks.notNull(shardManager, "ShardManager");
		
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		
		if(id != null) {
			return shardManager.retrieveUserById(id);
		}else if(ID_PATTERN.matcher(value).matches()) {
			return shardManager.retrieveUserById(value);
		}
		
		return new EmptyRestAction<User>(shardManager.getShardCache().getElementById(0), null);
	}
	
	/**
	 * Retrieve an user by id or mention
	 * 
	 * @param shardManager the shard manager instance to search for the user in
	 * @param jda the JDA instance to make the request from if it does not exist in
	 * the shard manager
	 * @param value the mention or id of the user
	 * 
	 * @return RestAction - Type: User<br>
	 * On request, gets the User with id matching provided id from Discord.
	 */
	public static RestAction<User> retrieveUserById(ShardManager shardManager, JDA jda, String value) {
		Checks.notNull(shardManager, "ShardManager");
		
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		
		if(id != null) {
			User user = shardManager.getUserById(id);
			if(user != null) {
				return new EmptyRestAction<User>(user.getJDA(), user);
			}
			
			return jda.retrieveUserById(id);
		}else if(ID_PATTERN.matcher(value).matches()) {
			User user = shardManager.getUserById(value);
			if(user != null) {
				return new EmptyRestAction<User>(user.getJDA(), user);
			}
			
			return jda.retrieveUserById(value);
		}
		
		return new EmptyRestAction<User>(jda, null);
	}
	
	/**
	 * Get members by id, mention, effective name or tag
	 * If a member was found by id it will not check for names
	 * 
	 * @param guild the guild to search for the members in
	 * @param value the mention, id or tag of the member to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found members
	 */
	public static List<Member> getMembersByIdOrName(Guild guild, String value, boolean ignoreCase) {
		{
			Member member = ArgumentUtility.getMemberById(guild, value);
			if(member != null) {
				return List.of(member);
			}
		}
		
		Matcher matcher = USER_NAME_PATTERN.matcher(value);
		if(matcher.matches()) {
			String name = matcher.group(1);
			String descriminator = matcher.group(2);
			
			Member member = guild.getMemberCache().stream()
				.filter(a -> a.getUser().getDiscriminator().equals(descriminator))
				.filter(a -> a.getUser().getName().equals(name))
				.findFirst()
				.orElse(null);
			
			if(member != null) {
				return List.of(member);
			}
		}
		
		return guild.getMembersByEffectiveName(value, ignoreCase);
	}
	
	/**
	 * Get roles by id, mention or name
	 * If a role was found by id it will not check for names
	 * 
	 * @param guild the guild to search for the roles in
	 * @param value the mention, id or name of the role to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found roles
	 */
	public static List<Role> getRolesByIdOrName(Guild guild, String value, boolean ignoreCase) {
		String processed = ArgumentUtility.getGroup(MentionType.ROLE.getPattern(), 1, value);
		
		if(processed != null) {
			Role role = guild.getRoleById(processed);
			if(role != null) {
				return List.of(role);
			}
		}else if(ID_PATTERN.matcher(value).matches()) {
			Role role = guild.getRoleById(value);
			if(role != null) {
				return List.of(role);
			}
		}
		
		return guild.getRolesByName(value, ignoreCase);
	}
	
	/**
	 * Get emotes by id, mention or name
	 * If a category was found by id it will not check for names
	 * 
	 * @param guild the guild to search for the emotes in
	 * @param value the mention, id or name of the emote to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found emotes
	 */
	public static List<Emote> getEmotesByIdOrName(Guild guild, String value, boolean ignoreCase) {
		Emote emote = ArgumentUtility.getEmoteById(guild, value);
		if(emote != null) {
			return List.of(emote);
		}
		
		return guild.getEmotesByName(value, ignoreCase);
	}
	
	/**
	 * Get text channels by id, mention or name
	 * If a text channel was found by id it will not check for names
	 * 
	 * @param guild the guild to search for the text channels in
	 * @param value the mention, id or name of the text channel to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found text channels
	 */
	public static List<TextChannel> getTextChannelsByIdOrName(Guild guild, String value, boolean ignoreCase) {
		TextChannel channel = ArgumentUtility.getTextChannelById(guild, value);
		if(channel != null) {
			return List.of(channel);
		}
		
		return guild.getTextChannelsByName(value, ignoreCase);
	}
	
	/**
	 * Get voice channels by id or name
	 * If a voice channel was found by id it will not check for names
	 * 
	 * @param guild the guild to search for the voice channels in
	 * @param value the id or name of the voice channel to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found voice channels
	 */
	public static List<VoiceChannel> getVoiceChannelsByIdOrName(Guild guild, String value, boolean ignoreCase) {
		if(ID_PATTERN.matcher(value).matches()) {
			VoiceChannel channel = guild.getVoiceChannelById(value);
			if(channel != null) {
				return List.of(channel);
			}
		}
		
		return guild.getVoiceChannelsByName(value, ignoreCase);
	}
	
	/**
	 * Get categories by id or name.
	 * If a category was found by id it will not check for names
	 * 
	 * @param guild the guild to search for the categories in
	 * @param value the id or name of the category to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found categories
	 */
	public static List<Category> getCategoriesByIdOrName(Guild guild, String value, boolean ignoreCase) {
		if(ID_PATTERN.matcher(value).matches()) {
			Category category = guild.getCategoryById(value);
			if(category != null) {
				return List.of(category);
			}
		}
		
		return guild.getCategoriesByName(value, ignoreCase);
	}
	
	/**
	 * Get users by id, mention, name or tag.
	 * If a user was found by id it will not check for names
	 * 
	 * @param jda the shard manager instance to search for the users in
	 * @param value the id or name of the user to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found user
	 */
	public static List<User> getUsersByIdOrName(JDA jda, String value, boolean ignoreCase) {
		{
			User user = ArgumentUtility.getUserById(jda, value);
			if(user != null) {
				return List.of(user);
			}
		}
		
		Matcher matcher = USER_NAME_PATTERN.matcher(value);
		if(matcher.matches()) {
			String name = matcher.group(1);
			String descriminator = matcher.group(2);
			
			User user = jda.getUserCache().stream()
				.filter(a -> a.getDiscriminator().equals(descriminator))
				.filter(a -> a.getName().equals(name))
				.findFirst()
				.orElse(null);
			
			if(user != null) {
				return List.of(user);
			}
		}
		
		return jda.getUserCache().getElementsByName(value, ignoreCase);
	}
	
	/**
	 * Get users by id, mention, name or tag. 
	 * If a user is found by id it will not check for names
	 * 
	 * @param shardManager the shard manager instance to search for the users in
	 * @param value the id or name of the user to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found users
	 */
	public static List<User> getUsersByIdOrName(ShardManager shardManager, String value, boolean ignoreCase) {
		{
			User user = ArgumentUtility.getUserById(shardManager, value);
			if(user != null) {
				return List.of(user);
			}
		}
		
		Matcher matcher = USER_NAME_PATTERN.matcher(value);
		if(matcher.matches()) {
			String name = matcher.group(1);
			String descriminator = matcher.group(2);
			
			User user = shardManager.getUserCache().stream()
				.filter(a -> a.getDiscriminator().equals(descriminator))
				.filter(a -> a.getName().equals(name))
				.findFirst()
				.orElse(null);
			
			if(user != null) {
				return List.of(user);
			}
		}
		
		return shardManager.getUserCache().getElementsByName(value, ignoreCase);
	}
	
	/**
	 * Get guilds by id or name. 
	 * If a guild is found by id it will not check for names
	 * 
	 * @param jda the JDA instance to search for the guilds in
	 * @param value the id or name of the guild to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found guilds
	 */
	public static List<Guild> getGuildsByIdOrName(JDA jda, String value, boolean ignoreCase) {
		if(ID_PATTERN.matcher(value).matches()) {
			Guild guild = jda.getGuildById(value);
			if(guild != null) {
				return List.of(guild);
			}
		}
		
		return jda.getGuildsByName(value, ignoreCase);
	}
	
	/**
	 * Get guilds by id or name.
	 * If a guild is found by id it will not check for names
	 * 
	 * @param shardManager the shard manager instance to search for the guilds in
	 * @param value the id or name of the guild to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found guilds
	 */
	public static List<Guild> getGuildsByIdOrName(ShardManager shardManager, String value, boolean ignoreCase) {
		if(ID_PATTERN.matcher(value).matches()) {
			Guild guild = shardManager.getGuildById(value);
			if(guild != null) {
				return List.of(guild);
			}
		}
		
		return shardManager.getGuildsByName(value, ignoreCase);
	}
}