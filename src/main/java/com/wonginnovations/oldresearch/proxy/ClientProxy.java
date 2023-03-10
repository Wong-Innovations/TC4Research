package com.wonginnovations.oldresearch.proxy;

import com.wonginnovations.oldresearch.OldResearch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends Proxy {

    ProxyTESR proxyTESR = new ProxyTESR();

    @Override
    public void registerModel(ItemBlock itemBlock) {
        ModelLoader.setCustomModelResourceLocation(itemBlock, 0, new ModelResourceLocation(itemBlock.getRegistryName(), "inventory"));
    }

    @Override
    public void onConstruction(FMLConstructionEvent event) {
        super.onConstruction(event);
    }

    public void init(FMLInitializationEvent event) {
        super.init(event);
        this.proxyTESR.setupTESR();
    }

    @Override
    public void registerDisplayInformation() {
        OldResearch.aspectShift = FMLClientHandler.instance().hasOptifine();
        if(Loader.isModLoaded("JustEnoughItems")) {
            OldResearch.aspectShift = true;
        }
    }

    public boolean isClient() {
        return true;
    }

    public boolean isServer() {
        return false;
    }

    public World getClientWorld() {
        return Minecraft.getMinecraft().world;
    }

}
