package com.wonginnovations.oldresearch.api.capabilities;

import com.wonginnovations.oldresearch.common.lib.network.PacketSyncAspects;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketSyncKnowledge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

public class PlayerAspects {

    public PlayerAspects() {
    }

    public static void preInit() {
        CapabilityManager.INSTANCE.register(IPlayerAspects.class, new Capability.IStorage<IPlayerAspects>() {
            @Nullable
            @Override
            public NBTTagCompound writeNBT(Capability<IPlayerAspects> capability, IPlayerAspects instance, EnumFacing side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<IPlayerAspects> capability, IPlayerAspects instance, EnumFacing side, NBTBase nbt) {

            }
        }, DefaultImpl::new);
    }

    public static class Provider implements ICapabilitySerializable<NBTTagCompound> {
        public static final ResourceLocation NAME = new ResourceLocation("oldresearch", "aspects");
        private final PlayerAspects.DefaultImpl aspects = new PlayerAspects.DefaultImpl();

        public Provider() {
        }

        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
            return capability == OldResearchCapabilities.ASPECTS;
        }

        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
            return capability == OldResearchCapabilities.ASPECTS ? OldResearchCapabilities.ASPECTS.cast(this.aspects) : null;
        }

        public NBTTagCompound serializeNBT() {
            return this.aspects.serializeNBT();
        }

        public void deserializeNBT(NBTTagCompound nbt) {
            this.aspects.deserializeNBT(nbt);
        }
    }

    public static class DefaultImpl implements IPlayerAspects {

        private final HashMap<Aspect, Integer> aspects = new HashMap<>();

        private DefaultImpl() {
        }

        public boolean foundAspect(Aspect aspect) {
            return aspects.containsKey(aspect);
        }

        public int getAspect(Aspect aspect) {
            return aspects.get(aspect);
        }

        public void setAspect(Aspect aspect, int amount) {
            aspects.put(aspect, amount);
        }

        public void sync(@Nonnull EntityPlayerMP player) {
            PacketHandler.INSTANCE.sendTo(new PacketSyncAspects(player), player);
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound rootTag = new NBTTagCompound();
            NBTTagList aspectList = new NBTTagList();

            this.aspects.forEach((aspect, amount) -> {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("aspect", aspect.getTag());
                tag.setInteger("amount", amount);
                aspectList.appendTag(tag);
            });

            rootTag.setTag("aspectList", aspectList);

            return rootTag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            if (!nbt.hasKey("aspectList")) return;
            NBTTagList aspectList = nbt.getTagList("aspectList", 9);
            for (int i = 0; i < aspectList.tagCount(); i++) {
                NBTTagCompound tag = aspectList.getCompoundTagAt(i);
                aspects.put(Aspect.getAspect(tag.getString("aspect")), tag.getInteger("amount"));
            }
        }
    }

}
