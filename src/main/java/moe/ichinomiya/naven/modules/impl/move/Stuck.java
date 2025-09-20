package moe.ichinomiya.naven.modules.impl.move;

import moe.ichinomiya.naven.Naven;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.api.types.Priority;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventMove;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.events.impl.EventRespawn;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.modules.impl.combat.Aura;
import moe.ichinomiya.naven.modules.impl.combat.ThrowableAimAssist;
import moe.ichinomiya.naven.ui.cooldown.CooldownBar;
import moe.ichinomiya.naven.utils.ChatUtils;
import moe.ichinomiya.naven.utils.EntityWatcher;
import moe.ichinomiya.naven.utils.RotationManager;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;

@ModuleInfo(name = "Stuck", description = "Stuck in air!", category = Category.MOVEMENT)
public class Stuck extends Module {
    int stage = 0;
    Packet<?> packet;
    float lastYaw, lastPitch;
    boolean tryDisable = false;

    private final CooldownBar alert = new CooldownBar(1000, "Bad Packets E") {
        @Override
        public float getState() {
            return 1F - (EntityWatcher.fullPlayerPlayerCounter / 20f);
        }
    };


    @Override
    public void onEnable() {
        stage = 0;
        packet = null;

        lastYaw = RotationManager.rotations.x;
        lastPitch = RotationManager.rotations.y;

        tryDisable = false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mc.thePlayer == null) {
            return;
        }

        if (enabled) {
            super.setEnabled(true);
        } else {
            if (stage == 3) {
                super.setEnabled(false);
            } else {
                tryDisable = true;
            }
        }
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        Aura.disableHelper.reset();
        Module blink = Naven.getInstance().getModuleManager().getModule(Blink.class);

        if (blink.isEnabled()) {
            blink.toggle();
            return;
        }

        Module scaffold = Naven.getInstance().getModuleManager().getModule(Scaffold.class);

        if (scaffold.isEnabled()) {
            scaffold.toggle();
            return;
        }

        if (e.getType() == EventType.PRE) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionY = 0;
            mc.thePlayer.motionZ = 0;

            alert.setCreateTime(System.currentTimeMillis());
            Naven.getInstance().getCooldownBarManager().addBar(alert);

            if (stage == 1) {
                stage = 2;

                float rotationYaw = mc.thePlayer.rotationYaw;
                float rotationPitch = mc.thePlayer.rotationPitch;

                if (shouldRotate() && (lastYaw != rotationYaw || lastPitch != rotationPitch)) {
                    lastYaw = rotationYaw;
                    lastPitch = rotationPitch;
                }

                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
            }

            if (tryDisable) {
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + 1337, mc.thePlayer.posY, mc.thePlayer.posZ + 1337, mc.thePlayer.onGround));
                tryDisable = false;
            }
        } else {
            if (EntityWatcher.fullPlayerPlayerCounter >= 20) {
                toggle();
            }
        }
    }

    private boolean shouldRotate() {
        if (packet instanceof C08PacketPlayerBlockPlacement) {
            C08PacketPlayerBlockPlacement blockPlacement = (C08PacketPlayerBlockPlacement) packet;

            if (blockPlacement.getPlacedBlockDirection() == 255 && blockPlacement.getPosition().equals(C08PacketPlayerBlockPlacement.field_179726_a)) {
                if (blockPlacement.getStack() != null) {
                    if (blockPlacement.getStack().getItem() instanceof ItemFood || blockPlacement.getStack().getItem() instanceof ItemBow) {
                        return false;
                    }
                }
            }

            return true;
        } else if (packet instanceof C07PacketPlayerDigging) {
            C07PacketPlayerDigging playerDigging = (C07PacketPlayerDigging) packet;

            if (playerDigging.getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) {
                    return true;
                }
            }
            return false;
        }

        return false;
    }

    @EventTarget
    public void onMove(EventMove e) {
        if (stage <= 3) {
            e.setX(0);
            e.setY(0);
            e.setZ(0);
        }
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        stage = 3;
        packet = null;
        toggle();
    }

    @EventTarget(Priority.HIGH)
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof C00Handshake || e.getPacket() instanceof C00PacketLoginStart || e.getPacket() instanceof C00PacketServerQuery || e.getPacket() instanceof C01PacketPing || e.getPacket() instanceof C01PacketEncryptionResponse || e.getPacket() instanceof C01PacketChatMessage || e.getPacket() instanceof C14PacketTabComplete) {
            return;
        }

        if (e.getPacket() instanceof C16PacketClientStatus) {
            C16PacketClientStatus ePacket = (C16PacketClientStatus) e.getPacket();
            if (ePacket.getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
                return;
            }
        }

        if (e.getPacket() instanceof C08PacketPlayerBlockPlacement || e.getPacket() instanceof C07PacketPlayerDigging) {
            packet = e.getPacket();
            stage = 1;
            e.setCancelled(true);
        } else if (e.getPacket() instanceof S08PacketPlayerPosLook) {
            stage = 3;
            toggle();
        }
    }
}
