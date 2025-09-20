package moe.ichinomiya.naven.ui.notification;

import lombok.Data;
import moe.ichinomiya.naven.Naven;
import moe.ichinomiya.naven.utils.RenderUtils;
import moe.ichinomiya.naven.utils.SmoothAnimationTimer;
import moe.ichinomiya.naven.utils.font.GlyphPageFontRenderer;

@Data
public class Notification {
    private static final GlyphPageFontRenderer font = Naven.getInstance().getFontManager().siyuan18;

    private final NotificationLevel level;
    private String message;

    private long maxAge, createTime = System.currentTimeMillis();

    private SmoothAnimationTimer widthTimer = new SmoothAnimationTimer(0),
            heightTimer = new SmoothAnimationTimer(0);

    public Notification(NotificationLevel level, String message, long age) {
        this.level = level;
        this.message = message;
        this.maxAge = age;
    }

    public void render() {
        RenderUtils.drawBoundRoundedRect(2, 4, getWidth(), 20, 5, level.getColor());
        font.drawStringWithShadow(message, 6, 7, 0xFFFFFFFF);
    }

    public void renderShader() {
        RenderUtils.drawBoundRoundedRect(2, 4, getWidth(), 20, 5, level.getColor());
    }

    public float getWidth() {
        int stringWidth = font.getStringWidth(message);
        return stringWidth + 12;
    }

    public float getHeight() {
        return 24;
    }
}
