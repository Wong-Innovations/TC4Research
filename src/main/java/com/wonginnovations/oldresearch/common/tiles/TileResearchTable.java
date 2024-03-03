package com.wonginnovations.oldresearch.common.tiles;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.common.blocks.ModBlocks;
import com.wonginnovations.oldresearch.common.items.ItemResearchNote;
import com.wonginnovations.oldresearch.common.items.ModItems;
import com.wonginnovations.oldresearch.common.lib.network.PacketAspectPool;
import com.wonginnovations.oldresearch.common.lib.network.PacketHandler;
import com.wonginnovations.oldresearch.common.lib.network.PacketSyncResearchTableAspects;
import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import com.wonginnovations.oldresearch.common.lib.research.ResearchNoteData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.items.IScribeTools;
import thaumcraft.common.blocks.essentia.BlockJar;
import thaumcraft.common.blocks.world.ore.BlockCrystal;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.HexUtils;
import thaumcraft.common.tiles.TileThaumcraftInventory;

public class TileResearchTable extends TileThaumcraftInventory {

    public AspectList bonusAspects = new AspectList();
    public ResearchNoteData note;
    public int nextRecalc;

    public TileResearchTable() {
        super(2);
        this.syncedSlots = new int[]{0, 1};
    }

    @Override
    public NBTTagCompound writeSyncNBT(NBTTagCompound nbtCompound) {
        NBTTagList savedTag = new NBTTagList();
        Aspect[] list = this.bonusAspects.getAspects();

        for (Aspect aspect : list) {
            if(aspect != null && this.bonusAspects.getAmount(aspect) > 0) {
                NBTTagCompound tc = new NBTTagCompound();
                tc.setString("aspect", aspect.getTag());
                savedTag.appendTag(tc);
            }
        }

        nbtCompound.setTag("bonusAspects", savedTag);
        return super.writeSyncNBT(nbtCompound);
    }

    @Override
    public void readSyncNBT(NBTTagCompound nbtCompound) {
        this.bonusAspects = new AspectList();
        if (nbtCompound.hasKey("bonusAspects")) {
            NBTTagList list = nbtCompound.getTagList("bonusAspects", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound tc = list.getCompoundTagAt(i);
                this.bonusAspects.add(Aspect.getAspect(tc.getString("aspect")), 1);
            }
        }

        super.readSyncNBT(nbtCompound);
    }

    protected void setWorldCreate(@NotNull World worldIn) {
        super.setWorldCreate(worldIn);
        if (!this.hasWorld()) {
            this.setWorld(worldIn);
        }

    }

    public void consumeInkFromTable() {
        if (this.getStackInSlot(0).getItem() instanceof IScribeTools && this.getStackInSlot(0).getItemDamage() < this.getStackInSlot(0).getMaxDamage()) {
            this.getStackInSlot(0).setItemDamage(this.getStackInSlot(0).getItemDamage() + 1);
            this.syncTile(false);
            this.markDirty();
        }
    }

    public boolean canConsumeInkFromTable() {
        return this.getStackInSlot(0).getItem() instanceof IScribeTools && this.getStackInSlot(0).getItemDamage() < this.getStackInSlot(0).getMaxDamage();
    }

    @Override
    public void update() {
        super.update();
        if(!this.world.isRemote && this.nextRecalc++ > 600) {
            this.nextRecalc = 0;
            this.recalculateBonus();
            PacketHandler.INSTANCE.sendToAllAround(new PacketSyncResearchTableAspects(this.getPos(), this.bonusAspects), new NetworkRegistry.TargetPoint(this.getWorld().provider.getDimension(), (double)this.pos.getX() + 0.5, (double)this.pos.getY() + 0.5, (double)this.pos.getZ() + 0.5, 128.0));
            this.markDirty();
        }
    }

    public void markDirty() {
        super.markDirty();
        this.gatherResults();
    }

    public void gatherResults() {
        this.note = null;
        if(this.getStackInSlot(1).getItem() instanceof ItemResearchNote) {
            this.note = OldResearchManager.getData(this.getStackInSlot(1));
        }
    }

    public void placeAspect(int q, int r, Aspect aspect, EntityPlayer player) {
        if(this.note == null) {
            this.gatherResults();
        }

        if(this.canConsumeInkFromTable()) {
            if(this.getStackInSlot(1).getItem() instanceof ItemResearchNote && this.note != null && this.getStackInSlot(1).getItemDamage() < 64) {
                boolean r1 = ThaumcraftCapabilities.getKnowledge(player).isResearchComplete("RESEARCHER1");
                boolean r2 = ThaumcraftCapabilities.getKnowledge(player).isResearchComplete("RESEARCHER2");
                HexUtils.Hex hex = new HexUtils.Hex(q, r);
                OldResearchManager.HexEntry he;
                if(aspect != null) {
                    he = new OldResearchManager.HexEntry(aspect, 2);
                    if(r2 && this.world.rand.nextFloat() < 0.1F) {
                        this.world.playSound(player.posX, player.posY, player.posZ, new SoundEvent(new ResourceLocation("entity.experience_orb.pickup")), SoundCategory.PLAYERS, 0.2F, 0.9F + player.world.rand.nextFloat() * 0.2F, false);
                    } else if(OldResearch.proxy.playerKnowledge.getAspectPoolFor(player.getGameProfile().getName(), aspect) <= 0) {
                        this.bonusAspects.remove(aspect, 1);
                        // this will cause problems later
                        player.world.notifyBlockUpdate(this.pos, world.getBlockState(this.pos), world.getBlockState(this.pos), 3);
                        this.markDirty();
                    } else {
                        OldResearch.proxy.playerKnowledge.addAspectPool(player.getGameProfile().getName(), aspect, -1);
                        PacketHandler.INSTANCE.sendTo(new PacketAspectPool(aspect.getTag(), 0, OldResearch.proxy.playerKnowledge.getAspectPoolFor(player.getGameProfile().getName(), aspect)), (EntityPlayerMP)player);
                    }
                } else {
                    float f = this.world.rand.nextFloat();
                    if(this.note.hexEntries.get(hex.toString()).aspect != null && (r1 && f < 0.25F || r2 && f < 0.5F)) {
                        this.world.playSound(player.posX, player.posY, player.posZ, new SoundEvent(new ResourceLocation("entity.experience_orb.pickup")), SoundCategory.PLAYERS, 0.2F, 0.9F + player.world.rand.nextFloat() * 0.2F, false);
                        OldResearch.proxy.playerKnowledge.addAspectPool(player.getGameProfile().getName(), this.note.hexEntries.get(hex.toString()).aspect, 1);
                        PacketHandler.INSTANCE.sendTo(new PacketAspectPool(this.note.hexEntries.get(hex.toString()).aspect.getTag(), 0, OldResearch.proxy.playerKnowledge.getAspectPoolFor(player.getGameProfile().getName(), this.note.hexEntries.get(hex.toString()).aspect)), (EntityPlayerMP)player);
                    }

                    he = new OldResearchManager.HexEntry(null, 0);
                }

                this.note.hexEntries.put(hex.toString(), he);
                this.note.hexes.put(hex.toString(), hex);
                OldResearchManager.updateData(this.getStackInSlot(1), this.note);
                this.consumeInkFromTable();
                if(!this.world.isRemote && OldResearchManager.checkResearchCompletion(this.getStackInSlot(1), this.note, player.getGameProfile().getName())) {
                    this.getStackInSlot(1).setItemDamage(64);
                    this.world.addBlockEvent(this.pos, ModBlocks.RESEARCHTABLE, 1, 1);
                    this.syncTile(false);
                }
            }

            this.world.notifyBlockUpdate(this.pos, world.getBlockState(this.pos), world.getBlockState(this.pos), 3);
            this.markDirty();
        }
    }

    private void recalculateBonus() {
        if(!this.world.isDaytime() && this.world.getLight(this.pos) < 4 && !this.world.canBlockSeeSky(this.pos) && this.world.rand.nextInt(20) == 0) {
            this.bonusAspects.merge(Aspect.ENTROPY, 1);
        }

        if((float)this.pos.getY() > (float)this.world.getActualHeight() * 0.5F && this.world.rand.nextInt(20) == 0) {
            this.bonusAspects.merge(Aspect.AIR, 1);
        }

        if((float)this.pos.getY() > (float)this.world.getActualHeight() * 0.66F && this.world.rand.nextInt(20) == 0) {
            this.bonusAspects.merge(Aspect.AIR, 1);
        }

        if((float)this.pos.getY() > (float)this.world.getActualHeight() * 0.75F && this.world.rand.nextInt(20) == 0) {
            this.bonusAspects.merge(Aspect.AIR, 1);
        }

        for(int x = -8; x <= 8; ++x) {
            for(int z = -8; z <= 8; ++z) {
                for(int y = -8; y <= 8; ++y) {
                    if(y + this.pos.getY() > 0 && y + this.pos.getY() < this.world.getActualHeight()) {
                        IBlockState bs = this.world.getBlockState(new BlockPos(x + this.pos.getX(), y + this.pos.getY(), z + this.pos.getZ()));
                        Block bi = bs.getBlock();
                        int md = bi.getMetaFromState(bs);
                        Material bm = bs.getMaterial();
                        if(bi instanceof BlockCrystal && md == 0) {
                            if(this.bonusAspects.getAmount(Aspect.AIR) < 1 && this.world.rand.nextInt(10) == 0) {
                                this.bonusAspects.merge(Aspect.AIR, 1);
                                return;
                            }
                        } else if(bm != Material.FIRE && bm != Material.LAVA) {
                            if(bm == Material.GROUND) {
                                if(this.bonusAspects.getAmount(Aspect.EARTH) < 1 && this.world.rand.nextInt(20) == 0) {
                                    this.bonusAspects.merge(Aspect.EARTH, 1);
                                    return;
                                }
                            } else if(bi instanceof BlockCrystal && md == 3) {
                                if(this.bonusAspects.getAmount(Aspect.EARTH) < 1 && this.world.rand.nextInt(10) == 0) {
                                    this.bonusAspects.merge(Aspect.EARTH, 1);
                                    return;
                                }
                            } else if(bm == Material.WATER) {
                                if(this.bonusAspects.getAmount(Aspect.WATER) < 1 && this.world.rand.nextInt(15) == 0) {
                                    this.bonusAspects.merge(Aspect.WATER, 1);
                                    return;
                                }
                            } else if(bi instanceof BlockCrystal && md == 2) {
                                if(this.bonusAspects.getAmount(Aspect.WATER) < 1 && this.world.rand.nextInt(10) == 0) {
                                    this.bonusAspects.merge(Aspect.WATER, 1);
                                    return;
                                }
                            } else if(bm != Material.CIRCUITS && bm != Material.PISTON) {
                                if(bi instanceof BlockCrystal && md == 4) {
                                    if(this.bonusAspects.getAmount(Aspect.ORDER) < 1 && this.world.rand.nextInt(10) == 0) {
                                        this.bonusAspects.merge(Aspect.ORDER, 1);
                                        return;
                                    }
                                } else if(bi instanceof BlockCrystal && md == 5 && this.bonusAspects.getAmount(Aspect.ENTROPY) < 1 && this.world.rand.nextInt(10) == 0) {
                                    this.bonusAspects.merge(Aspect.ENTROPY, 1);
                                    return;
                                }
                            } else if(this.bonusAspects.getAmount(Aspect.ORDER) < 1 && this.world.rand.nextInt(20) == 0) {
                                this.bonusAspects.merge(Aspect.ORDER, 1);
                                return;
                            }
                        } else if(this.bonusAspects.getAmount(Aspect.FIRE) < 1 && this.world.rand.nextInt(20) == 0) {
                            this.bonusAspects.merge(Aspect.FIRE, 1);
                            return;
                        }

                        if(bi == Blocks.BOOKSHELF && this.world.rand.nextInt(300) == 0 || bi instanceof BlockJar && md == 1 && this.world.rand.nextInt(200) == 0) {
                            Aspect[] aspects = new Aspect[0];
                            aspects = Aspect.aspects.values().toArray(aspects);
                            this.bonusAspects.merge(aspects[this.world.rand.nextInt(aspects.length)], 1);
                            return;
                        }
                    }
                }
            }
        }
    }

    public int getSizeInventory() {
        return 2;
    }

    public boolean hasScribingTools() {
        return super.getStackInSlot(0).getItem() instanceof IScribeTools || super.getSyncedStackInSlot(0).getItem() instanceof IScribeTools;
    }

    public boolean hasResearchNote() {
        return super.getStackInSlot(1).getItem() instanceof ItemResearchNote || super.getSyncedStackInSlot(1).getItem() instanceof ItemResearchNote;
    }

    public ItemStack getStackInSlot(int var1) {
        return (this.getSyncedStackInSlot(var1).isEmpty())? super.getStackInSlot(var1) : this.getSyncedStackInSlot(var1);
    }

    public String getName() {
        return "Research Table";
    }

    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        switch (i) {
            case 0:
                if (itemstack.getItem() instanceof IScribeTools) {
                    return true;
                }
            case 1:
                if (itemstack.getItem() == ModItems.RESEARCHNOTE) {
                    return true;
                }
        }

        return false;
    }

    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        if (this.world != null && this.world.isRemote) {
            this.syncTile(false);
        }
    }

    public boolean receiveClientEvent(int i, int j) {
        if (i == 1) {
            if (this.world.isRemote) {
                this.world.playSound(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), SoundsTC.learn, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }

            return true;
        } else {
            return super.receiveClientEvent(i, j);
        }
    }

}
