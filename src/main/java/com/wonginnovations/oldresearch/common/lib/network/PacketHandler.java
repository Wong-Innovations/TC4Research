package com.wonginnovations.oldresearch.common.lib.network;

import com.wonginnovations.oldresearch.Tags;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MODID.toLowerCase());

    public static void preInit() {
        int discriminator = 0;
        INSTANCE.registerMessage(PacketAspectCombinationToServer.class, PacketAspectCombinationToServer.class, discriminator++, Side.SERVER);
        INSTANCE.registerMessage(PacketAspectDiscovery.class, PacketAspectDiscovery.class, discriminator++, Side.CLIENT);
        INSTANCE.registerMessage(PacketAspectDiscoveryError.class, PacketAspectDiscoveryError.class, discriminator++, Side.CLIENT);
        INSTANCE.registerMessage(PacketAspectPlaceToServer.class, PacketAspectPlaceToServer.class, discriminator++, Side.SERVER);
        INSTANCE.registerMessage(PacketAspectPool.class, PacketAspectPool.class, discriminator++, Side.CLIENT);
        INSTANCE.registerMessage(PacketGivePlayerNoteToServer.class, PacketGivePlayerNoteToServer.class, discriminator++, Side.SERVER);
        INSTANCE.registerMessage(PacketCopyPlayerNoteToServer.class, PacketCopyPlayerNoteToServer.class, discriminator++, Side.SERVER);
        INSTANCE.registerMessage(PacketSyncAspects.class, PacketSyncAspects.class, discriminator++, Side.CLIENT);
        INSTANCE.registerMessage(PacketSyncResearchTableData.class, PacketSyncResearchTableData.class, discriminator++, Side.CLIENT);
        INSTANCE.registerMessage(PacketScanSelfToServer.class, PacketScanSelfToServer.class, discriminator++, Side.SERVER);
        INSTANCE.registerMessage(PacketScanSlotToServer.class, PacketScanSlotToServer.class, discriminator++, Side.SERVER);
    }

}
