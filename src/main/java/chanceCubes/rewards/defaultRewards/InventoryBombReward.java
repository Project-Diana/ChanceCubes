package chanceCubes.rewards.defaultRewards;

import java.util.Random;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import chanceCubes.CCubesCore;

public class InventoryBombReward implements IChanceCubeReward {

    private Random rand = new Random();

    @Override
    public void trigger(World world, int x, int y, int z, EntityPlayer player) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack == null) continue;
            EntityItem ient = new EntityItem(world, x, y, z, stack);
            ient.motionY = rand.nextInt(1) - 0.5;
            ient.delayBeforeCanPickup = 40;
            world.spawnEntityInWorld(ient);
        }
        for (ItemStack stack : player.inventory.armorInventory) {
            if (stack == null) continue;
            EntityItem ient = new EntityItem(world, x, y, z, stack);
            ient.motionY = rand.nextInt(1) - 0.5;
            ient.delayBeforeCanPickup = 40;
            world.spawnEntityInWorld(ient);
        }

        player.inventory.openInventory();

        for (int i = 0; i < player.inventory.mainInventory.length; i++)
            player.inventory.mainInventory[i] = new ItemStack(Blocks.deadbush, 64);

        for (int i = 0; i < player.inventory.armorInventory.length; i++) {
            ItemStack stack = new ItemStack(Blocks.deadbush, 64);
            if (i == 0) {
                stack.setStackDisplayName("ButtonBoy");
                stack.stackSize = 13;
            } else if (i == 1) {
                stack.setStackDisplayName("TheBlackswordsman");
                stack.stackSize = 13;
            }
            player.inventory.armorInventory[i] = stack;
        }

        player.inventory.closeInventory();

        player.addChatMessage(new ChatComponentText("Inventory Bomb!!!!"));

    }

    @Override
    public int getChanceValue() {
        return -65;
    }

    @Override
    public String getName() {
        return CCubesCore.MODID + ":Inventory_Bomb";
    }

}
