package com.jockie.bot.core.utility;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message.MentionType;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.EmoteImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.RestAction.EmptyRestAction;
import net.dv8tion.jda.core.utils.Checks;

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
	public static Role getRole(Guild guild, String value) {
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
	public static Member getMember(Guild guild, String value) {
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
	public static TextChannel getTextChannel(Guild guild, String value) {
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
	public static Emote getEmote(Guild guild, String value) {
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
	public static User getUser(JDA jda, String value) {
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
	public static User getUser(ShardManager shardManager, String value) {
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
	 * @return RestAction - Type: User</br>
	 * On request, gets the User with id matching provided id from Discord.
	 */
	public static RestAction<User> retrieveUser(JDA jda, String value) {
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
	 * @return RestAction - Type: User</br>
	 * On request, gets the User with id matching provided id from Discord.
	 */
	public static RestAction<User> retrieveUser(ShardManager shardManager, String value) {
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
	 * @return RestAction - Type: User</br>
	 * On request, gets the User with id matching provided id from Discord.
	 */
	public static RestAction<User> retrieveUser(ShardManager shardManager, JDA jda, String value) {
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
	 * Get a member by id, mention, effective name or tag
	 * 
	 * @param guild the guild to search for the member in
	 * @param value the mention, id or tag of the member
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found member, may be null
	 */
	public static Member getMemberByIdOrTag(Guild guild, String value, boolean ignoreCase) {
		{
			Member member = ArgumentUtility.getMember(guild, value);
			if(member != null) {
				return member;
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
				return member;
			}
		}
		
		List<Member> members = guild.getMembersByEffectiveName(value, ignoreCase);
		if(members.size() == 1) {
			return members.get(0);
		}
		
		return null;
	}
	
	/**
	 * Get a role by id, mention or name
	 * 
	 * @param guild the guild to search for the role in
	 * @param value the mention, id or name of the role
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found role, may be null
	 */
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
	
	/**
	 * Get an emote by id, mention or name
	 * 
	 * @param guild the guild to search for the emote in
	 * @param value the mention, id or name of the emote
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found emote, may be null
	 */
	public static Emote getEmoteByIdOrName(Guild guild, String value, boolean ignoreCase) {
		Emote emote = ArgumentUtility.getEmote(guild, value);
		if(emote != null) {
			return emote;
		}
		
		List<Emote> emotes = guild.getEmotesByName(value, ignoreCase);
		if(emotes.size() == 1) {
			emote = emotes.get(0);
		}
		
		return emote;
	}
	
	/**
	 * Get a text channel by id, mention or name
	 * 
	 * @param guild the guild to search for the text channel in
	 * @param value the mention, id or name of the text channel
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found text channel, may be null
	 */
	public static TextChannel getTextChannelByIdOrName(Guild guild, String value, boolean ignoreCase) {
		TextChannel channel = ArgumentUtility.getTextChannel(guild, value);
		if(channel != null) {
			return channel;
		}
		
		List<TextChannel> channels = guild.getTextChannelsByName(value, ignoreCase);
		if(channels.size() == 1) {
			return channels.get(0);
		}
		
		return null;
	}
	
	/**
	 * Get a voice channel by id or name
	 * 
	 * @param guild the guild to search for the voice channel in
	 * @param value the id or name of the voice channel
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found voice channel, may be null
	 */
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
	
	/**
	 * Get a category by id or name
	 * 
	 * @param guild the guild to search for the category in
	 * @param value the id or name of the category
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found category, may be null
	 */
	public static Category getCategoryByIdOrName(Guild guild, String value, boolean ignoreCase) {
		if(ID_PATTERN.matcher(value).matches()) {
			return guild.getCategoryById(value);
		}
		
		List<Category> categories = guild.getCategoriesByName(value, ignoreCase);
		if(categories.size() == 1) {
			return categories.get(0);
		}
		
		return null;
	}
	
	/**
	 * Get a user by id, mention, name or tag
	 * 
	 * @param jda the shard manager instance to search for the user in
	 * @param value the id or name of the user
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found user, may be null
	 */
	public static User getUserByIdOrTag(JDA jda, String value, boolean ignoreCase) {
		{
			User user = ArgumentUtility.getUser(jda, value);
			if(user != null) {
				return user;
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
				return user;
			}
		}
		
		List<User> users = jda.getUserCache().getElementsByName(value, ignoreCase);
		if(users.size() == 1) {
			return users.get(0);
		}
		
		return null;
	}
	
	/**
	 * Get a user by id, mention, name or tag
	 * 
	 * @param shardManager the shard manager instance to search for the user in
	 * @param value the id or name of the user
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found user, may be null
	 */
	public static User getUserByIdOrTag(ShardManager manager, String value, boolean ignoreCase) {
		{
			User user = ArgumentUtility.getUser(manager, value);
			if(user != null) {
				return user;
			}
		}
		
		Matcher matcher = USER_NAME_PATTERN.matcher(value);
		if(matcher.matches()) {
			String name = matcher.group(1);
			String descriminator = matcher.group(2);
			
			User user = manager.getUserCache().stream()
				.filter(a -> a.getDiscriminator().equals(descriminator))
				.filter(a -> a.getName().equals(name))
				.findFirst()
				.orElse(null);
			
			if(user != null) {
				return user;
			}
		}
		
		List<User> users = manager.getUserCache().getElementsByName(value, ignoreCase);
		if(users.size() == 1) {
			return users.get(0);
		}
		
		return null;
	}
	
	/**
	 * Get a guild by id or name
	 * 
	 * @param jda the JDA instance to search for the guild in
	 * @param value the id or name of the guild
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found guild, may be null
	 */
	public static Guild getGuildByIdOrName(JDA jda, String value, boolean ignoreCase) {
		if(ID_PATTERN.matcher(value).matches()) {
			return jda.getGuildById(value);
		}
		
		List<Guild> guilds = jda.getGuildsByName(value, ignoreCase);
		if(guilds.size() == 1) {
			return guilds.get(0);
		}
		
		return null;
	}
	
	/**
	 * Get a guild by id or name
	 * 
	 * @param shardManager the shard manager instance to search for the guild in
	 * @param value the id or name of the guild
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found guild, may be null
	 */
	public static Guild getGuildByIdOrName(ShardManager shardManager, String value, boolean ignoreCase) {
		if(ID_PATTERN.matcher(value).matches()) {
			return shardManager.getGuildById(value);
		}
		
		List<Guild> guilds = shardManager.getGuildsByName(value, ignoreCase);
		if(guilds.size() == 1) {
			return guilds.get(0);
		}
		
		return null;
	}
}