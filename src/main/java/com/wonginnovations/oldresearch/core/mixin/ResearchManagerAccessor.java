package com.wonginnovations.oldresearch.core.mixin;

import com.google.gson.JsonObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.common.lib.research.ResearchManager;

@Mixin(value = ResearchManager.class, remap = false)
public interface ResearchManagerAccessor {

    @Invoker("parseResearchJson")
    static ResearchEntry parseResearchJson(JsonObject obj) { return null; }

    @Invoker("addResearchToCategory")
    static void addResearchToCategory(ResearchEntry entry) {}

}
