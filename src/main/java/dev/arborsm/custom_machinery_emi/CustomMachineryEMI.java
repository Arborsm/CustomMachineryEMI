package dev.arborsm.custom_machinery_emi;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

@Mod(CustomMachineryEMI.MOD_ID)
public class CustomMachineryEMI {
    public static final String MOD_ID = "custom_machinery_emi";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CustomMachineryEMI(final IEventBus MOD_BUS, final ModContainer ignoredModContainer) {
        LOGGER.info("Custom Machinery EMI Loaded");
        MOD_BUS.addListener(this::clientSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
    }
}
