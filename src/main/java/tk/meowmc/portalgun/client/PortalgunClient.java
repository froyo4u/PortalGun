package tk.meowmc.portalgun.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.lwjgl.glfw.GLFW;
import qouteall.q_misc_util.api.McRemoteProcedureCall;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.client.renderer.ClawRenderer;
import tk.meowmc.portalgun.client.renderer.CustomPortalEntityRenderer;
import tk.meowmc.portalgun.client.renderer.PortalGunRenderer;
import tk.meowmc.portalgun.client.renderer.models.PortalOverlayModel;
import tk.meowmc.portalgun.misc.RemoteCallables;

import java.util.UUID;

import static tk.meowmc.portalgun.Portalgun.id;

import com.mojang.blaze3d.platform.InputConstants;

@Environment(EnvType.CLIENT)
public class PortalgunClient implements ClientModInitializer {
    public static final ModelLayerLocation OVERLAY_MODEL_LAYER = new ModelLayerLocation(id("portal_overlay"), "main");
    
    @Override
    public void onInitializeClient() {
        KeyMapping clearPortals = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.portalgun.clearportals", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.portalgun"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (clearPortals.consumeClick()) {
                McRemoteProcedureCall.tellServerToInvoke("tk.meowmc.portalgun.misc.RemoteCallables.removeOldPortals");
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.options.keyAttack.isDown() && !client.player.getCooldowns().isOnCooldown(Portalgun.PORTALGUN)) {
                RemoteCallables.playAnim();
                McRemoteProcedureCall.tellServerToInvoke("tk.meowmc.portalgun.misc.RemoteCallables.portal1Place");
            }
        });

        GeoItemRenderer.registerItemRenderer(Portalgun.PORTALGUN, new PortalGunRenderer());
        GeoItemRenderer.registerItemRenderer(Portalgun.PORTALGUN_CLAW, new ClawRenderer());

        EntityModelLayerRegistry.registerModelLayer(OVERLAY_MODEL_LAYER,
                PortalOverlayModel::getTexturedModelData);
        EntityRendererRegistry.register(Portalgun.CUSTOM_PORTAL, CustomPortalEntityRenderer::new);
    }
}
