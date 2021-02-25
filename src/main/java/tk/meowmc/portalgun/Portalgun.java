package tk.meowmc.portalgun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tk.meowmc.portalgun.items.PortalGunItem;

public class Portalgun implements ModInitializer {
    MinecraftClient client = MinecraftClient.getInstance();

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "portalgun";
    public static final String KEY = MODID + ":portalgun_portals";
    public static final String MOD_NAME = "PortalGun Mod";


    public static final Item PORTALGUN = new PortalGunItem(new FabricItemSettings().fireproof().group(ItemGroup.TOOLS).maxCount(1));


    public static final Identifier PORTAL1_SHOOT = new Identifier("portalgun:portal1_shoot");
    public static SoundEvent PORTAL1_SHOOT_EVENT = new SoundEvent(PORTAL1_SHOOT);

    public static final Identifier PORTAL2_SHOOT = new Identifier("portalgun:portal2_shoot");
    public static SoundEvent PORTAL2_SHOOT_EVENT = new SoundEvent(PORTAL2_SHOOT);

    public static final Identifier PORTAL_OPEN = new Identifier("portalgun:portal_open");
    public static SoundEvent PORTAL_OPEN_EVENT = new SoundEvent(PORTAL_OPEN);
    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing");
        Registry.register(Registry.ITEM, new Identifier(MODID, "portalgun"), PORTALGUN);
        Registry.register(Registry.SOUND_EVENT, PORTAL1_SHOOT, PORTAL1_SHOOT_EVENT);
        Registry.register(Registry.SOUND_EVENT, PORTAL2_SHOOT, PORTAL2_SHOOT_EVENT);
        Registry.register(Registry.SOUND_EVENT, PORTAL_OPEN, PORTAL_OPEN_EVENT);
    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }


}
