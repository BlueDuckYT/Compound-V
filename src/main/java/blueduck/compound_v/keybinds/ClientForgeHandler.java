package blueduck.compound_v.keybinds;

import blueduck.compound_v.CompoundVMod;
import blueduck.compound_v.util.C2SHeldPowerPacket;
import blueduck.compound_v.util.C2SPushPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompoundVMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeHandler {
    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        // BUG FIX: Only process on END phase to prevent double-firing per tick
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().player == null) return;
        if (Minecraft.getInstance().screen != null) return; // Don't fire while in menus

        // Single press: toggle activation (consumeClick only fires once per press)
        if (KeyBinding.POWER_KEY.consumeClick()) {
            PacketHandler.sendToServer(new C2SPushPacket());
        }

        // Held key: continuous activation (for laser eyes etc.)
        boolean down = KeyBinding.POWER_KEY.isDown();
        if (down) {
            PacketHandler.sendToServer(new C2SHeldPowerPacket());
        } else if (powerKeyWasDown) {
            // Key-up edge: tell the server to release immediately (e.g. cut the spider web)
            // instead of waiting for the hold-timeout window to lapse.
            PacketHandler.sendToServer(new blueduck.compound_v.util.C2SReleasePacket());
        }
        powerKeyWasDown = down;

        // Client-side spider swing: runs EVERY client tick for smooth pendulum motion.
        // Server-side velocity edits on the local player get corrected only intermittently
        // (the client is authoritative over its own movement), which made the swing lurch
        // ~once a second. Driving it here, client-side, is smooth and authoritative; vanilla
        // syncs the resulting position to the server normally.
        clientSpiderSwing();
    }

    /**
     * Cobweb immunity, run at the END of the local player's own tick — i.e. right after their
     * move() has applied (and re-applied) cobweb's velocity damping for this tick. Doing it on
     * ClientTickEvent fired at the wrong point relative to movement, so it never took. Here we
     * neutralize the stuck multiplier and restore the damped horizontal velocity so the Spider
     * player walks through webbing at full speed.
     */
    @SubscribeEvent
    public static void clientPlayerPostTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null || event.player != mc.player) return;
        clientCobwebImmunity();
    }

    /**
     * Cobweb immunity (client-side). Movement is client-authoritative for the local player, so
     * the only reliable place to negate cobweb's slowdown is here. When the local Spider player
     * is inside a cobweb, neutralize the stuck-speed multiplier the client applied this tick and
     * scale the damped horizontal velocity back up, so they move through webbing freely.
     */
    private static void clientCobwebImmunity() {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return;
        if (!player.hasEffect(blueduck.compound_v.registry.EffectReg.SPIDER.get())) return;

        // Scan the player's occupied blocks for cobweb (feet + body).
        boolean inWeb = false;
        net.minecraft.world.level.block.state.BlockState webState = null;
        net.minecraft.core.BlockPos.MutableBlockPos mp = new net.minecraft.core.BlockPos.MutableBlockPos();
        int minX = (int) Math.floor(player.getX() - 0.3), maxX = (int) Math.floor(player.getX() + 0.3);
        int minY = (int) Math.floor(player.getY()), maxY = (int) Math.floor(player.getY() + player.getBbHeight());
        int minZ = (int) Math.floor(player.getZ() - 0.3), maxZ = (int) Math.floor(player.getZ() + 0.3);
        outer:
        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++) {
                    mp.set(x, y, z);
                    var st = player.level().getBlockState(mp);
                    if (st.is(net.minecraft.world.level.block.Blocks.COBWEB)) {
                        inWeb = true; webState = st; break outer;
                    }
                }
        if (!inWeb) return;

        // Neutralize the stuck multiplier so the NEXT move isn't pre-damped, and scale this
        // tick's damped horizontal velocity back up to roughly normal (cobweb cut it to ~0.25,
        // so ×4 recovers it), capped so we never fling the player.
        player.makeStuckInBlock(webState, new net.minecraft.world.phys.Vec3(1.0, 1.0, 1.0));
        var v = player.getDeltaMovement();
        double nx = Math.max(-0.34, Math.min(0.34, v.x * 4.0));
        double nz = Math.max(-0.34, Math.min(0.34, v.z * 4.0));
        // Don't fight upward jumps; only undo the harsh downward damping a little.
        double ny = v.y < 0 ? Math.max(v.y * 2.5, -0.5) : v.y;
        player.setDeltaMovement(nx, ny, nz);
    }

    private static boolean powerKeyWasDown = false;

    private static java.lang.Double clientRopeLength = null;

    private static void clientSpiderSwing() {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null || mc.level == null) {
            clientRopeLength = null;
            return;
        }
        // Only fully clear the rope on a real exit: V released or no Spider effect. A single
        // tick where the web isn't found must NOT clear it, or scroll adjustments get wiped.
        if (!KeyBinding.POWER_KEY.isDown()
                || !player.hasEffect(blueduck.compound_v.registry.EffectReg.SPIDER.get())) {
            clientRopeLength = null;
            return;
        }

        blueduck.compound_v.entity.WebProjectileEntity web = findOwnStuckWeb(player, mc);
        if (web == null) {
            // No stuck block-web right now (in flight, on a mob, or briefly undetected).
            // Leave clientRopeLength as-is so a pending scroll adjustment survives; just
            // don't apply swing physics this tick.
            return;
        }

        // Pendulum physics — mirrors the server formula but applied locally for smoothness.
        net.minecraft.world.phys.Vec3 anchor = web.position();
        net.minecraft.world.phys.Vec3 pos = player.position().add(0, player.getBbHeight() * 0.5, 0);
        net.minecraft.world.phys.Vec3 toAnchor = anchor.subtract(pos);
        double dist = toAnchor.length();
        if (dist < 0.5) return;

        if (clientRopeLength == null) clientRopeLength = dist;
        net.minecraft.world.phys.Vec3 ropeDir = toAnchor.normalize();
        net.minecraft.world.phys.Vec3 motion = player.getDeltaMovement();

        if (dist > clientRopeLength) {
            double radial = motion.dot(ropeDir);
            if (radial < 0) motion = motion.subtract(ropeDir.scale(radial));
            double pullBack = (dist - clientRopeLength);
            motion = motion.add(ropeDir.scale(pullBack * 0.5));
        }
        if (clientRopeLength < dist - 0.1) {
            motion = motion.add(ropeDir.scale(blueduck.compound_v.Config.spiderReelPull));
        }

        net.minecraft.world.phys.Vec3 look = player.getLookAngle();
        motion = motion.add(look.x * blueduck.compound_v.Config.spiderSwingControl, 0,
                look.z * blueduck.compound_v.Config.spiderSwingControl);

        double max = blueduck.compound_v.Config.spiderMaxSwingSpeed;
        if (motion.length() > max) motion = motion.normalize().scale(max);

        player.setDeltaMovement(motion);
        player.fallDistance = 0;
    }

    /** Finds the local player's own stuck, block-anchored web (tolerating unresolved owner). */
    private static blueduck.compound_v.entity.WebProjectileEntity findOwnStuckWeb(
            net.minecraft.world.entity.player.Player player, Minecraft mc) {
        blueduck.compound_v.entity.WebProjectileEntity web = null;
        double best = Double.MAX_VALUE;
        var box = player.getBoundingBox().inflate(80.0);
        java.util.UUID myId = player.getUUID();
        for (var e : mc.level.getEntitiesOfClass(blueduck.compound_v.entity.WebProjectileEntity.class, box)) {
            if (!e.isStuck() || e.isOnMob()) continue;
            var owner = e.getOwner();
            boolean mine = (owner != null) ? owner.getUUID().equals(myId) : true; // tolerate unresolved owner
            if (!mine) continue;
            double d = e.distanceToSqr(player);
            if (d < best) { best = d; web = e; }
        }
        return web;
    }

    /** Lets the scroll handler adjust the client rope length directly for instant feel. */
    public static void adjustClientRope(double delta) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        // Seed from the current player->anchor distance if we don't have a rope length yet,
        // so a scroll that happens before the swing loop has initialized still registers.
        if (clientRopeLength == null && player != null && mc.level != null) {
            var web = findOwnStuckWeb(player, mc);
            if (web != null) {
                net.minecraft.world.phys.Vec3 pos = player.position().add(0, player.getBbHeight() * 0.5, 0);
                clientRopeLength = web.position().distanceTo(pos);
            }
        }
        if (clientRopeLength != null) {
            clientRopeLength = Math.max(blueduck.compound_v.Config.spiderMinRope,
                    Math.min(blueduck.compound_v.Config.spiderMaxRope, clientRopeLength + delta));
        }
    }

    /**
     * Scroll-wheel capture for scroll-aware powers (currently Telekinesis hold distance).
     * While the power key is held AND the player has a scroll-aware power, the scroll is
     * consumed (so it doesn't change the hotbar) and sent to the server as a direction.
     */
    @SubscribeEvent
    public static void onScroll(net.minecraftforge.client.event.InputEvent.MouseScrollingEvent event) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        if (Minecraft.getInstance().screen != null) return;
        if (!KeyBinding.POWER_KEY.isDown()) return; // only intercept while holding the power key
        boolean tk = player.hasEffect(blueduck.compound_v.registry.EffectReg.TELEKINESIS.get());
        boolean size = player.hasEffect(blueduck.compound_v.registry.EffectReg.SIZE_CONTROL_ADVANCED.get());
        boolean spider = player.hasEffect(blueduck.compound_v.registry.EffectReg.SPIDER.get());
        boolean laser = (player.hasEffect(blueduck.compound_v.registry.EffectReg.LASER_EYES_BASIC.get())
                || player.hasEffect(blueduck.compound_v.registry.EffectReg.LASER_EYES_ADVANCED.get()))
                && blueduck.compound_v.Config.laserIntensityAdjustable;
        if (!tk && !size && !spider && !laser) return;

        double delta = event.getScrollDelta();
        if (delta == 0) return;
        event.setCanceled(true); // don't change the hotbar slot
        int dir = delta > 0 ? 1 : -1;

        // For spider, also adjust the LOCAL rope length immediately so the client-driven
        // swing responds instantly (scroll DOWN shortens / reels in, scroll UP adds slack —
        // matching the server-side block rope convention).
        if (spider) {
            adjustClientRope(dir * blueduck.compound_v.Config.spiderReelStep);
        }

        PacketHandler.sendToServer(new blueduck.compound_v.util.C2SScrollPacket(dir));
    }
}
