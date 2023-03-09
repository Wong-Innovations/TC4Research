package com.wonginnovations.oldresearch.common.blocks;

import com.wonginnovations.oldresearch.OldResearch;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = OldResearch.ID)
public class ModBlocks {

    public static final Block RESEARCHTABLE = new BlockResearchTable();

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Block> event) {

        IForgeRegistry<Block> r = event.getRegistry();

        r.register(RESEARCHTABLE);

    }

    @SubscribeEvent
    public static void registerBlockItems(RegistryEvent.Register<Item> event) {

        IForgeRegistry<Item> r = event.getRegistry();

        r.register(makeItem(RESEARCHTABLE).setTranslationKey("research_table"));


    }

    private static ItemBlock makeItem(Block block) {
        ItemBlock itemBlock = new ItemBlock(block);
        itemBlock.setRegistryName(block.getRegistryName());
        OldResearch.proxy.registerModel(itemBlock);
        return itemBlock;
    }

}
