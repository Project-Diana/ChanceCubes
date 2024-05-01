package chanceCubes.rewards.defaultRewards;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import chanceCubes.CCubesCore;

public class RottenFoodReward implements IChanceCubeReward {

    @Override
    public void trigger(World world, int x, int y, int z, EntityPlayer player) {
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            ItemStack stack = player.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemFood)
                player.inventory.mainInventory[i] = new ItemStack(Items.rotten_flesh, stack.stackSize);
        }

        player.addChatMessage(new ChatComponentText("Ewwww it's all rotten"));

    }

    @Override
    public int getChanceValue() {
        return -30;
    }

    @Override
    public String getName() {
        return CCubesCore.MODID + ":Rotten_Food";
    }

}
