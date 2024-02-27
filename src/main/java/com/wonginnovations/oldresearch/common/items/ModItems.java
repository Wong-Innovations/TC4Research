package com.wonginnovations.oldresearch.common.items;

import com.wonginnovations.oldresearch.Tags;
import com.wonginnovations.oldresearch.api.research.curio.BaseCurio;
import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public class ModItems {

    public static final Item RESEARCHNOTE = new ItemResearchNote();
    public static final Item CURIO = new ItemCurio();

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {

        IForgeRegistry<Item> r = event.getRegistry();

        r.register(RESEARCHNOTE);
        r.register(CURIO);

    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        int i = 0;
        for (BaseCurio curio : OldResearchManager.CURIOS) {
            ModelLoader.setCustomModelResourceLocation(ModItems.CURIO, i++, new ModelResourceLocation(curio.getTexture().toString()));
        }
    }

}
