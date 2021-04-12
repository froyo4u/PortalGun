package tk.meowmc.portalgun.items;

import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.ModMain;
import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.portal.PortalManipulation;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.client.PortalgunClient;
import tk.meowmc.portalgun.misc.PortalMethods;
import tk.meowmc.portalgun.misc.TaskList;

import static net.minecraft.state.property.Properties.LAYERS;
import static net.minecraft.util.hit.HitResult.Type.BLOCK;
import static net.minecraft.util.hit.HitResult.Type.MISS;
import static tk.meowmc.portalgun.misc.PortalMethods.*;

public class PortalGunItem extends Item {
    public static final String KEY = Portalgun.MODID + ":portalgun_portals";
    public static HitResult hit;
    public static BlockHitResult blockHit;
    public static BlockPos blockPos;
    public static BlockState blockState;
    public static Portal newPortal1;
    public static Portal newPortal2;
    public static Entity portal1;
    public static Entity portal2;
    public static boolean waitPortal = false;
    public static BlockState space1BlockState;
    public static BlockState space2BlockState;
    public static BlockState space3BlockState;
    BlockPos space1BlockPos;
    BlockPos space2BlockPos;
    BlockPos space3BlockPos;
    Direction direction;
    Vec3d positionCorrectionVec;
    public static CompoundTag tag;
    public static CompoundTag portalsTag;
    public static boolean portal1Exists = false;
    public static boolean portal2Exists = false;

    public PortalGunItem(Settings settings) {
        super(settings);
    }

    public static void removeOldPortals(CompoundTag tag, CompoundTag portalsTag, Entity portal1, Entity portal2) {
        if (portal1 != null && portal2 != null) {
            portal1.kill();
            portal2.kill();
        }
        portalsTag.remove("Left" + "Portal");
        portalsTag.remove("Right" + "Portal");
        newPortal1.removed = false;
        newPortal2.removed = false;
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return false;
    }

    public static boolean isSnowUp(Direction direction) {
        return blockState.getBlock().is(Blocks.SNOW) && blockState.get(LAYERS) == 1 &&
                space2BlockState.getBlock().is(Blocks.SNOW) && space2BlockState.get(LAYERS) == 1 &&
                direction == Direction.UP;
    }

    public static boolean notSnowUp(Direction direction) {
        return !blockState.getBlock().is(Blocks.SNOW);
    }

    public void portal1Spawn(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        tag = itemStack.getOrCreateTag();
        portalsTag = tag.getCompound(world.getRegistryKey().toString());
        MinecraftClient client = MinecraftClient.getInstance();
        Entity entity = client.getCameraEntity();
        hit = entity.raycast(50.0D, 0.0F, false);
        blockHit = (BlockHitResult) hit;
        blockPos = blockHit.getBlockPos();
        blockState = world.getBlockState(blockPos);

        if (hit.getType() == BLOCK && PortalgunClient.delay) {
            direction = blockHit.getSide();

            space1BlockPos = blockPos.add(-1, 0, 0);
            space2BlockPos = blockPos.add(0, -1, 0);
            space3BlockPos = blockPos.add(0, -1, -1);


            switch (direction) {
                case UP:
                    space1BlockPos = blockPos.add(0, 1, 0);
                    space2BlockPos = blockPos.add(0, 0, -1);
                    space3BlockPos = blockPos.add(0, 1, -1);
                    break;
                case DOWN:
                    space1BlockPos = blockPos.add(0, -1, 0);
                    space2BlockPos = blockPos.add(0, 0, -1);
                    space3BlockPos = blockPos.add(0, -1, -1);
                    break;
                case NORTH:
                    space1BlockPos = blockPos.add(0, 0, -1);
                    space3BlockPos = blockPos.add(0, -1, -1);
                    break;
                case SOUTH:
                    space1BlockPos = blockPos.add(0, 0, 1);
                    space3BlockPos = blockPos.add(0, -1, 1);
                    break;
                case EAST:
                    space1BlockPos = blockPos.add(1, 0, 0);
                    space3BlockPos = blockPos.add(1, -1, 0);
                    break;
                case WEST:
                    space3BlockPos = blockPos.add(-1, -1, 0);
                    break;
            }

            dirOut1 = blockHit.getSide().getOpposite().getVector();
            if (dirOut1.getY() == 0) {
                dirUp1 = new Vec3i(0, 1, 0);
            } else {
                dirUp1 = user.getHorizontalFacing().getVector();
            }
            dirRight1 = dirUp1.crossProduct(dirOut1);

            dirRight1 = new Vec3i(-dirRight1.getX(), -dirRight1.getY(), -dirRight1.getZ());

            if (dirUp1.getY() != 1) {
                if (dirUp1.getZ() == -1) {
                    space2BlockPos = blockPos.add(0, 0, 1);
                    space3BlockPos = blockPos.add(0, 1, 1);
                }

                if (dirRight1.getX() == -1) {
                    space2BlockPos = blockPos.add(0, 0, -1);
                    space3BlockPos = blockPos.add(0, 1, -1);
                }

                switch (dirRight1.getZ()) {
                    case 1:
                        space2BlockPos = blockPos.add(-1, 0, 0);
                        space3BlockPos = blockPos.add(-1, 1, 0);
                        break;
                    case -1:
                        space2BlockPos = blockPos.add(1, 0, 0);
                        space3BlockPos = blockPos.add(1, 1, 0);
                        break;
                }
            }

            space1BlockState = world.getBlockState(space1BlockPos);
            space2BlockState = world.getBlockState(space2BlockPos);
            space3BlockState = world.getBlockState(space3BlockPos);

            double distanceX = blockPos.getX() - user.getX();
            double distanceY = blockPos.getY() - (user.getY() + user.getEyeHeight(user.getPose()));
            double distanceZ = blockPos.getZ() - user.getZ();

            Vec3d distanceVec = new Vec3d(distanceX, distanceY, distanceZ);

            double distance = distanceVec.length();
            int delay = (int) (0.5 * distance);

            ModMain.serverTaskList.addTask(TaskList.withDelay(delay, TaskList.oneShotTask(() -> {
                waitPortal = false;
            })));

            if (!user.getItemCooldownManager().isCoolingDown(this))
                user.getItemCooldownManager().set(this, 4);

            if (!world.isClient) {
                if (!waitPortal && !space1BlockState.isOpaque() && space2BlockState.isOpaque() && !space3BlockState.isOpaque()) {
                    world.playSound(null,
                            user.getX(),
                            user.getY(),
                            user.getZ(),
                            Portalgun.PORTAL1_SHOOT_EVENT,
                            SoundCategory.NEUTRAL,
                            1.0F,
                            1F);


                    waitPortal = true;


                    if (portalsTag.contains("Left" + "Portal")) {
                        newPortal1 = (Portal) ((ServerWorld) world).getEntity(portalsTag.getUuid("Left" + "Portal"));
                        if (newPortal1 != null) {
                            portal1Exists = true;
                        }
                    }

                    if (portalsTag.contains("Right" + "Portal")) {
                        newPortal2 = (Portal) ((ServerWorld) world).getEntity(portalsTag.getUuid("Right" + "Portal"));
                        if (newPortal2 != null) {
                            portal2Exists = true;
                        }
                    }

                    if (notSnowUp(direction) || isSnowUp(direction)) {
                        PortalMethods.portal1Methods(user, hit, world);

                        if (isSnowUp(direction)) {
                            newPortal1.updatePosition(newPortal1.getX(), newPortal1.getY() - 0.875, newPortal1.getZ());
                        }

                        if (newPortal2 != null && notSnowUp(direction))
                            newPortal1.setDestinationDimension(newPortal2.world.getRegistryKey());


                        PortalManipulation.adjustRotationToConnect(newPortal1, newPortal2);

                        ModMain.serverTaskList.addTask(TaskList.withDelay(delay, TaskList.oneShotTask(() -> {
                            if (McHelper.getServer().getThread() == Thread.currentThread()) {
                                if (portalsTag.contains("Left" + "Portal") && portalsTag.contains("Right" + "Portal")) {
                                    portal1 = (Portal) ((ServerWorld) world).getEntity(portalsTag.getUuid("Left" + "Portal"));
                                    portal2 = (Portal) ((ServerWorld) world).getEntity(portalsTag.getUuid("Right" + "Portal"));
                                }
                                world.playSound(null,
                                        newPortal1.getX(),
                                        newPortal1.getY(),
                                        newPortal1.getZ(),
                                        Portalgun.PORTAL_OPEN_EVENT,
                                        SoundCategory.NEUTRAL,
                                        1.0F,
                                        1F);
                                removeOldPortals(tag, portalsTag, portal1, portal2);
                                McHelper.spawnServerEntity(newPortal1);
                                McHelper.spawnServerEntity(newPortal2);
                            }
                            waitPortal = false;
                            if (newPortal2 != null) {
                                portalsTag.putUuid("Right" + "Portal", newPortal2.getUuid());
                                tag.put(world.getRegistryKey().toString(), portalsTag);
                            }
                            if (newPortal1 != null) {
                                portalsTag.putUuid("Left" + "Portal", newPortal1.getUuid());
                                tag.put(world.getRegistryKey().toString(), portalsTag);
                            }
                        })));
                    }
                }
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        tag = itemStack.getOrCreateTag();
        portalsTag = tag.getCompound(world.getRegistryKey().toString());
        MinecraftClient client = MinecraftClient.getInstance();
        user.getItemCooldownManager().set(this, 4);
        Entity entity = client.getCameraEntity();
        hit = entity.raycast(50.0D, 0.0F, false);
        blockHit = (BlockHitResult) hit;
        blockPos = blockHit.getBlockPos();
        blockState = world.getBlockState(blockPos);

        if (hit.getType() == MISS)
            return TypedActionResult.fail(itemStack);
        else if (hit.getType() == BLOCK) {
            direction = blockHit.getSide();

            space1BlockPos = blockPos.add(-1, 0, 0);
            space2BlockPos = blockPos.add(0, -1, 0);
            space3BlockPos = blockPos.add(0, -1, -1);

            switch (direction) {
                case UP:
                    space1BlockPos = blockPos.add(0, 1, 0);
                    space2BlockPos = blockPos.add(0, 0, -1);
                    space3BlockPos = blockPos.add(0, 1, -1);
                    break;
                case DOWN:
                    space1BlockPos = blockPos.add(0, -1, 0);
                    space2BlockPos = blockPos.add(0, 0, -1);
                    space3BlockPos = blockPos.add(0, -1, -1);
                    break;
                case NORTH:
                    space1BlockPos = blockPos.add(0, 0, -1);
                    space3BlockPos = blockPos.add(0, -1, -1);
                    break;
                case SOUTH:
                    space1BlockPos = blockPos.add(0, 0, 1);
                    space3BlockPos = blockPos.add(0, -1, 1);
                    break;
                case EAST:
                    space1BlockPos = blockPos.add(1, 0, 0);
                    space3BlockPos = blockPos.add(1, -1, 0);
                    break;
                case WEST:
                    space3BlockPos = blockPos.add(-1, -1, 0);
                    break;
            }

            dirOut2 = blockHit.getSide().getOpposite().getVector();
            if (dirOut2.getY() == 0) {
                dirUp2 = new Vec3i(0, 1, 0);
            } else {
                dirUp2 = user.getHorizontalFacing().getVector();
            }
            dirRight2 = dirUp2.crossProduct(dirOut2);

            dirRight2 = new Vec3i(-dirRight2.getX(), -dirRight2.getY(), -dirRight2.getZ());

            if (dirUp2.getY() != 1) {
                if (dirUp2.getZ() == -1) {
                    space2BlockPos = blockPos.add(0, 0, 1);
                    space3BlockPos = blockPos.add(0, 1, 1);
                }

                if (dirRight2.getX() == -1) {
                    space2BlockPos = blockPos.add(0, 0, -1);
                    space3BlockPos = blockPos.add(0, 1, -1);
                }

                switch (dirRight2.getZ()) {
                    case 1:
                        space2BlockPos = blockPos.add(-1, 0, 0);
                        space3BlockPos = blockPos.add(-1, 1, 0);
                        break;
                    case -1:
                        space2BlockPos = blockPos.add(1, 0, 0);
                        space3BlockPos = blockPos.add(1, 1, 0);
                        break;
                }
            }

            space1BlockState = world.getBlockState(space1BlockPos);
            space2BlockState = world.getBlockState(space2BlockPos);
            space3BlockState = world.getBlockState(space3BlockPos);

            double distanceX = blockPos.getX() - user.getX();
            double distanceY = blockPos.getY() - (user.getY() + user.getEyeHeight(user.getPose()));
            double distanceZ = blockPos.getZ() - user.getZ();

            Vec3d distanceVec = new Vec3d(distanceX, distanceY, distanceZ);

            double distance = distanceVec.length();

            int delay = (int) (0.5 * distance);

            client.attackCooldown = 10;
            client.gameRenderer.firstPersonRenderer.resetEquipProgress(user.getActiveHand());

            if (!world.isClient) {
                if (!waitPortal && !space1BlockState.isOpaque() && space2BlockState.isOpaque() && !space3BlockState.isOpaque()) {

                    world.playSound(null,
                            user.getX(),
                            user.getY(),
                            user.getZ(),
                            Portalgun.PORTAL2_SHOOT_EVENT,
                            SoundCategory.NEUTRAL,
                            1.0F,
                            1F);

                    waitPortal = true;

                    if (portalsTag.contains("Left" + "Portal")) {
                        newPortal1 = (Portal) ((ServerWorld) world).getEntity(portalsTag.getUuid("Left" + "Portal"));
                        if (newPortal1 != null) {
                            portal1Exists = true;
                        }
                    }

                    if (portalsTag.contains("Right" + "Portal")) {
                        newPortal2 = (Portal) ((ServerWorld) world).getEntity(portalsTag.getUuid("Right" + "Portal"));
                        if (newPortal2 != null) {
                            portal2Exists = true;
                        }
                    }

                    if (notSnowUp(direction) || isSnowUp(direction)) {
                        PortalMethods.portal2Methods(user, hit, world);

                        PortalManipulation.adjustRotationToConnect(newPortal1, newPortal2);

                        ModMain.serverTaskList.addTask(TaskList.withDelay(delay, TaskList.oneShotTask(() -> {
                            if (McHelper.getServer().getThread() == Thread.currentThread()) {
                                if (portalsTag.contains("Left" + "Portal") && portalsTag.contains("Right" + "Portal")) {
                                    portal1 = (Portal) ((ServerWorld) world).getEntity(portalsTag.getUuid("Left" + "Portal"));
                                    portal2 = (Portal) ((ServerWorld) world).getEntity(portalsTag.getUuid("Right" + "Portal"));
                                }
                                world.playSound(null,
                                        newPortal2.getX(),
                                        newPortal2.getY(),
                                        newPortal2.getZ(),
                                        Portalgun.PORTAL_OPEN_EVENT,
                                        SoundCategory.NEUTRAL,
                                        1.0F,
                                        1F);
                                removeOldPortals(tag, portalsTag, portal1, portal2);
                                McHelper.spawnServerEntity(newPortal1);
                                McHelper.spawnServerEntity(newPortal2);
                            }
                            waitPortal = false;
                            if (newPortal2 != null) {
                                portalsTag.putUuid("Right" + "Portal", newPortal2.getUuid());
                                tag.put(world.getRegistryKey().toString(), portalsTag);
                            }
                            if (newPortal1 != null) {
                                portalsTag.putUuid("Left" + "Portal", newPortal1.getUuid());
                                tag.put(world.getRegistryKey().toString(), portalsTag);
                            }
                        })));
                    }
                }
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }

        return TypedActionResult.pass(itemStack);
    }

}
