package com.wonginnovations.oldresearch.core.mixin;

import com.wonginnovations.oldresearch.common.items.ItemCurio;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.config.ConfigItems;

@Mixin(value = ConfigItems.class, remap = false)
public abstract class ConfigItemsMixin {

    @Redirect(method = "initItems", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/registries/IForgeRegistry;register(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V", ordinal = 1))
    private static void instantiateItemCurio(IForgeRegistry<Item> instance, IForgeRegistryEntry<Item> iForgeRegistryEntry) {
        ItemsTC.curio = new ItemCurio();
        instance.register(ItemsTC.curio);
    }

}
