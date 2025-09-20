package moe.ichinomiya.naven.modules.impl.move;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventRespawn;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@ModuleInfo(name = "FastWeb", description = "Allows you to walk faster on webs", category = Category.MOVEMENT)
public class FastWeb extends Module {
    public static boolean shouldCancelWeb = false;

    @Override
    public void onEnable() {
        shouldCancelWeb = false;
    }

    @Override
    public void onDisable() {
        shouldCancelWeb = false;
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        shouldCancelWeb = false;
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE && mc.thePlayer != null && mc.thePlayer.ticksExisted > 50) {
            shouldCancelWeb = false;

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 2; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos pos = new BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);
                        if (mc.theWorld.getBlockState(pos).getBlock() == Blocks.web) {
                            shouldCancelWeb = true;
                            mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, pos, EnumFacing.UP));
                            mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.UP));
                        }
                    }
                }
            }
        }
    }
}
