package tk.meowmc.portalgun.client;

import com.qouteall.immersive_portals.network.McRemoteProcedureCall;
import com.qouteall.immersive_portals.render.PortalEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib3.renderer.geo.GeoItemRenderer;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.client.renderer.ClawRenderer;
import tk.meowmc.portalgun.client.renderer.PortalGunRenderer;
import tk.meowmc.portalgun.misc.RemoteCallables;

import java.util.UUID;

import static tk.meowmc.portalgun.Portalgun.id;

@Environment(EnvType.CLIENT)
public class PortalgunClient implements ClientModInitializer {

    public static final Identifier PacketID = id("spawn_packet");

    public static void onEntitySpawn(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        EntityType<?> type = Registry.ENTITY_TYPE.get(buf.readVarInt());
        UUID entityUUID = buf.readUuid();
        int entityID = buf.readVarInt();
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        float pitch = (buf.readByte() * 360) / 256.0F;
        float yaw = (buf.readByte() * 360) / 256.0F;
        ClientWorld world = MinecraftClient.getInstance().world;
        Entity entity = type.create(world);
        client.execute(() -> {
            if (entity != null) {
                entity.updatePosition(x, y, z);
                entity.updateTrackedPosition(x, y, z);
                entity.pitch = pitch;
                entity.yaw = yaw;
                entity.setEntityId(entityID);
                entity.setUuid(entityUUID);
                assert world != null;
                world.addEntity(entityID, entity);
            }
        });
    }

    @Override
    public void onInitializeClient() {
        KeyBinding clearPortals = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.portalgun.clearportals", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.portalgun"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (clearPortals.wasPressed()) {
                McRemoteProcedureCall.tellServerToInvoke("tk.meowmc.portalgun.misc.RemoteCallables.removeOldPortals");
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.options.keyAttack.isPressed() && !client.player.getItemCooldownManager().isCoolingDown(Portalgun.PORTALGUN)) {
                RemoteCallables.playAnim();
                McRemoteProcedureCall.tellServerToInvoke("tk.meowmc.portalgun.misc.RemoteCallables.portal1Place");
            }
        });

        GeoItemRenderer.registerItemRenderer(Portalgun.PORTALGUN, new PortalGunRenderer());
        GeoItemRenderer.registerItemRenderer(Portalgun.PORTALGUN_CLAW, new ClawRenderer());

        EntityRendererRegistry.INSTANCE.register(Portalgun.CUSTOM_PORTAL, (dispatcher, context) -> new PortalEntityRenderer(dispatcher));

        ClientPlayNetworking.registerGlobalReceiver(PacketID, PortalgunClient::onEntitySpawn);
    }
}
