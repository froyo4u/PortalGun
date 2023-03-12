package tk.meowmc.portalgun.items;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.chunk_loading.ChunkLoader;
import qouteall.imm_ptl.core.chunk_loading.DimensionalChunkPos;
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
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.client.renderer.PortalGunRenderer;
import tk.meowmc.portalgun.entities.CustomPortal;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.world.phys.HitResult.Type.ENTITY;
import static net.minecraft.world.phys.HitResult.Type.MISS;

public class PortalGunItem extends Item implements GeoItem {
    private static final Logger LOGGER = LogManager.getLogger();
    
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
            private final PortalGunRenderer renderer = new PortalGunRenderer();
            
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
    
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (world.isClientSide()) {
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
        
        BlockPos blockPos = blockHit.getBlockPos().relative(((BlockHitResult) hit).getDirection());
        
        Direction wallFacing = blockHit.getDirection();
        
        Direction upDir = getUpDirection(user, wallFacing);
        
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
                    .allMatch(p -> world.getBlockState(p).isSolidRender(world, p));
                if (boxIsAllAir && wallIsSolid) {
                    return testingArea;
                }
                else {
                    return null;
                }
            }
        ).filter(Objects::nonNull).findFirst().orElse(null);
        
        if (areaForPlacing == null) {
            return InteractionResultHolder.fail(itemStack);
        }
        
        triggerAnim(
            user,
            GeoItem.getOrAssignId(user.getItemInHand(hand), ((ServerLevel) world)),
            "portalGunController", "shoot_anim"
        );
        
        PortalGunRecord record = PortalGunRecord.get();
        
        Map<PortalGunRecord.PortalGunKind, PortalGunRecord.PortalGunInfo> infoMap =
            record.data.computeIfAbsent(user.getUUID(), k -> new HashMap<>());
        
        PortalGunRecord.PortalGunKind kind = PortalGunRecord.PortalGunKind._2x1;
        
        PortalGunRecord.PortalGunInfo portalGunInfo = infoMap.computeIfAbsent(
            kind, k -> PortalGunRecord.PortalGunInfo.empty()
        );
        
        Vec3 wallFacingVec = Vec3.atLowerCornerOf(wallFacing.getNormal());
        Vec3 newPortalOrigin = Helper
            .getBoxSurface(areaForPlacing.toRealNumberBox(), wallFacing.getOpposite())
            .getCenter()
            .add(wallFacingVec.scale(Portalgun.portalOffset));
        
        if (portalGunInfo.portal1() == null && portalGunInfo.portal2() == null) {
            // should create a new unpaired portal
            CustomPortal newPortal = CustomPortal.entityType.create(world);
            Validate.notNull(newPortal);
            
            newPortal.setOriginPos(newPortalOrigin);
            newPortal.setOrientationAndSize(
                Vec3.atLowerCornerOf(rightDir.getNormal()),
                Vec3.atLowerCornerOf(upDir.getNormal()),
                0.9,
                1.8
            );
            newPortal.setDestination(newPortalOrigin.add(0, 10, 0));
            newPortal.setDestinationDimension(world.dimension());
            newPortal.setIsVisible(false);
            newPortal.teleportable = false;
            newPortal.ownerId = user.getUUID();
            newPortal.kind = kind;
            
            McHelper.spawnServerEntity(newPortal);
            
            infoMap.put(kind,
                portalGunInfo.withPortal1(
                    new PortalGunRecord.SidedPortalInfo(
                        newPortal.getUUID(), world.dimension(), newPortal.getOriginPos(),
                        newPortal.getOrientationRotation()
                    )
                )
            );
            record.setDirty();
        }
        else if (portalGunInfo.portal1() != null && portalGunInfo.portal2() == null) {
            // should finish pairing
            
            ServerLevel firstPortalWorld = McHelper.getServerWorld(portalGunInfo.portal1().portalDim());
            
            // create and spawn the new portal
            CustomPortal newPortal = CustomPortal.entityType.create(world);
            Validate.notNull(newPortal);
            newPortal.setOriginPos(newPortalOrigin);
            newPortal.setOrientationAndSize(
                Vec3.atLowerCornerOf(rightDir.getNormal()),
                Vec3.atLowerCornerOf(upDir.getNormal()),
                0.9,
                1.8
            );
            newPortal.setDestinationDimension(portalGunInfo.portal1().portalDim());
            newPortal.setDestination(portalGunInfo.portal1().portalPos());
            newPortal.setOtherSideOrientation(portalGunInfo.portal1().portalOrientation());
            newPortal.ownerId = user.getUUID();
            newPortal.kind = kind;
    
            McHelper.spawnServerEntity(newPortal);
    
            PortalGunRecord.PortalGunInfo newPortalGunInfo = portalGunInfo
                .withPortal2(
                    new PortalGunRecord.SidedPortalInfo(
                        newPortal.getUUID(), world.dimension(), newPortal.getOriginPos(),
                        newPortal.getOrientationRotation()
                    )
                )
                .withUpdateCounterIncremented();
            infoMap.put(kind, newPortalGunInfo);
            record.setDirty();
        }
        else {
            Validate.isTrue(portalGunInfo.portal1() != null);
            // already paired, should break pairing
            infoMap.remove(kind);
            record.setDirty();
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
