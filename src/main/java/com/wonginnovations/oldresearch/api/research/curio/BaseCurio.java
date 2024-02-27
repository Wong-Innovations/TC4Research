package com.wonginnovations.oldresearch.api.research.curio;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.common.lib.research.ScanManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.IPlayerWarp;

public class BaseCurio {

    private String name;
    private AspectList aspects;
    private final int[] warp = new int[]{0,0,0};
    private ResourceLocation texture;

    public BaseCurio() {}

    public BaseCurio(String name) {
        this.name = name;
        this.texture = new ResourceLocation("oldresearch", "curio_" + name);
    }

    public BaseCurio(String name, ResourceLocation texture) {
        this.name = name;
        this.texture = texture;
    }

    public BaseCurio setName(String name) {
        this.name = name;
        return this;
    }

    public BaseCurio setAspects(AspectList aspects) {
        this.aspects = aspects;
        return this;
    }

    public BaseCurio setWarp(IPlayerWarp.EnumWarpType type, int i) {
        warp[type.ordinal()] = i;
        return this;
    }

    public BaseCurio setTexture(String texture) {
        this.texture = new ResourceLocation(texture);
        return this;
    }

    public BaseCurio setTexture(ResourceLocation texture) {
        this.texture = texture;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }

    public void onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
        if (!worldIn.isRemote) {
            for (Aspect aspect : aspects.getAspects()) {
                if (OldResearch.proxy.playerKnowledge.hasDiscoveredAspect(player.getGameProfile().getName(), aspect)
                    || OldResearch.proxy.playerKnowledge.hasDiscoveredParentAspects(player.getGameProfile().getName(), aspect)
                ) {
                    ScanManager.checkAndSyncAspectKnowledge(player, aspect, (int) Math.floor(aspects.getAmount(aspect) * (player.getRNG().nextFloat() / 2.0F)));
                }
            }
            for (int i : warp) {
                if (i != 0) {
                    ThaumcraftApi.internalMethods.addWarpToPlayer(player, i, IPlayerWarp.EnumWarpType.values()[i]);
                }
            }
        }
    }

}
