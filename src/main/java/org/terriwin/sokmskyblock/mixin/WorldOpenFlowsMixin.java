package org.terriwin.sokmskyblock.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldOpenFlows.class)
public class WorldOpenFlowsMixin {

    @Unique
    private static final ResourceKey<WorldPreset> SKYBLOCK_KEY = ResourceKey.create(
            Registries.WORLD_PRESET,
            ResourceLocation.fromNamespaceAndPath("sokmskyblock", "sokm_skyblock")
    );

    @Inject(method = "confirmWorldCreation", at = @At("HEAD"), cancellable = true)
    private static void sokmskyblock$skipExperimental(
            Minecraft minecraft,
            CreateWorldScreen screen,
            Lifecycle lifecycle,
            Runnable action,
            boolean skipCheck,
            CallbackInfo ci) {
        WorldCreationUiState uiState = screen.getUiState();
        Holder<WorldPreset> preset = uiState.getWorldType().preset();
        if (preset != null && preset.is(SKYBLOCK_KEY)) {
            action.run();
            ci.cancel();
        }
    }
}
