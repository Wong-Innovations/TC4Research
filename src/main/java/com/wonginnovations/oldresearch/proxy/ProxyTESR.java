package com.wonginnovations.oldresearch.proxy;

import com.wonginnovations.oldresearch.client.renderer.TileResearchTableRenderer;
import com.wonginnovations.oldresearch.common.tiles.TileResearchTable;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ProxyTESR {

    public ProxyTESR() {
    }

    public void setupTESR() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileResearchTable.class, new TileResearchTableRenderer());
    }
}
