package moe.ichinomiya.naven.utils.mousetweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;

import java.util.List;

public class DeobfuscationLayer {

    protected static Minecraft mc = Minecraft.getMinecraft();

    protected static GuiScreen getCurrentScreen() {
        return mc.currentScreen;
    }

    protected static boolean isGuiContainer(GuiScreen guiScreen) {
        return (guiScreen instanceof GuiContainer);
    }

    protected static boolean isGuiContainerCreative(GuiScreen guiScreen) {
        return (guiScreen instanceof GuiContainerCreative);
    }

    protected static boolean isVanillaCraftingOutputSlot(Container container, Slot slot) {
        return ((container instanceof ContainerWorkbench) && (getSlotNumber(slot) == 0))
                || ((container instanceof ContainerPlayer) && (getSlotNumber(slot) == 0))
                || ((container instanceof ContainerFurnace) && (getSlotNumber(slot) == 2))
                || ((container instanceof ContainerRepair) && (getSlotNumber(slot) == 2));
    }

    protected static GuiContainer asGuiContainer(GuiScreen guiScreen) {
        if (guiScreen instanceof GuiContainer)
            return (GuiContainer) guiScreen;

        return null;
    }

    protected static Container asContainer(Object obj) {
        return (Container) obj;
    }

    protected static Slot asSlot(Object obj) {
        return (Slot) obj;
    }

    protected static Container getContainer(GuiContainer guiContainer) {
        return guiContainer.inventorySlots;
    }

    protected static List<?> getSlots(Container container) {
        return container.inventorySlots;
    }

    protected static Slot getSlot(Container container, int index) {
        return (Slot) (getSlots(container).get(index));
    }

    protected static ItemStack getSlotStack(Slot slot) {
        return (slot == null) ? null : slot.getStack();
    }

    protected static int getWindowId(Container container) {
        return container.windowId;
    }

    protected static void windowClick(int windowId, int slotNumber, int mouseButton, int shiftPressed) {
        // if (slotNumber != -1) {
        getPlayerController().windowClick(windowId, slotNumber, mouseButton, shiftPressed, getThePlayer());
        // }
    }

    protected static EntityPlayerSP getThePlayer() {
        return mc.thePlayer;
    }

    protected static InventoryPlayer getInventoryPlayer() {
        return getThePlayer().inventory;
    }

    protected static int getDisplayWidth() {
        return mc.displayWidth;
    }

    protected static int getDisplayHeight() {
        return mc.displayHeight;
    }

    protected static ItemStack getStackOnMouse() {
        return getInventoryPlayer().getItemStack();
    }

    protected static PlayerControllerMP getPlayerController() {
        return mc.playerController;
    }

    protected static int getSlotNumber(Slot slot) {
        return slot.slotNumber;
    }

    protected static int getItemStackSize(ItemStack itemStack) {
        return itemStack.stackSize;
    }

    protected static int getMaxItemStackSize(ItemStack itemStack) {
        return itemStack.getMaxStackSize();
    }

    protected static ItemStack copyItemStack(ItemStack itemStack) {
        return (itemStack == null) ? null : itemStack.copy();
    }

    protected static boolean areStacksCompatible(ItemStack itemStack1, ItemStack itemStack2) {
        return (itemStack1 == null || itemStack2 == null) || itemStack1.isItemEqual(itemStack2);
    }

    protected static boolean isMouseOverSlot(GuiContainer guiContainer, Slot slot) {
        return guiContainer.isMouseOverSlot(slot,
                getRequiredMouseX(),
                getRequiredMouseY());
    }

    protected static Slot getSelectedSlot(GuiContainer guiContainer, Container container, int slotCount) {
        for (int i = 0; i < slotCount; i++) {
            Slot slot = getSlot(container, i);
            if (isMouseOverSlot(guiContainer, slot))
                return slot;
        }

        return null;
    }

    /**
     * Disables the vanilla RMB drag mechanic in the given GuiContainer.
     * If your guiContainer is based on the vanilla GuiContainer, you can use this method to disable the RMB drag.
     *
     * @param guiContainer The guiContainer to disable RMB drag in.
     */
    public static void disableVanillaRMBDrag(GuiContainer guiContainer) {
        guiContainer.ignoreMouseUp = true;
        guiContainer.dragSplitting = false;
    }

    protected static int getRequiredMouseX() {
        ScaledResolution var8 = new ScaledResolution(mc);
        int var9 = var8.getScaledWidth();
        return (Mouse.getX() * var9) / getDisplayWidth();
    }

    protected static int getRequiredMouseY() {
        ScaledResolution var8 = new ScaledResolution(mc);
        int var10 = var8.getScaledHeight();
        return var10 - ((Mouse.getY() * var10) / getDisplayHeight()) - 1;
    }
}
