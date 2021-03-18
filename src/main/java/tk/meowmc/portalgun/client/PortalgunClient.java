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
                    McRemoteProcedureCall.tellServerToInvoke("tk.meowmc.portalgun.misc.RemoteCallables.removeOldPortal1");
                    McRemoteProcedureCall.tellServerToInvoke("tk.meowmc.portalgun.misc.RemoteCallables.removeOldPortal2");
                });
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (client.options.keyAttack.isPressed()) {
                if (tickCounter > 4)
                    tickCounter = 3;
                delay = tickCounter % 3 == 0;

                // Portalgun.logInt(Level.INFO, tickCounter);
                if (delay) {
                    McHelper.executeOnServerThread(() -> {
                        McRemoteProcedureCall.tellServerToInvoke("tk.meowmc.portalgun.misc.RemoteCallables.portal1Place");
                    });
                }
            }
        });
    }
}
