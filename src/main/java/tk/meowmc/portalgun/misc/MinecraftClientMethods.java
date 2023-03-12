package tk.meowmc.portalgun.misc;

public class MinecraftClientMethods {
//    private static final MinecraftClient client = MinecraftClient.getInstance();
//    public static RegistryKey<World> remotePointedDim;
//    public static HitResult remoteHitResult;
//    public static boolean isContextSwitched = false;
//
//    public MinecraftClientMethods() {
//    }
//
//    public static boolean isPointingToPortal() {
//        return remotePointedDim != null;
//    }
//
//    private static BlockHitResult createMissedHitResult(Vec3d from, Vec3d to) {
//        Vec3d dir = to.subtract(from).normalize();
//        return BlockHitResult.createMissed(to, Direction.getFacing(dir.x, dir.y, dir.z), new BlockPos(to));
//    }
//
//    private static boolean hitResultIsMissedOrNull(HitResult bhr) {
//        return bhr == null || bhr.getType() == HitResult.Type.MISS;
//    }
//
//    private static double getCurrentTargetDistance() {
//        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
//        if (hitResultIsMissedOrNull(client.crosshairTarget)) {
//            return 23333.0D;
//        } else {
//            if (client.crosshairTarget instanceof BlockHitResult) {
//                BlockPos hitPos = ((BlockHitResult) client.crosshairTarget).getBlockPos();
//                if (client.world.getBlockState(hitPos).getBlock() == PortalPlaceholderBlock.instance) {
//                    return 23333.0D;
//                }
//            }
//
//            return cameraPos.distanceTo(client.crosshairTarget.getPos());
//        }
//    }
//
//    public static void myHandleBlockBreaking(boolean isKeyPressed) {
//        if (!client.player.isUsingItem() && !client.player.isHolding(PORTALGUN)) {
//            if (isKeyPressed && isPointingToPortal()) {
//                BlockHitResult blockHitResult = (BlockHitResult) remoteHitResult;
//                BlockPos blockPos = blockHitResult.getBlockPos();
//                ClientWorld remoteWorld = ClientWorldLoader.getWorld(remotePointedDim);
//                if (!remoteWorld.getBlockState(blockPos).isAir()) {
//                    Direction direction = blockHitResult.getSide();
//                    if (myUpdateBlockBreakingProgress(blockPos, direction)) {
//                        client.particleManager.addBlockBreakingParticles(blockPos, direction);
//                        client.player.swingHand(Hand.MAIN_HAND);
//                    }
//                }
//            } else if (!client.player.isHolding(PORTALGUN))
//                client.interactionManager.cancelBlockBreaking();
//        }
//
//    }
//
//    public static boolean myUpdateBlockBreakingProgress(BlockPos blockPos, Direction direction) {
//        ClientWorld oldWorld = client.world;
//        client.world = ClientWorldLoader.getWorld(remotePointedDim);
//        isContextSwitched = true;
//
//        boolean var3 = false;
//        if (!client.player.isHolding(PORTALGUN))
//            try {
//                var3 = client.interactionManager.updateBlockBreakingProgress(blockPos, direction);
//            } finally {
//                client.world = oldWorld;
//                isContextSwitched = false;
//            }
//
//        return var3;
//    }
//
//    public static void myAttackBlock() {
//        ClientWorld targetWorld = ClientWorldLoader.getWorld(remotePointedDim);
//        BlockPos blockPos = ((BlockHitResult) remoteHitResult).getBlockPos();
//        if (!targetWorld.isAir(blockPos)) {
//            ClientWorld oldWorld = client.world;
//            client.world = targetWorld;
//            isContextSwitched = true;
//
//            if (!client.player.isHolding(PORTALGUN))
//                try {
//                    client.interactionManager.attackBlock(blockPos, ((BlockHitResult) remoteHitResult).getSide());
//                } finally {
//                    client.world = oldWorld;
//                    isContextSwitched = false;
//                }
//
//            client.player.swingHand(Hand.MAIN_HAND);
//        }
//    }
//
//    public static void doAttack() {
//        if (client.attackCooldown <= 0) {
//            if (client.crosshairTarget == null) {
//                Portalgun.LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
//                if (client.interactionManager.hasLimitedAttackSpeed() && !client.player.isHolding(PORTALGUN)) {
//                    client.attackCooldown = 10;
//                }
//
//            } else if (!client.player.isRiding()) {
//                switch (client.crosshairTarget.getType()) {
//                    case ENTITY:
//                        client.interactionManager.attackEntity(client.player, ((EntityHitResult) client.crosshairTarget).getEntity());
//                        break;
//                    case BLOCK:
//                        BlockHitResult blockHitResult = (BlockHitResult) client.crosshairTarget;
//                        BlockPos blockPos = blockHitResult.getBlockPos();
//                        if (!client.world.getBlockState(blockPos).isAir() && !client.player.isHolding(PORTALGUN)) {
//                            client.interactionManager.attackBlock(blockPos, blockHitResult.getSide());
//                            break;
//                        }
//                    case MISS:
//                        if (client.interactionManager.hasLimitedAttackSpeed() && !client.player.isHolding(PORTALGUN)) {
//                            client.attackCooldown = 10;
//                        }
//                        if (!client.player.isHolding(PORTALGUN))
//                            client.player.resetLastAttackedTicks();
//                }
//                if (!client.player.isHolding(PORTALGUN))
//                    client.player.swingHand(Hand.MAIN_HAND);
//            }
//        }
//    }
//
//    private static ActionResult myInteractBlock(Hand hand, ClientWorld targetWorld, BlockHitResult blockHitResult) {
//        ClientWorld oldWorld = client.world;
//
//        ActionResult var4;
//        try {
//            client.player.world = targetWorld;
//            client.world = targetWorld;
//            isContextSwitched = true;
//            var4 = client.interactionManager.interactBlock(client.player, hand, blockHitResult);
//        } finally {
//            client.player.world = oldWorld;
//            client.world = oldWorld;
//            isContextSwitched = false;
//        }
//
//        return var4;
//    }
}
