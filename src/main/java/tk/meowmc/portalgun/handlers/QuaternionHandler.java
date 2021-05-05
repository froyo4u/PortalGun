package tk.meowmc.portalgun.handlers;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Quaternion;

public class QuaternionHandler implements TrackedDataHandler<Quaternion> {
    public static final QuaternionHandler quaternionHandler = new QuaternionHandler();

    static {
        TrackedDataHandlerRegistry.register(quaternionHandler);
    }

    @Override
    public void write(PacketByteBuf buf, Quaternion quaternion) {
        buf.writeFloat(quaternion.getX());
        buf.writeFloat(quaternion.getY());
        buf.writeFloat(quaternion.getZ());
        buf.writeFloat(quaternion.getW());
    }

    @Override
    public Quaternion read(PacketByteBuf buf) {
        return new Quaternion(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    @Override
    public Quaternion copy(Quaternion quaternion) {
        return new Quaternion(quaternion);
    }
}
