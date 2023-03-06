package tk.meowmc.portalgun.items;

import org.apache.logging.log4j.Level;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.portal.PortalManipulation;
import qouteall.q_misc_util.my_util.MyTaskList;
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

import static net.minecraft.world.phys.HitResult.Type.BLOCK;
import static net.minecraft.world.phys.HitResult.Type.MISS;
import static tk.meowmc.portalgun.misc.PortalMethods.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PortalGunItem extends Item implements IAnimatable {
    public static String controllerName = "portalgunController";
    public static boolean portal1Exists = false;
    public static boolean portal2Exists = false;
    public static boolean outline1Exists = false;
    public static boolean outline2Exists = false;
    public static boolean waitPortal = false;
    public static HitResult hit;
    public static BlockHitResult blockHit;
    public static BlockPos blockPos;
    public static BlockState blockState;
    static AnimationController animController;
    static Direction direction;
    public CustomPortal newPortal1;
    public CustomPortal newPortal2;
    public PortalOverlay portalOutline1;
    public PortalOverlay portalOutline2;
    public CompoundTag tag;
    public CompoundTag portalsTag;
    public AnimationFactory factory = new AnimationFactory(this);

    public PortalGunItem(Properties settings) {
        super(settings);
    }

    public static void removeOldPortals(Player user) {
        PortalGunItem gunItem = (PortalGunItem) Portalgun.PORTALGUN;
        if (gunItem.newPortal1 != null) {
            gunItem.newPortal1.kill();
            gunItem.portalsTag.remove("PrimaryPortal" + user.getStringUUID());
            gunItem.newPortal1.myUnsetRemoved();
        }
        if (gunItem.newPortal2 != null) {
            gunItem.newPortal2.kill();
            gunItem.portalsTag.remove("SecondaryPortal" + user.getStringUUID());
            gunItem.newPortal2.myUnsetRemoved();
        }
        gunItem.tag.remove(user.level.dimension().toString());
    }

    /**
     * Thanks to Fusion Flux for this btw
     */
    private boolean validPos(net.minecraft.world.level.Level world, Vec3i up, Vec3i right, Vec3 portalPos1) {
        Vec3 CalculatedAxisW;
        Vec3 CalculatedAxisH;
        Vec3 posNormal;
        CalculatedAxisW = Vec3.atLowerCornerOf(right);
        CalculatedAxisH = Vec3.atLowerCornerOf(up).scale(-1);

        BlockPos upperPos = new BlockPos(
                portalPos1.x() - CalculatedAxisW.cross(CalculatedAxisH).x(),
                portalPos1.y() - CalculatedAxisW.cross(CalculatedAxisH).y(),
                portalPos1.z() - CalculatedAxisW.cross(CalculatedAxisH).z());
        BlockPos lowerPos = new BlockPos(
                portalPos1.x() - CalculatedAxisW.cross(CalculatedAxisH).x() - Math.abs(CalculatedAxisH.x()),
                portalPos1.y() - CalculatedAxisW.cross(CalculatedAxisH).y() + CalculatedAxisH.y(),
                portalPos1.z() - CalculatedAxisW.cross(CalculatedAxisH).z() - Math.abs(CalculatedAxisH.z()));

        if (!world.getBlockState(upperPos).isFaceSturdy(world, upperPos, direction) ||
                (!world.getBlockState(lowerPos).isFaceSturdy(world, lowerPos, direction)
                ) || world.getBlockState(new BlockPos(portalPos1)).canOcclude() || world.getBlockState(new BlockPos(
                portalPos1.x() - Math.abs(CalculatedAxisH.x()),
                portalPos1.y() + CalculatedAxisH.y(),
                portalPos1.z() - Math.abs(CalculatedAxisH.z()))).canOcclude()) {
            Portalgun.logString(Level.WARN, "portalInvalid");
            Portalgun.logString(Level.INFO, "Upper" + upperPos);
            Portalgun.logString(Level.INFO, "Lower" + lowerPos);
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
    public boolean canAttackBlock(BlockState state, net.minecraft.world.level.Level world, BlockPos pos, Player miner) {
        return false;
    }

    public void portal1Spawn(net.minecraft.world.level.Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        tag = itemStack.getOrCreateTag();
        portalsTag = tag.getCompound(world.dimension().toString());
        user.getCooldowns().addCooldown(this, 4);
        hit = user.pick(100.0D, 1.0F, false);
        blockHit = (BlockHitResult) hit;
        blockPos = blockHit.getBlockPos();
        blockState = world.getBlockState(blockPos);
        animController = GeckoLibUtil.getControllerForStack(this.factory, itemStack, controllerName);


        if (hit.getType() == BLOCK) {
            direction = blockHit.getDirection();


            dirOut1 = blockHit.getDirection().getOpposite().getNormal();
            if (dirOut1.getY() == 0) {
                dirUp1 = new Vec3i(0, 1, 0);
            } else {
                dirUp1 = user.getDirection().getNormal();
            }
            dirRight1 = dirUp1.cross(dirOut1);


            double distanceX = blockPos.getX() - user.getX();
            double distanceY = blockPos.getY() - user.getEyeY();
            double distanceZ = blockPos.getZ() - user.getZ();

            Vec3 distanceVec = new Vec3(distanceX, distanceY, distanceZ);

            double distance = distanceVec.length();

            int delay = (int) (0.3 * distance);


            if (!world.isClientSide && !waitPortal) {
                world.playSound(null,
                        user.getX(),
                        user.getY(),
                        user.getZ(),
                        Portalgun.PORTAL1_SHOOT_EVENT,
                        SoundSource.NEUTRAL,
                        1.0F,
                        1F);

                if (validPos(world, dirUp1, dirRight1, calcPortalPos(blockPos, dirUp1, dirOut1, new Vec3i(-dirRight1.getX(), -dirRight1.getY(), -dirRight1.getZ())))) {

                    if (portalsTag.contains("PrimaryPortal" + user.getStringUUID())) {
                        newPortal1 = (CustomPortal) ((ServerLevel) world).getEntity(portalsTag.getUUID("PrimaryPortal" + user.getStringUUID()));
                        if (newPortal1 == null) {
                            newPortal1 = Portalgun.CUSTOM_PORTAL.create(world);
                            portal1Exists = false;
                        } else
                            portal1Exists = true;
                    }
                    if (portalsTag.contains("SecondaryPortal" + user.getStringUUID())) {
                        newPortal2 = (CustomPortal) ((ServerLevel) world).getEntity(portalsTag.getUUID("SecondaryPortal" + user.getStringUUID()));
                        if (newPortal2 == null) {
                            newPortal2 = Portalgun.CUSTOM_PORTAL.create(world);
                            portal2Exists = false;
                        } else
                            portal2Exists = true;
                    }

                    if (portalsTag.contains("PrimaryOutline" + user.getStringUUID())) {
                        portalOutline1 = (PortalOverlay) ((ServerLevel) world).getEntity(portalsTag.getUUID("PrimaryOutline" + user.getStringUUID()));

                        if (portalOutline1 == null) {
                            portalOutline1 = Portalgun.PORTAL_OVERLAY.create(world);
                            outline1Exists = false;
                        } else
                            outline1Exists = true;
                    }
                    if (portalsTag.contains("SecondaryOutline" + user.getStringUUID())) {
                        portalOutline2 = (PortalOverlay) ((ServerLevel) world).getEntity(portalsTag.getUUID("SecondaryOutline" + user.getStringUUID()));

                        if (portalOutline2 == null) {
                            portalOutline2 = Portalgun.PORTAL_OVERLAY.create(world);
                            outline2Exists = false;
                        } else
                            outline2Exists = true;
                    }

                    waitPortal = true;
                    IPGlobal.serverTaskList.addTask(MyTaskList.withDelay(delay, MyTaskList.oneShotTask(() -> {
                        Vec3 outlinePos = calcOutlinePos(blockPos, dirUp1, dirOut1, dirRight1);

                        PortalMethods.portal1Methods(user, hit, world);

                        portalOutline1.moveTo(outlinePos);

                        if (portal2Exists)
                            PortalManipulation.adjustRotationToConnect(newPortal1, newPortal2);
                        else
                            PortalManipulation.adjustRotationToConnect(newPortal1, newPortal1);

                        {
                            world.playSound(null,
                                    newPortal1.getX(),
                                    newPortal1.getY(),
                                    newPortal1.getZ(),
                                    Portalgun.PORTAL_OPEN_EVENT,
                                    SoundSource.NEUTRAL,
                                    1.0F,
                                    1F);
                            if (!portal1Exists) {
                                McHelper.spawnServerEntity(newPortal1);
                                if (portalOutline1 != null)
                                    McHelper.spawnServerEntity(portalOutline1);
                            }
                            if (portal1Exists)
                                newPortal1.reloadAndSyncToClient();
                            if (portal2Exists)
                                newPortal2.reloadAndSyncToClient();

                            portalOutline1.moveTo(outlinePos);

                            portal1Exists = true;
                        }
                        waitPortal = false;
                        if (newPortal2 != null)
                            portalsTag.putUUID("SecondaryPortal" + user.getStringUUID(), newPortal2.getUUID());
                        if (newPortal1 != null)
                            portalsTag.putUUID("PrimaryPortal" + user.getStringUUID(), newPortal1.getUUID());

                        if (portalOutline2 != null)
                            portalsTag.putUUID("SecondaryOutline" + user.getStringUUID(), portalOutline2.getUUID());
                        if (portalOutline1 != null)
                            portalsTag.putUUID("PrimaryOutline" + user.getStringUUID(), portalOutline1.getUUID());

                        tag.put(world.dimension().toString(), portalsTag);
                    })));
                }
            }
            user.awardStat(Stats.ITEM_USED.get(this));
        }
    }

    public InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        tag = itemStack.getOrCreateTag();
        portalsTag = tag.getCompound(world.dimension().toString());
        user.getCooldowns().addCooldown(this, 4);
        hit = user.pick(100.0D, 1.0F, false);
        blockHit = (BlockHitResult) hit;
        blockPos = blockHit.getBlockPos();
        blockState = world.getBlockState(blockPos);
        animController = GeckoLibUtil.getControllerForStack(this.factory, itemStack, controllerName);

        if (hit.getType() == MISS)
            return InteractionResultHolder.fail(itemStack);
        else if (hit.getType() == BLOCK) {
            direction = blockHit.getDirection();


            dirOut2 = blockHit.getDirection().getOpposite().getNormal();
            if (dirOut2.getY() == 0) {
                dirUp2 = new Vec3i(0, 1, 0);
            } else {
                dirUp2 = user.getDirection().getNormal();
            }
            dirRight2 = dirUp2.cross(dirOut2);


            double distanceX = blockPos.getX() - user.getX();
            double distanceY = blockPos.getY() - user.getEyeY();
            double distanceZ = blockPos.getZ() - user.getZ();

            Vec3 distanceVec = new Vec3(distanceX, distanceY, distanceZ);

            double distance = distanceVec.length();

            int delay = (int) (0.3 * distance);

            if (world.isClientSide) {
                animController.markNeedsReload();
                animController.setAnimation(new AnimationBuilder().addAnimation("portal_shoot", false));
            }

            if (!world.isClientSide && !waitPortal) {
                world.playSound(null,
                        user.getX(),
                        user.getY(),
                        user.getZ(),
                        Portalgun.PORTAL2_SHOOT_EVENT,
                        SoundSource.NEUTRAL,
                        1.0F,
                        1F);

                if (validPos(world, dirUp2, dirRight2, calcPortalPos(blockPos, dirUp2, dirOut2, new Vec3i(-dirRight2.getX(), -dirRight2.getY(), -dirRight2.getZ())))) {

                    if (portalsTag.contains("PrimaryPortal" + user.getStringUUID())) {
                        newPortal1 = (CustomPortal) ((ServerLevel) world).getEntity(portalsTag.getUUID("PrimaryPortal" + user.getStringUUID()));
                        if (newPortal1 == null) {
                            newPortal1 = Portalgun.CUSTOM_PORTAL.create(world);
                            portal1Exists = false;
                        } else
                            portal1Exists = true;
                    }
                    if (portalsTag.contains("SecondaryPortal" + user.getStringUUID())) {
                        newPortal2 = (CustomPortal) ((ServerLevel) world).getEntity(portalsTag.getUUID("SecondaryPortal" + user.getStringUUID()));
                        if (newPortal2 == null) {
                            newPortal2 = Portalgun.CUSTOM_PORTAL.create(world);
                            portal2Exists = false;
                        } else
                            portal2Exists = true;
                    }

                    if (portalsTag.contains("PrimaryOutline" + user.getStringUUID())) {
                        portalOutline1 = (PortalOverlay) ((ServerLevel) world).getEntity(portalsTag.getUUID("PrimaryOutline" + user.getStringUUID()));

                        if (portalOutline1 == null) {
                            portalOutline1 = Portalgun.PORTAL_OVERLAY.create(world);
                            outline1Exists = false;
                        } else
                            outline1Exists = true;
                    }
                    if (portalsTag.contains("SecondaryOutline" + user.getStringUUID())) {
                        portalOutline2 = (PortalOverlay) ((ServerLevel) world).getEntity(portalsTag.getUUID("SecondaryOutline" + user.getStringUUID()));

                        if (portalOutline2 == null) {
                            portalOutline2 = Portalgun.PORTAL_OVERLAY.create(world);
                            outline2Exists = false;
                        } else
                            outline2Exists = true;
                    }


                    waitPortal = true;
                    IPGlobal.serverTaskList.addTask(MyTaskList.withDelay(delay, MyTaskList.oneShotTask(() -> {

                        Vec3 outlinePos = calcOutlinePos(blockPos, dirUp2, dirOut2, dirRight2);

                        PortalMethods.portal2Methods(user, hit, world);

                        portalOutline2.moveTo(outlinePos);

                        if (portal1Exists)
                            PortalManipulation.adjustRotationToConnect(newPortal2, newPortal1);
                        else
                            PortalManipulation.adjustRotationToConnect(newPortal2, newPortal2);

                        {
                            world.playSound(null,
                                    newPortal2.getX(),
                                    newPortal2.getY(),
                                    newPortal2.getZ(),
                                    Portalgun.PORTAL_OPEN_EVENT,
                                    SoundSource.NEUTRAL,
                                    1.0F,
                                    1F);
                            if (!portal2Exists) {
                                world.addFreshEntity(newPortal2);
                                if (portalOutline2 != null)
                                    world.addFreshEntity(portalOutline2);
                            }
                            if (portal2Exists)
                                newPortal2.reloadAndSyncToClient();
                            if (portal1Exists)
                                newPortal1.reloadAndSyncToClient();

                            portalOutline2.moveTo(outlinePos);

                            portal2Exists = true;
                        }
                        waitPortal = false;
                        if (newPortal2 != null)
                            portalsTag.putUUID("SecondaryPortal" + user.getStringUUID(), newPortal2.getUUID());
                        if (newPortal1 != null)
                            portalsTag.putUUID("PrimaryPortal" + user.getStringUUID(), newPortal1.getUUID());

                        if (portalOutline2 != null)
                            portalsTag.putUUID("SecondaryOutline" + user.getStringUUID(), portalOutline2.getUUID());
                        if (portalOutline1 != null)
                            portalsTag.putUUID("PrimaryOutline" + user.getStringUUID(), portalOutline1.getUUID());

                        tag.put(world.dimension().toString(), portalsTag);
                    })));
                }
            }
            user.awardStat(Stats.ITEM_USED.get(this));
        }

        return InteractionResultHolder.pass(itemStack);
    }
}
