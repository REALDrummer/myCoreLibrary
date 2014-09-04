package REALDrummer.utils;

import org.bukkit.ChatColor;

import static REALDrummer.myWiki.*;
import static REALDrummer.utils.ListUtilities.contains;

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
            if (isColorCode(text.substring(i, i + 2), ColorCodeType.FORMATTING) && isColorCode(text.substring(i + 2, i + 4), ColorCodeType.COLOR))
                text = text.substring(0, i) + text.substring(i + 2, i + 4) + text.substring(i, i + 2) + text.substring(i + 4);

        // replace all anti color codes with non antis
        String current_color_code = "";
        for (int i = 0; i < text.length() - 1; i++) {
            // if it's not an anti-color code, use it to keep track of the current color code
            if (isColorCode(text.substring(i, i + 2), ColorCodeType.BASIC, ColorCodeType.CONVERTED))
                current_color_code = current_color_code + text.substring(i, i + 2);
            // if it's an anti-color code, replace it with the current color code
            else if (isColorCode(text.substring(i, i + 2), ColorCodeType.ANTI)) {
                while (text.length() > i + 2 && isColorCode(text.substring(i, i + 2), ColorCodeType.ANTI)) {
                    current_color_code = current_color_code.replaceAll("[&\u00A7]" + text.toCharArray()[i + 1], "");
                    if (current_color_code.equals(""))
                        current_color_code = "&f";
                    text = text.substring(0, i) + text.substring(i + 2);
                }
                text = text.substring(0, i) + current_color_code + text.substring(i);
            }
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /** This method can remove all color codes from a given String, including anti-color codes, which are not recognized by Bukkit's <tt>ChatColor.stripColor()</tt> method.
     * 
     * @param text
     *            is the String that must have its color codeds removed.
     * @return <b><tt>text</b></tt> without color codes. */
    public static String decolor(String text) {
        if (!text.contains("&") && !text.contains("%") && !text.contains("\u00A7"))
            return text;
        for (int i = 0; i < text.length() - 2; i++) {
            if (isColorCode(text.substring(i, i + 2))) {
                if (i + 2 < text.length())
                    text = text.substring(0, i) + text.substring(i + 2);
                else
                    text = text.substring(0, i);
                i -= 2;
            }
        }
        return text;
    }

    // old isColorCode Boolean parameters: non_formatting, non_anti
    /** This method can determine whether or not a String is a color code or not and what type or color code it is (formatting vs. color color codes and/or normal vs.
     * anti-color codes).
     * 
     * @param text
     *            is the two-character String that this method analyzes to see whether or not it is a color code.
     * @param types
     *            is a list of {@link ColorCodeType}s that should be searched for.
     * @return <b>true</b> if the <tt>String</tt> is a color code type that matches <b><tt>types</b></tt>; <b>false</b> otherwise. */
    public static boolean isColorCode(String text, ColorCodeType... types) {
        char type_marker = text.toCharArray()[0], color_marker = text.toCharArray()[1];

        // if type_marker isn't any kind of accepted color code indicator char, return false
        if (type_marker != '&' && type_marker != '%' && type_marker != ChatColor.COLOR_CHAR)
            return false;

        // figure out the color code's secondary type (color vs. formatting)
        boolean color_color_code = false, formatting_color_code = false;

        // first, check the second char to see if the second char is a color color code
        for (char color_char : COLOR_COLOR_CODE_CHARS)
            if (color_char == color_marker) {
                color_color_code = true;
                break;
            }

        // if it wasn't a color color code, see if it's a formatting color code
        if (!color_color_code) {
            for (char color_char : FORMATTING_COLOR_CODE_CHARS)
                if (color_char == color_marker) {
                    formatting_color_code = true;
                    break;
                }

            // if it wasn't a color color code and it wasn't a formatting color code, it's not any kind of color code
            if (!formatting_color_code)
                return false;
        }

        /* if types is empty, we can treat it as "any ColorCodeType is acceptable", so since we already know that it's some kind of color code by now, if types is empty, just
         * return true */
        if (types.length == 0)
            return true;

        // now check the secondary color code type (formatting vs. color) against the primary type (anti vs. converted vs. unconverted)
        if (type_marker == '&')
            return contains(types, ColorCodeType.BASIC) || color_color_code && contains(types, ColorCodeType.BASIC_COLOR) || formatting_color_code
                    && contains(types, ColorCodeType.BASIC_FORMATTING);
        else if (type_marker == '%')
            return contains(types, ColorCodeType.ANTI) || color_color_code && contains(types, ColorCodeType.ANTI_COLOR) || formatting_color_code
                    && contains(types, ColorCodeType.ANTI_FORMATTING);
        else
            return contains(types, ColorCodeType.CONVERTED) || color_color_code && contains(types, ColorCodeType.CONVERTED_COLOR) || formatting_color_code
                    && contains(types, ColorCodeType.CONVERTED_FORMATTING);
    }

    public enum ColorCodeType {
        CONVERTED_COLOR, CONVERTED_FORMATTING, BASIC_COLOR, BASIC_FORMATTING, ANTI_COLOR, ANTI_FORMATTING, CONVERTED, BASIC, ANTI, COLOR, FORMATTING;
    }

}