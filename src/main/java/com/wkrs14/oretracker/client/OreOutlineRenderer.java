package com.wkrs14.oretracker.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.List;

public final class OreOutlineRenderer {
    private static final int MAX_LINES = 48;

    private OreOutlineRenderer() {
    }

    public static void render(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS
                || !OreTrackerConfig.isEnabled()
                || OreScanner.results().isEmpty()
                || minecraft.level == null
                || player == null) {
            return;
        }

        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        long time = System.currentTimeMillis();
        float pulse = (float) ((Math.sin(time / 220.0D) + 1.0D) * 0.5D);
        List<OreScanner.DetectedOre> ores = OreScanner.results();

        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        if (OreTrackerConfig.showLines()) {
            // Interpolation keeps the line origin locked to the camera while moving.
            float partialTick = event.getPartialTick();
            Vec3 playerPosition = new Vec3(
                    Mth.lerp(partialTick, player.xo, player.getX()),
                    Mth.lerp(partialTick, player.yo, player.getY()) + player.getBbHeight() * 0.55D,
                    Mth.lerp(partialTick, player.zo, player.getZ()));
            double lineRangeSq = (double) OreTrackerConfig.getLineRange() * OreTrackerConfig.getLineRange();
            int drawn = 0;
            for (OreScanner.DetectedOre ore : ores) {
                Vec3 target = Vec3.atCenterOf(ore.pos());
                if (playerPosition.distanceToSqr(target) > lineRangeSq) {
                    continue;
                }
                addTracer(buffer, pose, playerPosition, target, ore.type());
                if (++drawn >= MAX_LINES) {
                    break;
                }
            }
        }

        for (OreScanner.DetectedOre ore : ores) {
            OreType type = ore.type();
            float brightness = 0.05F;
            float red = Math.min(1.0F, type.red() + brightness);
            float green = Math.min(1.0F, type.green() + brightness);
            float blue = Math.min(1.0F, type.blue() + brightness);
            AABB box = new AABB(ore.pos()).inflate(0.004D);
            LevelRenderer.renderLineBox(pose, buffer, box, red, green, blue, 0.62F + pulse * 0.22F);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.lineWidth(2.0F);
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.lineWidth(1.0F);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        pose.popPose();
    }

    private static void addTracer(BufferBuilder buffer, PoseStack pose, Vec3 eye, Vec3 end, OreType type) {
        Vec3 toTarget = end.subtract(eye);
        double distance = toTarget.length();
        Vec3 direction = distance < 1.0E-6D
                ? new Vec3(0.0D, 0.0D, 1.0D)
                : toTarget.scale(1.0D / distance);

        // Start just in front of the near plane. Starting exactly at the eye can clip the
        // complete line while standing still and causes visible jitter while moving.
        Vec3 start = eye.add(direction.scale(Math.min(0.05D, distance * 0.15D)));

        Matrix4f matrix = pose.last().pose();
        float alpha = 0.46F;
        float red = Math.min(1.0F, type.red() + 0.15F);
        float green = Math.min(1.0F, type.green() + 0.15F);
        float blue = Math.min(1.0F, type.blue() + 0.15F);

        buffer.vertex(matrix, (float) start.x, (float) start.y, (float) start.z)
                .color(red, green, blue, alpha)
                .normal(pose.last().normal(), (float) direction.x, (float) direction.y, (float) direction.z)
                .endVertex();
        buffer.vertex(matrix, (float) end.x, (float) end.y, (float) end.z)
                .color(red, green, blue, alpha)
                .normal(pose.last().normal(), (float) direction.x, (float) direction.y, (float) direction.z)
                .endVertex();
    }
}


