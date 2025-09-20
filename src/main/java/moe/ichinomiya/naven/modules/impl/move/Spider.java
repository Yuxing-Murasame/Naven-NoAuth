package moe.ichinomiya.naven.modules.impl.move;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;

import static moe.ichinomiya.naven.events.api.types.Priority.LOWEST;

@ModuleInfo(name = "Spider", description = "Allows you to climb up walls", category = Category.MOVEMENT)
public class Spider extends Module {
    public static boolean shouldCancelClick = false;
    List<BlockPos> positions = new ArrayList<>();

    @Override
    public void onDisable() {
        shouldCancelClick = false;
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            shouldCancelClick = false;
            for (BlockPos position : positions) {
                shouldCancelClick = true;
                for (int i = 0; i < 2; i++) {
                    mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, position.up(i), EnumFacing.DOWN));
                    mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, position.up(i), EnumFacing.DOWN));
                }
            }

            positions.clear();
        }
    }

    @EventTarget
    public void onBB(EventBondingBoxSet e) {
        if (e.getPos().getY() > mc.thePlayer.posY && e.getBoundingBox() != null) {
            if (e.getBlock() != Blocks.bedrock && e.getBlock() != Blocks.barrier) {
                positions.add(new BlockPos(e.getBoundingBox().minX, e.getBoundingBox().minY, e.getBoundingBox().minZ));
                e.setCancelled(true);
            }
        }
    }
}
