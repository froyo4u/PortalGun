package tk.meowmc.portalgun.misc;

import net.minecraft.util.math.Direction;

public interface EntityAttatchments {
    boolean isRolling();

    void setRolling(boolean rolling);

    Direction getDirection();

    void setDirection(Direction direction);

    double getMaxFallSpeed();

    void setMaxFallSpeed(double maxFallSpeed);
}
