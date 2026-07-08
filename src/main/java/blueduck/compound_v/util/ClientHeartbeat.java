package blueduck.compound_v.util;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Client-only: plays the Warden heartbeat at a world position for the local player. Triggered by
 * {@link S2CHeartbeatPacket}, so only the player who received the packet hears it. Playing at a
 * position (rather than at the player) makes it directional with distance falloff, so the Advanced
 * Laser Eyes holder can locate wounded players by ear.
 */
public class ClientHeartbeat {

    public static void play(double x, double y, double z, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        mc.level.playLocalSound(x, y, z,
                SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS,
                1.0F, pitch, false);
    }
}
