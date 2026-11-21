package com.wkrs14.oretracker.client;

import com.wkrs14.oretracker.OreTrackerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OreTrackerMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        while (TrackerKeys.TOGGLE.consumeClick()) {
            if (minecraft.screen == null && minecraft.player != null) {
                boolean enabled = OreTrackerConfig.toggleEnabled();
                minecraft.player.displayClientMessage(Component.translatable(
                        enabled ? "message.oretracker.enabled" : "message.oretracker.disabled"), true);
            }
        }
        while (TrackerKeys.OPEN_MENU.consumeClick()) {
            if (minecraft.screen == null && minecraft.player != null) {
                minecraft.setScreen(new TrackerScreen());
            }
        }

        OreScanner.tick(minecraft);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        OreOutlineRenderer.render(event);
    }
}
