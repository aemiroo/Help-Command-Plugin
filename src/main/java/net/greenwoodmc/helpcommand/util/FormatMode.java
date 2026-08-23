package net.greenwoodmc.helpcommand.util;

import java.util.Locale;


public enum FormatMode {


    LEGACY,
    MINIMESSAGE;


    public static FormatMode parse(String value) {
        if (value == null) {
            return null;
        }

        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
