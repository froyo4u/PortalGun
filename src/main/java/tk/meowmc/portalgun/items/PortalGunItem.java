package tk.meowmc.portalgun.items;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.portal.PortalManipulation;
import qouteall.q_misc_util.Helper;
import qouteall.q_misc_util.my_util.AARotation;
import qouteall.q_misc_util.my_util.IntBox;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import tk.meowmc.portalgun.PortalGunRecord;
import tk.meowmc.portalgun.PortalGunMod;
import tk.meowmc.portalgun.client.renderer.PortalGunItemRenderer;
import tk.meowmc.portalgun.entities.CustomPortal;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.world.phys.HitResult.Type.ENTITY;
import static net.minecraft.world.phys.HitResult.Type.MISS;

public class PortalGunItem extends Item implements GeoItem {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int COOLDOWN_TICKS = 4;
    
    public final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    
    private static final RawAnimation SHOOT_ANIM = RawAnimation.begin().thenPlay("portal_shoot");
    
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
    
    public PortalGunItem(Properties settings) {
        super(settings);
        
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }
    
    
    // Utilise our own render hook to define our custom renderer
    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private final PortalGunItemRenderer renderer = new PortalGunItemRenderer();
            
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return this.renderer;
            }
        });
    }
    
    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }
    
    // Register our animation controllers
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
            new AnimationController<>(this, "portalGunController", 1, state -> PlayState.CONTINUE)
                .triggerableAnim("shoot_anim", SHOOT_ANIM)
        );
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
    
    @Override
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner) {
        return false;
    }
    
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (world.isClientSide()) {
            return InteractionResultHolder.fail(itemStack);
        }
        
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        
        HitResult hit = player.pick(100.0D, 1.0F, false);
        
        if (!(hit instanceof BlockHitResult blockHit)) {
            return InteractionResultHolder.fail(itemStack);
        }
        
        boolean success = interact(
            (ServerPlayer) player, (ServerLevel) world, hand, blockHit,
            PortalGunRecord.PortalGunSide.orange
        );
        
        player.awardStat(Stats.ITEM_USED.get(this));
        
        return success ? InteractionResultHolder.success(itemStack) : InteractionResultHolder.pass(itemStack);
    }
    
    public InteractionResult onAttack(
        Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction
    ) {
        if (world.isClientSide()) {
            return InteractionResult.PASS;
        }
        
        ItemStack itemStack = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        
        HitResult hit = player.pick(100.0D, 1.0F, false);
        
        if (!(hit instanceof BlockHitResult blockHit)) {
            return InteractionResult.FAIL;
        }
        
        boolean success = interact(
            (ServerPlayer) player, (ServerLevel) world, hand, blockHit,
            PortalGunRecord.PortalGunSide.blue
        );
        
        player.awardStat(Stats.ITEM_USED.get(this));
        
        return success ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
    
    // return whether successful
    public boolean interact(
        ServerPlayer player,
        ServerLevel world,
        InteractionHand hand,
        BlockHitResult blockHit,
        PortalGunRecord.PortalGunSide side
    ) {
        BlockPos blockPos = blockHit.getBlockPos().relative(blockHit.getDirection());
        
        Direction wallFacing = blockHit.getDirection();
        
        Direction upDir = getUpDirection(player, wallFacing);
        
        Direction rightDir = AARotation.dirCrossProduct(upDir, wallFacing);
        
        BlockPos regionSize = new BlockPos(
            rightDir.getNormal()
                .offset(upDir.getNormal().multiply(2))
                .offset(wallFacing.getNormal())
        );
        
        IntBox areaForPlacing = IntBox.getBoxByPosAndSignedSize(BlockPos.ZERO, regionSize).stream().map(
            offset -> {
                BlockPos testingBasePos = blockPos.subtract(offset);
                IntBox testingArea = IntBox.getBoxByPosAndSignedSize(testingBasePos, regionSize);
                boolean boxIsAllAir = testingArea.stream().allMatch(p -> world.getBlockState(p).isAir());
                boolean wallIsSolid = testingArea.stream().map(p -> p.relative(wallFacing.getOpposite()))
                    .allMatch(p -> isBlockSolid(world, p));
                if (boxIsAllAir && wallIsSolid) {
                    return testingArea;
                }
                else {
                    return null;
                }
            }
        ).filter(Objects::nonNull).findFirst().orElse(null);
        
        if (areaForPlacing == null) {
            return false;
        }
        
        triggerAnim(
            player,
            GeoItem.getOrAssignId(player.getItemInHand(hand), ((ServerLevel) world)),
            "portalGunController", "shoot_anim"
        );
        
        PortalGunRecord record = PortalGunRecord.get();
        
        PortalGunRecord.PortalGunKind kind = PortalGunRecord.PortalGunKind._2x1;
        
        PortalGunRecord.PortalDescriptor descriptor =
            new PortalGunRecord.PortalDescriptor(player.getUUID(), kind, side);
        
        PortalGunRecord.PortalDescriptor otherSideDescriptor = descriptor.getTheOtherSide();
        
        PortalGunRecord.PortalInfo thisSideInfo = record.data.get(descriptor);
        PortalGunRecord.PortalInfo otherSideInfo = record.data.get(otherSideDescriptor);
        
        Vec3 wallFacingVec = Vec3.atLowerCornerOf(wallFacing.getNormal());
        Vec3 newPortalOrigin = Helper
            .getBoxSurface(areaForPlacing.toRealNumberBox(), wallFacing.getOpposite())
            .getCenter()
            .add(wallFacingVec.scale(PortalGunMod.portalOffset));
        
        CustomPortal newPortal = CustomPortal.entityType.create(world);
        Validate.notNull(newPortal);
        
        newPortal.setOriginPos(newPortalOrigin);
        newPortal.setOrientationAndSize(
            Vec3.atLowerCornerOf(rightDir.getNormal()),
            Vec3.atLowerCornerOf(upDir.getNormal()),
            kind.getWidth(),
            kind.getHeight()
        );
        newPortal.descriptor = descriptor;
        newPortal.wallBox = areaForPlacing.getMoved(wallFacing.getOpposite().getNormal());
        newPortal.thisSideUpdateCounter = thisSideInfo == null ? 0 : thisSideInfo.updateCounter();
        newPortal.otherSideUpdateCounter = otherSideInfo == null ? 0 : otherSideInfo.updateCounter();
        PortalManipulation.makePortalRound(newPortal, 100);
        
        if (otherSideInfo == null) {
            // spawn an unpaired portal. it's invisible and not teleportable
            newPortal.setDestinationDimension(world.dimension());
            newPortal.setDestination(newPortalOrigin.add(0, 10, 0));
            newPortal.setIsVisible(false);
            newPortal.teleportable = false;
        }
        else {
            // spawn an linked portal
            newPortal.setDestinationDimension(otherSideInfo.portalDim());
            newPortal.setDestination(otherSideInfo.portalPos());
            newPortal.setOtherSideOrientation(otherSideInfo.portalOrientation());
        }
        McHelper.spawnServerEntity(newPortal);
        
        newPortal.thisSideUpdateCounter += 1;
        thisSideInfo = new PortalGunRecord.PortalInfo(
            newPortal.getUUID(),
            world.dimension(),
            newPortalOrigin,
            newPortal.getOrientationRotation(),
            newPortal.thisSideUpdateCounter
        );
        record.data.put(descriptor, thisSideInfo);
        record.setDirty();
        
        return true;
    }
    
    public static boolean isBlockSolid(Level world, BlockPos p) {
        return world.getBlockState(p).isSolidRender(world, p);
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
