package moe.ichinomiya.naven.ui.widgets;

import moe.ichinomiya.naven.Naven;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.utils.RenderUtils;
import moe.ichinomiya.naven.utils.SmoothAnimationTimer;
import moe.ichinomiya.naven.utils.StencilUtils;
import moe.ichinomiya.naven.utils.TimeHelper;
import moe.ichinomiya.naven.utils.font.FontManager;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;

import java.awt.*;

public class TargetHUDWidget extends DraggableWidget {
    BooleanValue value;

    private EntityPlayer entity;
    private final TimeHelper timer = new TimeHelper();
    private final SmoothAnimationTimer animation = new SmoothAnimationTimer(0, 0.2f);
    private final SmoothAnimationTimer targetAnimation = new SmoothAnimationTimer(0, 0.3f);
    int rgb = new Color(23, 22, 38, 200).getRGB();

    float animatedWidth, animatedHeight;
    int width;

    public TargetHUDWidget(BooleanValue value) {
        super("Target HUD");
        this.value = value;
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getType() == EventType.SEND) {
            if (e.getPacket() instanceof C02PacketUseEntity) {
                C02PacketUseEntity packet = (C02PacketUseEntity) e.getPacket();
                if (packet.getAction() == C02PacketUseEntity.Action.ATTACK) {
                    Entity entity = packet.getEntityFromWorld(mc.theWorld);
                    if (entity instanceof EntityPlayer) {
                        if (this.entity != entity) {
                            this.entity = (EntityPlayer) entity;
                            float percent = this.entity.getHealth() / this.entity.getMaxHealth();
                            animation.target = animation.value = percent;
                        }

                        targetAnimation.target = 1;
                        timer.reset();
                    }
                }
            }
        }
    }

    @EventTarget
    public void onUpdate(EventMotion e) {
        if (mc.currentScreen instanceof GuiChat) {
            this.entity = mc.thePlayer;
            float percent = this.entity.getHealth() / this.entity.getMaxHealth();
            animation.target = animation.value = percent;
            targetAnimation.target = 1;
            timer.reset();
        }

        if (targetAnimation.target > 0 && timer.delay(1500)) {
            targetAnimation.target = 0;
        }

        if (targetAnimation.target > 0 && (entity != null && (entity.isDead || entity.getHealth() <= 0))) {
            targetAnimation.target = 0;
        }
    }

    @Override
    public void renderBackground() {

    }

    @Override
    public void renderShader() {
        if (targetAnimation.value > 0.08) {
            RenderUtils.drawBoundRoundedRect((width - animatedWidth) / 2f, (56 - animatedHeight) / 2f, animatedWidth, animatedHeight, 6, 0xFFFFFFFF);
        }
    }

    @Override
    public void renderBody() {
        targetAnimation.update(true);

        if (targetAnimation.value > 0.08) {
            String name = entity.getDisplayName().getUnformattedText();

            FontManager fontManager = Naven.getInstance().getFontManager();
            width = Math.max(120, fontManager.siyuan18.getStringWidth(name) + 53);

            StencilUtils.write(false);
            animatedWidth = width * targetAnimation.value;
            animatedHeight = 56 * targetAnimation.value;
            RenderUtils.drawBoundRoundedRect((width - animatedWidth) / 2f, (56 - animatedHeight) / 2f, animatedWidth, animatedHeight, 6, 0xFFFFFFFF);
            StencilUtils.erase(true);
            RenderUtils.drawBoundRoundedRect(0, 0, width, 56, 6, rgb);
            for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
                if (mc.theWorld.getPlayerEntityByUUID(info.getGameProfile().getId()) == entity) {
                    mc.getTextureManager().bindTexture(info.getLocationSkin());
                    GlStateManager.color(1, 1, 1);
                    RenderUtils.drawScaledCustomSizeModalRect(9, 10, 8.0f, 8.0f, 8, 8, 32, 32, 64.0f, 64.0f);
                    if (entity.isWearing(EnumPlayerModelParts.HAT)) {
                        RenderUtils.drawScaledCustomSizeModalRect(9, 10, 40.0f, 8.0f, 8, 8, 32, 32, 64.0f, 64.0f);
                    }
                    GlStateManager.bindTexture(0);
                    break;
                }
            }
            fontManager.siyuan18.drawStringWithShadow(name, 46, 7, 0xFFFFFFFF);
            fontManager.opensans14.drawStringWithShadow("Health: " + Math.round(entity.getHealth() * 10) / 10f + (entity.getAbsorptionAmount() > 0 ? "+" + Math.round(entity.getAbsorptionAmount() * 10) / 10f : ""), 46, 19, 0xFFFFFFFF);

            String blocking;
            if (entity.getHeldItem() != null && entity.getHeldItem().getItem() instanceof ItemSword) {
                blocking = entity.serverUsingItem ? "Blocking (" + Math.round(entity.getBlockRate() * 100) + "%)" : "Not Blocking (" + Math.round(entity.getBlockRate() * 100) + "%)";
            } else {
                blocking = entity.serverUsingItem ? "Using" : "Not Using";
            }

            fontManager.opensans14.drawStringWithShadow("Distance: " + Math.round(mc.thePlayer.getDistanceToEntity(entity) * 10) / 10f, 46, 27, 0xFFFFFFFF);
            fontManager.opensans14.drawStringWithShadow(blocking, 46, 35, 0xFFFFFFFF);

            animation.target = entity.getHealth() / entity.getMaxHealth();
            animation.update(true);
            RenderUtils.drawBoundRoundedRect(8, 47, (width - 16) * animation.value, 4, 2, new Color(148, 42, 43).getRGB());
            StencilUtils.dispose();
        }
    }

    @Override
    public float getWidth() {
        return animatedWidth;
    }

    @Override
    public float getHeight() {
        return animatedHeight;
    }

    @Override
    public boolean shouldRender() {
        return value.getCurrentValue();
    }
}
