package tk.meowmc.portalgun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tk.meowmc.portalgun.config.PortalGunConfig;
import tk.meowmc.portalgun.entities.CustomPortal;
import tk.meowmc.portalgun.items.ClawItem;
import tk.meowmc.portalgun.items.PortalGunItem;

public class Portalgun implements ModInitializer {
    public static final String MODID = "portalgun";
    public static final String KEY = MODID + ":portalgun_portals";
    public static final String MOD_NAME = "PortalGun Mod";
    
    public static final double portalOffset = 0.001;
    public static final double portalOverlayOffset = 0.001;


    public static final Item PORTALGUN = new PortalGunItem(new FabricItemSettings().fireResistant().stacksTo(1).rarity(Rarity.EPIC));
    public static final Item PORTALGUN_BODY = new Item(new FabricItemSettings().fireResistant().stacksTo(1).rarity(Rarity.RARE));
    public static final Item PORTALGUN_CLAW = new ClawItem(new FabricItemSettings().fireResistant().stacksTo(1).rarity(Rarity.RARE));

    public static final ResourceLocation PORTAL1_SHOOT = new ResourceLocation("portalgun:portal1_shoot");
    public static final ResourceLocation PORTAL2_SHOOT = new ResourceLocation("portalgun:portal2_shoot");
    public static final ResourceLocation PORTAL_OPEN = new ResourceLocation("portalgun:portal_open");
    public static final ResourceLocation PORTAL_CLOSE = new ResourceLocation("portalgun:portal_close");
    
    public static SoundEvent PORTAL1_SHOOT_EVENT = SoundEvent.createVariableRangeEvent(PORTAL1_SHOOT);
    public static SoundEvent PORTAL2_SHOOT_EVENT = SoundEvent.createVariableRangeEvent(PORTAL2_SHOOT);
    public static SoundEvent PORTAL_OPEN_EVENT = SoundEvent.createVariableRangeEvent(PORTAL_OPEN);
    public static SoundEvent PORTAL_CLOSE_EVENT = SoundEvent.createVariableRangeEvent(PORTAL_CLOSE);

    public static final Logger LOGGER = LogManager.getLogger();
    
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.ITEM, id("portal_gun"), PORTALGUN);
        Registry.register(BuiltInRegistries.ITEM, id("portalgun_body"), PORTALGUN_BODY);
        Registry.register(BuiltInRegistries.ITEM, id("portalgun_claw"), PORTALGUN_CLAW);

        Registry.register(BuiltInRegistries.ENTITY_TYPE, id("custom_portal"), CustomPortal.entityType);

        Registry.register(BuiltInRegistries.SOUND_EVENT, PORTAL1_SHOOT, PORTAL1_SHOOT_EVENT);
        Registry.register(BuiltInRegistries.SOUND_EVENT, PORTAL2_SHOOT, PORTAL2_SHOOT_EVENT);
        Registry.register(BuiltInRegistries.SOUND_EVENT, PORTAL_OPEN, PORTAL_OPEN_EVENT);
        Registry.register(BuiltInRegistries.SOUND_EVENT, PORTAL_CLOSE, PORTAL_CLOSE_EVENT);

        PortalGunConfig.register();
    }


}
