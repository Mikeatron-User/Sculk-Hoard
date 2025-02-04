package com.github.sculkhorde.systems.cursor_system;

import java.util.UUID;

public interface ICursor {

    void moveTo(double x, double y, double z);
    void setToBeDeleted();
    boolean isSetToBeDeleted();

    boolean canBeManuallyTicked();

    void tick();

    UUID getUUID();

    default UUID createUUID()
    {
        return UUID.randomUUID();
    }

}
