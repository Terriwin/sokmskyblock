package org.terriwin.sokmskyblock.mixin;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {

    @Inject(method = "init", at = @At("TAIL"))
    private void sokmskyblock$setDefaultPreset(CallbackInfo ci) {
        WorldCreationUiState uiState = ((CreateWorldScreen) (Object) this).getUiState();

        ResourceKey<WorldPreset> skyblockKey = ResourceKey.create(
                Registries.WORLD_PRESET,
                ResourceLocation.fromNamespaceAndPath("sokmskyblock", "sokm_skyblock")
        );

        uiState.getNormalPresetList().stream()
                .filter(entry -> entry.preset() != null)
                .filter(entry -> entry.preset().is(skyblockKey))
                .findFirst()
                .ifPresent(uiState::setWorldType);
    }
}
