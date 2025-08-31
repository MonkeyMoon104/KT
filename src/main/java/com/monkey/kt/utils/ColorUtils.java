package com.monkey.kt.utils;

import java.util.logging.Level;

public class ColorUtils {

    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    public static final String BRIGHT_BLACK = "\u001B[90m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_PURPLE = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";

    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";
    public static final String ITALIC = "\u001B[3m";

    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_PURPLE = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";

    private static final boolean COLORS_ENABLED = !Boolean.parseBoolean(
            System.getProperty("kt.disable.colors", "false"));

    public static String colorize(String color, String text) {
        if (!COLORS_ENABLED) {
            return text;
        }
        return color + text + RESET;
    }

    public static String format(String text, String... formats) {
        if (!COLORS_ENABLED || formats.length == 0) {
            return text;
        }

        StringBuilder sb = new StringBuilder();
        for (String format : formats) {
            sb.append(format);
        }
        sb.append(text).append(RESET);
        return sb.toString();
    }

    public static String success(String text) {
        return colorize(BRIGHT_GREEN, text);
    }

    public static String info(String text) {
        return colorize(CYAN, text);
    }

    public static String warning(String text) {
        return colorize(YELLOW, text);
    }

    public static String error(String text) {
        return colorize(BRIGHT_RED, text);
    }

    public static String debug(String text) {
        return colorize(BRIGHT_BLACK, text);
    }

    public static String severe(String text) {
        return format(text, BOLD, BRIGHT_RED);
    }

    public static String forLevel(Level level, String text) {
        if (level == Level.SEVERE) {
            return severe(text);
        } else if (level == Level.WARNING) {
            return warning(text);
        } else if (level == Level.INFO) {
            return info(text);
        } else if (level == Level.CONFIG) {
            return colorize(PURPLE, text);
        } else if (level == Level.FINE || level == Level.FINER || level == Level.FINEST) {
            return debug(text);
        }
        return text;
    }

    public static String database(String text) {
        return colorize(BRIGHT_BLUE, text);
    }

    public static String sql(String text) {
        return colorize(PURPLE, text);
    }

    public static String transaction(String text) {
        return format(text, BOLD, CYAN);
    }

    public static String batch(String text) {
        return colorize(BRIGHT_PURPLE, text);
    }

    public static String economy(String text) {
        return format(text, BOLD, YELLOW);
    }

    public static String purchase(String text) {
        return colorize(GREEN, text);
    }


    public static String stripColors(String text) {
        if (text == null) return null;
        return text.replaceAll("\u001B\\[[0-9;]*m", "");
    }

    public static String separator(char character, int length, String color) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(character);
        }
        return colorize(color, sb.toString());
    }

    public static String separator() {
        return separator('=', 50, BRIGHT_BLACK);
    }

    public static String header(String text) {
        String border = separator('=', text.length() + 4, BRIGHT_BLUE);
        String content = colorize(BRIGHT_WHITE, "| " + text + " |");
        return border + "\n" + content + "\n" + border;
    }

    public static String plugin(String text) {
        return format(text, BOLD, BRIGHT_CYAN);
    }

    public static String performance(String text) {
        return colorize(BRIGHT_YELLOW, text);
    }

    public static String config(String text) {
        return colorize(PURPLE, text);
    }

    public static boolean areColorsEnabled() {
        return COLORS_ENABLED;
    }
    public static String rainbow(String text) {
        if (!COLORS_ENABLED) return text;

        String[] colors = {RED, YELLOW, GREEN, CYAN, BLUE, PURPLE};
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != ' ') {
                String color = colors[i % colors.length];
                result.append(color).append(c);
            } else {
                result.append(c);
            }
        }
        result.append(RESET);
        return result.toString();
    }
}