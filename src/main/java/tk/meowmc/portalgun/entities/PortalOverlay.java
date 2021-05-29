package tk.meowmc.portalgun.entities;

import com.qouteall.immersive_portals.Helper;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import tk.meowmc.portalgun.handlers.QuaternionHandler;

import static tk.meowmc.portalgun.client.PortalgunClient.PacketID;

public class PortalOverlay extends Entity {
    public static final TrackedData<Quaternion> quaternion = DataTracker.registerData(PortalOverlay.class, QuaternionHandler.quaternionHandler);
    public static final TrackedData<Float> roll = DataTracker.registerData(PortalOverlay.class, TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Boolean> color = DataTracker.registerData(PortalOverlay.class, TrackedDataHandlerRegistry.BOOLEAN);
    public Vec3d axisH;
    public Vec3d axisW;

    public PortalOverlay(EntityType<?> type, World world) {
        super(type, world);
        this.ignoreCameraFrustum = true;
    }


    @Override
    protected void initDataTracker() {
        getDataTracker().startTracking(quaternion, Quaternion.IDENTITY);
        getDataTracker().startTracking(roll, 0F);
        getDataTracker().startTracking(color, false);
    }

    @Override
    protected void readCustomDataFromTag(CompoundTag compoundTag) {
        setColor(compoundTag.getBoolean("color"));
        setRoll(compoundTag.getFloat("roll"));
        axisH = Helper.getVec3d(compoundTag, "axisH").normalize();
        axisW = Helper.getVec3d(compoundTag, "axisW").normalize();
    }

    @Override
    protected void writeCustomDataToTag(CompoundTag compoundTag) {
        compoundTag.putBoolean("color", this.getColor());
        compoundTag.putFloat("roll", this.getRoll());
        Helper.putVec3d(compoundTag, "axisH", this.axisH);
        Helper.putVec3d(compoundTag, "axisW", this.axisW);
    }

    @Override
    public Packet<?> createSpawnPacket() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeVarInt(
                Registry.ENTITY_TYPE.getRawId(this.getType()))
                .writeUuid(this.getUuid())
                .writeVarInt(this.getEntityId())
                .writeDouble(this.getX())
                .writeDouble(this.getY())
                .writeDouble(this.getZ())
                .writeByte(MathHelper.floor(this.pitch * 256.0F / 360.0F))
                .writeByte(MathHelper.floor(this.yaw * 256.0F / 360.0F));

        return ServerPlayNetworking.createS2CPacket(PacketID, buf);
    }

    @Override
    public boolean isCollidable() {
        return false;
    }

    @Override
    public boolean doesNotCollide(double offsetX, double offsetY, double offsetZ) {
        return true;
    }

    @Override
    public Direction getHorizontalFacing() {
        return Direction.fromRotation(this.yaw);
    }

    @Override
    public boolean collidesWith(Entity entity) {
        return false;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean shouldRender(double dist) {
        double d = 64.0D * getRenderDistanceMultiplier();
        return dist < d * d;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Vec3d method_30950(float f) {
        return this.getPos();
    }

    public Quaternion getRotation() {
        return getDataTracker().get(quaternion);
    }

    public Float getRoll() {
        return getDataTracker().get(roll);
    }

    public void setRoll(Float roll) {
        getDataTracker().set(PortalOverlay.roll, roll);
    }

    public Boolean getColor() {
        return getDataTracker().get(color);
    }

    public void setColor(Boolean color) {
        this.getDataTracker().set(PortalOverlay.color, color);
    }

    public Integer getColorInt() {
        return !getDataTracker().get(color) ? 24031 : 15630107;
    }


}
