package com.wonginnovations.oldresearch.common.lib.events;

import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.KilledByPlayer;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.SetMetadata;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thaumcraft.api.items.ItemsTC;

@Mod.EventBusSubscriber
public abstract class WorldEvents {

    private static final LootCondition[] CHANCE = new LootCondition[] { new RandomChance(0.3F) };

    private static final LootEntry[] SOME = new LootEntry[] {
        new LootEntryItem(
            ItemsTC.curio,
            1,
            1,
            new LootFunction[]{
                new SetMetadata(new LootCondition[0], new RandomValueRange(7)),
                new SetCount(new LootCondition[0], new RandomValueRange(1,3))
            },
            CHANCE,
            "knowledgefrag_chance"
        )
    };

    private static final LootEntry[] MORE = new LootEntry[] {
        new LootEntryItem(
            ItemsTC.curio,
            1,
            1,
            new LootFunction[]{
                new SetMetadata(new LootCondition[0], new RandomValueRange(7)),
                new SetCount(new LootCondition[0], new RandomValueRange(3,6))
            },
            CHANCE,
            "knowledgefrag_chance"
        )
    };

    private static final LootEntry[] ALWAYS = new LootEntry[] {
        new LootEntryItem(
            ItemsTC.curio,
            1,
            1,
            new LootFunction[]{
                new SetMetadata(new LootCondition[0], new RandomValueRange(7)),
                new SetCount(new LootCondition[0], new RandomValueRange(1,3))
            },
            new LootCondition[0],
            "knowledgefrag_chance"
        )
    };

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        switch (event.getName().toString()) {
            case "minecraft:chests/stronghold_library":
            case "minecraft:chests/end_city_treasure":
                event.getTable().addPool(
                        new LootPool(MORE, new LootCondition[0], new RandomValueRange(1,3), new RandomValueRange(0), "knowledgefrags")
                );
                break;
            case "minecraft:chests/nether_bridge":
            case "minecraft:chests/woodland_mansion":
            case "minecraft:chests/stronghold_crossing":
            case "minecraft:chests/stronghold_corridor":
            case "minecraft:chests/simple_dungeon":
            case "minecraft:chests/jungle_temple":
            case "minecraft:chests/igloo_chest":
            case "minecraft:chests/desert_pyramid":
            case "minecraft:chests/abandoned_mineshaft":
            case "minecraft:chests/village_blacksmith":
            case "minecraft:fishing/treasure":
                event.getTable().addPool(
                    new LootPool(SOME, new LootCondition[0], new RandomValueRange(1,3), new RandomValueRange(0), "knowledgefrags")
                );
                break;
            case "thaumcraft:cultist":
                event.getTable().addPool(
                    new LootPool(
                        SOME,
                        new LootCondition[]{
                            new RandomChance(0.5f),
                            new KilledByPlayer(false)
                        },
                        new RandomValueRange(1),
                        new RandomValueRange(0),
                        "knowledgefrags"
                    )
                );
                break;
        }
    }

}
