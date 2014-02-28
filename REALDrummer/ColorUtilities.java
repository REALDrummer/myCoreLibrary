package REALDrummer;

import org.bukkit.ChatColor;

public class ColorUtilities {
    /** This method actiavtes any color codes in a given String and returns the message with color codes eliminated from the text and colors added to the text. This method is
     * necessary because it does two (2) things that <a href="ChatColor#translateAlternateColorCodes(char, String)">CraftBukkit's color code translating method</a> cannot.
     * <b>1)</b> It rearranges color codes in the text to ensure that every one is used. With CraftBukkit's standard methods, any formatting color codes (e.g. &k for magic or
     * &l for bold) that <i>precede</i> color color codes (e.g. &a for light green or &4 for dark red) are automatically cancelled, but if the formatting color codes comes
     * <i>after</i> the color color code, the following text will be colored AND formatted. This method can simply switch the places of the formatting and color color codes in
     * these instances to ensure that both are used (e.g. "&k&4", which normally results in dark red text, becomes "&4&k", which results in dark red magic text). <b>2)</b> It
     * allows the use of anti-color codes, an invention of mine. Anti-color codes use percent symbols (%) in place of ampersands (&) and work in the opposite way of normal
     * color codes. They allow the user to cancel one coloring or formatting in text without having to rewrite all of the previous color codes. For example, normally to change
     * from a dark red, magic, bold text ("&4&k&l") to a dark red magic text ("&4&k"), you would have to use "&4&k"; with this feature, however, you can simply use "%l" to
     * cancel the bold formatting. This feature is essential for the AutoCorrect abilities; for example, the profanity filter must have the ability to execute a magic color
     * code, but then cancel it without losing any colors designated by the sender earlier in the message. Without this ability, the white color code ("&f") could perhaps be
     * used to cancel the magic formatting, but in a red message containing a profanity, that would result in the rest of the message after the covered up profanity being
     * white.
     * 
     * @param text
     *            is the string that must be color coded.
     * @return the String colored according to the color codes given */
    public static String colorCode(String text) {
        text = "&f" + text;
        // put color codes in the right order if they're next to each other
        for (int i = 0; i < text.length() - 3; i++)
            if (isColorCode(text.substring(i, i + 2), false, true) && isColorCode(text.substring(i + 2, i + 4), true, true))
                text = text.substring(0, i) + text.substring(i + 2, i + 4) + text.substring(i, i + 2) + text.substring(i + 4);
        // replace all anti color codes with non antis
        String current_color_code = "";
        for (int i = 0; i < text.length() - 1; i++) {
            if (isColorCode(text.substring(i, i + 2), null, true))
                current_color_code = current_color_code + text.substring(i, i + 2);
            else if (isColorCode(text.substring(i, i + 2), null, false)) {
                while (text.length() > i + 2 && isColorCode(text.substring(i, i + 2), null, false)) {
                    current_color_code = current_color_code.replaceAll("&" + text.substring(i + 1, i + 2), "");
                    if (current_color_code.equals(""))
                        current_color_code = "&f";
                    text = text.substring(0, i) + text.substring(i + 2);
                }
                text = text.substring(0, i) + current_color_code + text.substring(i);
            }
        }
        String colored_text = ChatColor.translateAlternateColorCodes('&', text);
        return colored_text;
    }

    /** This method can remove all color codes from a given String, including anti-color codes, which are not recognized by Bukkit's <tt>ChatColor.stripColor()</tt> method.
     * 
     * @param text
     *            is the String that must have its color codeds removed.
     * @return <b><tt>text</b></tt> without color codes. */
    public static String decolor(String text) {
        if (!text.contains("&") && !text.contains("%"))
            return text;
        for (int i = 0; i < text.length() - 2; i++) {
            if (isColorCode(text.substring(i, i + 2), null, null)) {
                if (i + 2 < text.length())
                    text = text.substring(0, i) + text.substring(i + 2);
                else
                    text = text.substring(0, i);
                i -= 2;
            }
        }
        return text;
    }

    /** This method can determine whether or not a String is a color code or not and what type or color code it is (formatting vs. color color codes and/or normal vs.
     * anti-color codes).
     * 
     * @param text
     *            is the two-character String that this method analyzes to see whether or not it is a color code.
     * @param non_formatting
     *            is a Boolean that can have three values. <b>true</b> means that the color code must be non-formatting, e.g. "&a" (light green) or "&4" (dark red).
     *            <b>false</b> means that the color code must be formatting, e.g. "&k" for magic or "&l" for bold. <b>null</b> means that it can be either a formatting or
     *            non-formatting color code to return true.
     * @param non_anti
     *            works similarly to non_formatting, but for anti-color codes vs. normal color codes. "true" means that the color code must <i>not</i> be an anti-color code.
     * @return true if the String is a color code and the other standards set by the Boolean parameters are met; false otherwise */
    public static Boolean isColorCode(String text, Boolean non_formatting, Boolean non_anti) {
        if (!text.startsWith("&") && !text.startsWith("%"))
            return false;
        if (non_anti != null)
            if (non_anti && text.startsWith("%"))
                return false;
            else if (!non_anti && text.startsWith("&"))
                return false;
        if (non_formatting == null || non_formatting)
            for (char color_color_code_char : Wiki.COLOR_COLOR_CODE_CHARS)
                if (text.toCharArray()[1] == color_color_code_char)
                    return true;
        if (non_formatting == null || !non_formatting)
            for (char formatting_color_code_char : Wiki.FORMATTING_COLOR_CODE_CHARS)
                if (text.toCharArray()[1] == formatting_color_code_char)
                    return true;
        return false;
    }

}