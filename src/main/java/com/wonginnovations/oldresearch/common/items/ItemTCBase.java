package com.wonginnovations.oldresearch.common.items;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.IThaumcraftItems;

public class ItemTCBase extends Item implements IThaumcraftItems {
    protected final String BASE_NAME;
    protected String[] VARIANTS;
    protected int[] VARIANTS_META;

    public ItemTCBase(String name, String... variants) {
        this.setRegistryName("oldresearch:" + name);
        this.setTranslationKey(name);
        this.setCreativeTab(ConfigItems.TABTC);
        this.setNoRepair();
        this.setHasSubtypes(variants.length > 1);
        this.BASE_NAME = name;
        if (variants.length == 0) {
            this.VARIANTS = new String[]{name};
        } else {
            this.VARIANTS = variants;
        }

        this.VARIANTS_META = new int[this.VARIANTS.length];

        for(int m = 0; m < this.VARIANTS.length; this.VARIANTS_META[m] = m++) {
        }

        ConfigItems.ITEM_VARIANT_HOLDERS.add(this);
    }

    public String getTranslationKey(ItemStack itemStack) {
        return this.hasSubtypes && itemStack.getMetadata() < this.VARIANTS.length && this.VARIANTS[itemStack.getMetadata()] != this.BASE_NAME ? String.format(super.getTranslationKey() + ".%s", this.VARIANTS[itemStack.getMetadata()]) : super.getTranslationKey(itemStack);
    }

    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == ConfigItems.TABTC || tab == CreativeTabs.SEARCH) {
            if (!this.getHasSubtypes()) {
                super.getSubItems(tab, items);
            } else {
                for(int meta = 0; meta < this.VARIANTS.length; ++meta) {
                    items.add(new ItemStack(this, 1, meta));
                }
            }
        }

    }

    public Item getItem() {
        return this;
    }

    public String[] getVariantNames() {
        return this.VARIANTS;
    }

    public int[] getVariantMeta() {
        return this.VARIANTS_META;
    }

    public ItemMeshDefinition getCustomMesh() {
        return null;
    }

    public ModelResourceLocation getCustomModelResourceLocation(String variant) {
        return variant.equals(this.BASE_NAME) ? new ModelResourceLocation("oldresearch:" + this.BASE_NAME) : new ModelResourceLocation("oldresearch:" + this.BASE_NAME, variant);
    }
}

