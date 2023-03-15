package com.wonginnovations.oldresearch;

import com.wonginnovations.oldresearch.api.registration.IModelRegister;
import com.wonginnovations.oldresearch.client.ResearchNoteColorHandler;
import com.wonginnovations.oldresearch.client.lib.RenderEventHandler;
import com.wonginnovations.oldresearch.common.items.ModItems;
import com.wonginnovations.oldresearch.common.lib.network.PacketHandler;
import com.wonginnovations.oldresearch.common.lib.network.PacketSyncWarp;
import com.wonginnovations.oldresearch.common.lib.network.PacketWarpMessage;
import com.wonginnovations.oldresearch.common.tiles.TileResearchTable;
import com.wonginnovations.oldresearch.proxy.Proxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = OldResearch.ID, name = OldResearch.NAME, version = OldResearch.VERSION, dependencies = "required-after:thaumcraft")
@Mod.EventBusSubscriber(modid = OldResearch.ID)
public class OldResearch {

    public static final String ID = "oldresearch";
    public static final String NAME = "TC4 Research Port";
    public static final String VERSION = "1.0.0";

    @Mod.Instance("oldresearch")
    public static OldResearch instance;
    public static final Logger LOGGER = LogManager.getLogger(ID);

    @SidedProxy(clientSide = "com.wonginnovations.oldresearch.proxy.ClientProxy", serverSide = "com.wonginnovations.oldresearch.proxy.Proxy")
    public static Proxy proxy;

    public static boolean aspectShift = false; // this may have to be non-static
    public static RenderEventHandler renderEventHandler = new RenderEventHandler(); // same with this

    @Mod.EventHandler
    public void onConstruction(FMLConstructionEvent event) {
        proxy.onConstruction(event);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        for (Item item : Item.REGISTRY) {
            if (item.getRegistryName().getNamespace().equals(ID) && item instanceof IModelRegister) {
                ((IModelRegister) item).registerModels();
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onColorHandlerEvent(ColorHandlerEvent.Item event) {
        event.getItemColors().registerItemColorHandler(new ResearchNoteColorHandler(), ModItems.RESEARCHNOTE);
    }

    public static void addWarpToPlayer(EntityPlayer player, int amount, boolean temporary) {
        if(!player.world.isRemote) {
            if(proxy.getPlayerKnowledge() != null) {
                if(temporary || amount >= 0) {
                    if(amount != 0) {
                        if(temporary) {
                            if(amount < 0 && proxy.getPlayerKnowledge().getWarpTemp(player.getGameProfile().getName()) <= 0) {
                                return;
                            }

                            proxy.getPlayerKnowledge().addWarpTemp(player.getGameProfile().getName(), amount);
                            PacketHandler.INSTANCE.sendTo(new PacketSyncWarp(player, (byte)2), (EntityPlayerMP)player);
                            PacketHandler.INSTANCE.sendTo(new PacketWarpMessage(player, (byte)2, amount), (EntityPlayerMP)player);
                        } else {
                            proxy.getPlayerKnowledge().addWarpPerm(player.getGameProfile().getName(), amount);
                            PacketHandler.INSTANCE.sendTo(new PacketSyncWarp(player, (byte)0), (EntityPlayerMP)player);
                            PacketHandler.INSTANCE.sendTo(new PacketWarpMessage(player, (byte)0, amount), (EntityPlayerMP)player);
                        }

                        proxy.getPlayerKnowledge().setWarpCounter(player.getGameProfile().getName(), proxy.getPlayerKnowledge().getWarpTotal(player.getGameProfile().getName()));
                    }
                }
            }
        }
    }

    public static void addStickyWarpToPlayer(EntityPlayer player, int amount) {
        if(!player.world.isRemote) {
            if(proxy.getPlayerKnowledge() != null) {
                if(amount != 0) {
                    if(amount >= 0 || proxy.getPlayerKnowledge().getWarpSticky(player.getGameProfile().getName()) > 0) {
                        proxy.getPlayerKnowledge().addWarpSticky(player.getGameProfile().getName(), amount);
                        PacketHandler.INSTANCE.sendTo(new PacketSyncWarp(player, (byte)1), (EntityPlayerMP)player);
                        PacketHandler.INSTANCE.sendTo(new PacketWarpMessage(player, (byte)1, amount), (EntityPlayerMP)player);
                        proxy.getPlayerKnowledge().setWarpCounter(player.getGameProfile().getName(), proxy.getPlayerKnowledge().getWarpTotal(player.getGameProfile().getName()));
                    }
                }
            }
        }
    }

}
