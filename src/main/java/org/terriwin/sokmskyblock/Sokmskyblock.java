package org.terriwin.sokmskyblock;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(Sokmskyblock.MODID)
public class Sokmskyblock {

    public static final String MODID = "sokmskyblock";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Sokmskyblock(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(SkyblockSetup.class);
        NeoForge.EVENT_BUS.register(SkyblockCommands.class);
    }
}
