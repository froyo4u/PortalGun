package tk.meowmc.portalgun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tk.meowmc.portalgun.config.PortalGunConfig;
import tk.meowmc.portalgun.entities.CustomPortal;
import tk.meowmc.portalgun.entities.PortalOverlay;
import tk.meowmc.portalgun.items.ClawItem;
import tk.meowmc.portalgun.items.PortalGunItem;

public class Portalgun implements ModInitializer {
    public static final String MODID = "portalgun";
    public static final String KEY = MODID + ":portalgun_portals";
    public static final String MOD_NAME = "PortalGun Mod";


    public static final Item PORTALGUN = new PortalGunItem(new FabricItemSettings().fireproof().group(ItemGroup.TOOLS).maxCount(1).rarity(Rarity.EPIC));
    public static final Item PORTALGUN_BODY = new Item(new FabricItemSettings().fireproof().group(ItemGroup.MATERIALS).maxCount(1).rarity(Rarity.RARE));
    public static final Item PORTALGUN_CLAW = new ClawItem(new FabricItemSettings().fireproof().group(ItemGroup.MATERIALS).maxCount(1).rarity(Rarity.RARE));

    public static final EntityType<CustomPortal> CUSTOM_PORTAL = FabricEntityTypeBuilder.create(SpawnGroup.MISC, CustomPortal::new)
            .dimensions(EntityDimensions.changing(0F, 0F))
            .build();

    public static final EntityType<PortalOverlay> PORTAL_OVERLAY = FabricEntityTypeBuilder.create(SpawnGroup.AMBIENT, PortalOverlay::new)
            .dimensions(EntityDimensions.changing(0F, 0F))
            .build();

    public static final Identifier PORTAL1_SHOOT = new Identifier("portalgun:portal1_shoot");
    public static final Identifier PORTAL2_SHOOT = new Identifier("portalgun:portal2_shoot");
    public static final Identifier PORTAL_OPEN = new Identifier("portalgun:portal_open");
    public static final Identifier PORTAL_CLOSE = new Identifier("portalgun:portal_close");

    public static SoundEvent PORTAL1_SHOOT_EVENT = new SoundEvent(PORTAL1_SHOOT);
    public static SoundEvent PORTAL2_SHOOT_EVENT = new SoundEvent(PORTAL2_SHOOT);
    public static SoundEvent PORTAL_OPEN_EVENT = new SoundEvent(PORTAL_OPEN);
    public static SoundEvent PORTAL_CLOSE_EVENT = new SoundEvent(PORTAL_CLOSE);

    public static Logger LOGGER = LogManager.getLogger();

    public static void logString(Level level, String message) {
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

    public static void logInt(Level level, int message) {
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

    public static void logDouble(Level level, double message) {
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    @Override
    public void onInitialize() {
        logString(Level.INFO, "Initializing");
        Registry.register(Registry.ITEM, id("portal_gun"), PORTALGUN);
        Registry.register(Registry.ITEM, id("portalgun_body"), PORTALGUN_BODY);
        Registry.register(Registry.ITEM, id("portalgun_claw"), PORTALGUN_CLAW);

        Registry.register(Registry.ENTITY_TYPE, id("custom_portal"), CUSTOM_PORTAL);
        Registry.register(Registry.ENTITY_TYPE, id("portal_overlay"), PORTAL_OVERLAY);

        Registry.register(Registry.SOUND_EVENT, PORTAL1_SHOOT, PORTAL1_SHOOT_EVENT);
        Registry.register(Registry.SOUND_EVENT, PORTAL2_SHOOT, PORTAL2_SHOOT_EVENT);
        Registry.register(Registry.SOUND_EVENT, PORTAL_OPEN, PORTAL_OPEN_EVENT);
        Registry.register(Registry.SOUND_EVENT, PORTAL_CLOSE, PORTAL_CLOSE_EVENT);

        PortalGunConfig.register();
    }


}
