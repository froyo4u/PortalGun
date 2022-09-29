package tk.meowmc.portalgun.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import static tk.meowmc.portalgun.Portalgun.PORTALGUN;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    @Shadow
    @Final
    public ClientPlayNetworkHandler networkHandler;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile, PlayerPublicKey playerPublicKey) {
        super(world, profile, playerPublicKey);
    }

    /**
     * @author someone
     * @reason cuz
     */
    @Overwrite
    public void swingHand(Hand hand) {
        if (!isHolding(PORTALGUN)) {
            super.swingHand(hand);
            this.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
        }
    }
}
