package tk.meowmc.portalgun.items;

import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.ModMain;
import com.qouteall.immersive_portals.my_util.MyTaskList;
import com.qouteall.immersive_portals.portal.PortalManipulation;
import net.minecraft.block.BlockState;
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
import org.apache.logging.log4j.Level;
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
import tk.meowmc.portalgun.entities.PortalOverlay;
import tk.meowmc.portalgun.misc.PortalMethods;

import static net.minecraft.util.hit.HitResult.Type.BLOCK;
import static net.minecraft.util.hit.HitResult.Type.MISS;
import static tk.meowmc.portalgun.misc.PortalMethods.*;

public class PortalGunItem extends Item implements IAnimatable {
    public static final String KEY = Portalgun.MODID + ":portalgun_portals";
    public static String controllerName = "portalgunController";
    public static CustomPortal newPortal1;
    public static CustomPortal newPortal2;
    public static PortalOverlay portalOutline1;
    public static PortalOverlay portalOutline2;
    public static Entity portal1;
    public static Entity portal2;
    public static boolean portal1Exists = false;
    public static boolean portal2Exists = false;
    public static boolean outline1Exists = false;
    public static boolean outline2Exists = false;
    public static boolean waitPortal = false;
    public static CompoundTag tag;
    public static CompoundTag portalsTag;
    public static HitResult hit;
    public static BlockHitResult blockHit;
    public static BlockPos blockPos;
    public static BlockState blockState;
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

    /**
     * Thanks to Fusion Flux for this btw
     */
    private boolean validPos(World world, Vec3i up, Vec3i right, Vec3d portalPos1) {
        Vec3d CalculatedAxisW;
        Vec3d CalculatedAxisH;
        Vec3d posNormal;
        CalculatedAxisW = Vec3d.of(right);
        CalculatedAxisH = Vec3d.of(up).multiply(-1);

        BlockPos upperPos = new BlockPos(
                portalPos1.getX() - CalculatedAxisW.crossProduct(CalculatedAxisH).getX(),
                portalPos1.getY() - CalculatedAxisW.crossProduct(CalculatedAxisH).getY(),
                portalPos1.getZ() - CalculatedAxisW.crossProduct(CalculatedAxisH).getZ());
        BlockPos lowerPos = new BlockPos(
                portalPos1.getX() - CalculatedAxisW.crossProduct(CalculatedAxisH).getX() - Math.abs(CalculatedAxisH.getX()),
                portalPos1.getY() - CalculatedAxisW.crossProduct(CalculatedAxisH).getY() + CalculatedAxisH.getY(),
                portalPos1.getZ() - CalculatedAxisW.crossProduct(CalculatedAxisH).getZ() - Math.abs(CalculatedAxisH.getZ()));

        if (!world.getBlockState(upperPos).isSideSolidFullSquare(world, upperPos, direction) ||
                (!world.getBlockState(lowerPos).isSideSolidFullSquare(world, lowerPos, direction)
                ) || !world.getBlockState(new BlockPos(portalPos1)).isAir() || !world.getBlockState(new BlockPos(
                portalPos1.getX() - Math.abs(CalculatedAxisH.getX()),
                portalPos1.getY() + CalculatedAxisH.getY(),
                portalPos1.getZ() - Math.abs(CalculatedAxisH.getZ()))).isAir()) {
            Portalgun.logString(Level.WARN, "portalInvalid");
            Portalgun.logString(Level.INFO, "Upper" + upperPos.toString());
            Portalgun.logString(Level.INFO, "Lower" + lowerPos.toString());
            return false;
        }
        return true;
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
        user.getItemCooldownManager().set(this, 4);
        hit = user.raycast(50.0D, 1.0F, false);
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

                if (validPos(world, dirUp1, dirRight1, calcPortalPos(blockPos, dirUp1, dirOut1, new Vec3i(-dirRight1.getX(), -dirRight1.getY(), -dirRight1.getZ())))) {

                    if (portalsTag.contains("PrimaryPortal" + user.getUuidAsString())) {
                        newPortal1 = (CustomPortal) ((ServerWorld) world).getEntity(portalsTag.getUuid("PrimaryPortal" + user.getUuidAsString()));
                        if (newPortal1 == null) {
                            newPortal1 = Portalgun.CUSTOM_PORTAL.create(world);
                            portal1Exists = false;
                        } else
                            portal1Exists = true;
                    }
                    if (portalsTag.contains("SecondaryPortal" + user.getUuidAsString())) {
                        newPortal2 = (CustomPortal) ((ServerWorld) world).getEntity(portalsTag.getUuid("SecondaryPortal" + user.getUuidAsString()));
                        if (newPortal2 == null) {
                            newPortal2 = Portalgun.CUSTOM_PORTAL.create(world);
                            portal2Exists = false;
                        } else
                            portal2Exists = true;
                    }

                    if (portalsTag.contains("PrimaryOutline" + user.getUuidAsString())) {
                        portalOutline1 = (PortalOverlay) ((ServerWorld) world).getEntity(portalsTag.getUuid("PrimaryOutline" + user.getUuidAsString()));

                        if (portalOutline1 == null) {
                            portalOutline1 = Portalgun.PORTAL_OVERLAY.create(world);
                            outline1Exists = false;
                        } else
                            outline1Exists = true;
                    }
                    if (portalsTag.contains("SecondaryOutline" + user.getUuidAsString())) {
                        portalOutline2 = (PortalOverlay) ((ServerWorld) world).getEntity(portalsTag.getUuid("SecondaryOutline" + user.getUuidAsString()));

                        if (portalOutline2 == null) {
                            portalOutline2 = Portalgun.PORTAL_OVERLAY.create(world);
                            outline2Exists = false;
                        } else
                            outline2Exists = true;
                    }

                    waitPortal = true;
                    ModMain.serverTaskList.addTask(MyTaskList.withDelay(delay, MyTaskList.oneShotTask(() -> {
                        Vec3d outlinePos = calcOutlinePos(blockPos, dirUp1, dirOut1, dirRight1);

                        PortalMethods.portal1Methods(user, hit, world);

                        portalOutline1.updatePosition(outlinePos.x, outlinePos.y, outlinePos.z);

                        if (portal2Exists)
                            PortalManipulation.adjustRotationToConnect(newPortal1, newPortal2);
                        else
                            PortalManipulation.adjustRotationToConnect(newPortal1, newPortal1);

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
                                if (portalOutline1 != null)
                                    McHelper.spawnServerEntity(portalOutline1);
                            }
                            if (portal1Exists)
                                newPortal1.reloadAndSyncToClient();
                            if (portal2Exists)
                                newPortal2.reloadAndSyncToClient();
                        }
                        waitPortal = false;
                        if (newPortal2 != null)
                            portalsTag.putUuid("SecondaryPortal" + user.getUuidAsString(), newPortal2.getUuid());
                        if (newPortal1 != null)
                            portalsTag.putUuid("PrimaryPortal" + user.getUuidAsString(), newPortal1.getUuid());

                        if (portalOutline2 != null)
                            portalsTag.putUuid("SecondaryOutline" + user.getUuidAsString(), portalOutline2.getUuid());
                        if (portalOutline1 != null)
                            portalsTag.putUuid("PrimaryOutline" + user.getUuidAsString(), portalOutline1.getUuid());

                        tag.put(world.getRegistryKey().toString(), portalsTag);
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
        user.getItemCooldownManager().set(this, 4);
        hit = user.raycast(50.0D, 1.0F, false);
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

                if (validPos(world, dirUp2, dirRight2, calcPortalPos(blockPos, dirUp2, dirOut2, new Vec3i(-dirRight2.getX(), -dirRight2.getY(), -dirRight2.getZ())))) {

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

                    if (portalsTag.contains("PrimaryOutline" + user.getUuidAsString())) {
                        portalOutline1 = (PortalOverlay) ((ServerWorld) world).getEntity(portalsTag.getUuid("PrimaryOutline" + user.getUuidAsString()));

                        if (portalOutline1 == null) {
                            portalOutline1 = Portalgun.PORTAL_OVERLAY.create(world);
                            outline1Exists = false;
                        } else
                            outline1Exists = true;
                    }
                    if (portalsTag.contains("SecondaryOutline" + user.getUuidAsString())) {
                        portalOutline2 = (PortalOverlay) ((ServerWorld) world).getEntity(portalsTag.getUuid("SecondaryOutline" + user.getUuidAsString()));

                        if (portalOutline2 == null) {
                            portalOutline2 = Portalgun.PORTAL_OVERLAY.create(world);
                            outline2Exists = false;
                        } else
                            outline2Exists = true;
                    }


                    waitPortal = true;
                    ModMain.serverTaskList.addTask(MyTaskList.withDelay(delay, MyTaskList.oneShotTask(() -> {

                        Vec3d outlinePos = calcOutlinePos(blockPos, dirUp2, dirOut2, dirRight2);

                        PortalMethods.portal2Methods(user, hit, world);

                        portalOutline2.updatePosition(outlinePos.x, outlinePos.y, outlinePos.z);

                        if (portal1Exists)
                            PortalManipulation.adjustRotationToConnect(newPortal2, newPortal1);
                        else
                            PortalManipulation.adjustRotationToConnect(newPortal2, newPortal2);

                        if (McHelper.getServer().getThread() == Thread.currentThread()) {
                            world.playSound(null,
                                    newPortal2.getX(),
                                    newPortal2.getY(),
                                    newPortal2.getZ(),
                                    Portalgun.PORTAL_OPEN_EVENT,
                                    SoundCategory.NEUTRAL,
                                    1.0F,
                                    1F);
                            if (!portal2Exists) {
                                removeOldPortals(user);
                                McHelper.spawnServerEntity(newPortal2);
                                if (portalOutline2 != null)
                                    McHelper.spawnServerEntity(portalOutline2);
                            }
                            if (portal1Exists)
                                newPortal1.reloadAndSyncToClient();
                            if (portal2Exists)
                                newPortal2.reloadAndSyncToClient();
                        }
                        waitPortal = false;
                        if (newPortal2 != null)
                            portalsTag.putUuid("SecondaryPortal" + user.getUuidAsString(), newPortal2.getUuid());
                        if (newPortal1 != null)
                            portalsTag.putUuid("PrimaryPortal" + user.getUuidAsString(), newPortal1.getUuid());

                        if (portalOutline2 != null)
                            portalsTag.putUuid("SecondaryOutline" + user.getUuidAsString(), portalOutline2.getUuid());
                        if (portalOutline1 != null)
                            portalsTag.putUuid("PrimaryOutline" + user.getUuidAsString(), portalOutline1.getUuid());

                        tag.put(world.getRegistryKey().toString(), portalsTag);
                    })));
                }
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }

        return TypedActionResult.pass(itemStack);
    }
}
