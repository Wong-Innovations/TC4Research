package com.wonginnovations.oldresearch.common.lib.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("OldResearch".toLowerCase());

    public static void init() {
        int discriminator = 0;
        INSTANCE.registerMessage(PacketResearchComplete.class, PacketResearchComplete.class, discriminator++, Side.CLIENT);
    }

}
