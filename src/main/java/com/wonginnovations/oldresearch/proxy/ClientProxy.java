package com.wonginnovations.oldresearch.proxy;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.api.research.curio.BaseCurio;
import com.wonginnovations.oldresearch.common.items.ItemCurio;
import com.wonginnovations.oldresearch.common.items.ModItems;
import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

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

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        this.proxyTESR.setupTESR();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        this.registerModels();
    }

    @Override
    public void registerDisplayInformation() {
        OldResearch.aspectShift = FMLClientHandler.instance().hasOptifine();
        if(Loader.isModLoaded("JustEnoughItems")) {
            OldResearch.aspectShift = true;
        }
    }

    @Override
    public boolean isClient() {
        return true;
    }

    @Override
    public boolean isServer() {
        return false;
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getMinecraft().world;
    }

    public void registerModels() {
        int i = 0;
        for (BaseCurio curio : OldResearchManager.CURIOS) {
            ModelLoader.setCustomModelResourceLocation(ModItems.CURIO, i++, new ModelResourceLocation(curio.getTexture(), "inventory"));
        }
    }

}
