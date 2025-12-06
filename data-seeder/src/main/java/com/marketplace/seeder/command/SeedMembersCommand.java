package com.marketplace.seeder.command;

import com.marketplace.common.command.Command;

/**
 * Command to seed member data into PostgreSQL.
 * Input: Number of members to create
 * Output: Number of members actually created
 */
public interface SeedMembersCommand extends Command<Integer, Integer> {
}

