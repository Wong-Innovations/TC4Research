package com.wonginnovations.oldresearch.common.items;

import com.wonginnovations.oldresearch.Tags;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public class ModItems {

    public static final Item RESEARCHNOTE = new ItemResearchNote();
    public static final Item KNOWLEDGEFRAGMENT = new ItemKnowledgeFragment();
    public static final Item CURIO = new ItemCurio();

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {

        IForgeRegistry<Item> r = event.getRegistry();

        r.register(RESEARCHNOTE);
        r.register(KNOWLEDGEFRAGMENT);
        r.register(CURIO);

    }

}
