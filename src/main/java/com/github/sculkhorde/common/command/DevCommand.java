package com.github.sculkhorde.common.command;

import com.github.sculkhorde.common.entity.dev.ChunkInfectEntity;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.chunk_cursor_system.ChunkCursorInfector;
import com.github.sculkhorde.systems.chunk_cursor_system.ChunkCursorPurifier;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;

public class DevCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        defaults();
        return Commands.literal("dev")
                .then(Commands.literal("test")
                        .executes(DevCommand::test)
                )
                .then(Commands.literal("infect")
                        .then(Commands.literal("blocks")
                                .then(Commands.literal("square")
                                        .then(Commands.argument("center", BlockPosArgument.blockPos())
                                                .then(Commands.argument("radius", IntegerArgumentType.integer(0))
                                                        .executes(DevCommand::blockInfectionSquare)
                                                )
                                        )
                                )
                                .then(Commands.literal("area")
                                        .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                                                .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                                                        .executes(DevCommand::blockInfectionRectangle)
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("chunks")
                                .then(Commands.literal("square")
                                        .then(Commands.argument("center", BlockPosArgument.blockPos())
                                                .then(Commands.argument("radius", IntegerArgumentType.integer(0))
                                                        .executes(DevCommand::chunkInfectionSquare)
                                                )
                                        )
                                )
                                .then(Commands.literal("area")
                                        .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                                                .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                                                        .executes(DevCommand::chunkInfectionRectangle)
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("purify")
                        .then(Commands.literal("blocks")
                                .then(Commands.literal("square")
                                        .then(Commands.argument("center", BlockPosArgument.blockPos())
                                                .then(Commands.argument("radius", IntegerArgumentType.integer(0))
                                                        .executes(DevCommand::blockPurificationSquare)
                                                )
                                        )
                                )
                                .then(Commands.literal("area")
                                        .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                                                .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                                                        .executes(DevCommand::blockPurificationRectangle)
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("chunks")
                                .then(Commands.literal("square")
                                        .then(Commands.argument("center", BlockPosArgument.blockPos())
                                                .then(Commands.argument("radius", IntegerArgumentType.integer(0))
                                                        .executes(DevCommand::chunkPurificationSquare)
                                                )
                                        )
                                )
                                .then(Commands.literal("area")
                                        .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                                                .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                                                        .executes(DevCommand::chunkPurificationRectangle)
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("settings")
                        .then(Commands.literal("speed")
                                .then(Commands.argument("blocks_per_tick", IntegerArgumentType.integer(1))
                                        .executes(DevCommand::setBlocksPerTick)
                                )
                        )
                        .then(Commands.literal("fade_distance")
                                .then(Commands.argument("distance", IntegerArgumentType.integer(0))
                                        .executes(DevCommand::setFadeDistance)
                                )
                        )
                        .then(Commands.literal("max_adjacent")
                                .then(Commands.argument("max", IntegerArgumentType.integer(-1))
                                        .executes(DevCommand::setMaxAdjacent)
                                )
                        )
                        .then(Commands.literal("no_features")
                                .then(Commands.argument("no_features", BoolArgumentType.bool())
                                        .executes(DevCommand::setNoFeatures)
                                )
                        )
                        .then(Commands.literal("disable_obstruction")
                                .then(Commands.argument("disable_obstruction", BoolArgumentType.bool())
                                        .executes(DevCommand::setDisableObstruction)
                                )
                        )
                        .then(Commands.literal("solid_fill")
                                .then(Commands.argument("solid_fill", BoolArgumentType.bool())
                                        .executes(DevCommand::setSolidFill)
                                )
                        )
                        .then(Commands.literal("fill")
                                .then(Commands.argument("fill", BoolArgumentType.bool())
                                        .executes(DevCommand::setFill)
                                )
                        )
                        .then(Commands.literal("cave_mode")
                                .then(Commands.argument("cave_mode", BoolArgumentType.bool())
                                        .executes(DevCommand::setCaveMode)
                                )
                        )
                        .then(Commands.literal("reset")
                                .executes(DevCommand::resetSettings)
                        )
                        .then(Commands.literal("dump")
                                .executes(DevCommand::dumpSettings)
                        )
                )
                .then(Commands.literal("stop")
                        .executes(DevCommand::stop)
                )
                .then(Commands.literal("pause")
                        .executes(DevCommand::pause)
                )
                .then(Commands.literal("resume")
                        .executes(DevCommand::resume)
                )
                .then(Commands.literal("summon_infector_entity")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("radius", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("destroy_after", BoolArgumentType.bool())
                                                .executes(DevCommand::entityTest)
                                        )
                                )
                        )
                )
                .then(Commands.literal("summon_infector_entity_on_player")
                        .then(Commands.argument("radius", IntegerArgumentType.integer(0))
                                .executes(DevCommand::entityTestTracked)
                        )
                )
                .then(Commands.literal("help")
                        .executes(DevCommand::help)
                );
    }

    private static int test(CommandContext<CommandSourceStack> context) {
        BlockPos center = BlockPos.containing(context.getSource().getPosition());
        ServerLevel level = context.getSource().getLevel();
        int radius = 2;

        ChunkCursorInfector infector = ChunkCursorInfector.of()
                .chunkCenter(level, center, radius)
                .caveMode(cave_mode)
                .disableObstruction(disable_obstruction)
                .solidFill(solid_fill)
                .fillMode(fill)
                .doNotPlaceFeatures(no_features)
                .blocksPerTick(blocksPerTick)
                .maxAdjacentBlocks(maxAdjacent)
                .fadeDistance(fadeDistance);

        infectors.add(infector);
        infector.executeOnEnd(() -> DevCommand.removeInfector(infector));
        infector.start();

        return 0;
    }

    protected static ArrayList<ChunkCursorInfector> infectors = new ArrayList<>();
    protected static ArrayList<ChunkCursorPurifier> purifiers = new ArrayList<>();

    protected static int blocksPerTick;
    protected static int fadeDistance;
    protected static int maxAdjacent;
    protected static boolean no_features;
    protected static boolean disable_obstruction;
    protected static boolean solid_fill;
    protected static boolean fill;
    protected static boolean cave_mode;

    protected static void defaults() {
        blocksPerTick = 128;
        fadeDistance = 0;
        maxAdjacent = -1;
        no_features = false;
        disable_obstruction = false;
        solid_fill = false;
        fill = false;
        cave_mode = false;
    }

    public static int setBlocksPerTick (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        blocksPerTick = context.getArgument("blocks_per_tick", Integer.class);
        return 0;
    }

    public static int setFadeDistance (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        fadeDistance = context.getArgument("distance", Integer.class);
        return 0;
    }

    public static int setMaxAdjacent (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        maxAdjacent = context.getArgument("max", Integer.class);
        return 0;
    }

    public static int setNoFeatures (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        no_features = context.getArgument("no_features", Boolean.class);
        return 0;
    }

    public static int setDisableObstruction (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        disable_obstruction = context.getArgument("disable_obstruction", Boolean.class);
        return 0;
    }

    public static int setSolidFill (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        solid_fill = context.getArgument("solid_fill", Boolean.class);
        return 0;
    }

    public static int setFill (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        fill = context.getArgument("fill", Boolean.class);
        return 0;
    }

    public static int setCaveMode (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        cave_mode = context.getArgument("cave_mode", Boolean.class);
        return 0;
    }

    public static int resetSettings (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        defaults();
        return 0;
    }

    public static int dumpSettings (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        context.getSource().sendSuccess(() -> Component.literal("speed: " + blocksPerTick + " blocks per tick"), false);
        context.getSource().sendSuccess(() -> Component.literal("fade_distance: " + fadeDistance + " blocks"), false);
        context.getSource().sendSuccess(() -> Component.literal("max_adjacent: " + maxAdjacent + " blocks"), false);
        context.getSource().sendSuccess(() -> Component.literal("cave_mode: " + cave_mode), false);
        context.getSource().sendSuccess(() -> Component.literal("no_features: " + no_features), false);
        context.getSource().sendSuccess(() -> Component.literal("disable_obstruction: " + disable_obstruction), false);
        context.getSource().sendSuccess(() -> Component.literal("solid_fill: " + solid_fill), false);
        context.getSource().sendSuccess(() -> Component.literal("fill: " + fill), false);
        return 0;
    }

    public static int help (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        context.getSource().sendSuccess(() -> Component.literal("------- Setting Commands -------"), false);
        context.getSource().sendSuccess(() -> Component.literal("   speed: Sets how many blocks to change per tick"), false);
        context.getSource().sendSuccess(() -> Component.literal("   fade_distance: Sets how many blocks in from the outer radius to begin randomly not changing blocks"), false);
        context.getSource().sendSuccess(() -> Component.literal("   max_adjacent: Sets how many extra blocks the system can check outside the standard range. Good for overhangs"), false);
        context.getSource().sendSuccess(() -> Component.literal("   cave_mode: Kinda what it says on the tin, bit buggy though"), false);
        context.getSource().sendSuccess(() -> Component.literal("   no_features: When set to True: sculk spawners / grass will not be placed by infectors or purifiers"), false);
        context.getSource().sendSuccess(() -> Component.literal("   disable_obstruction: Will ignore the obstruction code, continuing even when the block isn't exposed to air"), false);
        context.getSource().sendSuccess(() -> Component.literal("   solid_fill: Will change the block even if it isn't exposed to air, good for purifiers"), false);
        context.getSource().sendSuccess(() -> Component.literal("   fill: Will limit the vertical search to the specific area specified"), false);
        context.getSource().sendSuccess(() -> Component.literal("   reset: Resets all settings to their default settings"), false);
        context.getSource().sendSuccess(() -> Component.literal("   dump: Prints the current settings to chat"), false);
        context.getSource().sendSuccess(() -> Component.literal("--------------------------------"), false);
        context.getSource().sendSuccess(() -> Component.literal("------- General Commands -------"), false);
        context.getSource().sendSuccess(() -> Component.literal("   blocks: Radius will function in blocks instead of chunks, allows for more fine control"), false);
        context.getSource().sendSuccess(() -> Component.literal("   chunks: Radius will function in chunks instead of blocks, allows for easier large scale modification"), false);
        context.getSource().sendSuccess(() -> Component.literal("       square: Centers a square on the specified block with the specified radius. Width = (r*2) + 1"), false);
        context.getSource().sendSuccess(() -> Component.literal("       area: Uses two block positions or chunks to fill in the area with the specified type"), false);
        context.getSource().sendSuccess(() -> Component.literal("--------------------------------"), false);
        context.getSource().sendSuccess(() -> Component.literal("-------  Entity Commands -------"), false);
        context.getSource().sendSuccess(() -> Component.literal("Warning! You will likely need to run /kill @e[type=sculkhorde:chunk_infect_entity] after running the commands below"), false);
        context.getSource().sendSuccess(() -> Component.literal("Warning! Always run /kill @e[type=sculkhorde:chunk_infect_entity] on world load to be safe"), false);
        context.getSource().sendSuccess(() -> Component.literal("   summon_infector_entity: Summons an example entity with a Chunk Infector attached, if its told not to destroy itself, it will continue to infect blocks whenever it is moved"), false);
        context.getSource().sendSuccess(() -> Component.literal("   summon_infector_entity_on_player: Summons the above entity but it will tp to the player every 10 ticks, forever infecting the area around it"), false);
        context.getSource().sendSuccess(() -> Component.literal("--------------------------------"), false);
        return 0;
    }

    public static int pause (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        for (ChunkCursorInfector infector : infectors) {infector.stop();}
        for (ChunkCursorPurifier purifier : purifiers) {purifier.stop();}
        return 0;
    }

    public static int resume (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        for (ChunkCursorInfector infector : infectors) {infector.start();}
        for (ChunkCursorPurifier purifier : purifiers) {purifier.start();}
        return 0;
    }

    public static int stop (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ArrayList<ChunkCursorInfector> infectorsB = new ArrayList<>(infectors);
        ArrayList<ChunkCursorPurifier> purifiersB = new ArrayList<>(purifiers);

        for (ChunkCursorInfector infector : infectorsB) {infector.stopAndFinish();}
        for (ChunkCursorPurifier purifier : purifiersB) {purifier.stopAndFinish();}

        return 0;
    }

    public static void removeInfector(ChunkCursorInfector infector) {
        SculkHorde.LOGGER.info("Removing: " + infector + "...");
        infectors.remove(infector);
    }

    public static void removePurifier(ChunkCursorPurifier purifier) {
        SculkHorde.LOGGER.info("Removing: " + purifier + "...");
        purifiers.remove(purifier);
    }

    public static int entityTest (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Level level = context.getSource().getLevel();
        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
        int radius = context.getArgument("radius", Integer.class);
        boolean shouldDestroy = context.getArgument("destroy_after", Boolean.class);

        ChunkInfectEntity entity = new ChunkInfectEntity(level);

        entity.setCenter(pos);
        entity.setRadius(radius);
        entity.shouldDestroy(shouldDestroy);

        entity.infector
                .caveMode(cave_mode)
                .disableObstruction(disable_obstruction)
                .doNotPlaceFeatures(no_features)
                .blocksPerTick(blocksPerTick);

        level.addFreshEntity(entity);

        return 0;
    }

    public static int entityTestTracked (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Level level = context.getSource().getLevel();
        int radius = context.getArgument("radius", Integer.class);
        Entity tracked = context.getSource().getEntity();

        ChunkInfectEntity entity = new ChunkInfectEntity(level);

        entity.setCenter(tracked.blockPosition());
        entity.setRadius(radius);
        entity.shouldDestroy(false);
        entity.setTrackedEntity(tracked);
        entity.infector
                .caveMode(cave_mode)
                .disableObstruction(disable_obstruction)
                .solidFill(solid_fill)
                .fillMode(fill)
                .doNotPlaceFeatures(no_features)
                .blocksPerTick(blocksPerTick);

        level.addFreshEntity(entity);

        return 0;
    }

    public static int blockInfectionSquare (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos center = BlockPosArgument.getBlockPos(context, "center");
        int radius = context.getArgument("radius", Integer.class);

        ChunkCursorInfector infector = ChunkCursorInfector.of()
                .level(context.getSource().getLevel())
                .center(center, radius)
                .caveMode(cave_mode)
                .disableObstruction(disable_obstruction)
                .solidFill(solid_fill)
                .fillMode(fill)
                .doNotPlaceFeatures(no_features)
                .blocksPerTick(blocksPerTick)
                .maxAdjacentBlocks(maxAdjacent)
                .fadeDistance(fadeDistance);

        infectors.add(infector);
        infector.executeOnEnd(() -> DevCommand.removeInfector(infector));
        infector.start();



        return 0;
    }

    public static int blockInfectionRectangle (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos1 = BlockPosArgument.getBlockPos(context, "pos1");
        BlockPos pos2 = BlockPosArgument.getBlockPos(context, "pos2");

        ChunkCursorInfector infector = ChunkCursorInfector.of()
                .level(context.getSource().getLevel())
                .pos1(pos1)
                .pos2(pos2)
                .caveMode(cave_mode)
                .disableObstruction(disable_obstruction)
                .solidFill(solid_fill)
                .fillMode(fill)
                .doNotPlaceFeatures(no_features)
                .blocksPerTick(blocksPerTick)
                .maxAdjacentBlocks(maxAdjacent)
                .fadeDistance(fadeDistance);

        infectors.add(infector);
        infector.executeOnEnd(() -> DevCommand.removeInfector(infector));
        infector.start();

        return 0;
    }

    public static int chunkInfectionSquare (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos center = BlockPosArgument.getBlockPos(context, "center");
        ServerLevel level = context.getSource().getLevel();
        int radius = context.getArgument("radius", Integer.class);

        ChunkCursorInfector infector = ChunkCursorInfector.of()
                .chunkCenter(level, center, radius)
                .caveMode(cave_mode)
                .disableObstruction(disable_obstruction)
                .solidFill(solid_fill)
                .fillMode(fill)
                .doNotPlaceFeatures(no_features)
                .blocksPerTick(blocksPerTick)
                .maxAdjacentBlocks(maxAdjacent)
                .fadeDistance(fadeDistance);

        infectors.add(infector);
        infector.executeOnEnd(() -> DevCommand.removeInfector(infector));
        infector.start();

        return 0;
    }

    public static int chunkInfectionRectangle (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos1 = BlockPosArgument.getBlockPos(context, "pos1");
        BlockPos pos2 = BlockPosArgument.getBlockPos(context, "pos2");

        ServerLevel level = context.getSource().getLevel();

        LevelChunk chunk1 = level.getChunkAt(pos1);
        LevelChunk chunk2 = level.getChunkAt(pos2);

        ChunkCursorInfector infector = ChunkCursorInfector.of()
                .chunkArea(chunk1, chunk2)
                .caveMode(cave_mode)
                .disableObstruction(disable_obstruction)
                .solidFill(solid_fill)
                .fillMode(fill)
                .doNotPlaceFeatures(no_features)
                .blocksPerTick(blocksPerTick)
                .maxAdjacentBlocks(maxAdjacent)
                .fadeDistance(fadeDistance);

        infectors.add(infector);
        infector.executeOnEnd(() -> DevCommand.removeInfector(infector));
        infector.start();

        return 0;
    }

    // ----------------

    public static int blockPurificationSquare (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos center = BlockPosArgument.getBlockPos(context, "center");
        int radius = context.getArgument("radius", Integer.class);

        ChunkCursorPurifier purifier = ChunkCursorPurifier.of()
                .level(context.getSource().getLevel())
                .center(center, radius)
                .caveMode(cave_mode)
                .disableObstruction(disable_obstruction)
                .solidFill(solid_fill)
                .fillMode(fill)
                .doNotPlaceFeatures(no_features)
                .blocksPerTick(blocksPerTick)
                .maxAdjacentBlocks(maxAdjacent)
                .fadeDistance(fadeDistance);

        purifiers.add(purifier);
        purifier.executeOnEnd(() -> DevCommand.removePurifier(purifier));
        purifier.start();

        return 0;
    }

    public static int blockPurificationRectangle (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos1 = BlockPosArgument.getBlockPos(context, "pos1");
        BlockPos pos2 = BlockPosArgument.getBlockPos(context, "pos2");

        ChunkCursorPurifier purifier = ChunkCursorPurifier.of()
                .level(context.getSource().getLevel())
                .pos1(pos1)
                .pos2(pos2)
                .caveMode(cave_mode)
                .disableObstruction(disable_obstruction)
                .solidFill(solid_fill)
                .fillMode(fill)
                .doNotPlaceFeatures(no_features)
                .blocksPerTick(blocksPerTick)
                .maxAdjacentBlocks(maxAdjacent)
                .fadeDistance(fadeDistance);

        purifiers.add(purifier);
        purifier.executeOnEnd(() -> DevCommand.removePurifier(purifier));
        purifier.start();

        return 0;
    }

    public static int chunkPurificationSquare (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos center = BlockPosArgument.getBlockPos(context, "center");
        ServerLevel level = context.getSource().getLevel();
        int radius = context.getArgument("radius", Integer.class);

        ChunkCursorPurifier purifier = ChunkCursorPurifier.of()
                .chunkCenter(level, center, radius)
                .caveMode(cave_mode)
                .disableObstruction(disable_obstruction)
                .solidFill(solid_fill)
                .fillMode(fill)
                .doNotPlaceFeatures(no_features)
                .blocksPerTick(blocksPerTick)
                .maxAdjacentBlocks(maxAdjacent)
                .fadeDistance(fadeDistance);

        purifiers.add(purifier);
        purifier.executeOnEnd(() -> DevCommand.removePurifier(purifier));
        purifier.start();

        return 0;
    }

    public static int chunkPurificationRectangle (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos1 = BlockPosArgument.getBlockPos(context, "pos1");
        BlockPos pos2 = BlockPosArgument.getBlockPos(context, "pos2");

        ServerLevel level = context.getSource().getLevel();

        LevelChunk chunk1 = level.getChunkAt(pos1);
        LevelChunk chunk2 = level.getChunkAt(pos2);

        ChunkCursorPurifier purifier = ChunkCursorPurifier.of()
                .chunkArea(chunk1, chunk2)
                .caveMode(cave_mode)
                .disableObstruction(disable_obstruction)
                .solidFill(solid_fill)
                .fillMode(fill)
                .doNotPlaceFeatures(no_features)
                .blocksPerTick(blocksPerTick)
                .maxAdjacentBlocks(maxAdjacent)
                .fadeDistance(fadeDistance);

        purifiers.add(purifier);
        purifier.executeOnEnd(() -> DevCommand.removePurifier(purifier));
        purifier.start();

        return 0;
    }

    @Override public int run(CommandContext<CommandSourceStack> context) {return 0;}
}