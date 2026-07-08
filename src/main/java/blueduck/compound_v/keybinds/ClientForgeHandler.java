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

        // In spectator there are no powers to drive. Crucially, RESET the held-state latch and
        // drain any pending click so a transition INTO or OUT OF spectator (or a dimension change)
        // can't leave powerKeyWasDown stuck - which previously wedged the held/scroll handling
        // until the next clean key press.
        if (Minecraft.getInstance().player.isSpectator()) {
            KeyBinding.POWER_KEY.consumeClick(); // drain so it doesn't fire on return
            powerKeyWasDown = false;
            return;
        }

        // Single press: toggle activation (consumeClick only fires once per press). Guard with
        // the raw key state: consumeClick can return a BUFFERED press that surfaces on the same
        // tick you release V, which would shoot a stray second web right as you let go. Only fire
        // if the key is genuinely still held right now.
        boolean heldNow = KeyBinding.isPowerKeyHeld();
        boolean clicked = KeyBinding.POWER_KEY.consumeClick();
        if (clicked && heldNow) {
            PacketHandler.sendToServer(new C2SPushPacket());
        }

        // Held key: continuous activation (for laser eyes etc.)
        boolean down = heldNow;
        if (down) {
            PacketHandler.sendToServer(new C2SHeldPowerPacket());
        } else if (powerKeyWasDown) {
            // Key-up edge: tell the server to release immediately (e.g. cut the spider web)
            // instead of waiting for the hold-timeout window to lapse.
            PacketHandler.sendToServer(new blueduck.compound_v.util.C2SReleasePacket());
        }
        powerKeyWasDown = down;

        // Jump press-EDGE for the spider jump actions (rope jump / wall jump). Computed once per
        // tick so holding space doesn't repeat-fire (which would spam webs on the refire chain).
        boolean jumpDown = Minecraft.getInstance().options.keyJump.isDown();
        jumpPressedThisTick = jumpDown && !jumpWasDown;
        jumpWasDown = jumpDown;

        // Client-side spider swing: runs EVERY client tick for smooth pendulum motion.
        // Server-side velocity edits on the local player get corrected only intermittently
        // (the client is authoritative over its own movement), which made the swing lurch
        // ~once a second. Driving it here, client-side, is smooth and authoritative; vanilla
        // syncs the resulting position to the server normally.
        clientSpiderSwing();
    }

    /**
     * Cobweb immunity, run at the END of the local player's own tick - i.e. right after their
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
        clientWallClimb();
        clientSlimeMovement();
    }

    /**
     * Wall + ceiling climbing (client-side, like the swing/cobweb logic, so it doesn't fight
     * client movement prediction). HOLD SNEAK while a Spider holder is against a wall or beneath
     * a ceiling to cling; gravity is cancelled and WASD/look move you along the surface. Holding
     * sneak gives the crouch pose for free, so it reads as a spider-crawl. Releasing sneak or
     * leaving the surface keeps your current velocity (momentum carryover) so you drop, swing, or
     * wall-jump naturally.
     */
    /** Nearest cardinal horizontal Direction for a (mostly-horizontal) vector, or null if ~zero. */
    private static net.minecraft.core.Direction directionFromVec(net.minecraft.world.phys.Vec3 v) {
        if (Math.abs(v.x) < 1.0e-4 && Math.abs(v.z) < 1.0e-4) return null;
        if (Math.abs(v.x) > Math.abs(v.z)) {
            return v.x > 0 ? net.minecraft.core.Direction.EAST : net.minecraft.core.Direction.WEST;
        }
        return v.z > 0 ? net.minecraft.core.Direction.SOUTH : net.minecraft.core.Direction.NORTH;
    }

    private static boolean wasCeilingClimbing = false;

    /** Clear the forced crawl pose when ceiling-climbing stops (any exit path). */
    private static void clearCeilingPose() {
        if (!wasCeilingClimbing) return;
        wasCeilingClimbing = false;
        var p = Minecraft.getInstance().player;
        if (p != null && p.isSwimming() && !p.isInWater()) {
            p.setSwimming(false);
            p.setPose(net.minecraft.world.entity.Pose.STANDING);
        }
    }

    /**
     * Slime movement, client-side (authoritative over the local player, so it won't be fought by
     * client prediction the way server-side velocity edits are). Detects slime mode via the same
     * proxy the render layer uses (SLIME effect + the refreshed JUMP boost). Blocks walking,
     * launches a hop on jump, and bounces on hard landings unless sneaking.
     */
    private static void clientSlimeMovement() {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return;
        boolean slime = player.hasEffect(blueduck.compound_v.registry.EffectReg.SLIME.get())
                && player.hasEffect(net.minecraft.world.effect.MobEffects.JUMP);
        if (!slime) { slimePrevY = 0; slimeWasGround = false; return; }

        net.minecraft.world.phys.Vec3 motion = player.getDeltaMovement();
        double prevY = slimePrevY;

        if (player.onGround()) {
            // BOUNCE: hard landing (fell >~1 block) and not sneaking -> bounce back up.
            if (prevY < -blueduck.compound_v.Config.slimeBounceMinImpact && !player.isShiftKeyDown()) {
                double bounce = Math.min(-prevY * blueduck.compound_v.Config.slimeBounceFactor,
                        blueduck.compound_v.Config.slimeMaxBounce);
                if (bounce > 0.1) {
                    player.setDeltaMovement(motion.x, bounce, motion.z);
                    player.hurtMarked = true;
                }
            } else {
                // NO WALKING: zero horizontal movement on the ground.
                player.setDeltaMovement(0, motion.y, 0);
            }
            player.fallDistance = 0;
        } else if (slimeWasGround && motion.y > 0.0) {
            // Just jumped: launch horizontally in the input direction so you hop around.
            float fwd = player.zza, strafe = player.xxa;
            if (fwd != 0 || strafe != 0) {
                net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                net.minecraft.world.phys.Vec3 flatLook = new net.minecraft.world.phys.Vec3(look.x, 0, look.z).normalize();
                // player.xxa is +1 for LEFT (A); the leftward vector for a forward (lx,lz) is
                // (lz,-lx). The previous (-lz,lx) was the right vector, so strafe came out inverted.
                net.minecraft.world.phys.Vec3 flatSide = new net.minecraft.world.phys.Vec3(flatLook.z, 0, -flatLook.x);
                net.minecraft.world.phys.Vec3 hop = flatLook.scale(fwd).add(flatSide.scale(strafe));
                if (hop.lengthSqr() > 1.0e-4) {
                    hop = hop.normalize().scale(blueduck.compound_v.Config.slimeHopSpeed);
                    player.setDeltaMovement(hop.x, motion.y, hop.z);
                    player.hurtMarked = true;
                }
            }
        }

        slimeWasGround = player.onGround();
        slimePrevY = player.getDeltaMovement().y;
    }

    private static double slimePrevY = 0;
    private static boolean slimeWasGround = false;

    private static void clientWallClimb() {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return;
        if (!player.hasEffect(blueduck.compound_v.registry.EffectReg.SPIDER.get())) { clearCeilingPose(); return; }
        if (!blueduck.compound_v.Config.spiderWallClimbEnabled) { clearCeilingPose(); return; }
        if (!player.isShiftKeyDown()) { clearCeilingPose(); return; }   // hold sneak to climb
        // Note: we do NOT exclude onGround here - you approach a wall on the ground, so climbing
        // must be able to start from a standing position (pressing W then lifts you up the wall).
        if (player.isInWater()
                || player.getAbilities().flying || player.isPassenger()) { clearCeilingPose(); return; }

        var level = player.level();
        net.minecraft.core.BlockPos base = player.blockPosition();
        net.minecraft.world.phys.AABB box = player.getBoundingBox();
        double gap = blueduck.compound_v.Config.spiderClimbStickGap; // how close the wall must be

        // --- Detect an adjacent WALL: a solid block whose face is within `gap` of the player's
        // bounding box on that side. We check the NEAREST qualifying side so that at a convex
        // corner you can strafe from one face onto the perpendicular one - each tick re-picks
        // whichever wall you're actually pressed against.
        boolean wall = false;
        net.minecraft.core.Direction wallDir = null;
        double bestGap = Double.MAX_VALUE;
        net.minecraft.core.Direction[] horiz = {
                net.minecraft.core.Direction.NORTH, net.minecraft.core.Direction.SOUTH,
                net.minecraft.core.Direction.EAST, net.minecraft.core.Direction.WEST};
        for (net.minecraft.core.Direction d : horiz) {
            // Is there a solid block on this side at head or foot level?
            net.minecraft.core.BlockPos head = base.above(1).relative(d);
            net.minecraft.core.BlockPos foot = base.relative(d);
            boolean headSolid = !level.getBlockState(head).getCollisionShape(level, head).isEmpty();
            boolean footSolid = !level.getBlockState(foot).getCollisionShape(level, foot).isEmpty();
            if (!headSolid && !footSolid) continue;

            // Distance from the player's box edge to that block's NEAR face. Only counts as a
            // climbable wall if we're basically touching it (within gap).
            double edgeGap;
            if (d == net.minecraft.core.Direction.EAST) {
                // Block is east; its west (near) face is at foot.getX().
                edgeGap = foot.getX() - box.maxX;
            } else if (d == net.minecraft.core.Direction.WEST) {
                // Block is west; its east (near) face is at foot.getX()+1.
                edgeGap = box.minX - (foot.getX() + 1);
            } else if (d == net.minecraft.core.Direction.SOUTH) {
                // Block is south; its north (near) face is at foot.getZ().
                edgeGap = foot.getZ() - box.maxZ;
            } else { // NORTH
                // Block is north; its south (near) face is at foot.getZ()+1.
                edgeGap = box.minZ - (foot.getZ() + 1);
            }

            if (edgeGap <= gap && edgeGap < bestGap) {
                bestGap = edgeGap;
                wall = true;
                wallDir = d;
            }
        }

        // --- Detect a CEILING just above the head ---
        // --- Detect a CEILING just above the head ---
        // Use a STABLE head reference (feet + standing height), NOT box.maxY: once ceiling-crawl
        // forces the swim pose the hitbox shrinks ~1.2 blocks, and keying off box.maxY would then
        // probe a block well below the real ceiling (air) and drop you out of climb mode. We also
        // keep a little extra reach (ceilingReach) so the shrunk crawler stays attached.
        double headRef = wasCeilingClimbing ? player.getY() + 1.8 : box.maxY;
        net.minecraft.core.BlockPos ceil = net.minecraft.core.BlockPos.containing(
                player.getX(), headRef + 0.05, player.getZ());
        double ceilingReach = wasCeilingClimbing ? 0.6 : gap + 0.1;
        boolean ceiling = blueduck.compound_v.Config.spiderCeilingClimbEnabled
                && !level.getBlockState(ceil).getCollisionShape(level, ceil).isEmpty()
                && (ceil.getY() - headRef) <= ceilingReach + 0.1;

        if (!wall && !ceiling) return; // nothing to cling to

        float forward = player.zza;   // W/S (-1..1)
        float strafe = player.xxa;    // A/D (-1..1)
        double speed = blueduck.compound_v.Config.spiderClimbSpeed;

        // WALL JUMP: pressing the vanilla JUMP key while clinging launches you off the surface
        // in your look direction, carrying momentum. No extra keybind - reuses space.
        if (jumpPressedThisTick && blueduck.compound_v.Config.spiderWallJumpEnabled) {
            net.minecraft.world.phys.Vec3 look = player.getLookAngle();
            double power = blueduck.compound_v.Config.spiderWallJumpPower;
            // Push off along your look, with a guaranteed upward component so it always pops.
            player.setDeltaMovement(
                    look.x * power,
                    Math.max(look.y * power, 0.0) + blueduck.compound_v.Config.spiderWallJumpLift,
                    look.z * power);
            player.hurtMarked = true;
            player.resetFallDistance();
            player.fallDistance = 0;
            return; // launched - skip clinging this tick
        }

        if (wall && !ceiling) {
            // WALL CLIMB. Movement is projected onto the wall plane and oriented by where you
            // LOOK, so you climb in the direction you aim - look up-left and press forward to go
            // up-and-left along the wall, etc. W/S = along your look (up/down the wall), A/D =
            // perpendicular (sideways across the wall). Setting spiderClimbLookRelative=false
            // falls back to simple axis-locked climbing (W straight up, A/D flat sideways).
            net.minecraft.world.phys.Vec3 wallNormal = new net.minecraft.world.phys.Vec3(
                    wallDir.getStepX(), 0, wallDir.getStepZ());
            net.minecraft.world.phys.Vec3 climbVel;

            if (blueduck.compound_v.Config.spiderClimbLookRelative) {
                // Project the look direction onto the wall plane (remove the component into the
                // wall). This vector points "the way you're facing" along the surface.
                net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                net.minecraft.world.phys.Vec3 lookOnWall = look.subtract(wallNormal.scale(look.dot(wallNormal)));
                if (lookOnWall.lengthSqr() < 1.0e-4) {
                    // Looking straight into/away from the wall - fall back to world-up reference.
                    lookOnWall = new net.minecraft.world.phys.Vec3(0, 1, 0);
                } else {
                    lookOnWall = lookOnWall.normalize();
                }
                // Sideways axis = perpendicular to look, on the wall plane. Order chosen so A/D
                // map to the player's left/right correctly (the reverse cross was inverted).
                net.minecraft.world.phys.Vec3 sideOnWall = lookOnWall.cross(wallNormal).normalize();
                climbVel = lookOnWall.scale(forward).add(sideOnWall.scale(strafe));
                // Normalize to full climb speed so movement isn't slowed just because you're
                // looking shallowly (the projected look can be short). Preserve "no input = still".
                if (climbVel.lengthSqr() > 1.0e-4) {
                    climbVel = climbVel.normalize().scale(speed);
                } else {
                    climbVel = net.minecraft.world.phys.Vec3.ZERO;
                }
            } else {
                // Axis-locked: W/S straight up/down, A/D flat sideways along the wall face.
                double vy = (forward > 0) ? speed : (forward < 0) ? -speed : 0.0;
                net.minecraft.world.phys.Vec3 along = new net.minecraft.world.phys.Vec3(
                        wallDir.getStepZ(), 0, -wallDir.getStepX());
                climbVel = along.scale(strafe * speed).add(0, vy, 0);
            }

            // Press gently INTO the wall so we stay attached.
            net.minecraft.world.phys.Vec3 into = wallNormal.scale(0.08);
            net.minecraft.world.phys.Vec3 along = new net.minecraft.world.phys.Vec3(
                    wallDir.getStepZ(), 0, -wallDir.getStepX()); // for corner-wrap probe below

            // CONVEX CORNER WRAP: if we're strafing toward the edge of this wall (the wall block
            // in the strafe direction is GONE) but there's a wall just around that corner, curl
            // the motion around it so you climb around outside corners instead of drifting off.
            if (strafe != 0) {
                net.minecraft.core.Direction strafeDir = directionFromVec(along.scale(strafe));
                if (strafeDir != null) {
                    net.minecraft.core.BlockPos ahead = base.relative(strafeDir).relative(wallDir);
                    boolean aheadWallGone = level.getBlockState(ahead).getCollisionShape(level, ahead).isEmpty();
                    net.minecraft.core.BlockPos corner = base.relative(strafeDir);
                    boolean cornerSolid = !level.getBlockState(corner).getCollisionShape(level, corner).isEmpty();
                    if (aheadWallGone && cornerSolid) {
                        // The face turns here - angle the push toward the new (perpendicular) wall.
                        into = new net.minecraft.world.phys.Vec3(
                                strafeDir.getStepX(), 0, strafeDir.getStepZ()).scale(0.12);
                    }
                }
            }

            player.setDeltaMovement(climbVel.x + into.x, climbVel.y, climbVel.z + into.z);
            player.resetFallDistance();
            player.fallDistance = 0;
            clearCeilingPose(); // wall mode uses the normal pose, not the crawl
        } else if (ceiling) {
            // CEILING CLIMB. Hang under the ceiling; WASD crawls along the underside relative to
            // look facing. We actively HUG the ceiling: rather than a tiny up-push (which loses to
            // gravity and lets you sag out of range), compute the gap to the ceiling underside and
            // set a vertical velocity that closes it, keeping you flush each tick.
            net.minecraft.world.phys.Vec3 look = player.getLookAngle();
            net.minecraft.world.phys.Vec3 flatLook = new net.minecraft.world.phys.Vec3(look.x, 0, look.z).normalize();
            net.minecraft.world.phys.Vec3 flatSide = new net.minecraft.world.phys.Vec3(-flatLook.z, 0, flatLook.x);
            net.minecraft.world.phys.Vec3 move = flatLook.scale(forward * speed).add(flatSide.scale(strafe * speed));

            // Desired head position: just below the ceiling block's underside. Use the same
            // stable head reference as detection so the shrinking crawl pose doesn't skew the gap.
            double headTop = wasCeilingClimbing ? player.getY() + 1.8 : box.maxY;
            double targetTop = ceil.getY() - 0.02;
            double gapToCeil = targetTop - headTop;            // + = rise, - = sink a touch
            double vy = gapToCeil * 0.5;                        // proportional pull toward ceiling
            vy = Math.max(-0.2, Math.min(0.2, vy));             // clamp for smoothness

            player.setDeltaMovement(move.x, vy, move.z);
            player.resetFallDistance();
            player.fallDistance = 0;
            // 1-block-tall crawl/swim pose (asserted each tick; vanilla tries to reset it).
            player.setSwimming(true);
            player.setPose(net.minecraft.world.entity.Pose.SWIMMING);
            wasCeilingClimbing = true;
        } else {
            // Left the ceiling - drop the forced swim pose so the player stands normally again.
            clearCeilingPose();
        }
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
    private static boolean jumpWasDown = false;
    private static boolean jumpPressedThisTick = false;

    private static java.lang.Double clientRopeLength = null;
    // Ticks since the player was last on the ground, for the swing-jump grace check below.
    private static int spiderTicksSinceGround = 99;

    private static void clientSpiderSwing() {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null || mc.level == null) {
            clientRopeLength = null;
            return;
        }
        // Only fully clear the rope on a real exit: V released or no Spider effect. A single
        // tick where the web isn't found must NOT clear it, or scroll adjustments get wiped.
        if (!KeyBinding.isPowerKeyHeld()
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

        // Pendulum physics - mirrors the server formula but applied locally for smoothness.
        net.minecraft.world.phys.Vec3 anchor = web.position();
        net.minecraft.world.phys.Vec3 pos = player.position().add(0, player.getBbHeight() * 0.5, 0);
        net.minecraft.world.phys.Vec3 toAnchor = anchor.subtract(pos);
        double dist = toAnchor.length();
        if (dist < 0.5) return;

        if (clientRopeLength == null) clientRopeLength = dist;

        net.minecraft.world.phys.Vec3 ropeDir = toAnchor.normalize();
        net.minecraft.world.phys.Vec3 motion = player.getDeltaMovement();
        boolean onGround = player.onGround();
        // Update the ground-grace counter. onGround() can already read false on the exact tick you
        // jump off the floor, so we track how long since we were genuinely grounded.
        if (onGround) spiderTicksSinceGround = 0;
        else spiderTicksSinceGround++;

        // JUMP OFF THE ROPE: pressing the vanilla JUMP key while genuinely SWINGING (airborne for
        // a few ticks) releases the web and launches you, KEEPING your swing momentum plus an
        // upward boost. We require having been off the ground for several ticks so that jumping
        // while standing on the floor does NOT count as a swing-jump - instead the web stays
        // tethered and you do a normal jump, letting you hop off the ground INTO a swing on your
        // existing line rather than cutting it and starting a new one.
        boolean genuinelyAirborne = spiderTicksSinceGround > blueduck.compound_v.Config.spiderSwingJumpGroundGrace;
        if (genuinelyAirborne && jumpPressedThisTick
                && blueduck.compound_v.Config.spiderSwingJumpEnabled) {
            net.minecraft.world.phys.Vec3 launch = motion.scale(
                    blueduck.compound_v.Config.spiderSwingJumpMomentum);
            double up = launch.y + blueduck.compound_v.Config.spiderSwingJumpLift;
            player.setDeltaMovement(launch.x, up, launch.z);
            player.hurtMarked = true;
            player.resetFallDistance();
            player.fallDistance = 0;
            // Cut the web: clear local rope and tell the server to release so it doesn't re-grab.
            clientRopeLength = null;
            PacketHandler.sendToServer(new blueduck.compound_v.util.C2SReleasePacket());
            // CHAIN: immediately fire the next web at whatever you're looking at, so tapping space
            // mid-swing flows straight into the next swing (easy web-to-web ceiling traversal).
            // The release packet is queued first, so the old web is cut before the new fire.
            if (blueduck.compound_v.Config.spiderSwingJumpRefires) {
                PacketHandler.sendToServer(new C2SPushPacket());
            }
            return;
        }

        // REEL: pulling the rope shorter than your current distance pulls you toward the anchor.
        // This works on the ground too (so you can reel yourself UP off the floor).
        if (clientRopeLength < dist - 0.1) {
            motion = motion.add(ropeDir.scale(blueduck.compound_v.Config.spiderReelPull));
            // When reeling while standing on the ground, the upward pull must beat the ground
            // friction/landing reset, or you stay stuck to the floor. If the anchor is above you,
            // guarantee a minimum upward velocity to actually break off the ground.
            if (onGround && ropeDir.y > 0.1) {
                double minLift = blueduck.compound_v.Config.spiderReelGroundLift;
                if (motion.y < minLift) {
                    motion = new net.minecraft.world.phys.Vec3(motion.x, minLift, motion.z);
                }
            }
        }

        // PENDULUM SWING: only while AIRBORNE. On the ground a web on a block shouldn't drag you
        // around - you walk normally and only the reel above lifts you. The swing is gravity-
        // driven: gravity builds momentum through the arc, the rope constraint keeps you on the
        // circle, and directional input is only a SLIGHT left/right bias - it never overrides the
        // pendulum.
        if (!onGround) {
            // Gravity is the primary driver of the swing - add it explicitly so momentum builds
            // as you fall through the bottom of the arc.
            motion = motion.add(0, -blueduck.compound_v.Config.spiderSwingGravity, 0);

            // Rope constraint: keep the player on the circle of radius clientRopeLength. Remove
            // any velocity that would stretch the rope past its length, then correct position
            // drift by pulling back toward the rope length.
            if (dist > clientRopeLength) {
                double radial = motion.dot(ropeDir);
                if (radial < 0) motion = motion.subtract(ropeDir.scale(radial));
                double pullBack = (dist - clientRopeLength);
                motion = motion.add(ropeDir.scale(pullBack * 0.5));
            }

            // PUMPING (W/S): like pumping your legs on a swing. Pressing W pushes along your
            // current tangential (along-the-arc) motion to BUILD momentum; S pushes against it to
            // slow down. This is how you build speed - you must actively pump, not just hang. It
            // amplifies the swing you already have rather than steering, so gravity still rules
            // the arc; with no tangential motion yet (hanging straight down) there's nothing to
            // pump, so a gentle gravity-driven start gets you going first.
            float fwd = player.zza; // W = +1, S = -1
            if (fwd != 0) {
                // Tangential velocity = motion minus its radial (along-rope) component.
                double radialComp = motion.dot(ropeDir);
                net.minecraft.world.phys.Vec3 tangential = motion.subtract(ropeDir.scale(radialComp));
                if (tangential.lengthSqr() > 1.0e-4) {
                    net.minecraft.world.phys.Vec3 tangDir = tangential.normalize();
                    double pump = blueduck.compound_v.Config.spiderSwingPump * fwd;
                    motion = motion.add(tangDir.scale(pump));
                } else if (fwd > 0) {
                    // Dead hang with no swing yet: pressing W kicks off the swing in the direction
                    // you're looking (projected horizontally), so you can start pumping.
                    net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                    net.minecraft.world.phys.Vec3 flatLook = new net.minecraft.world.phys.Vec3(look.x, 0, look.z);
                    if (flatLook.lengthSqr() > 1.0e-4) {
                        motion = motion.add(flatLook.normalize().scale(blueduck.compound_v.Config.spiderSwingPump));
                    }
                }
            }

            // SLIGHT LEFT/RIGHT STEERING: A/D applies a small horizontal nudge PERPENDICULAR to
            // the swing plane, biasing the arc sideways. It's deliberately weak - a lean, not a
            // joystick - so it can't beat gravity or fight the pendulum.
            float strafe = player.xxa; // A/D
            if (strafe != 0) {
                // Horizontal direction along the rope (the swing's forward-ish axis), then rotate
                // 90° in the horizontal plane to get "sideways".
                net.minecraft.world.phys.Vec3 flatRope = new net.minecraft.world.phys.Vec3(ropeDir.x, 0, ropeDir.z);
                if (flatRope.lengthSqr() > 1.0e-4) {
                    flatRope = flatRope.normalize();
                    net.minecraft.world.phys.Vec3 sideways = new net.minecraft.world.phys.Vec3(-flatRope.z, 0, flatRope.x);
                    double lean = blueduck.compound_v.Config.spiderSwingControl * strafe;
                    motion = motion.add(sideways.scale(lean));
                }
            }
        }

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
        if (!KeyBinding.isPowerKeyHeld()) return; // only intercept while holding the power key
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
        // swing responds instantly (scroll DOWN shortens / reels in, scroll UP adds slack -
        // matching the server-side block rope convention).
        if (spider) {
            adjustClientRope(dir * blueduck.compound_v.Config.spiderReelStep);
        }

        PacketHandler.sendToServer(new blueduck.compound_v.util.C2SScrollPacket(dir));
    }
}
