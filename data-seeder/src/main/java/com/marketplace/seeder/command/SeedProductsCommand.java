package com.marketplace.seeder.command;

import com.marketplace.common.command.Command;

/**
 * Command to seed product data into MongoDB.
 * Input: Number of products to create
 * Output: Number of products actually created
 */
public interface SeedProductsCommand extends Command<Integer, Integer> {
}

