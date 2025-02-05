package com.github.sculkhorde.systems;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.LivingArmorEntity;
import com.github.sculkhorde.core.SculkHorde;
import com.google.common.base.Predicates;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

public class DebugSlimeSystem {
    protected PlayerTeam redDebugTeam;
    public static String redDebugTeamID = "debug_red";
    protected PlayerTeam yellowDebugTeam;
    public static String yellowDebugTeamID = "debug_yellow";
    protected PlayerTeam greenDebugTeam;
    public static String greenDebugTeamID = "debug_green";
    protected PlayerTeam blueDebugTeam;
    public static String blueDebugTeamID = "debug_blue";

    protected ArrayList<Slime> debugSlimes = new ArrayList<>();

    public DebugSlimeSystem()
    {
        createTeams();
    }

    protected void createTeams()
    {
        redDebugTeam = createTeamIfAbsent(redDebugTeamID, Component.literal(redDebugTeamID));
        redDebugTeam.setColor(ChatFormatting.RED);
        yellowDebugTeam = createTeamIfAbsent(yellowDebugTeamID, Component.literal(yellowDebugTeamID));
        yellowDebugTeam.setColor(ChatFormatting.YELLOW);
        greenDebugTeam = createTeamIfAbsent(greenDebugTeamID, Component.literal(greenDebugTeamID));
        greenDebugTeam.setColor(ChatFormatting.GREEN);
        blueDebugTeam = createTeamIfAbsent(blueDebugTeamID, Component.literal(blueDebugTeamID));
        blueDebugTeam.setColor(ChatFormatting.BLUE);
    }

    protected PlayerTeam createTeamIfAbsent(String teamID, Component teamDisplayName) {
        Scoreboard scoreboard = SculkHorde.savedData.level.getServer().getScoreboard();
        if (scoreboard.getPlayerTeam(teamID) != null)
        {
            return scoreboard.getPlayerTeam(teamID);
        }

        PlayerTeam playerteam = scoreboard.addPlayerTeam(teamID);
        playerteam.setDisplayName(teamDisplayName);
        return playerteam;
    }

    public Slime createDebugSlime(Level level, BlockPos pos)
    {
        Slime slime = new Slime(EntityType.SLIME, level);
        slime.setPos(pos.getCenter());
        slime.setInvulnerable(true);
        slime.goalSelector.removeAllGoals(Predicates.alwaysTrue());
        slime.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, Integer.MAX_VALUE));
        level.addFreshEntity(slime);
        return slime;
    }

    public void glowRed(LivingEntity entity)
    {
        joinTeam(redDebugTeam, entity);
    }

    public void glowYellow(LivingEntity entity)
    {
        joinTeam(yellowDebugTeam, entity);
    }

    public void glowGreen(LivingEntity entity)
    {
        joinTeam(greenDebugTeam, entity);
    }

    public void glowBlue(LivingEntity entity)
    {
        joinTeam(blueDebugTeam, entity);
    }

    private void joinTeam(PlayerTeam team, LivingEntity entity) {
        Scoreboard scoreboard = SculkHorde.savedData.level.getServer().getScoreboard();

        scoreboard.addPlayerToTeam(entity.getStringUUID(), team);
    }
}
