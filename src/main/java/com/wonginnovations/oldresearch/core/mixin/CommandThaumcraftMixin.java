package com.wonginnovations.oldresearch.core.mixin;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.common.lib.CommandThaumcraft;

@Mixin(value = CommandThaumcraft.class, remap = false)
public abstract class CommandThaumcraftMixin {

    @Inject(method = "revokeResearch", at = @At("HEAD"), cancellable = true)
    public void revokeResearchInjection(ICommandSender icommandsender, EntityPlayerMP player, String research, CallbackInfo ci) {
        if (ResearchCategories.getResearch(research) != null || research.startsWith("rn_")) {
            CommandThaumcraft.revokeRecursiveResearch(player, research);
            ThaumcraftCapabilities.getKnowledge(player).sync(player);
            player.sendMessage(new TextComponentString("§5" + icommandsender.getName() + " removed " + research + " research and its children."));
            icommandsender.sendMessage(new TextComponentString("§5Success!"));
        } else {
            icommandsender.sendMessage(new TextComponentString("§cResearch does not exist."));
        }
        ci.cancel();
    }

}
