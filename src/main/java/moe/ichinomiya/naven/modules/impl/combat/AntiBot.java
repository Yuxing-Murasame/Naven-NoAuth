package moe.ichinomiya.naven.modules.impl.combat;

import moe.ichinomiya.naven.Naven;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.events.impl.EventRespawn;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.utils.TimeHelper;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S02PacketChat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@ModuleInfo(name = "AntiBot", description = "Prevents you from attacking bots", category = Category.COMBAT)
public class AntiBot extends Module {
    BooleanValue noArmor = ValueBuilder.create(this, "No Armor").setDefaultBooleanValue(true).build().getBooleanValue();
    ConcurrentHashMap<String, TimeHelper> respawnMap = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        respawnMap.clear();
    }

    @Override
    public void onDisable() {
        respawnMap.clear();
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        respawnMap.clear();
    }

    public static boolean isBot(Entity entity) {
        AntiBot module = (AntiBot) Naven.getInstance().getModuleManager().getModule(AntiBot.class);

        if (!module.isEnabled()) {
            return false;
        }

        if (!(entity instanceof EntityPlayer)) {
            return false;
        }

        if (module.noArmor.getCurrentValue() && !hasArmor((EntityPlayer) entity)) {
            return true;
        }

        if (module.respawnMap.containsKey(entity.getName())) {
            if (!module.respawnMap.get(entity.getName()).delay(5500)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasArmor(EntityPlayer player) {
        for (int i = 1; i < 5; i++) {
            ItemStack stack = player.getEquipmentInSlot(i);
            if (stack != null) {
                return true;
            }
        }

        return false;
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof S02PacketChat && mc.thePlayer != null) {
            S02PacketChat packet = (S02PacketChat) e.getPacket();
            String text = packet.getChatComponent().getFormattedText();

            if (text != null && (text.startsWith("§r§b起床战争 §r§f>> §r") || text.startsWith("§r§b起床战争§r§7>>§r§f §r"))) {
                String displayName = getDisplayName(text);

                if (displayName != null) {
                    TimeHelper timer = new TimeHelper();
                    respawnMap.put(displayName.replaceAll("\247.", ""), timer);
                    timer.reset();
                }
            }
        }
    }

    private static String getDisplayName(String text) {
        List<String> list = new ArrayList<>();
        for (NetworkPlayerInfo player : mc.thePlayer.sendQueue.getPlayerInfoMap()) {
            if (player.getDisplayName() != null && player.getDisplayName().getFormattedText() != null) {
                String[] split = player.getDisplayName().getFormattedText().split(" \\| ");
                if (split.length >= 2) {
                    String displayName = split[1].replace("\247r", "");
                    if (text.contains(displayName) && text.replace(displayName, "").contains("死")) {
                        list.add(displayName);
                    }
                }
            }
        }

        String displayName = null;
        if (list.size() == 1) {
            displayName = list.get(0);
        } else if (list.size() == 2) {
            displayName = list.get(1);
        }
        return displayName;
    }
}
