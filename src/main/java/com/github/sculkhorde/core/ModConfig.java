package com.github.sculkhorde.core;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ModConfig {

    public static final Server SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    public static final DataGen DATAGEN;
    public static final ForgeConfigSpec DATAGEN_SPEC;

    public static class Server {

        public final ForgeConfigSpec.ConfigValue<Boolean> target_faw_entities;
        public final ForgeConfigSpec.ConfigValue<Boolean> target_spore_entities;
        public final ForgeConfigSpec.ConfigValue<Boolean> target_dawn_of_the_flood_entities;
        public final ForgeConfigSpec.ConfigValue<Boolean> target_the_flesh_that_hates_entities;
        public final ForgeConfigSpec.ConfigValue<Boolean> target_abominations_infection_entities;
        public final ForgeConfigSpec.ConfigValue<Boolean> target_another_dimension_infection_entities;
        public final ForgeConfigSpec.ConfigValue<Boolean> target_complete_distortion_entities;
        public final ForgeConfigSpec.ConfigValue<Boolean> target_entomophobia_entities;
        public final ForgeConfigSpec.ConfigValue<Boolean> target_phayriosis_entities;
        public final ForgeConfigSpec.ConfigValue<Boolean> target_prion_infection_entities;
        public final ForgeConfigSpec.ConfigValue<Boolean> target_swarm_infection_entities;
        public final ForgeConfigSpec.ConfigValue<Boolean> target_bulbus_infection_entities;
        public final ForgeConfigSpec.ConfigValue<Boolean> target_withering_away_reborn_entities;
        public final ForgeConfigSpec.ConfigValue<Boolean> target_mi_alliance_entities;
        public final ForgeConfigSpec.ConfigValue<Boolean> target_deeper_and_darker_entities;
        public final ForgeConfigSpec.ConfigValue<Boolean> target_scape_and_run_parasites_entities;



        public final ForgeConfigSpec.ConfigValue<Boolean> block_infestation_enabled;
        public final ForgeConfigSpec.ConfigValue<Boolean> chunk_loading_enabled;
        public final ForgeConfigSpec.ConfigValue<Boolean> disable_defeating_sculk_horde;
        public final ForgeConfigSpec.ConfigValue<Integer> max_unit_population;
        public final ForgeConfigSpec.ConfigValue<Boolean> trigger_ancient_node_automatically;
        public final ForgeConfigSpec.ConfigValue<Integer> trigger_ancient_node_wait_days;
        public final ForgeConfigSpec.ConfigValue<Integer> trigger_ancient_node_time_of_day;
        public final ForgeConfigSpec.ConfigValue<Boolean> should_all_other_mobs_attack_the_sculk_horde;

        public final ForgeConfigSpec.ConfigValue<Integer> gravemind_mass_goal_for_immature_stage;
        public final ForgeConfigSpec.ConfigValue<Integer> gravemind_mass_goal_for_mature_stage;

        public final ForgeConfigSpec.ConfigValue<Integer> sculk_node_chunkload_radius;
        public final ForgeConfigSpec.ConfigValue<Integer> sculk_node_spawn_cooldown_minutes;

        public final ForgeConfigSpec.ConfigValue<Boolean> should_sculk_mites_spawn_in_deep_dark;

        public final ForgeConfigSpec.ConfigValue<Boolean> should_phantoms_load_chunks;
        public final ForgeConfigSpec.ConfigValue<Boolean> should_sculk_nodes_and_raids_spawn_phantoms;
        public final ForgeConfigSpec.ConfigValue<Boolean> should_ancient_node_spawn_phantoms;
        
        public final ForgeConfigSpec.ConfigValue<Boolean> sculk_raid_enabled;
        public final ForgeConfigSpec.ConfigValue<Integer> sculk_raid_enderman_scouting_duration_minutes;
        public final ForgeConfigSpec.ConfigValue<Integer> sculk_raid_global_cooldown_between_raids_minutes;
        public final ForgeConfigSpec.ConfigValue<Integer> sculk_raid_no_raid_zone_duration_minutes;

        public final ForgeConfigSpec.ConfigValue<Boolean> experimental_features_enabled;
        public final ForgeConfigSpec.ConfigValue<Double> purification_speed_multiplier;
        public final ForgeConfigSpec.ConfigValue<Integer> infestation_purifier_range;
        private final ForgeConfigSpec.ConfigValue<List<? extends String>> items_infection_cursors_can_eat;
        public static final HashMap<String, Boolean> infection_cursor_item_eat_list = new HashMap<>();

        private final ForgeConfigSpec.ConfigValue<List<? extends String>> make_block_infestable;
        public static final HashMap<String, Boolean> manually_configured_infestable_blocks = new HashMap<>();

        public final ForgeConfigSpec.DoubleValue infection_speed_multiplier;
        public final ForgeConfigSpec.ConfigValue<Integer> max_nodes_active;
        public final ForgeConfigSpec.ConfigValue<Boolean> disable_auto_performance_system;

        public final ForgeConfigSpec.ConfigValue<Integer> minutes_required_for_performance_increase;
        public final ForgeConfigSpec.ConfigValue<Integer> seconds_required_for_performance_decrease;

        private final ForgeConfigSpec.ConfigValue<List<? extends String>> sculk_horde_target_blacklist;

        public final ForgeConfigSpec.ConfigValue<Integer> max_infestation_cursor_population;

        public void loadItemsInfectionCursorsCanEat()
        {
            infection_cursor_item_eat_list.clear();
            for(String item : ModConfig.SERVER.items_infection_cursors_can_eat.get())
            {
                infection_cursor_item_eat_list.put(item, true);
            }
        }

        public boolean isItemEdibleToCursors(ItemEntity itemEntity)
        {
            ItemStack itemStack = itemEntity.getItem();
            Item item = itemStack.getItem();
            ResourceLocation itemResourceLocation = BuiltInRegistries.ITEM.getKey(item);


            if(itemResourceLocation == null)
            {
                return false;
            }

            String itemName = itemResourceLocation.toString();
            if(infection_cursor_item_eat_list.containsKey(itemName))
            {
                return true;
            }

            if(item.isEdible())
            {
                return true;
            }

            if (itemName.contains("sapling")) {
                return true;
            }

            return false;
        }

        public void loadConfiguredInfestableBlocks()
        {
            manually_configured_infestable_blocks.clear();
            for(String block : ModConfig.SERVER.make_block_infestable.get())
            {
                manually_configured_infestable_blocks.put(block, true);
            }
        }

        public boolean isBlockConfiguredToBeInfestable(BlockState blockState)
        {
            Block block = blockState.getBlock();
            ResourceLocation itemResourceLocation = BuiltInRegistries.BLOCK.getKey(block);


            if(itemResourceLocation == null)
            {
                return false;
            }

            String blockName = itemResourceLocation.toString();
            if(manually_configured_infestable_blocks.containsKey(blockName))
            {
                return true;
            }

            return false;
        }

        public boolean isEntityOnSculkHordeTargetBlacklist(Entity entity)
        {
            ResourceLocation entityResourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            String entityNameSpace = entityResourceLocation.toString();

            if(sculk_horde_target_blacklist.get().contains(entityNameSpace))
            {
                return true;
            }

            return false;
        }

        public Server(ForgeConfigSpec.Builder builder) {

            Config.setInsertionOrderPreserved(true);

            builder.push("Performance Settings");
            disable_auto_performance_system = builder.comment("Should the automatic performance system be disabled? (Default False)").define("disable_auto_performance_system", false);
            max_unit_population = builder.comment("How many sculk mobs should be allowed to exist at one time? (Default 200)").defineInRange("max_unit_population",200, 0, 1000);
            max_nodes_active = builder.comment("How many nodes can be active at once? (Default 1)").defineInRange("max_nodes_active",1, 0, 1000);
            minutes_required_for_performance_increase = builder.comment("How many MINUTES of good performance required before increasing performance mode? (Default 5)").defineInRange("minutes_required_for_performance_increase",5, 1, Integer.MAX_VALUE);
            seconds_required_for_performance_decrease = builder.comment("How many SECONDS of poor performance required before decreasing performance mode? (Default 30)").defineInRange("seconds_required_for_performance_decrease",30, 1, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Mod Compatability");
            target_faw_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'From Another World'? (Default false)").define("target_faw_entities",false);
            target_spore_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'Fungal Infection:Spore'? (Default false)").define("target_spore_entities",false);
            target_deeper_and_darker_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'Deeper and Darker'? (Default true)").define("target_deeper_and_darker_entities",true);
            target_mi_alliance_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'Mi Alliance'? (Default true)").define("target_mi_alliance_entities",true);
            target_scape_and_run_parasites_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'Scape and Run Parsites'? (Default true)").define("target_scape_and_run_parasites_entities",true);
            target_the_flesh_that_hates_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'The Flesh That Hates'? (Default true)").define("target_the_flesh_that_hates_entities",true);
            target_dawn_of_the_flood_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'Dawn of the Flood'? (Default true)").define("target_dawn_of_the_flood_entities",true);
            target_prion_infection_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'Prion Infection'? (Default true)").define("target_prion_infection_entities",true);
            target_withering_away_reborn_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'Withering Away: Reborn'? (Default true)").define("target_withering_away_reborn_entities",true);
            target_entomophobia_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'Entomophobia'? (Default true)").define("target_entomophobia_entities",true);
            target_abominations_infection_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'Abominations Infection'? (Default true)").define("target_abominations_infection_entities",true);
            target_another_dimension_infection_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'Another Dimension Infection'? (Default true)").define("target_another_dimension_infection_entities",true);
            target_phayriosis_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'Pharyriosis Parasite Infection'? (Default true)").define("target_phayriosis_entities",true);
            target_complete_distortion_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'Complete Distortion: Infection from Otherworld'? (Default true)").define("target_complete_distortion_entities",true);
            target_swarm_infection_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'Swarm Infection'? (Default true)").define("target_swarm_infection_entities",true);
            target_bulbus_infection_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'The Bulbus Infection'? (Default true)").define("target_bulbus_infection_entities",true);
            builder.pop();

            builder.push("General Variables");
            should_all_other_mobs_attack_the_sculk_horde = builder.comment("Should all other entities attack the sculk horde by default? (Default true)").define("should_all_other_mobs_attack_the_sculk_horde",true);
            block_infestation_enabled = builder.comment("Should the Sculk Horde infest blocks? (Default true)").define("block_infestation_enabled",true);
            chunk_loading_enabled = builder.comment("Should the Sculk Horde load chunks? If disabled, and will ruin the intended experience. For example, raids wont work properly (Default true)").define("chunk_loading_enabled",true);
            disable_defeating_sculk_horde = builder.comment("Should players be able to defeat the Sculk Horde?").define("disable_defeating_sculk_horde",false);
            sculk_horde_target_blacklist = builder.comment("Add entities to this list to stop the sculk horde from attacking them. I.E. minecraft:creeper. Be careful what you put in here, this can potentially lead to issues.").defineList("sculk_horde_target_blacklist", Arrays.asList(""), entry -> true);
            builder.pop();

            builder.push("Trigger Automatically Variables");
            trigger_ancient_node_automatically = builder.comment("Should the Sculk Horde start automatically? Requires that chunk loading is enabled to work reliably, otherwise will only trigger if the ancient node's chunk is loaded. If enabled on a save where previously disabled, the node will trigger automatically if the time conditions are met. (Default false)").define("trigger_ancient_node_automatically", false);
            trigger_ancient_node_wait_days = builder.comment("How many days to wait before triggering the ancient node? (Default 0)").defineInRange("trigger_ancient_node_wait_days", 0, 0, Integer.MAX_VALUE);
            trigger_ancient_node_time_of_day = builder.comment("What time of day in ticks must pass before triggering the ancient node after the wait days have elapsed? If wait days is set to 0, set time of day to a time greater than 1000 ticks to allow for world startup and lag to finish (Default 2000)").defineInRange("trigger_ancient_node_time_of_day", 2000, 0, 23999);
            builder.pop();

            builder.push("Infestation / Purification Variables");
            purification_speed_multiplier = builder.comment("How much faster or slower should purification spread? (Default 1)").defineInRange("purification_speed_multiplier",1f, 0.001, 10f);
            infection_speed_multiplier = builder.comment("How much faster or slower should infection spread? (Default 1)").defineInRange("infection_speed_multiplier",1.0, 0.001, 10);
            max_infestation_cursor_population = builder.comment("How many infestation cursors are allowed to exist at one time. WARNING: This only applies if performance mode is at HIGH. To keep performance at HIGH, set disable_auto_performance_system to true (Default 200)").defineInRange("max_infestation_cursor_population", 200, 1, Integer.MAX_VALUE);
            infestation_purifier_range = builder.comment("How far should the infestation purifier reach? (Default 5)").defineInRange("purifier_range",48, 0, 100);
            items_infection_cursors_can_eat = builder.comment("What dropped items should cursors eat? This prevents lag and boosts their lifespan.").defineList("items_infection_cursors_can_eat", Arrays.asList("minecraft:wheat_seeds", "minecraft:bamboo", "minecraft:stick", "minecraft:poppy", "minecraft:dandelion", "minecraft:blue_orchid", "minecraft:allium", "minecraft:azure_bluet", "minecraft:red_tulip", "minecraft:orange_tulip", "minecraft:white_tulip", "minecraft:pink_tulip", "minecraft:oxeye_daisy", "minecraft:cornflower", "minecraft:lily_of_the_valley", "minecraft:sunflower", "minecraft:lilac", "minecraft:rose_bush", "minecraft:peony"), entry -> true);
            make_block_infestable = builder.comment("Add blocks to this list to make them infestable. I.E. minecraft:dirt. Be careful what you put in here, this can potentially lead to issues. This will not work with blocks that are air, have a block entity, are already considered an infested block, or have the not infestable tag.").defineList("make_block_infestable", Arrays.asList(""), entry -> true);
            builder.pop();

            builder.push("Gravemind Variables");
            gravemind_mass_goal_for_immature_stage = builder.comment("How much mass is needed for the Gravemind to enter the immature stage? (Default 5000)").defineInRange("gravemind_mass_goal_for_immature_stage",5000, 0, Integer.MAX_VALUE);
            gravemind_mass_goal_for_mature_stage = builder.comment("How much mass is needed for the Gravemind to enter the mature stage? (Default 20000)").defineInRange("gravemind_mass_goal_for_mature_stage",20000, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Sculk Node Variables");
            sculk_node_chunkload_radius = builder.comment("How many chunks should be loaded around a sculk node? (Default 15)").defineInRange("sculk_node_chunkload_radius",15, 0, 15);
            sculk_node_spawn_cooldown_minutes = builder.comment("How many minutes should pass before another Sculk node can spawn? (Default 120)").defineInRange("sculk_node_spawn_cooldown_minutes",120, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Sculk Mite Variables");
            should_sculk_mites_spawn_in_deep_dark = builder.comment("Should sculk mites spawn in deep dark? (Default false)").define("should_sculk_mites_spawn_in_deep_dark",false);
            builder.pop();

            builder.push("Sculk Phantom Variables");
            should_phantoms_load_chunks = builder.comment("Should sculk phantoms load chunks? (Default true)").define("should_phantoms_load_chunks",true);
            should_sculk_nodes_and_raids_spawn_phantoms = builder.comment("Should sculk phantoms be spawned by sculk nodes and raids? (Default true)").define("should_sculk_nodes_and_raids_spawn_phantoms",true);
            should_ancient_node_spawn_phantoms = builder.comment("Should sculk phantoms be spawned when the ancient node is triggered? (Default true)").define("should_ancient_node_spawn_phantoms",true);
            builder.pop();

            builder.push("Experimental Features");
            experimental_features_enabled = builder.comment("Should experimental features be enabled? (Default false)").define("experimental_features_enabled",false);
            builder.pop();

            builder.push("Sculk Raid Variables");
            sculk_raid_enabled = builder.comment("Should sculk raids be enabled? (Default true)").define("sculk_raid_enabled",true);
            sculk_raid_enderman_scouting_duration_minutes = builder.comment("How long should the Sculk Enderman scout for? (Default 8)").defineInRange("sculk_raid_enderman_scouting_duration_minutes",8, 0, Integer.MAX_VALUE);
            sculk_raid_global_cooldown_between_raids_minutes = builder.comment("How long should the global cooldown between raids be in minutes? (Default 300)").defineInRange("sculk_raid_global_cooldown_between_raids_minutes", 300 , 0, Integer.MAX_VALUE);
            sculk_raid_no_raid_zone_duration_minutes = builder.comment("How long should the no raid zone last at a location in minutes? This occurs when a raid succeeds or fails so that the same location is not raided for a while. (Default 480)").defineInRange("sculk_raid_no_raid_zone_duration_minutes", 480 , 0, Integer.MAX_VALUE);
            builder.pop();
        }
    }

    public static boolean isExperimentalFeaturesEnabled() {
        return SERVER.experimental_features_enabled.get();
    }

    public static class DataGen {

        public DataGen(ForgeConfigSpec.Builder builder){

        }

    }

    static {
        Pair<Server, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER = commonSpecPair.getLeft();
        SERVER_SPEC = commonSpecPair.getRight();

        Pair<DataGen , ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(DataGen::new);
        DATAGEN = commonPair.getLeft();
        DATAGEN_SPEC = commonPair.getRight();

    }

    public static void loadConfig(ForgeConfigSpec config, String path) {
        final CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(WritingMode.REPLACE).build();
        file.load();
        config.setConfig(file);
    }
}
