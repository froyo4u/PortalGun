package tk.meowmc.portalgun.client;

import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.network.McRemoteProcedureCall;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib3.renderer.geo.GeoItemRenderer;
import tk.meowmc.portalgun.Portalgun;
import tk.meowmc.portalgun.client.renderer.ClawRenderer;
import tk.meowmc.portalgun.client.renderer.PortalGunRenderer;
import tk.meowmc.portalgun.misc.RemoteCallables;

@Environment(EnvType.CLIENT)
public class PortalgunClient implements ClientModInitializer {

    public static int tickCounter = 3;
    public static boolean delay = false;

    @Override
    public void onInitializeClient() {
        KeyBinding clearPortals = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.portalgun.clearportals", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.portalgun"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (clearPortals.wasPressed()) {
                McHelper.executeOnServerThread(() -> {
                    McRemoteProcedureCall.tellServerToInvoke("tk.meowmc.portalgun.misc.RemoteCallables.removeOldPortals");
                });
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (client.options.keyAttack.isPressed()) {
                if (tickCounter > 4)
                    tickCounter = 3;
                delay = tickCounter % 3 == 0;

                if (delay) {
                    RemoteCallables.playAnim();
                    McHelper.executeOnServerThread(() -> {
                        McRemoteProcedureCall.tellServerToInvoke("tk.meowmc.portalgun.misc.RemoteCallables.portal1Place");
                    });
                }
            }
        });

        GeoItemRenderer.registerItemRenderer(Portalgun.PORTALGUN, new PortalGunRenderer());
        GeoItemRenderer.registerItemRenderer(Portalgun.PORTALGUN_CLAW, new ClawRenderer());
    }
}
