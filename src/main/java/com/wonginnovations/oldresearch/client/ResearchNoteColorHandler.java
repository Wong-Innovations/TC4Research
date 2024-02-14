package com.wonginnovations.oldresearch.client;

import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import com.wonginnovations.oldresearch.common.lib.research.ResearchNoteData;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class ResearchNoteColorHandler implements IItemColor {

    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        switch (tintIndex) {
            case 0: return Color.WHITE.getRGB();
            case 1: {
                int c = 10066329;
                ResearchNoteData rd = OldResearchManager.getData(stack);
                if(rd != null) {
                    c = rd.color;
                }

                return c;
            }
            default: {
                // Shouldn't get here
                return Color.BLACK.getRGB();
            }
        }
    }

}
