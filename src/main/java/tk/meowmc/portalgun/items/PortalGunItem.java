package tk.meowmc.portalgun.items;

import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.ModMain;
import com.qouteall.immersive_portals.my_util.MyTaskList;
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
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.entities.CustomPortal;
import tk.meowmc.portalgun.misc.PortalMethods;
import tk.meowmc.portalgun.misc.TaskList;

import static net.minecraft.state.property.Properties.LAYERS;
import static net.minecraft.util.hit.HitResult.Type.BLOCK;
import static net.minecraft.util.hit.HitResult.Type.MISS;
import static tk.meowmc.portalgun.misc.PortalMethods.*;

public class PortalGunItem extends Item implements IAnimatable {
    public static final String KEY = Portalgun.MODID + ":portalgun_portals";
    public static String controllerName = "portalgunController";
    public static CustomPortal newPortal1;
    public static CustomPortal newPortal2;
    public static Entity portal1;
    public static Entity portal2;
    public static boolean portal1Exists = false;
    public static boolean portal2Exists = false;
    public static boolean waitPortal = false;
    public static CompoundTag tag;
    public static CompoundTag portalsTag;
    public static HitResult hit;
    public static BlockHitResult blockHit;
    public static BlockPos blockPos;
    public static BlockState blockState;
    public static BlockPos upperPos;
    public static BlockPos lowerPos;
    public static BlockPos spaceUpperPos;
    public static BlockPos spaceLowerPos;
    public static BlockState upperBlockState;
    public static BlockState lowerBlockState;
    public static BlockState spaceUpperBlockState;
    public static BlockState spaceLowerBlockState;
    static AnimationController animController;
    static Direction direction;
    public AnimationFactory factory = new AnimationFactory(this);

    public PortalGunItem(Settings settings) {
        super(settings);
    }

    public static void removeOldPortals(PlayerEntity user) {
        if (newPortal1 != null) {
            newPortal1.kill();
            portalsTag.remove("PrimaryPortal" + user.getUuidAsString());
            newPortal1.removed = false;
        }
        if (newPortal2 != null) {
            newPortal2.kill();
            portalsTag.remove("SecondaryPortal" + user.getUuidAsString());
            newPortal2.removed = false;
        }
        tag.remove(user.world.getRegistryKey().toString());
    }

    public static boolean isSnowUp(Direction direction) {
        return (upperBlockState.isOpaque() && lowerBlockState.isOpaque()) && (!spaceUpperBlockState.isOpaque() && !spaceLowerBlockState.isOpaque()) && (upperBlockState.getBlock().is(Blocks.SNOW) && upperBlockState.get(LAYERS) == 1 && lowerBlockState.getBlock().is(Blocks.SNOW) && lowerBlockState.get(LAYERS) == 1 && direction == Direction.UP);
    }

    public static boolean canPlace(World world, Direction direction) {
        return (upperBlockState.isSideSolidFullSquare(world, upperPos, direction) &&
                lowerBlockState.isSideSolidFullSquare(world, lowerPos, direction) &&
                upperBlockState.isOpaque() &&
                lowerBlockState.isOpaque()) &&
                (!spaceUpperBlockState.isOpaque() && !spaceLowerBlockState.isOpaque());
    }

    private <P extends Item & IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        AnimationController controller = new AnimationController(this, controllerName, 1, this::predicate);
        animationData.addAnimationController(controller);
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return false;
    }

    public void portal1Spawn(World world, PlayerEntity user, Hand hand) {
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
        animController = GeckoLibUtil.getControllerForStack(this.factory, itemStack, controllerName);
        client.attackCooldown = 0;


        if (hit.getType() == BLOCK) {
            direction = blockHit.getSide();


            dirOut1 = blockHit.getSide().getOpposite().getVector();
            if (dirOut1.getY() == 0) {
                dirUp1 = new Vec3i(0, 1, 0);
            } else {
                dirUp1 = user.getHorizontalFacing().getVector();
            }
            dirRight1 = dirUp1.crossProduct(dirOut1);

            dirRight1 = new Vec3i(-dirRight1.getX(), -dirRight1.getY(), -dirRight1.getZ());


            double distanceX = blockPos.getX() - user.getX();
            double distanceY = blockPos.getY() - (user.getY() + user.getEyeHeight(user.getPose()));
            double distanceZ = blockPos.getZ() - user.getZ();

            Vec3d distanceVec = new Vec3d(distanceX, distanceY, distanceZ);

            double distance = distanceVec.length();

            int delay = (int) (0.5 * distance);


            if (!world.isClient && !waitPortal) {
                world.playSound(null,
                        user.getX(),
                        user.getY(),
                        user.getZ(),
                        Portalgun.PORTAL1_SHOOT_EVENT,
                        SoundCategory.NEUTRAL,
                        1.0F,
                        1F);


                upperPos = new BlockPos(
                        blockPos.getX() - dirRight1.crossProduct(dirUp1).getX(),
                        blockPos.getY() - dirRight1.crossProduct(dirUp1).getY() + dirUp1.getY(),
                        blockPos.getZ() - dirRight1.crossProduct(dirUp1).getZ());
                lowerPos = new BlockPos(
                        blockPos.getX() - dirRight1.crossProduct(dirUp1).getX() - dirUp1.getX(),
                        blockPos.getY() - dirRight1.crossProduct(dirUp1).getY(),
                        blockPos.getZ() - dirRight1.crossProduct(dirUp1).getZ() - dirUp1.getZ());

                spaceUpperPos = new BlockPos(
                        blockPos.getX() - dirRight1.crossProduct(dirUp1).getX(),
                        blockPos.getY() - dirRight1.crossProduct(dirUp1).getY() + dirUp1.getY(),
                        blockPos.getZ() - dirRight1.crossProduct(dirUp1).getZ());
                spaceLowerPos = new BlockPos(
                        blockPos.getX() - dirRight1.crossProduct(dirUp1).getX() - dirUp1.getX(),
                        blockPos.getY() - dirRight1.crossProduct(dirUp1).getY(),
                        blockPos.getZ() - dirRight1.crossProduct(dirUp1).getZ() - dirUp1.getZ());

                switch (direction) {
                    case UP:
                        upperPos = upperPos.add(0, 1, 0);
                        lowerPos = lowerPos.add(0, 1, 0);
                        spaceUpperPos = spaceUpperPos.add(0, 2, 0);
                        spaceLowerPos = spaceLowerPos.add(0, 2, 0);
                        break;
                    case DOWN:
                        upperPos = upperPos.add(0, -1, 0);
                        lowerPos = lowerPos.add(0, -1, 0);
                        break;
                    case NORTH:
                        upperPos = upperPos.add(0, -1, -1);
                        lowerPos = lowerPos.add(0, -1, -1);
                        spaceUpperPos = spaceUpperPos.add(0, -1, -2);
                        spaceLowerPos = spaceLowerPos.add(0, -1, -2);
                        break;
                    case SOUTH:
                        upperPos = upperPos.add(0, -1, 1);
                        lowerPos = lowerPos.add(0, -1, 1);
                        spaceUpperPos = spaceUpperPos.add(0, -1, 2);
                        spaceLowerPos = spaceLowerPos.add(0, -1, 2);
                        break;
                    case EAST:
                        upperPos = upperPos.add(1, -1, 0);
                        lowerPos = lowerPos.add(1, -1, 0);
                        spaceUpperPos = spaceUpperPos.add(2, -1, 0);
                        spaceLowerPos = spaceLowerPos.add(2, -1, 0);
                        break;
                    case WEST:
                        upperPos = upperPos.add(-1, -1, 0);
                        lowerPos = lowerPos.add(-1, -1, 0);
                        spaceUpperPos = spaceUpperPos.add(-2, -1, 0);
                        spaceLowerPos = spaceLowerPos.add(-2, -1, 0);
                        break;
                }

                upperBlockState = world.getBlockState(upperPos);
                lowerBlockState = world.getBlockState(lowerPos);
                spaceUpperBlockState = world.getBlockState(spaceUpperPos);
                spaceLowerBlockState = world.getBlockState(spaceLowerPos);

                if (isSnowUp(direction) || canPlace(world, direction)) {


                    if (portalsTag.contains("PrimaryPortal" + user.getUuidAsString())) {
                        newPortal1 = (CustomPortal) ((ServerWorld) world).getEntity(portalsTag.getUuid("PrimaryPortal" + user.getUuidAsString()));
                        if (newPortal1 == null) {
                            newPortal1 = Portalgun.CUSTOM_PORTAL.create(McHelper.getServer().getWorld(world.getRegistryKey()));
                            portal1Exists = false;
                        } else
                            portal1Exists = true;
                    }
                    if (portalsTag.contains("SecondaryPortal" + user.getUuidAsString())) {
                        newPortal2 = (CustomPortal) ((ServerWorld) world).getEntity(portalsTag.getUuid("SecondaryPortal" + user.getUuidAsString()));
                        if (newPortal2 == null) {
                            newPortal2 = Portalgun.CUSTOM_PORTAL.create(McHelper.getServer().getWorld(world.getRegistryKey()));
                            portal2Exists = false;
                        } else
                            portal2Exists = true;
                    }
                    PortalMethods.portal1Methods(user, hit, world);

                    PortalManipulation.adjustRotationToConnect(newPortal1, newPortal2);

                    waitPortal = true;
                    ModMain.serverTaskList.addTask(MyTaskList.withDelay(delay, MyTaskList.oneShotTask(() -> {
                        if (McHelper.getServer().getThread() == Thread.currentThread()) {
                            world.playSound(null,
                                    newPortal1.getX(),
                                    newPortal1.getY(),
                                    newPortal1.getZ(),
                                    Portalgun.PORTAL_OPEN_EVENT,
                                    SoundCategory.NEUTRAL,
                                    1.0F,
                                    1F);
                            if (!portal1Exists) {
                                removeOldPortals(user);
                                McHelper.spawnServerEntity(newPortal1);
                            }
                            if (!portal2Exists) {
                                removeOldPortals(user);
                                McHelper.spawnServerEntity(newPortal2);
                            }
                            if (portal1Exists)
                                newPortal1.reloadAndSyncToClient();
                            if (portal2Exists)
                                newPortal2.reloadAndSyncToClient();
                        }
                        waitPortal = false;
                        if (newPortal2 != null) {
                            portalsTag.putUuid("SecondaryPortal" + user.getUuidAsString(), newPortal2.getUuid());
                            tag.put(world.getRegistryKey().toString(), portalsTag);
                        }
                        if (newPortal1 != null) {
                            portalsTag.putUuid("PrimaryPortal" + user.getUuidAsString(), newPortal1.getUuid());
                            tag.put(world.getRegistryKey().toString(), portalsTag);
                        }
                    })));
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
        animController = GeckoLibUtil.getControllerForStack(this.factory, itemStack, controllerName);

        if (hit.getType() == MISS)
            return TypedActionResult.fail(itemStack);
        else if (hit.getType() == BLOCK) {
            direction = blockHit.getSide();


            dirOut2 = blockHit.getSide().getOpposite().getVector();
            if (dirOut2.getY() == 0) {
                dirUp2 = new Vec3i(0, 1, 0);
            } else {
                dirUp2 = user.getHorizontalFacing().getVector();
            }
            dirRight2 = dirUp2.crossProduct(dirOut2);

            dirRight2 = new Vec3i(-dirRight2.getX(), -dirRight2.getY(), -dirRight2.getZ());


            double distanceX = blockPos.getX() - user.getX();
            double distanceY = blockPos.getY() - (user.getY() + user.getEyeHeight(user.getPose()));
            double distanceZ = blockPos.getZ() - user.getZ();

            Vec3d distanceVec = new Vec3d(distanceX, distanceY, distanceZ);

            double distance = distanceVec.length();

            int delay = (int) (0.5 * distance);

            if (world.isClient) {
                animController.markNeedsReload();
                animController.setAnimation(new AnimationBuilder().addAnimation("portal_shoot", false));
            }

            if (!world.isClient && !waitPortal) {
                world.playSound(null,
                        user.getX(),
                        user.getY(),
                        user.getZ(),
                        Portalgun.PORTAL2_SHOOT_EVENT,
                        SoundCategory.NEUTRAL,
                        1.0F,
                        1F);


                upperPos = new BlockPos(
                        blockPos.getX() - dirRight2.crossProduct(dirUp2).getX(),
                        blockPos.getY() - dirRight2.crossProduct(dirUp2).getY() + dirUp2.getY(),
                        blockPos.getZ() - dirRight2.crossProduct(dirUp2).getZ());
                lowerPos = new BlockPos(
                        blockPos.getX() - dirRight2.crossProduct(dirUp2).getX() - dirUp2.getX(),
                        blockPos.getY() - dirRight2.crossProduct(dirUp2).getY(),
                        blockPos.getZ() - dirRight2.crossProduct(dirUp2).getZ() - dirUp2.getZ());

                spaceUpperPos = new BlockPos(
                        blockPos.getX() - dirRight2.crossProduct(dirUp2).getX(),
                        blockPos.getY() - dirRight2.crossProduct(dirUp2).getY() + dirUp2.getY(),
                        blockPos.getZ() - dirRight2.crossProduct(dirUp2).getZ());
                spaceLowerPos = new BlockPos(
                        blockPos.getX() - dirRight2.crossProduct(dirUp2).getX() - dirUp2.getX(),
                        blockPos.getY() - dirRight2.crossProduct(dirUp2).getY(),
                        blockPos.getZ() - dirRight2.crossProduct(dirUp2).getZ() - dirUp2.getZ());

                switch (direction) {
                    case UP:
                        upperPos = upperPos.add(0, 1, 0);
                        lowerPos = lowerPos.add(0, 1, 0);
                        spaceUpperPos = spaceUpperPos.add(0, 2, 0);
                        spaceLowerPos = spaceLowerPos.add(0, 2, 0);
                        break;
                    case DOWN:
                        upperPos = upperPos.add(0, -1, 0);
                        lowerPos = lowerPos.add(0, -1, 0);
                        break;
                    case NORTH:
                        upperPos = upperPos.add(0, -1, -1);
                        lowerPos = lowerPos.add(0, -1, -1);
                        spaceUpperPos = spaceUpperPos.add(0, -1, -2);
                        spaceLowerPos = spaceLowerPos.add(0, -1, -2);
                        break;
                    case SOUTH:
                        upperPos = upperPos.add(0, -1, 1);
                        lowerPos = lowerPos.add(0, -1, 1);
                        spaceUpperPos = spaceUpperPos.add(0, -1, 2);
                        spaceLowerPos = spaceLowerPos.add(0, -1, 2);
                        break;
                    case EAST:
                        upperPos = upperPos.add(1, -1, 0);
                        lowerPos = lowerPos.add(1, -1, 0);
                        spaceUpperPos = spaceUpperPos.add(2, -1, 0);
                        spaceLowerPos = spaceLowerPos.add(2, -1, 0);
                        break;
                    case WEST:
                        upperPos = upperPos.add(-1, -1, 0);
                        lowerPos = lowerPos.add(-1, -1, 0);
                        spaceUpperPos = spaceUpperPos.add(-2, -1, 0);
                        spaceLowerPos = spaceLowerPos.add(-2, -1, 0);
                        break;
                }

                upperBlockState = world.getBlockState(upperPos);
                lowerBlockState = world.getBlockState(lowerPos);
                spaceUpperBlockState = world.getBlockState(spaceUpperPos);
                spaceLowerBlockState = world.getBlockState(spaceLowerPos);

                if (isSnowUp(direction) || canPlace(world, direction)) {

                    if (portalsTag.contains("PrimaryPortal" + user.getUuidAsString())) {
                        newPortal1 = (CustomPortal) ((ServerWorld) world).getEntity(portalsTag.getUuid("PrimaryPortal" + user.getUuidAsString()));
                        if (newPortal1 == null) {
                            newPortal1 = Portalgun.CUSTOM_PORTAL.create(McHelper.getServer().getWorld(world.getRegistryKey()));
                            portal1Exists = false;
                        } else
                            portal1Exists = true;
                    }
                    if (portalsTag.contains("SecondaryPortal" + user.getUuidAsString())) {
                        newPortal2 = (CustomPortal) ((ServerWorld) world).getEntity(portalsTag.getUuid("SecondaryPortal" + user.getUuidAsString()));
                        if (newPortal2 == null) {
                            newPortal2 = Portalgun.CUSTOM_PORTAL.create(McHelper.getServer().getWorld(world.getRegistryKey()));
                            portal2Exists = false;
                        } else
                            portal2Exists = true;
                    }
                    PortalMethods.portal2Methods(user, hit, world);

                    PortalManipulation.adjustRotationToConnect(newPortal1, newPortal2);

                    waitPortal = true;
                    ModMain.serverTaskList.addTask(MyTaskList.withDelay(delay, MyTaskList.oneShotTask(() -> {
                        if (McHelper.getServer().getThread() == Thread.currentThread()) {
                            world.playSound(null,
                                    newPortal2.getX(),
                                    newPortal2.getY(),
                                    newPortal2.getZ(),
                                    Portalgun.PORTAL_OPEN_EVENT,
                                    SoundCategory.NEUTRAL,
                                    1.0F,
                                    1F);
                            if (!portal1Exists) {
                                removeOldPortals(user);
                                McHelper.spawnServerEntity(newPortal1);
                            }
                            if (!portal2Exists) {
                                removeOldPortals(user);
                                McHelper.spawnServerEntity(newPortal2);
                            }
                            if (portal1Exists)
                                newPortal1.reloadAndSyncToClient();
                            if (portal2Exists)
                                newPortal2.reloadAndSyncToClient();
                        }
                        waitPortal = false;
                        if (newPortal2 != null) {
                            portalsTag.putUuid("SecondaryPortal" + user.getUuidAsString(), newPortal2.getUuid());
                            tag.put(world.getRegistryKey().toString(), portalsTag);
                        }
                        if (newPortal1 != null) {
                            portalsTag.putUuid("PrimaryPortal" + user.getUuidAsString(), newPortal1.getUuid());
                            tag.put(world.getRegistryKey().toString(), portalsTag);
                        }
                    })));
                }
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }

        return TypedActionResult.pass(itemStack);
    }
}
