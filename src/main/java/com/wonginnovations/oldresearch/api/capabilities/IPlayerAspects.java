package com.wonginnovations.oldresearch.api.capabilities;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import thaumcraft.api.aspects.Aspect;

public interface IPlayerAspects extends INBTSerializable<NBTTagCompound> {

    boolean foundAspect(Aspect aspect);

    int getAspect(Aspect aspect);

    void setAspect(Aspect aspect, int amount);

    void sync(EntityPlayerMP player);

}
