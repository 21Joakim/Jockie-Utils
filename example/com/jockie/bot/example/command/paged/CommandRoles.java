package com.jockie.bot.example.command.paged;

import com.jockie.bot.core.command.impl.CommandImpl;
import com.jockie.bot.core.paged.impl.PagedManager;
import com.jockie.bot.core.paged.impl.PagedResult;

import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandRoles extends CommandImpl {

	public CommandRoles() {
		super("roles");
		
		super.setDescription("Get a list of all roles");
	}
	
	public void onCommand(MessageReceivedEvent event) {
		PagedResult<Role> paged = new PagedResult<Role>(event.getGuild().getRoles(), Role::getAsMention /* Alternatively: role -> role.getAsMention() */);
		paged.setListIndexesContinuously(true);
		paged.setTimeout(false);
		
		PagedManager.addPagedResult(event, paged);
	}
}