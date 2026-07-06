package org.terriwin.sokmskyblock;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class SkyblockCommands {

    private static final int FREE_RESTARTS = 3;

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("sokm")
                        .then(Commands.literal("restart")
                                .executes(SkyblockCommands::executeRestart))
        );
    }

    private static int executeRestart(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        if (!(source.getEntity() instanceof ServerPlayer)) {
            source.sendFailure(Component.literal("Эту команду может использовать только игрок."));
            return 0;
        }

        MinecraftServer server = source.getServer();
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return 0;

        SkyblockSavedData data = SkyblockSavedData.get(overworld);

        if (!data.isIslandPlaced()) {
            source.sendFailure(Component.literal("Это не SoKM SkyBlock мир."));
            return 0;
        }

        boolean hasCheats = source.hasPermission(2);
        int usedCount = data.getRestartCount();

        if (!hasCheats && usedCount >= FREE_RESTARTS) {
            source.sendFailure(Component.literal(
                    "Достигнут лимит бесплатных восстановлений (" + FREE_RESTARTS + "/" + FREE_RESTARTS + "). " +
                    "Включите читы или обратитесь к оператору."));
            return 0;
        }

        SkyblockSetup.restoreIsland(overworld);

        if (!hasCheats) {
            data.incrementRestartCount();
            int remaining = FREE_RESTARTS - data.getRestartCount();
            source.sendSuccess(() -> Component.literal(
                    "Остров восстановлен! Осталось бесплатных восстановлений: " + remaining + "/" + FREE_RESTARTS + "."), false);
        } else {
            source.sendSuccess(() -> Component.literal("Остров восстановлен!"), false);
        }

        return 1;
    }
}
