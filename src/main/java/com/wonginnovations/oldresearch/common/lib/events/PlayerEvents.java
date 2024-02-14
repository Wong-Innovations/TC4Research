package com.wonginnovations.oldresearch.common.lib.events;

import com.wonginnovations.oldresearch.common.items.ItemThaumonomicon;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;

@Mod.EventBusSubscriber
public class PlayerEvents {

    @SubscribeEvent
    public static void pickupItem(EntityItemPickupEvent event) {
        if (event.getEntityPlayer() != null && !event.getEntityPlayer().world.isRemote && event.getItem() != null) {
            IPlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(event.getEntityPlayer());

            if (event.getItem().getItem().getItem() instanceof ItemThaumonomicon && !knowledge.isResearchKnown("!gotthaumonomicon")) {
                knowledge.addResearch("!gotthaumonomicon");
                knowledge.sync((EntityPlayerMP)event.getEntityPlayer());
            }
        }
    }

}
