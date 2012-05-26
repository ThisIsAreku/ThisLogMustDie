package alexoft.tlmd;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;

public class ColorConverter {

    public static final char ALT_COLOR_CODE = '&';
    private static final Map<ChatColor, String> replacements = new EnumMap<ChatColor, String>(ChatColor.class);
    private static Main plugin;
    private static final ChatColor[] colors = ChatColor.values();

    public ColorConverter(Main plugin) {
        ColorConverter.plugin = plugin;
        replacements.put(ChatColor.BLACK, Ansi.ansi().fg(Ansi.Color.BLACK).toString());
        replacements.put(ChatColor.DARK_BLUE, Ansi.ansi().fg(Ansi.Color.BLUE).toString());
        replacements.put(ChatColor.DARK_GREEN, Ansi.ansi().fg(Ansi.Color.GREEN).toString());
        replacements.put(ChatColor.DARK_AQUA, Ansi.ansi().fg(Ansi.Color.CYAN).toString());
        replacements.put(ChatColor.DARK_RED, Ansi.ansi().fg(Ansi.Color.RED).toString());
        replacements.put(ChatColor.DARK_PURPLE, Ansi.ansi().fg(Ansi.Color.MAGENTA).toString());
        replacements.put(ChatColor.GOLD, Ansi.ansi().fg(Ansi.Color.YELLOW).bold().toString());
        replacements.put(ChatColor.GRAY, Ansi.ansi().fg(Ansi.Color.WHITE).toString());
        replacements.put(ChatColor.DARK_GRAY, Ansi.ansi().fg(Ansi.Color.BLACK).bold().toString());
        replacements.put(ChatColor.BLUE, Ansi.ansi().fg(Ansi.Color.BLUE).bold().toString());
        replacements.put(ChatColor.GREEN, Ansi.ansi().fg(Ansi.Color.GREEN).bold().toString());
        replacements.put(ChatColor.AQUA, Ansi.ansi().fg(Ansi.Color.CYAN).bold().toString());
        replacements.put(ChatColor.RED, Ansi.ansi().fg(Ansi.Color.RED).bold().toString());
        replacements.put(ChatColor.LIGHT_PURPLE, Ansi.ansi().fg(Ansi.Color.MAGENTA).bold().toString());
        replacements.put(ChatColor.YELLOW, Ansi.ansi().fg(Ansi.Color.YELLOW).bold().toString());
        replacements.put(ChatColor.WHITE, Ansi.ansi().fg(Ansi.Color.WHITE).bold().toString());
        replacements.put(ChatColor.MAGIC, Ansi.ansi().a(Attribute.BLINK_SLOW).toString());
        replacements.put(ChatColor.BOLD, Ansi.ansi().a(Attribute.UNDERLINE_DOUBLE).toString());
        replacements.put(ChatColor.STRIKETHROUGH, Ansi.ansi().a(Attribute.STRIKETHROUGH_ON).toString());
        replacements.put(ChatColor.UNDERLINE, Ansi.ansi().a(Attribute.UNDERLINE).toString());
        replacements.put(ChatColor.ITALIC, Ansi.ansi().a(Attribute.ITALIC).toString());
        replacements.put(ChatColor.RESET, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.DEFAULT).toString());
    }

    public static String convertColor(String m) {
        m = ChatColor.translateAlternateColorCodes(ALT_COLOR_CODE, m);
        if (plugin.use_color_codes) {
            return replaceColorCodes(m) + replacements.get(ChatColor.RESET);
        } else {
            return ChatColor.stripColor(m);
        }
    }

    public static String stripColorCodes(String m) {
        return ChatColor.stripColor(m);
    }

    private static String replaceColorCodes(String m) {
        String result = m;
        for (ChatColor color : colors) {
            if (replacements.containsKey(color)) {
                result = result.replaceAll("(?i)" + color.toString(), replacements.get(color));
            } else {
                result = result.replaceAll("(?i)" + color.toString(), "");
            }
        }
        return result;
    }
}