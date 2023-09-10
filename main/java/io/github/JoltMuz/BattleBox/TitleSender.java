package io.github.JoltMuz.BattleBox;

import org.bukkit.entity.Player;

public class TitleSender {
	private static final String NMS_VERSION = "net.minecraft.server.v1_8_R3.";

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        try {
            Class<?> packetPlayOutTimes = Class.forName(NMS_VERSION + "PacketPlayOutTitle");
            Class<?> packetPlayOutTitle = Class.forName(NMS_VERSION + "PacketPlayOutTitle");
            Class<?> iChatBaseComponent = Class.forName(NMS_VERSION + "IChatBaseComponent");
            Class<?> chatSerializer = Class.forName(NMS_VERSION + "IChatBaseComponent$ChatSerializer");

            Object timesPacket = packetPlayOutTimes.getConstructor(int.class, int.class, int.class)
                    .newInstance(fadeIn, stay, fadeOut);

            Object titleComponent = chatSerializer.getDeclaredMethod("a", String.class)
                    .invoke(null, "{\"text\": \"" + replaceColorCodes(title) + "\"}");

            Object subtitleComponent = chatSerializer.getDeclaredMethod("a", String.class)
                    .invoke(null, "{\"text\": \"" + replaceColorCodes(subtitle) + "\"}");

            Object titlePacket = packetPlayOutTitle.getConstructor(
                    packetPlayOutTitle.getDeclaredClasses()[0], iChatBaseComponent)
                    .newInstance(packetPlayOutTitle.getDeclaredClasses()[0].getField("TITLE").get(null), titleComponent);

            Object subtitlePacket = packetPlayOutTitle.getConstructor(
                    packetPlayOutTitle.getDeclaredClasses()[0], iChatBaseComponent)
                    .newInstance(packetPlayOutTitle.getDeclaredClasses()[0].getField("SUBTITLE").get(null), subtitleComponent);

            sendPacket(player, timesPacket);
            sendPacket(player, titlePacket);
            sendPacket(player, subtitlePacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String replaceColorCodes(String input) {
        try {
            if (input == null) {
                return null;
            }
            return input.replaceAll("&([0-9a-fk-or])", "\u00A7$1");
        } catch (NullPointerException e) {
            e.printStackTrace();
            return input;
        }
    }

    private static void sendPacket(Player player, Object packet) throws Exception {
        Object handle = player.getClass().getMethod("getHandle").invoke(player);
        Object connection = handle.getClass().getField("playerConnection").get(handle);
        connection.getClass().getMethod("sendPacket", Class.forName(NMS_VERSION + "Packet"))
                .invoke(connection, packet);
    }

}
