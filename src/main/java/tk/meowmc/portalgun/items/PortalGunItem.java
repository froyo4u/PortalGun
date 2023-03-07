package tk.meowmc.portalgun.items;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.chunk_loading.ChunkLoader;
import qouteall.imm_ptl.core.chunk_loading.DimensionalChunkPos;
import qouteall.imm_ptl.core.portal.PortalManipulation;
import qouteall.q_misc_util.Helper;
import qouteall.q_misc_util.my_util.AARotation;
import qouteall.q_misc_util.my_util.IntBox;
import qouteall.q_misc_util.my_util.MyTaskList;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import tk.meowmc.portalgun.PortalGunRecord;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.config.PortalGunConfig;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PortalGunItem extends Item implements IAnimatable {
    private static Logger LOGGER = LogManager.getLogger();
    
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
//        AnimationController animController =
//            GeckoLibUtil.getControllerForStack(this.animationFactory, itemStack, controllerName);
        if (world.isClientSide()) {
//            animController.markNeedsReload();
//            animController.setAnimation(new AnimationBuilder().addAnimation("portal_shoot", false));
            
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
        BlockState blockState = world.getBlockState(blockPos);
        
        Direction blockFacingDir = blockHit.getDirection();
        
        Direction dirUp = getUpDirection(user, blockFacingDir);
        
        Direction rightDir = AARotation.dirCrossProduct(dirUp, blockFacingDir);
        
        BlockPos regionSize = new BlockPos(
            rightDir.getNormal()
                .offset(dirUp.getNormal().multiply(2))
                .offset(blockFacingDir.getNormal())
        );
        
        IntBox areaForPlacing = IntBox.getBoxByPosAndSignedSize(BlockPos.ZERO, regionSize).stream().map(
            offset -> {
                BlockPos testingBasePos = blockPos.subtract(offset);
                IntBox testingArea = IntBox.getBoxByPosAndSignedSize(testingBasePos, regionSize);
                if (testingArea.stream().allMatch(p -> world.getBlockState(p).isAir())) {
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
        
        PortalGunRecord record = PortalGunRecord.get();
        
        Map<PortalGunRecord.PortalGunKind, PortalGunRecord.PortalGunInfo> infoMap =
            record.data.computeIfAbsent(user.getUUID(), k -> new HashMap<>());
        
        PortalGunRecord.PortalGunKind kind = PortalGunRecord.PortalGunKind._2x1;
        
        PortalGunRecord.PortalGunInfo portalGunInfo = infoMap.computeIfAbsent(
            kind, k -> PortalGunRecord.PortalGunInfo.empty()
        );
        
        if (portalGunInfo.portal1() == null && portalGunInfo.portal2() == null) {
            // should create a new unpaired portal
            CustomPortal newPortal = PortalManipulation.createOrthodoxPortal(
                CustomPortal.entityType,
                (ServerLevel) world,
                ((ServerLevel) world), // the dest dim doesn't matter as it's invisible and not teleportable
                blockFacingDir,
                Helper.getBoxSurface(areaForPlacing.toRealNumberBox(), blockFacingDir.getOpposite()),
                Vec3.atCenterOf(blockPos).add(0, 10, 0)
                // the dest pos doesn't matter as it's invisible and not teleportable
            );
            newPortal.setIsVisible(false);
            newPortal.teleportable = false;
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
            CustomPortal newPortal = PortalManipulation.createOrthodoxPortal(
                CustomPortal.entityType,
                (ServerLevel) world,
                firstPortalWorld,
                blockFacingDir,
                Helper.getBoxSurface(areaForPlacing.toRealNumberBox(), blockFacingDir.getOpposite()),
                portalGunInfo.portal1().portalPos()
            );
            newPortal.setOtherSideOrientation(portalGunInfo.portal1().portalOrientation());
            
            PortalGunRecord.PortalGunInfo newPortalGunInfo = portalGunInfo.withPortal2(
                new PortalGunRecord.SidedPortalInfo(
                    newPortal.getUUID(), world.dimension(), newPortal.getOriginPos(),
                    newPortal.getOrientationRotation()
                )
            );
            infoMap.put(kind, newPortalGunInfo);
            record.setDirty();
            Validate.isTrue(newPortalGunInfo.portal1() != null);
            Validate.isTrue(newPortalGunInfo.portal2() != null);
            
            // make the first portal visible and teleportable
            // to do that we firstly need to load the chunk
            ChunkLoader chunkLoader = new ChunkLoader(new DimensionalChunkPos(
                newPortalGunInfo.portal1().portalDim(),
                new ChunkPos(new BlockPos(portalGunInfo.portal1().portalPos()))
            ), 1);
            chunkLoader.loadChunksAndDo(() -> {
                Entity entity = firstPortalWorld
                    .getEntity(newPortalGunInfo.portal1().portalId());
                if (entity instanceof CustomPortal originalPortal) {
                    originalPortal.setIsVisible(true);
                    originalPortal.teleportable = true;
                    originalPortal.setDestinationDimension(newPortalGunInfo.portal2().portalDim());
                    originalPortal.setDestination(newPortalGunInfo.portal2().portalPos());
                    originalPortal.setOtherSideOrientation(newPortalGunInfo.portal2().portalOrientation());
                    
                    originalPortal.reloadAndSyncToClient();
                }
                else {
                    LOGGER.error("Cannot find the original portal to link {}", newPortalGunInfo.portal1());
                }
            });
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
