package tk.meowmc.portalgun.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.ModelLayerLocation;
import org.lwjgl.glfw.GLFW;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.client.renderer.CustomPortalEntityRenderer;
import tk.meowmc.portalgun.client.renderer.models.PortalOverlayModel;
import tk.meowmc.portalgun.entities.CustomPortal;
import tk.meowmc.portalgun.misc.RemoteCallables;

import static tk.meowmc.portalgun.Portalgun.id;

@Environment(EnvType.CLIENT)
public class PortalgunClient implements ClientModInitializer {
    public static final ModelLayerLocation OVERLAY_MODEL_LAYER = new ModelLayerLocation(id("portal_overlay"), "main");
    
    @Override
    public void onInitializeClient() {
        KeyMapping clearPortals = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.portalgun.clearportals", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.portalgun"));

        EntityModelLayerRegistry.registerModelLayer(OVERLAY_MODEL_LAYER, PortalOverlayModel::getTexturedModelData);
        EntityRendererRegistry.register(CustomPortal.entityType, CustomPortalEntityRenderer::new);
    }
}
