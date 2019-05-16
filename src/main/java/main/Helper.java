package main;

public class Helper {

    public static final String[] WEEKDAYS = new String[]{"---", "So", "Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"};

    public static String make12(String name) {
        while (name.length() < 12) {
            name = " " + name;
        }
        return name.substring(0, 12) + " ";
    }

    public static String make2(final int i) {
        if (i < 10) {
            return "0" + i;
        } else {
            return "" + i;
        }
    }

    public static String convertPseudoHTLM(final String text) {

        if (text == null) {
            return null;
        }

        return "<HTML>" + text.replace("\n", "<br>") + "</HTML>";

    }

    public static String getSecondsAsTimeString(int seconds) {

        int hours = seconds / 3600;
        seconds -= hours * 3600;

        int min = seconds / 60;
        seconds -= min * 60;

        String h = String.valueOf(hours);
        if (h.length() == 1) {
            h = "0" + h;
        }

        String m = String.valueOf(min);
        if (m.length() == 1) {
            m = "0" + m;
        }

        String s = String.valueOf(seconds);
        if (s.length() == 1) {
            s = "0" + s;
        }

        return h + ":" + m + ":" + s;
    }

}
