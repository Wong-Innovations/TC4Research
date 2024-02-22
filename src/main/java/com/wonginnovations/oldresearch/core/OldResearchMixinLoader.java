package com.wonginnovations.oldresearch.core;

import com.google.common.collect.ImmutableList;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.List;


public class OldResearchMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return ImmutableList.of("mixins.oldresearch.json");
    }

}
