package tk.meowmc.portalgun.entities;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.compat.PehkuiInterface;
import qouteall.imm_ptl.core.mc_utils.IPEntityEventListenableEntity;
import qouteall.imm_ptl.core.portal.*;
import qouteall.q_misc_util.Helper;
import qouteall.q_misc_util.dimension.DimId;
import qouteall.q_misc_util.my_util.SignalArged;
import qouteall.q_misc_util.my_util.SignalBiArged;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.items.PortalGunItem;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static tk.meowmc.portalgun.items.PortalGunItem.portal1Exists;
import static tk.meowmc.portalgun.items.PortalGunItem.portal2Exists;

public class CustomPortal extends Portal implements PortalLike, IPEntityEventListenableEntity {
    public static final SignalArged<CustomPortal> clientPortalTickSignal = new SignalArged();
    public static final SignalArged<CustomPortal> serverPortalTickSignal = new SignalArged();
    public static final SignalArged<CustomPortal> portalCacheUpdateSignal = new SignalArged();
    public static final SignalArged<CustomPortal> portalDisposeSignal = new SignalArged();
    public static final SignalBiArged<CustomPortal, NbtCompound> readPortalDataSignal = new SignalBiArged();
    public static final SignalBiArged<CustomPortal, NbtCompound> writePortalDataSignal = new SignalBiArged();
    public static final TrackedData<String> outline = DataTracker.registerData(CustomPortal.class, TrackedDataHandlerRegistry.STRING);
    public static final PortalAnimation defaultAnimation = null;
    public static EntityType<CustomPortal> entityType;
    protected PortalAnimation animation = null;
    private boolean interactable = true;

    public CustomPortal(@NotNull EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    private void initDefaultCullableRange() {
        cullableXStart = -(width / 2);
        cullableXEnd = (width / 2);
        cullableYStart = -(height / 2);
        cullableYEnd = (height / 2);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound compoundTag) {

        width = compoundTag.getDouble("width");
        height = compoundTag.getDouble("height");
        axisW = Helper.getVec3d(compoundTag, "axisW").normalize();
        axisH = Helper.getVec3d(compoundTag, "axisH").normalize();
        dimensionTo = DimId.getWorldId(compoundTag, "dimensionTo", world.isClient);
        destination = (Helper.getVec3d(compoundTag, "destination"));
        specificPlayerId = Helper.getUuid(compoundTag, "specificPlayer");
        if (compoundTag.contains("specialShape")) {
            specialShape = new GeometryPortalShape(
                    compoundTag.getList("specialShape", 6)
            );

            if (specialShape.triangles.isEmpty()) {
                specialShape = null;
            } else {
                if (!specialShape.isValid()) {
                    Helper.err("Portal shape invalid ");
                    specialShape = null;
                }
            }
        } else {
            specialShape = null;
        }
        if (compoundTag.contains("teleportable")) {
            teleportable = compoundTag.getBoolean("teleportable");
        }
        if (compoundTag.contains("cullableXStart")) {
            cullableXStart = compoundTag.getDouble("cullableXStart");
            cullableXEnd = compoundTag.getDouble("cullableXEnd");
            cullableYStart = compoundTag.getDouble("cullableYStart");
            cullableYEnd = compoundTag.getDouble("cullableYEnd");

            cullableXEnd = Math.min(cullableXEnd, width / 2);
            cullableXStart = Math.max(cullableXStart, -width / 2);
            cullableYEnd = Math.min(cullableYEnd, height / 2);
            cullableYStart = Math.max(cullableYStart, -height / 2);
        } else {
            if (specialShape != null) {
                cullableXStart = 0;
                cullableXEnd = 0;
                cullableYStart = 0;
                cullableYEnd = 0;
            } else {
                initDefaultCullableRange();
            }
        }
        if (compoundTag.contains("rotationA")) {
            rotation = new Quaternion(
                    compoundTag.getFloat("rotationB"),
                    compoundTag.getFloat("rotationC"),
                    compoundTag.getFloat("rotationD"),
                    compoundTag.getFloat("rotationA")
            );
        } else {
            rotation = null;
        }

        if (compoundTag.contains("interactable")) {
            interactable = compoundTag.getBoolean("interactable");
        }

        if (compoundTag.contains("scale")) {
            scaling = compoundTag.getDouble("scale");
        }
        if (compoundTag.contains("teleportChangesScale")) {
            teleportChangesScale = compoundTag.getBoolean("teleportChangesScale");
        }
        if (compoundTag.contains("teleportChangesGravity")) {
            teleportChangesGravity = compoundTag.getBoolean("teleportChangesGravity");
        }

        if (compoundTag.contains("portalTag")) {
            portalTag = compoundTag.getString("portalTag");
        }

        if (compoundTag.contains("fuseView")) {
            fuseView = compoundTag.getBoolean("fuseView");
        }

        if (compoundTag.contains("renderingMergable")) {
            renderingMergable = compoundTag.getBoolean("renderingMergable");
        }

        if (compoundTag.contains("hasCrossPortalCollision")) {
            hasCrossPortalCollision = compoundTag.getBoolean("hasCrossPortalCollision");
        }

        if (compoundTag.contains("commandsOnTeleported")) {
            NbtList list = compoundTag.getList("commandsOnTeleported", 8);
            commandsOnTeleported = list.stream()
                    .map(t -> ((NbtString) t).asString()).collect(Collectors.toList());
        } else {
            commandsOnTeleported = null;
        }

        if (compoundTag.contains("doRenderPlayer")) {
            doRenderPlayer = compoundTag.getBoolean("doRenderPlayer");
        }

        readPortalDataSignal.emit(this, compoundTag);

        updateCache();
    }

    @Environment(EnvType.CLIENT)
    private void acceptDataSync(Vec3d pos, NbtCompound customData) {

        setPosition(pos);
        readCustomDataFromNbt(customData);
    }

    @Environment(EnvType.CLIENT)
    private void startAnimationClient(PortalState animationStartState) {
    }

    @Override
    protected void initDataTracker() {
        getDataTracker().startTracking(outline, "null");
    }

    public String getOutline() {
        return getDataTracker().get(outline);
    }

    public void setOutline(String outline) {
        getDataTracker().set(CustomPortal.outline, outline);
    }

    @Override
    public void transformVelocity(@NotNull Entity entity) {
        Vec3d oldVelocity = McHelper.getWorldVelocity(entity);
        if (PehkuiInterface.invoker.isPehkuiPresent()) {
            if (teleportChangesScale) {
                McHelper.setWorldVelocity(entity, transformLocalVecNonScale(oldVelocity));
            } else {
                McHelper.setWorldVelocity(entity, transformLocalVec(oldVelocity));
            }
        } else {
            McHelper.setWorldVelocity(entity, transformLocalVec(oldVelocity));
        }

        final double maxVelocity = 0.85;
        if (oldVelocity.length() > maxVelocity) {
            // cannot be too fast
            McHelper.setWorldVelocity(
                    entity,
                    McHelper.getWorldVelocity(entity).normalize().multiply(0.78)
            );
        }

        // avoid cannot push minecart out of nether portal
        if (entity instanceof AbstractMinecartEntity && oldVelocity.lengthSquared() < 0.5) {
            McHelper.setWorldVelocity(entity, McHelper.getWorldVelocity(entity).multiply(2));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.animation != null)
            this.animation = null;

        if (!world.isClient) {

            BlockPos portalUpperPos = new BlockPos(
                    this.getX() - axisW.crossProduct(axisH).getX(),
                    this.getY() - axisW.crossProduct(axisH).getY(),
                    this.getZ() - axisW.crossProduct(axisH).getZ());
            BlockPos portalLowerPos = new BlockPos(
                    this.getX() - axisW.crossProduct(axisH).getX() - Math.abs(axisH.getX()),
                    this.getY() - axisW.crossProduct(axisH).getY() + axisH.getY(),
                    this.getZ() - axisW.crossProduct(axisH).getZ() - Math.abs(axisH.getZ()));

            BlockState portalUpperBlockState = this.world.getBlockState(portalUpperPos);
            BlockState portalLowerBlockState = this.world.getBlockState(portalLowerPos);


            Direction portalDirection = Direction.fromVector((int) this.getNormal().getX(), (int) this.getNormal().getY(), (int) this.getNormal().getZ());

            if ((!world.getBlockState(portalUpperPos).isSideSolidFullSquare(world, portalUpperPos, portalDirection)) ||
                    (!world.getBlockState(portalLowerPos).isSideSolidFullSquare(world, portalLowerPos, portalDirection)
                    ) || (world.getBlockState(new BlockPos(this.getPos())).isOpaque()) || (world.getBlockState(new BlockPos(
                    this.getX() - Math.abs(axisH.getX()),
                    this.getY() + axisH.getY(),
                    this.getZ() - Math.abs(axisH.getZ()))).isOpaque())) {
                Portalgun.logString(Level.INFO, "Upper" + portalUpperPos);
                Portalgun.logString(Level.INFO, "Lower" + portalLowerPos);

                this.kill();
                world.playSound(null,
                        this.getPos().getX(),
                        this.getPos().getY(),
                        this.getPos().getZ(),
                        Portalgun.PORTAL_CLOSE_EVENT,
                        SoundCategory.NEUTRAL,
                        1.0F,
                        1F);
                if (!this.getOutline().equals("null")) {
                    PortalOverlay portalOutline;
                    portalOutline = (PortalOverlay) ((ServerWorld) world).getEntity(UUID.fromString(this.getOutline()));
                    if (portalOutline != null)
                        portalOutline.kill();
                }
                PortalGunItem gunItem = (PortalGunItem) Portalgun.PORTALGUN;
                if (Objects.equals(this.portalTag, "portalgun_portal1")) {
                    if (portal2Exists) {
                        gunItem.newPortal2.setDestination(gunItem.newPortal2.getPos());
                        PortalManipulation.adjustRotationToConnect(gunItem.newPortal2, gunItem.newPortal2);
                        gunItem.newPortal2.reloadAndSyncToClient();
                    }
                    portal1Exists = false;
                } else if (Objects.equals(this.portalTag, "portalgun_portal2")) {
                    if (portal1Exists) {
                        gunItem.newPortal1.setDestination(gunItem.newPortal1.getPos());
                        PortalManipulation.adjustRotationToConnect(gunItem.newPortal1, gunItem.newPortal1);
                        gunItem.newPortal1.reloadAndSyncToClient();
                    }
                    portal2Exists = false;
                }
            }
        }
    }


}
