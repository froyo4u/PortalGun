package tk.meowmc.portalgun;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalManipulation;
import qouteall.q_misc_util.my_util.IntBox;
import tk.meowmc.portalgun.config.PortalGunConfig;
import tk.meowmc.portalgun.entities.CustomPortal;
import tk.meowmc.portalgun.items.ClawItem;
import tk.meowmc.portalgun.items.PortalGunItem;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PortalGunMod implements ModInitializer {
    public static final String MODID = "portalgun";
    public static final String KEY = MODID + ":portalgun_portals";
    public static final String MOD_NAME = "PortalGun Mod";
    
    public static final double portalOffset = 0.001;
    public static final double portalOverlayOffset = 0.001;
    
    
    public static final PortalGunItem PORTAL_GUN = new PortalGunItem(new FabricItemSettings().fireResistant().stacksTo(1).rarity(Rarity.EPIC));
    public static final Item PORTAL_GUN_BODY = new Item(new FabricItemSettings().fireResistant().stacksTo(1).rarity(Rarity.RARE));
    public static final ClawItem PORTAL_GUN_CLAW = new ClawItem(new FabricItemSettings().fireResistant().stacksTo(1).rarity(Rarity.RARE));
    
    public static final ResourceLocation PORTAL1_SHOOT = new ResourceLocation("portalgun:portal1_shoot");
    public static final ResourceLocation PORTAL2_SHOOT = new ResourceLocation("portalgun:portal2_shoot");
    public static final ResourceLocation PORTAL_OPEN = new ResourceLocation("portalgun:portal_open");
    public static final ResourceLocation PORTAL_CLOSE = new ResourceLocation("portalgun:portal_close");
    
    public static final SoundEvent PORTAL1_SHOOT_EVENT = SoundEvent.createVariableRangeEvent(PORTAL1_SHOOT);
    public static final SoundEvent PORTAL2_SHOOT_EVENT = SoundEvent.createVariableRangeEvent(PORTAL2_SHOOT);
    public static final SoundEvent PORTAL_OPEN_EVENT = SoundEvent.createVariableRangeEvent(PORTAL_OPEN);
    public static final SoundEvent PORTAL_CLOSE_EVENT = SoundEvent.createVariableRangeEvent(PORTAL_CLOSE);
    
    public static final Logger LOGGER = LogManager.getLogger();
    
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
    
    public static boolean isBlockSolid(Level world, BlockPos p) {
//        return true;
//        return !world.getBlockState(p).isAir();
        return world.getBlockState(p).isSolidRender(world, p);
    }
    
    public static boolean isAreaClear(Level world, IntBox airBox1) {
        return airBox1.fastStream().allMatch(p -> world.getBlockState(p).isAir());
    }
    
    public static boolean isWallValid(Level world, IntBox wallBox1) {
        return wallBox1.fastStream().allMatch(p -> isBlockSolid(world, p));
    }
    
    public static record PortalAwareRaytraceResult(
        Level world,
        BlockHitResult hitResult,
        List<Portal> portalsPassingThrough
    ) {}
    
    // TODO move this into ImmPtl
    @Nullable
    public static PortalAwareRaytraceResult portalAwareRayTrace(
        Entity entity, double maxDistance
    ) {
        return portalAwareRayTrace(
            entity.level,
            entity.getEyePosition(),
            entity.getViewVector(1),
            maxDistance,
            entity
        );
    }
    
    @Nullable
    public static PortalAwareRaytraceResult portalAwareRayTrace(
        Level world,
        Vec3 startingPoint,
        Vec3 direction,
        double maxDistance,
        Entity entity
    ) {
        return portalAwareRayTrace(world, startingPoint, direction, maxDistance, entity, List.of());
    }
    
    @Nullable
    public static PortalAwareRaytraceResult portalAwareRayTrace(
        Level world,
        Vec3 startingPoint,
        Vec3 direction,
        double maxDistance,
        Entity entity,
        @NotNull List<Portal> portalsPassingThrough
    ) {
        if (portalsPassingThrough.size() > 5) {
            return null;
        }
        
        Vec3 endingPoint = startingPoint.add(direction.scale(maxDistance));
        Optional<Pair<Portal, Vec3>> portalHit = PortalManipulation.raytracePortals(
            world, startingPoint, endingPoint, true
        );
        
        ClipContext context = new ClipContext(
            startingPoint,
            endingPoint,
            ClipContext.Block.OUTLINE,
            ClipContext.Fluid.NONE,
            entity
        );
        BlockHitResult blockHitResult = world.clip(context);
        
        boolean portalHitFound = portalHit.isPresent();
        boolean blockHitFound = blockHitResult.getType() == HitResult.Type.BLOCK;
        
        boolean shouldContinueRaytraceInsidePortal = false;
        if (portalHitFound && blockHitFound) {
            double portalDistance = portalHit.get().getSecond().distanceTo(startingPoint);
            double blockDistance = blockHitResult.getLocation().distanceTo(startingPoint);
            if (portalDistance < blockDistance) {
                // continue raytrace from within the portal
                shouldContinueRaytraceInsidePortal = true;
            }
            else {
                return new PortalAwareRaytraceResult(
                    world, blockHitResult, portalsPassingThrough
                );
            }
        }
        else if (!portalHitFound && blockHitFound) {
            return new PortalAwareRaytraceResult(
                world, blockHitResult, portalsPassingThrough
            );
        }
        else if (portalHitFound && !blockHitFound) {
            // continue raytrace from within the portal
            shouldContinueRaytraceInsidePortal = true;
        }
        
        if (shouldContinueRaytraceInsidePortal) {
            double portalDistance = portalHit.get().getSecond().distanceTo(startingPoint);
            Portal portal = portalHit.get().getFirst();
            Vec3 newStartingPoint = portal.transformPoint(portalHit.get().getSecond())
                .add(portal.getContentDirection().scale(0.001));
            Vec3 newDirection = portal.transformLocalVecNonScale(direction);
            double restDistance = maxDistance - portalDistance;
            if (restDistance < 0) {
                return null;
            }
            return portalAwareRayTrace(
                portal.getDestinationWorld(),
                newStartingPoint,
                newDirection,
                restDistance,
                entity,
                Stream.concat(
                    portalsPassingThrough.stream(), Stream.of(portal)
                ).collect(Collectors.toList())
            );
        }
        else {
            return null;
        }
    }
    
    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.ITEM, id("portal_gun"), PORTAL_GUN);
        Registry.register(BuiltInRegistries.ITEM, id("portalgun_body"), PORTAL_GUN_BODY);
        Registry.register(BuiltInRegistries.ITEM, id("portalgun_claw"), PORTAL_GUN_CLAW);
        
        Registry.register(BuiltInRegistries.ENTITY_TYPE, id("custom_portal"), CustomPortal.entityType);
        
        Registry.register(BuiltInRegistries.SOUND_EVENT, PORTAL1_SHOOT, PORTAL1_SHOOT_EVENT);
        Registry.register(BuiltInRegistries.SOUND_EVENT, PORTAL2_SHOOT, PORTAL2_SHOOT_EVENT);
        Registry.register(BuiltInRegistries.SOUND_EVENT, PORTAL_OPEN, PORTAL_OPEN_EVENT);
        Registry.register(BuiltInRegistries.SOUND_EVENT, PORTAL_CLOSE, PORTAL_CLOSE_EVENT);
        
        PortalGunConfig.register();
        
        // disable block breaking hand swinging
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() == PORTAL_GUN) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
        
        // add into creative inventory
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(PORTAL_GUN);
        });
    }
    
    
}
