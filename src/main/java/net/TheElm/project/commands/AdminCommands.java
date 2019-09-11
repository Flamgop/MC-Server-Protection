/*
 * This software is licensed under the MIT License
 * https://github.com/GStefanowich/MC-Server-Protection
 *
 * Copyright (c) 2019 Gregory Stefanowich
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.TheElm.project.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.TheElm.project.CoreMod;
import net.TheElm.project.config.SewingMachineConfig;
import net.TheElm.project.utilities.TranslatableServerSide;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.stream.Stream;

public final class AdminCommands {
    
    private static final DynamicCommandExceptionType PLAYERS_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType((player) -> 
        TranslatableServerSide.text( (ServerPlayerEntity)player, "player.none_found" )
    );
    
    private AdminCommands() {}
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Register the FLY command
        if (SewingMachineConfig.INSTANCE.COMMAND_FLIGHT_OP_LEVEL.get() >= 0) {
            dispatcher.register(CommandManager.literal("fly")
                .requires((source -> source.hasPermissionLevel(SewingMachineConfig.INSTANCE.COMMAND_FLIGHT_OP_LEVEL.get())))
                .then( CommandManager.argument( "target", EntityArgumentType.players())
                    .executes(AdminCommands::targetFlying)
                )
                .executes(AdminCommands::selfFlying)
            );
            CoreMod.logDebug("- Registered fly command");
        }
        
        // Register the GOD command
        if (SewingMachineConfig.INSTANCE.COMMAND_GODMODE_OP_LEVEL.get() >= 0) {
            dispatcher.register(CommandManager.literal("god")
                .requires(source -> source.hasPermissionLevel(SewingMachineConfig.INSTANCE.COMMAND_GODMODE_OP_LEVEL.get()))
                .then( CommandManager.argument( "target", EntityArgumentType.players())
                    .executes(AdminCommands::targetGod)
                )
                .executes(AdminCommands::selfGod)
            );
            CoreMod.logDebug("- Registered god mode command");
        }
        
        // Register the HEAL command
        if (SewingMachineConfig.INSTANCE.COMMAND_HEAL_OP_LEVEL.get() >= 0) {
            dispatcher.register(CommandManager.literal("heal")
                .requires(source -> source.hasPermissionLevel(SewingMachineConfig.INSTANCE.COMMAND_HEAL_OP_LEVEL.get()))
                .then( CommandManager.argument( "target", EntityArgumentType.players())
                    .executes(AdminCommands::targetHeal)
                )
                .executes(AdminCommands::selfHeal)
            );
            CoreMod.logDebug("- Registered heal command");
        }
    }
    
    private static int selfFlying(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        AdminCommands.toggleFlying(source.getPlayer());
        return Command.SINGLE_SUCCESS;
    }
    private static int targetFlying(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
        if (players.size() <= 0)
            throw PLAYERS_NOT_FOUND_EXCEPTION.create(player);
        return AdminCommands.toggleFlying(player, players.stream());
    }
    private static int toggleFlying(ServerPlayerEntity source, Stream<ServerPlayerEntity> players) {
        players.forEach(player -> TranslatableServerSide.send(source, "player.abilities.flying_other." + (AdminCommands.toggleFlying(player) ? "enabled" : "disabled"), player.getDisplayName()));
        return Command.SINGLE_SUCCESS;
    }
    private static boolean toggleFlying(ServerPlayerEntity player) {
        // Toggle flying for the player
        player.abilities.allowFlying = !player.abilities.allowFlying;
        player.setNoGravity(false);
        
        // Tell the player
        TranslatableServerSide.send(player, "player.abilities.flying_self." + (player.abilities.allowFlying ? "enabled" : "disabled"));
        
        // If flying was turned off, stop the playing mid-flight
        if (!player.abilities.allowFlying)
            player.abilities.flying = false;
        
        player.sendAbilitiesUpdate();
        return player.abilities.allowFlying;
    }
    
    private static int selfGod(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        AdminCommands.toggleGod(source.getPlayer());
        return Command.SINGLE_SUCCESS;
    }
    private static int targetGod(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
        if (players.size() <= 0 )
            throw PLAYERS_NOT_FOUND_EXCEPTION.create( player );
        return AdminCommands.toggleGod(player, players.stream());
    }
    private static int toggleGod(ServerPlayerEntity source, Stream<ServerPlayerEntity> players) {
        players.forEach(player -> TranslatableServerSide.send(source, "player.abilities.godmode_other." + (AdminCommands.toggleGod(player) ? "enabled" : "disabled"), player.getDisplayName()));
        return Command.SINGLE_SUCCESS;
    }
    private static boolean toggleGod(ServerPlayerEntity player) {
        // Toggle god mode for the player
        player.abilities.invulnerable = !player.abilities.invulnerable;
        player.sendAbilitiesUpdate();
    
        // Tell the player
        TranslatableServerSide.send(player, "player.abilities.godmode_self." + (player.abilities.invulnerable ? "enabled" : "disabled"));
        return player.abilities.invulnerable;
    }
    
    private static int selfHeal(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        AdminCommands.healPlayer(source.getPlayer());
        return Command.SINGLE_SUCCESS;
    }
    private static int targetHeal(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
        if (players.size() <= 0 )
            throw PLAYERS_NOT_FOUND_EXCEPTION.create( player );
        return AdminCommands.healPlayer(player, players.stream());
    }
    private static int healPlayer(ServerPlayerEntity source, Stream<ServerPlayerEntity> players) {
        players.forEach(player -> TranslatableServerSide.send(source, (AdminCommands.healPlayer(player)? "player.abilities.healed_other" : "player.abilities.healed_dead"), player.getDisplayName()));
        return Command.SINGLE_SUCCESS;
    }
    private static boolean healPlayer(ServerPlayerEntity player) {
        boolean alive;
        if (alive = player.isAlive()) {
            // Heal the player
            player.setHealth(player.getHealthMaximum());
            
            // Tell the player
            TranslatableServerSide.send(player, "player.abilities.healed_self");
        }
        return alive;
    }
    
}