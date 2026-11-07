package dev.mantic.hooks;

public final class Placeholders {

    private Placeholders() {
    }

    /** Replaces each {key} with its value; {@code pairs} alternates key, value. */
    public static String apply(String template, Object... pairs) {
        if (template == null) {
            return "";
        }
        String result = template;
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            result = result.replace("{" + pairs[i] + "}", String.valueOf(pairs[i + 1]));
        }
        return result;
    }
}
