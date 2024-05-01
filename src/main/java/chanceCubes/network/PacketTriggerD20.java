package chanceCubes.network;

import net.minecraft.tileentity.TileEntity;

import chanceCubes.CCubesCore;
import chanceCubes.tileentities.TileChanceD20;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketTriggerD20 implements IMessage {

    public int x;
    public int y;
    public int z;

    public PacketTriggerD20() {}

    public PacketTriggerD20(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
    }

    public static final class Handler implements IMessageHandler<PacketTriggerD20, IMessage> {

        @Override
        public IMessage onMessage(PacketTriggerD20 message, MessageContext ctx) {
            TileEntity ico;

            if ((ico = CCubesCore.proxy.getClientPlayer().worldObj.getTileEntity(message.x, message.y, message.z))
                != null)
                if (ico instanceof TileChanceD20)
                    ((TileChanceD20) ico).startBreaking(CCubesCore.proxy.getClientPlayer());

            return null;

        }
    }

}
