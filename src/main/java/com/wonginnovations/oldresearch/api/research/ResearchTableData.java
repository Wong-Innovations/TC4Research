package com.wonginnovations.oldresearch.api.research;

import com.wonginnovations.oldresearch.common.lib.research.ResearchNoteData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.ArrayUtils;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import java.util.Arrays;
import java.util.Iterator;

public class ResearchTableData {
    public EntityPlayer researcher = null;
    public AspectList bonusAspects = new AspectList();
    public ResearchNoteData note = null;

    public NBTTagCompound serialize() {
        NBTTagCompound nbt = new NBTTagCompound();
        if (this.researcher != null) {
            nbt.setString("player", this.researcher.getName());
        }

        NBTTagList savedTag = new NBTTagList();
        Aspect[] list = this.bonusAspects.getAspects();

        for (Aspect aspect : list) {
            if(aspect != null && this.bonusAspects.getAmount(aspect) > 0) {
                NBTTagCompound tc = new NBTTagCompound();
                tc.setString("aspect", aspect.getTag());
                savedTag.appendTag(tc);
            }
        }

        nbt.setTag("aspects", savedTag);

        return nbt;
    }

    public void deserialize(NBTTagCompound nbt) {
        if (nbt != null) {
            if (nbt.hasKey("player")) {
                this.researcher = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getPlayerEntityByName(nbt.getString("player"));// nbt.getString("player"); // get EntityPlayer from name somehow
            }

            NBTTagList list = nbt.getTagList("aspects", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound tc = list.getCompoundTagAt(i);
                this.bonusAspects.add(Aspect.getAspect(tc.getString("aspect")), 1);
            }
        }
    }

}
