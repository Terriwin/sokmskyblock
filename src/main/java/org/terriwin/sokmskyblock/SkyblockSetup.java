package org.terriwin.sokmskyblock;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import javax.annotation.Nullable;
import java.util.Optional;

public class SkyblockSetup {

    // Grass block in the structure sits at local (2, 0, 2).
    // Place origin at (-2, 64, -2) so grass lands at world (0, 64, 0).
    static final int BASE_Y = 64;
    static final BlockPos STRUCTURE_ORIGIN = new BlockPos(-2, BASE_Y, -2);

    // Structure is 9 blocks tall (Y 0–8); spawn one above the top
    static final BlockPos SPAWN_POS = new BlockPos(0, BASE_Y + 9, 0);

    private static final ResourceLocation STRUCTURE_ID =
            ResourceLocation.fromNamespaceAndPath("sokmskyblock", "sokmstart");

    // ── Event handlers ──────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        SkyblockSavedData data = SkyblockSavedData.get(overworld);
        if (!data.isIslandPlaced()) {
            if (placeIsland(overworld)) {
                overworld.setDefaultSpawnPos(SPAWN_POS, 0.0f);
                server.getGameRules().getRule(GameRules.RULE_SPAWN_RADIUS).set(0, server);
                data.setIslandPlaced(true);
                Sokmskyblock.LOGGER.info("[SokmSkyblock] Island placed, spawn set to {}", SPAWN_POS);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        var dim = event.getDimension();
        if (!dim.equals(Level.NETHER) && !dim.equals(Level.END)) return;

        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;

        ServerLevel overworld = serverLevel.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        if (SkyblockSavedData.get(overworld).isIslandPlaced()) {
            event.setCanceled(true);
        }
    }

    // ── Block placement helpers ─────────────────────────────────────────────

    /** Places the full island structure unconditionally. Returns true on success. */
    static boolean placeIsland(ServerLevel level) {
        Optional<StructureTemplate> template = getTemplate(level);
        if (template.isEmpty()) {
            Sokmskyblock.LOGGER.error("[SokmSkyblock] Could not load structure '{}'", STRUCTURE_ID);
            return false;
        }
        template.get().placeInWorld(
                level,
                STRUCTURE_ORIGIN,
                STRUCTURE_ORIGIN,
                new StructurePlaceSettings(),
                RandomSource.create(),
                Block.UPDATE_ALL
        );
        return true;
    }

    /** Restores only positions that are currently air, preserving player-placed blocks. */
    static void restoreIsland(ServerLevel level) {
        Optional<StructureTemplate> template = getTemplate(level);
        if (template.isEmpty()) {
            Sokmskyblock.LOGGER.error("[SokmSkyblock] Could not load structure '{}' for restore", STRUCTURE_ID);
            return;
        }
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .addProcessor(new AirOnlyProcessor(level));
        template.get().placeInWorld(
                level,
                STRUCTURE_ORIGIN,
                STRUCTURE_ORIGIN,
                settings,
                RandomSource.create(),
                Block.UPDATE_ALL
        );
    }

    private static Optional<StructureTemplate> getTemplate(ServerLevel level) {
        return level.getStructureManager().get(STRUCTURE_ID);
    }

    // ── Processor: skip non-air world positions and structure-air blocks ────

    private static final class AirOnlyProcessor extends StructureProcessor {
        private final ServerLevel level;

        AirOnlyProcessor(ServerLevel level) {
            this.level = level;
        }

        @Override
        @Nullable
        public StructureTemplate.StructureBlockInfo processBlock(
                LevelReader reader,
                BlockPos offset,
                BlockPos pivot,
                StructureTemplate.StructureBlockInfo original,
                StructureTemplate.StructureBlockInfo transformed,
                StructurePlaceSettings settings) {
            if (transformed.state().isAir()) return null;
            if (!level.isEmptyBlock(transformed.pos())) return null;
            return transformed;
        }

        @Override
        protected StructureProcessorType<?> getType() {
            return StructureProcessorType.NOP;
        }
    }
}
