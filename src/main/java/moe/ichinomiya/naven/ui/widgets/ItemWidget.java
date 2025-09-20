package moe.ichinomiya.naven.ui.widgets;

import moe.ichinomiya.naven.Naven;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.protocols.HYTUtils;
import moe.ichinomiya.naven.utils.SmoothAnimationTimer;
import moe.ichinomiya.naven.utils.font.GlyphPageFontRenderer;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemWidget extends DraggableWidget {
    final List<String> items = new ArrayList<>();
    boolean shouldRender = false;
    final SmoothAnimationTimer height = new SmoothAnimationTimer(0), width = new SmoothAnimationTimer(0);
    BooleanValue value;

    public ItemWidget(BooleanValue value) {
        super("Items");
        this.value = value;
    }

    @EventTarget
    public void onUpdate(EventMotion e) {
        items.clear();

        int soupCounts = 0;

        for (int slotIndex = 9; slotIndex < 45; slotIndex++) {
            ItemStack stack = mc.thePlayer.getSlotFromPlayerContainer(slotIndex).getStack();
            if (HYTUtils.isGodAxe(stack) && !items.contains("God Axe")) {
                items.add("God Axe");
            }

            if (HYTUtils.isKBBall(stack) && !items.contains("KB Ball")) {
                items.add("KB Ball");
            }

            if (HYTUtils.isEnchantedGoldenApple(stack) && !items.contains("Enchanted GApple")) {
                items.add("Enchanted GApple");
            }

            if (stack != null && stack.getItem() == Items.ender_pearl && !items.contains("Ender Pearl")) {
                items.add("Ender Pearl");
            }

            if (stack != null && stack.getItem() == Items.mushroom_stew) {
                soupCounts ++;
            }
        }

        if (soupCounts > 0) {
            items.add("Soups x" + soupCounts);
        }

        shouldRender = (!items.isEmpty() || mc.currentScreen instanceof GuiChat) && value.getCurrentValue();

        if (!shouldRender) {
            height.target = 0;
            width.target = 0;
        }
    }

    @Override
    public void renderBody() {
        GlyphPageFontRenderer font = Naven.getInstance().getFontManager().siyuan16;
        font.drawStringWithShadow(name, 2, 2, 0xFFFFFFFF);

        if (shouldRender) {
            height.target = 12;
            width.target = font.getStringWidth(name);

            for (String item : items) {
                font.drawStringWithShadow(item, 2, 2 + height.target, 0xFFFFFFFF);
                width.target = Math.max(width.target, font.getStringWidth(item));
                height.target += 10;
            }
        } else {
            height.target = 0;
            width.target = 0;
        }

        height.update(true);
        width.update(true);
    }

    @Override
    public float getWidth() {
        return width.value + 6;
    }

    @Override
    public float getHeight() {
        return height.value + 6;
    }

    @Override
    public boolean shouldRender() {
        return shouldRender || width.value > 1 || height.value > 1;
    }
}
