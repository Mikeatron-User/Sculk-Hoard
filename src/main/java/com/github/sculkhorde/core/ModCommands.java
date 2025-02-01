package com.github.sculkhorde.core;

import com.github.sculkhorde.common.command.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;

public class ModCommands {

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RegisterCommandsEvent.class, ModCommands::registerCommands);
    }

    private static void registerCommands(final RegisterCommandsEvent ev) {
        registerSubCommands(ev.getDispatcher(), ev.getBuildContext());
    }

    public static void registerSubCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal(SculkHorde.MOD_ID)
                .then(MassCommand.register(dispatcher, buildContext))
                .then(GravemindCommand.register(dispatcher, buildContext))
                .then(StatusCommand.register(dispatcher, buildContext))
                .then(StatusAllCommand.register(dispatcher, buildContext))
                .then(RaidCommand.register(dispatcher, buildContext))
                .then(StatisticsCommand.register(dispatcher, buildContext))
                .then(PlayerStatusCommand.register(dispatcher, buildContext))
                .then(ConfigCommand.register(dispatcher, buildContext))
                .then(SummonReinforcementsCommand.register(dispatcher, buildContext))
                .then(NodesStatusCommand.register(dispatcher, buildContext))
                .then(VesselCommand.register(dispatcher, buildContext))
                .then(ResetCommand.register(dispatcher, buildContext))
                .then(SoulReaperCommand.register(dispatcher, buildContext))
                .then(DevCommand.register(dispatcher, buildContext));
        dispatcher.register(cmd);
    }
}
