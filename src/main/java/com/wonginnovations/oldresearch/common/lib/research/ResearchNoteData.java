package com.wonginnovations.oldresearch.common.lib.research;

import java.util.ArrayList;
import java.util.HashMap;

import com.wonginnovations.oldresearch.OldResearch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.lib.utils.HexUtils;

public class ResearchNoteData {
    
    public String key;
    public int color;
    public AspectList aspects = new AspectList();
    public HashMap<String, OldResearchManager.HexEntry> hexEntries = new HashMap<>();
    public HashMap<String, HexUtils.Hex> hexes = new HashMap<>();
    public boolean complete;
    public int copies;

    public boolean isComplete() {
        return this.complete;
    }

    public void generateHexes(World world, EntityPlayer player, AspectList aspects, int complexity) {
        this.aspects = aspects;
        int radius = 1 + Math.min(3, complexity);
        HashMap<String, HexUtils.Hex> hexLocs = HexUtils.generateHexes(radius);
        ArrayList<HexUtils.Hex> outerRing = HexUtils.distributeRingRandomly(radius, aspects.size(), world.rand);

        for(HexUtils.Hex hex : hexLocs.values()) {
            hexes.put(hex.toString(), hex);
            hexEntries.put(hex.toString(), new OldResearchManager.HexEntry(null, 0));
        }

        int count = 0;

        for(HexUtils.Hex hex : outerRing) {
            hexes.put(hex.toString(), hex);
            hexEntries.put(hex.toString(), new OldResearchManager.HexEntry(aspects.getAspects()[count], 1));
            ++count;
        }

        if(complexity > 1) {
            int researchCompleted = OldResearch.proxy.getPlayerKnowledge().getResearchCompleted(player.getGameProfile().getName());
            int blanks = (researchCompleted % 10 < 2) ? 0
                            : (researchCompleted % 10 < 5) ? 1 : 2;
            blanks = blanks * (radius - 3);
            HexUtils.Hex[] temp = hexes.values().toArray(new HexUtils.Hex[0]);

            while(blanks > 0) {
                int indx = world.rand.nextInt(temp.length);
                if(hexEntries.get(temp[indx].toString()) != null && hexEntries.get(temp[indx].toString()).type == 0) {
                    boolean gtg = true;

                    for(int n = 0; n < 6; ++n) {
                        HexUtils.Hex neighbour = temp[indx].getNeighbour(n);
                        if(hexes.containsKey(neighbour.toString()) && hexEntries.get(neighbour.toString()).type == 1) {
                            int cc = 0;

                            for(int q = 0; q < 6; ++q) {
                                if(hexes.containsKey(hexes.get(neighbour.toString()).getNeighbour(q).toString())) {
                                    ++cc;
                                }

                                if(cc >= 2) {
                                    break;
                                }
                            }

                            if(cc < 2) {
                                gtg = false;
                                break;
                            }
                        }
                    }

                    if(gtg) {
                        hexes.remove(temp[indx].toString());
                        hexEntries.remove(temp[indx].toString());
                        temp = hexes.values().toArray(new HexUtils.Hex[0]);
                        --blanks;
                    }
                }
            }
        }
    }
    
}
