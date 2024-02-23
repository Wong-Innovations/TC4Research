package com.wonginnovations.oldresearch.api.capabilities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nonnull;

public class OldResearchCapabilities {

    @CapabilityInject(IPlayerAspects.class)
    public static final Capability<IPlayerAspects> ASPECTS = null;

    public static IPlayerAspects getPlayerAspects(@Nonnull EntityPlayer player) {
        return (ASPECTS != null)? player.getCapability(ASPECTS, null) : null;
    }

}
