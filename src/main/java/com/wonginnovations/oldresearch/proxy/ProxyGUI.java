package com.wonginnovations.oldresearch.proxy;

import com.wonginnovations.oldresearch.client.gui.GuiResearchTable;
import com.wonginnovations.oldresearch.common.container.ContainerResearchTable;
import com.wonginnovations.oldresearch.common.tiles.TileResearchTable;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ProxyGUI {

    public ProxyGUI() {
    }

    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (world instanceof WorldClient) {
            switch (ID) {
                case 1:
                    return new GuiResearchTable(player, (TileResearchTable)world.getTileEntity(new BlockPos(x, y, z)));
                default:
                    break;
            }
        }

        return null;
    }

    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case 1:
                return new ContainerResearchTable(player.inventory, (TileResearchTable)world.getTileEntity(new BlockPos(x, y, z)));
            default:
                return null;
        }
    }
}
