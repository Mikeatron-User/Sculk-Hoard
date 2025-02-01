package com.github.sculkhorde.util;

import com.github.sculkhorde.core.SculkHorde;

public class Log {

    private final String header;
    public boolean enabled = true;

    public Log(String header) {
        this.header = header+": ";
    }

    public Log() {
        this.header = "";
    }

    public void info(String string) {
        if (enabled) {
            SculkHorde.LOGGER.info(header + string);
        }
    }

    public void warn(String string) {
        if (enabled) {
            SculkHorde.LOGGER.warn(header + string);
        }
    }

    public void error(String string) {
        if (enabled) {
            SculkHorde.LOGGER.error(header + string);
        }
    }

}
