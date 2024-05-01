package chanceCubes.items;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import chanceCubes.CCubesCore;
import chanceCubes.config.CCubesSettings;

public class ItemChancePendant extends BaseChanceCubesItem {

    private int chanceIncrease;
    private String itemName = "";

    public ItemChancePendant(int tier, int chancebonus) {
        super("Chance_Cube_Unnamed");
        itemName = "chancePendantTier" + tier;
        this.setUnlocalizedName(itemName);
        this.setTextureName(CCubesCore.MODID + ":PendantT" + tier);
        this.setMaxStackSize(1);
        this.setMaxDamage(CCubesSettings.pendantUses);
        super.showDurabilityBar(new ItemStack(this));
        chanceIncrease = chancebonus;
        super.addLore("Increases the chance of Chance Cubes by:");
        super.addLore("      +" + chanceIncrease + " when the block is broken");
        super.addLore("Only needs to be in the players inventory to work");
        super.addLore("Note, this is NOT a percentage increase.");
    }

    public int getChanceIncrease() {
        return chanceIncrease;
    }

    public void damage(ItemStack stack) {
        stack.setItemDamage(stack.getItemDamage() + 1);
    }

    public boolean getIsRepairable(ItemStack stack, ItemStack repairStack) {
        if (stack.getItem() instanceof ItemChancePendant && repairStack.getItem()
            .equals(Blocks.lapis_block)) return true;
        return false;
    }

    public String getItemName() {
        return this.itemName;
    }
}
