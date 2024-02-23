package com.wonginnovations.oldresearch.api.research;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

public class ScanResult {
    public byte type;
    public int id;
    public int meta;
    public Entity entity;
    public String phenomena;

    public ScanResult(byte type, int blockId, int blockMeta, Entity entity, String phenomena) {
        this.type = type;
        this.id = blockId;
        this.meta = blockMeta;
        this.entity = entity;
        this.phenomena = phenomena;
    }

    public boolean equals(Object obj) {
        if(obj instanceof ScanResult) {
            ScanResult sr = (ScanResult)obj;
            if(this.type != sr.type) {
                return false;
            }

            if(this.type == 1 && (this.id != sr.id || this.meta != sr.meta)) {
                return false;
            }

            if(this.type == 2 && this.entity.getEntityId() != sr.entity.getEntityId()) {
                return false;
            }

            if(this.type == 3 && !this.phenomena.equals(sr.phenomena)) {
                return false;
            }
        }

        return true;
    }

//    public NBTTagCompound serialize() {
//        NBTTagCompound tag = new NBTTagCompound();
//        tag.setByte("type", type);
//        tag.setInteger("blockId", id);
//        tag.setInteger("blockMeta", meta);
//        tag.setTag("entity", entity.serializeNBT());
//        tag.setString("entityClass", entity.getClass().toString());
//        tag.setString("phenomena", phenomena);
//        return tag;
//    }
//
//    public static ScanResult deserialize(NBTTagCompound nbt) {
//        return new ScanResult(nbt.getByte("type"), nbt.getInteger("blockId"), nbt.getInteger("blockMeta"), nbt.getCompoundTag("entity"), "");
//    }
}
