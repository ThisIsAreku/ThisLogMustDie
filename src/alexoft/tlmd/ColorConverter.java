package alexoft.tlmd;

import java.util.EnumMap;
import java.util.Map;

import jline.ANSIBuffer.ANSICodes;
import jline.Terminal;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.CraftServer;

public class ColorConverter {
	public static final char COLOR_CODE = '&';
	private final Map<ChatColor, String> replacements = new EnumMap<ChatColor, String>(
			ChatColor.class);
	private final ChatColor[] colors = ChatColor.values();
	private final Terminal terminal;
	private Main plugin;
	private static ColorConverter instance;

	public ColorConverter(Main plugin) {
		instance = this;
		this.plugin = plugin;
		this.terminal = ((CraftServer) plugin.getServer()).getReader()
				.getTerminal();

		replacements.put(ChatColor.BLACK, ANSICodes.attrib(0));
		replacements.put(ChatColor.DARK_BLUE, ANSICodes.attrib(34));
		replacements.put(ChatColor.DARK_GREEN, ANSICodes.attrib(32));
		replacements.put(ChatColor.DARK_AQUA, ANSICodes.attrib(36));
		replacements.put(ChatColor.DARK_RED, ANSICodes.attrib(31));
		replacements.put(ChatColor.DARK_PURPLE, ANSICodes.attrib(35));
		replacements.put(ChatColor.GOLD, ANSICodes.attrib(33));
		replacements.put(ChatColor.GRAY, ANSICodes.attrib(37));
		replacements.put(ChatColor.DARK_GRAY, ANSICodes.attrib(0));
		replacements.put(ChatColor.BLUE, ANSICodes.attrib(34));
		replacements.put(ChatColor.GREEN, ANSICodes.attrib(32));
		replacements.put(ChatColor.AQUA, ANSICodes.attrib(36));
		replacements.put(ChatColor.RED, ANSICodes.attrib(31));
		replacements.put(ChatColor.LIGHT_PURPLE, ANSICodes.attrib(35));
		replacements.put(ChatColor.YELLOW, ANSICodes.attrib(33));
		replacements.put(ChatColor.WHITE, ANSICodes.attrib(37));
	}

	public static String convertColor(String message) {
		String result = ChatColor.translateAlternateColorCodes(COLOR_CODE,
				message);

		if (instance.terminal.isANSISupported() && instance.plugin.use_color_codes) {
			for (ChatColor color : instance.colors) {
				if (instance.replacements.containsKey(color)) {
					result = result.replaceAll(color.toString(),
							instance.replacements.get(color));
				} else {
					result = result.replaceAll(color.toString(), "");
				}
			}
			result += instance.replacements.get(ChatColor.WHITE);
		} else {
			result = ChatColor.stripColor(result);
		}
		return result;
	}
}
