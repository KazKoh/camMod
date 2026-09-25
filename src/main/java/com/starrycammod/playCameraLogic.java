package com.starrycammod;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//This is a lot more math than I really care to do ever.

@EventBusSubscriber(modid = "starcammod")
public class playCameraLogic {

    private static final Map<UUID, MoveSequence> activeSequences = new HashMap<>();

    public static class CameraSegment {
        public final Vec3 startPos;
        public final Vec3 targetPos;
        public final float startYaw;
        public final float startPitch;
        public final float targetYaw;
        public final float targetPitch;

        public CameraSegment(Vec3 startPos, Vec3 targetPos, float startYaw, float startPitch, float targetYaw, float targetPitch) {
            this.startPos = startPos;
            this.targetPos = targetPos;
            this.startYaw = startYaw;
            this.startPitch = startPitch;
            this.targetYaw = targetYaw;
            this.targetPitch = targetPitch;
        }
    }

    // Handles ticking step-by-step through segments
    public static class MoveSequence {
        public final List<CameraSegment> segments;
        public final int ticksPerSegment;
        public int currentSegmentIndex = 0;
        public int currentTick = 0;

        public MoveSequence(List<CameraSegment> segments, int durationInSeconds) {
            this.segments = segments;
            this.ticksPerSegment = durationInSeconds * 20; // 20 ticks per second
        }
    }

    public static void playCamLoop(List<camWriter> cameras, CommandContext<CommandSourceStack> context, int duration) {
        if (cameras.size() < 2) return;

        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return;

        List<CameraSegment> segments = new ArrayList<>();
        camWriter previous = null;

        // Build the sequential track of lines
        for (camWriter cam : cameras) {
            if (previous == null) {
                previous = cam;
            } else {
                segments.add(new CameraSegment(
                        previous.getCamPos(), cam.getCamPos(),
                        previous.getCamLookingX(), previous.getCamLookingY(),
                        cam.getCamLookingX(), cam.getCamLookingY()
                ));
                previous = cam;
            }
        }

        context.getSource().sendSuccess(() -> Component.literal("Starting the camera sequence!"), true);

        activeSequences.put(player.getUUID(), new MoveSequence(segments, duration));
    }

    //logic moves through to each point based on given duration.
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            UUID uuid = serverPlayer.getUUID();

            if (activeSequences.containsKey(uuid)) {
                MoveSequence sequence = activeSequences.get(uuid);
                if (sequence.currentSegmentIndex >= sequence.segments.size()) {
                    activeSequences.remove(uuid);
                    return;
                }

                CameraSegment currentSegment = sequence.segments.get(sequence.currentSegmentIndex);
                sequence.currentTick++;

                double progress = (double) sequence.currentTick / sequence.ticksPerSegment;

                if (progress >= 1.0) {
                    serverPlayer.connection.teleport(
                            currentSegment.targetPos.x, currentSegment.targetPos.y, currentSegment.targetPos.z,
                            currentSegment.targetPitch, currentSegment.targetYaw
                    );

                    sequence.currentSegmentIndex++;
                    sequence.currentTick = 0;

                    if (sequence.currentSegmentIndex >= sequence.segments.size()) {
                        activeSequences.remove(uuid);
                    }
                } else {
                    // Linearly interpolate coordinates
                    double newX = currentSegment.startPos.x + (currentSegment.targetPos.x - currentSegment.startPos.x) * progress;
                    double newY = currentSegment.startPos.y + (currentSegment.targetPos.y - currentSegment.startPos.y) * progress;
                    double newZ = currentSegment.startPos.z + (currentSegment.targetPos.z - currentSegment.startPos.z) * progress;
                    float newYaw = (float) (currentSegment.startYaw + (currentSegment.targetYaw - currentSegment.startYaw) * progress);
                    float newPitch = (float) (currentSegment.startPitch + (currentSegment.targetPitch - currentSegment.startPitch) * progress);

                    // Teleport the player subtly frame-by-frame
                    serverPlayer.connection.teleport(newX, newY, newZ, newPitch, newYaw);
                }
            }
        }
    }
}