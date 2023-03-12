package tk.meowmc.portalgun.items;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import tk.meowmc.portalgun.client.renderer.ClawRenderer;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClawItem extends Item implements GeoItem {
    
    public AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public ClawItem(Properties settings) {
        super(settings);
    
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }
    
    // Utilise our own render hook to define our custom renderer
    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private final ClawRenderer renderer = new ClawRenderer();
            
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return this.renderer;
            }
        });
    }
    
    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }
    
    // Register our animation controllers
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
            new AnimationController<>(
                this, "clawController", 1, state -> PlayState.CONTINUE
            )
        );
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
