package tk.meowmc.portalgun.misc;

import com.qouteall.immersive_portals.ClientWorldLoader;
import com.qouteall.immersive_portals.block_manipulation.BlockManipulationServer;
import com.qouteall.immersive_portals.commands.PortalCommand;
import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.portal.PortalPlaceholderBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import tk.meowmc.portalgun.Portalgun;

import static tk.meowmc.portalgun.Portalgun.PORTALGUN;

@SuppressWarnings({"ReturnOfNull", "UnnecessaryReturnStatement"})
public class MinecraftClientMethods {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    public static RegistryKey<World> remotePointedDim;
    public static HitResult remoteHitResult;
    public static boolean isContextSwitched = false;

    public MinecraftClientMethods() {
    }

    public static boolean isPointingToPortal() {
        return remotePointedDim != null;
    }

    private static BlockHitResult createMissedHitResult(Vec3d from, Vec3d to) {
        Vec3d dir = to.subtract(from).normalize();
        return BlockHitResult.createMissed(to, Direction.getFacing(dir.x, dir.y, dir.z), new BlockPos(to));
    }

    private static boolean hitResultIsMissedOrNull(HitResult bhr) {
        return bhr == null || bhr.getType() == HitResult.Type.MISS;
    }

    public static void updatePointedBlock(float tickDelta) {
        if (client.interactionManager != null && client.world != null) {
            remotePointedDim = null;
            remoteHitResult = null;
            Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
            float reachDistance = client.interactionManager.getReachDistance();
            PortalCommand.getPlayerPointingPortalRaw(client.player, tickDelta, (double) reachDistance, true).ifPresent((pair) -> {
                if (((Portal) pair.getFirst()).isInteractable()) {
                    double distanceToPortalPointing = ((Vec3d) pair.getSecond()).distanceTo(cameraPos);
                    if (distanceToPortalPointing < getCurrentTargetDistance() + 0.2D) {
                        client.crosshairTarget = createMissedHitResult(cameraPos, (Vec3d) pair.getSecond());
                        updateTargetedBlockThroughPortal(cameraPos, client.player.getRotationVec(tickDelta), client.player.world.getRegistryKey(), distanceToPortalPointing, (double) reachDistance, (Portal) pair.getFirst());
                    }
                }

            });
        }
    }

    private static double getCurrentTargetDistance() {
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
        if (hitResultIsMissedOrNull(client.crosshairTarget)) {
            return 23333.0D;
        } else {
            if (client.crosshairTarget instanceof BlockHitResult) {
                BlockPos hitPos = ((BlockHitResult) client.crosshairTarget).getBlockPos();
                if (client.world.getBlockState(hitPos).getBlock() == PortalPlaceholderBlock.instance) {
                    return 23333.0D;
                }
            }

            return cameraPos.distanceTo(client.crosshairTarget.getPos());
        }
    }

    private static void updateTargetedBlockThroughPortal(Vec3d cameraPos, Vec3d viewVector, RegistryKey<World> playerDimension, double beginDistance, double endDistance, Portal portal) {
        Vec3d from = portal.transformPoint(cameraPos.add(viewVector.multiply(beginDistance)));
        Vec3d to = portal.transformPoint(cameraPos.add(viewVector.multiply(endDistance)));
        RaycastContext context = new RaycastContext(from, to, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, client.player);
        ClientWorld world = ClientWorldLoader.getWorld(portal.dimensionTo);
        remoteHitResult = (HitResult) BlockView.raycast(context, (rayTraceContext, blockPos) -> {
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.getBlock() == PortalPlaceholderBlock.instance) {
                return null;
            } else if (blockState.getBlock() == Blocks.BARRIER) {
                return null;
            } else {
                FluidState fluidState = world.getFluidState(blockPos);
                Vec3d start = rayTraceContext.getStart();
                Vec3d end = rayTraceContext.getEnd();
                Vec3d correctedStart = start.subtract(end.subtract(start).multiply(0.0015D));
                VoxelShape solidShape = rayTraceContext.getBlockShape(blockState, world, blockPos);
                BlockHitResult blockHitResult = world.raycastBlock(correctedStart, end, blockPos, solidShape, blockState);
                VoxelShape fluidShape = rayTraceContext.getFluidShape(fluidState, world, blockPos);
                BlockHitResult blockHitResult2 = fluidShape.raycast(start, end, blockPos);
                double d = blockHitResult == null ? 1.7976931348623157E308D : rayTraceContext.getStart().squaredDistanceTo(blockHitResult.getPos());
                double e = blockHitResult2 == null ? 1.7976931348623157E308D : rayTraceContext.getStart().squaredDistanceTo(blockHitResult2.getPos());
                return d <= e ? blockHitResult : blockHitResult2;
            }
        }, (rayTraceContext) -> {
            Vec3d vec3d = rayTraceContext.getStart().subtract(rayTraceContext.getEnd());
            return BlockHitResult.createMissed(rayTraceContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), new BlockPos(rayTraceContext.getEnd()));
        });
        if (remoteHitResult.getPos().y < 0.1D) {
            remoteHitResult = new BlockHitResult(remoteHitResult.getPos(), Direction.DOWN, ((BlockHitResult) remoteHitResult).getBlockPos(), ((BlockHitResult) remoteHitResult).isInsideBlock());
        }

        if (remoteHitResult != null && !world.getBlockState(((BlockHitResult) remoteHitResult).getBlockPos()).isAir()) {
            client.crosshairTarget = createMissedHitResult(from, to);
            remotePointedDim = portal.dimensionTo;
        }

    }

    public static void myHandleBlockBreaking(boolean isKeyPressed) {
        if (/* !client.player.isUsingItem() && */ !client.player.isHolding(PORTALGUN)) {
            if (isKeyPressed && isPointingToPortal()) {
                BlockHitResult blockHitResult = (BlockHitResult) remoteHitResult;
                BlockPos blockPos = blockHitResult.getBlockPos();
                ClientWorld remoteWorld = ClientWorldLoader.getWorld(remotePointedDim);
                if (!remoteWorld.getBlockState(blockPos).isAir()) {
                    Direction direction = blockHitResult.getSide();
                    if (myUpdateBlockBreakingProgress(blockPos, direction)) {
                        client.particleManager.addBlockBreakingParticles(blockPos, direction);
                        client.player.swingHand(Hand.MAIN_HAND);
                    }
                }
            } else {
                client.interactionManager.cancelBlockBreaking();
            }
        }

    }

    public static boolean myUpdateBlockBreakingProgress(BlockPos blockPos, Direction direction) {
        ClientWorld oldWorld = client.world;
        client.world = ClientWorldLoader.getWorld(remotePointedDim);
        isContextSwitched = true;

        boolean var3;
        try {
            var3 = client.interactionManager.updateBlockBreakingProgress(blockPos, direction);
        } finally {
            client.world = oldWorld;
            isContextSwitched = false;
        }

        return var3;
    }

    public static void myAttackBlock() {
        ClientWorld targetWorld = ClientWorldLoader.getWorld(remotePointedDim);
        BlockPos blockPos = ((BlockHitResult) remoteHitResult).getBlockPos();
        if (!targetWorld.isAir(blockPos) && !client.player.isHolding(PORTALGUN)) {
            ClientWorld oldWorld = client.world;
            client.world = targetWorld;
            isContextSwitched = true;

            try {
                client.interactionManager.attackBlock(blockPos, ((BlockHitResult) remoteHitResult).getSide());
            } finally {
                client.world = oldWorld;
                isContextSwitched = false;
            }
        }
    }

    public static void doAttack() {
        if (client.attackCooldown <= 0) {
            if (client.crosshairTarget == null) {
                Portalgun.LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
                if (client.interactionManager.hasLimitedAttackSpeed()) {
                    client.attackCooldown = 10;
                }

            } else if (!client.player.isRiding()) {
                switch (client.crosshairTarget.getType()) {
                    case ENTITY:
                        client.interactionManager.attackEntity(client.player, ((EntityHitResult) client.crosshairTarget).getEntity());
                        break;
                    case BLOCK:
                        BlockHitResult blockHitResult = (BlockHitResult) client.crosshairTarget;
                        BlockPos blockPos = blockHitResult.getBlockPos();
                        if (!client.world.getBlockState(blockPos).isAir() && !client.player.isHolding(PORTALGUN)) {
                            client.interactionManager.attackBlock(blockPos, blockHitResult.getSide());
                            break;
                        } else if (!client.world.getBlockState(blockPos).isAir() && client.player.isHolding(PORTALGUN))
                            client.attackCooldown = 10;
                    case MISS:
                        if (client.interactionManager.hasLimitedAttackSpeed() || client.player.isHolding(PORTALGUN)) {
                            client.attackCooldown = 10;
                        }

                        client.player.resetLastAttackedTicks();
                }
                if (!client.player.isHolding(PORTALGUN))
                    client.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    public static void myItemUse(Hand hand) {
        ClientWorld targetWorld = ClientWorldLoader.getWorld(remotePointedDim);
        ItemStack itemStack = client.player.getStackInHand(hand);
        BlockHitResult blockHitResult = (BlockHitResult) remoteHitResult;
        Pair<BlockHitResult, RegistryKey<World>> result = BlockManipulationServer.getHitResultForPlacing(targetWorld, blockHitResult);
        blockHitResult = (BlockHitResult) result.getLeft();
        targetWorld = ClientWorldLoader.getWorld((RegistryKey) result.getRight());
        remoteHitResult = blockHitResult;
        remotePointedDim = (RegistryKey) result.getRight();
        int i = itemStack.getCount();
        ActionResult actionResult2 = myInteractBlock(hand, targetWorld, blockHitResult);
        if (!actionResult2.isAccepted()) {
            if (actionResult2 != ActionResult.FAIL) {
                if (!itemStack.isEmpty()) {
                    ActionResult actionResult3 = client.interactionManager.interactItem(client.player, targetWorld, hand);
                    if (actionResult3.isAccepted()) {
                        if (actionResult3.shouldSwingHand() && !client.player.isHolding(PORTALGUN)) {
                            client.player.swingHand(hand);
                        }

                        client.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                        return;
                    }
                }

            }
        } else {
            if (actionResult2.shouldSwingHand() && !client.player.isHolding(PORTALGUN)) {
                client.player.swingHand(hand);
                if (!itemStack.isEmpty() && (itemStack.getCount() != i || client.interactionManager.hasCreativeInventory())) {
                    client.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                }
            } else if (client.player.isHolding(PORTALGUN))
                client.attackCooldown = 10;

        }
    }

    private static ActionResult myInteractBlock(Hand hand, ClientWorld targetWorld, BlockHitResult blockHitResult) {
        ClientWorld oldWorld = client.world;

        ActionResult var4;
        try {
            client.player.world = targetWorld;
            client.world = targetWorld;
            isContextSwitched = true;
            var4 = client.interactionManager.interactBlock(client.player, targetWorld, hand, blockHitResult);
        } finally {
            client.player.world = oldWorld;
            client.world = oldWorld;
            isContextSwitched = false;
        }

        return var4;
    }
}
