package tk.meowmc.portalgun.items;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.portal.PortalManipulation;
import qouteall.q_misc_util.my_util.AARotation;
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
import static net.minecraft.world.phys.HitResult.Type.ENTITY;
import static net.minecraft.world.phys.HitResult.Type.MISS;
import static tk.meowmc.portalgun.misc.PortalMethods.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
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

import java.util.Arrays;
import java.util.Comparator;

public class PortalGunItem extends Item implements IAnimatable {
    public static String controllerName = "portalgunController";
    
    public final AnimationFactory animationFactory = new AnimationFactory(this);
    
    public PortalGunItem(Properties settings) {
        super(settings);
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
        return this.animationFactory;
    }
    
    @Override
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner) {
        return false;
    }
    
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        AnimationController animController =
            GeckoLibUtil.getControllerForStack(this.animationFactory, itemStack, controllerName);
        if (world.isClientSide()) {
            animController.markNeedsReload();
            animController.setAnimation(new AnimationBuilder().addAnimation("portal_shoot", false));
            
            return InteractionResultHolder.fail(itemStack);
        }
        
        CompoundTag tag = itemStack.getOrCreateTag();
        CompoundTag portalsTag = tag.getCompound(world.dimension().toString());
        user.getCooldowns().addCooldown(this, 4);
        HitResult hit = user.pick(100.0D, 1.0F, false);
        BlockHitResult blockHit = (BlockHitResult) hit;
        
        if (hit.getType() == MISS || hit.getType() == ENTITY) {
            return InteractionResultHolder.fail(itemStack);
        }
    
        BlockPos blockPos = blockHit.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        
        Direction blockFacingDir = blockHit.getDirection();
        
        Direction dirUp = getUpDirection(user, blockFacingDir);
        
        Direction rightDir = AARotation.dirCrossProduct(dirUp, blockFacingDir);
        
        
        
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
                    }
                    else
                        portal1Exists = true;
                }
                if (portalsTag.contains("SecondaryPortal" + user.getStringUUID())) {
                    newPortal2 = (CustomPortal) ((ServerLevel) world).getEntity(portalsTag.getUUID("SecondaryPortal" + user.getStringUUID()));
                    if (newPortal2 == null) {
                        newPortal2 = Portalgun.CUSTOM_PORTAL.create(world);
                        portal2Exists = false;
                    }
                    else
                        portal2Exists = true;
                }
                
                if (portalsTag.contains("PrimaryOutline" + user.getStringUUID())) {
                    portalOutline1 = (PortalOverlay) ((ServerLevel) world).getEntity(portalsTag.getUUID("PrimaryOutline" + user.getStringUUID()));
                    
                    if (portalOutline1 == null) {
                        portalOutline1 = Portalgun.PORTAL_OVERLAY.create(world);
                        outline1Exists = false;
                    }
                    else
                        outline1Exists = true;
                }
                if (portalsTag.contains("SecondaryOutline" + user.getStringUUID())) {
                    portalOutline2 = (PortalOverlay) ((ServerLevel) world).getEntity(portalsTag.getUUID("SecondaryOutline" + user.getStringUUID()));
                    
                    if (portalOutline2 == null) {
                        portalOutline2 = Portalgun.PORTAL_OVERLAY.create(world);
                        outline2Exists = false;
                    }
                    else
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
        
        
        return InteractionResultHolder.pass(itemStack);
    }
    
    private static Direction getUpDirection(Player user, Direction blockFacingDir) {
        return switch (blockFacingDir) {
            case DOWN, UP -> getHorizontalDirection(user);
            case NORTH, WEST, SOUTH, EAST -> Direction.UP;
        };
    }
    
    private static Direction getHorizontalDirection(Player user) {
        Vec3 viewVector = user.getViewVector(1);
        double x = viewVector.x;
        double z = viewVector.z;
        
        Direction[] horizontalDirections = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        
        return Arrays.stream(horizontalDirections)
            .min(Comparator.comparingDouble(
                dir -> Math.abs(dir.getStepX() * x + dir.getStepZ() * z)
            ))
            .orElse(Direction.NORTH);
    }
    
}
