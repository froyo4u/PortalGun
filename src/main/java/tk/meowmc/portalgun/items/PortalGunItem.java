package tk.meowmc.portalgun.items;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.compat.GravityChangerInterface;
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

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        
        return success ? InteractionResultHolder.fail(itemStack) : InteractionResultHolder.pass(itemStack);
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
        
        return InteractionResult.SUCCESS;
    }
    
    // return whether successful
    public boolean interact(
        ServerPlayer player,
        ServerLevel world,
        InteractionHand hand,
        BlockHitResult blockHit,
        PortalGunRecord.PortalGunSide side
    ) {
        world.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            side == PortalGunRecord.PortalGunSide.blue ?
                PortalGunMod.PORTAL1_SHOOT_EVENT : PortalGunMod.PORTAL2_SHOOT_EVENT,
            SoundSource.PLAYERS,
            1.0F, 1.0F
        );
        
        PortalGunRecord.PortalGunKind kind = PortalGunRecord.PortalGunKind._2x1;
        
        BlockPos blockPos = blockHit.getBlockPos().relative(blockHit.getDirection());
        
        Direction wallFacing = blockHit.getDirection();
        
        PortalPlacement placement = findPortalPlacement(player, world, kind, blockPos, wallFacing);
        
        if (placement == null) {
            return false;
        }
    
        Direction rightDir = placement.rotation.transformedX;
        Direction upDir = placement.rotation.transformedY;
        
        triggerAnim(
            player,
            GeoItem.getOrAssignId(player.getItemInHand(hand), ((ServerLevel) world)),
            "portalGunController", "shoot_anim"
        );
        
        PortalGunRecord record = PortalGunRecord.get();
        
        PortalGunRecord.PortalDescriptor descriptor =
            new PortalGunRecord.PortalDescriptor(player.getUUID(), kind, side);
        
        PortalGunRecord.PortalDescriptor otherSideDescriptor = descriptor.getTheOtherSide();
        
        PortalGunRecord.PortalInfo thisSideInfo = record.data.get(descriptor);
        PortalGunRecord.PortalInfo otherSideInfo = record.data.get(otherSideDescriptor);
        
        Vec3 wallFacingVec = Vec3.atLowerCornerOf(wallFacing.getNormal());
        Vec3 newPortalOrigin = Helper
            .getBoxSurface(placement.areaBox.toRealNumberBox(), wallFacing.getOpposite())
            .getCenter()
            .add(wallFacingVec.scale(PortalGunMod.portalOffset));
        
        CustomPortal portal = null;
        boolean isExistingPortal = false;
        
        if (thisSideInfo != null) {
            Entity entity = McHelper.getServerWorld(thisSideInfo.portalDim()).getEntity(thisSideInfo.portalId());
            if (entity instanceof CustomPortal customPortal) {
                portal = customPortal;
                isExistingPortal = true;
            }
        }
        
        if (portal == null) {
            portal = CustomPortal.entityType.create(world);
            Validate.notNull(portal);
        }
        
        portal.setOriginPos(newPortalOrigin);
        portal.setOrientationAndSize(
            Vec3.atLowerCornerOf(rightDir.getNormal()),
            Vec3.atLowerCornerOf(upDir.getNormal()),
            kind.getWidth() * 0.9,
            kind.getHeight() * 0.9
        );
        portal.descriptor = descriptor;
        portal.wallBox = placement.wallBox;
        portal.airBox = placement.areaBox;
        portal.thisSideUpdateCounter = thisSideInfo == null ? 0 : thisSideInfo.updateCounter();
        portal.otherSideUpdateCounter = otherSideInfo == null ? 0 : otherSideInfo.updateCounter();
        PortalManipulation.makePortalRound(portal, 20);
        portal.animation.defaultAnimation.durationTicks = 0; // disable default animation
        
        if (otherSideInfo == null) {
            // it's unpaired, invisible and not teleportable
            portal.setDestinationDimension(world.dimension());
            portal.setDestination(newPortalOrigin.add(0, 10, 0));
            portal.setIsVisible(false);
            portal.teleportable = false;
        }
        else {
            // it's linked portal
            portal.setDestinationDimension(otherSideInfo.portalDim());
            portal.setDestination(otherSideInfo.portalPos());
            portal.setOtherSideOrientation(otherSideInfo.portalOrientation());
            portal.setIsVisible(true);
            portal.teleportable = true;
            world.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                PortalGunMod.PORTAL_OPEN_EVENT,
                SoundSource.PLAYERS,
                1.0F, 1.0F
            );
        }
        
        portal.thisSideUpdateCounter += 1;
        thisSideInfo = new PortalGunRecord.PortalInfo(
            portal.getUUID(),
            world.dimension(),
            newPortalOrigin,
            portal.getOrientationRotation(),
            portal.thisSideUpdateCounter
        );
        record.data.put(descriptor, thisSideInfo);
        record.setDirty();
        
        if (!isExistingPortal) {
            McHelper.spawnServerEntity(portal);
        }
        else {
            portal.reloadAndSyncToClient();
        }
        
        return true;
    }
    
    private static record PortalPlacement(
        AARotation rotation,
        IntBox areaBox,
        IntBox wallBox
    ) {
    }
    
    @Nullable
    private static PortalPlacement findPortalPlacement(
        ServerPlayer player,
        ServerLevel world,
        PortalGunRecord.PortalGunKind kind,
        BlockPos interactingAirPos,
        Direction wallFacing
    ) {
        Direction playerGravity = GravityChangerInterface.invoker.getGravityDirection(player);
        
        Vec3 viewVector = player.getViewVector(1);
        Vec3 viewVectorLocal = GravityChangerInterface.invoker.transformWorldToPlayer(playerGravity, viewVector);
        
        Direction wallFacingLocal = GravityChangerInterface.invoker.transformDirWorldToPlayer(playerGravity, wallFacing);
        
        Direction[] upDirCandidates = Helper.getAnotherFourDirections(wallFacingLocal.getAxis());
        
        Arrays.sort(upDirCandidates, Comparator.comparingDouble((Direction dir) -> {
            if (dir == Direction.UP) {
                // the local up direction has the highest priority
                return 1;
            }
            // horizontal dot product
            return dir.getNormal().getX() * viewVectorLocal.x + dir.getNormal().getZ() * viewVectorLocal.z;
        }).reversed());
        
        BlockPos portalAreaSize = new BlockPos(kind.getWidth(), kind.getHeight(), 1);
        
        for (Direction upDir : upDirCandidates) {
            AARotation rot = AARotation.getAARotationFromYZ(upDir, wallFacing);
            BlockPos transformedSize = rot.transform(portalAreaSize);
            IntBox portalArea = IntBox.getBoxByPosAndSignedSize(interactingAirPos, transformedSize);
            IntBox wallArea = portalArea.getMoved(wallFacing.getOpposite().getNormal());
            
            if (CustomPortal.isAreaClear(world, portalArea) && CustomPortal.isWallValid(world, wallArea)) {
                return new PortalPlacement(rot, portalArea, wallArea);
            }
        }
        
        return null;
    }
}
