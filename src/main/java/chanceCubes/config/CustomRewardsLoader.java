package chanceCubes.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import chanceCubes.CCubesCore;
import chanceCubes.registry.ChanceCubeRegistry;
import chanceCubes.registry.GiantCubeRegistry;
import chanceCubes.rewards.defaultRewards.BasicReward;
import chanceCubes.rewards.rewardparts.ChestChanceItem;
import chanceCubes.rewards.rewardparts.CommandPart;
import chanceCubes.rewards.rewardparts.EntityPart;
import chanceCubes.rewards.rewardparts.ExpirencePart;
import chanceCubes.rewards.rewardparts.ItemPart;
import chanceCubes.rewards.rewardparts.MessagePart;
import chanceCubes.rewards.rewardparts.OffsetBlock;
import chanceCubes.rewards.rewardparts.OffsetTileEntity;
import chanceCubes.rewards.rewardparts.ParticlePart;
import chanceCubes.rewards.rewardparts.PotionPart;
import chanceCubes.rewards.rewardparts.SoundPart;
import chanceCubes.rewards.type.BlockRewardType;
import chanceCubes.rewards.type.ChestRewardType;
import chanceCubes.rewards.type.CommandRewardType;
import chanceCubes.rewards.type.EntityRewardType;
import chanceCubes.rewards.type.ExperienceRewardType;
import chanceCubes.rewards.type.IRewardType;
import chanceCubes.rewards.type.ItemRewardType;
import chanceCubes.rewards.type.MessageRewardType;
import chanceCubes.rewards.type.ParticleEffectRewardType;
import chanceCubes.rewards.type.PotionRewardType;
import chanceCubes.rewards.type.SoundRewardType;
import chanceCubes.util.CustomEntry;
import chanceCubes.util.HTTPUtil;
import chanceCubes.util.SchematicUtil;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;

public class CustomRewardsLoader {

    public static CustomRewardsLoader instance;

    private File folder;
    private File source;
    private static JsonParser json;

    public CustomRewardsLoader(File folder, File source) {
        instance = this;
        this.folder = folder;
        this.source = source;
        json = new JsonParser();

        CustomSoundsLoader customSounds = new CustomSoundsLoader(
            folder,
            new File(folder.getAbsolutePath() + "/CustomSounds-Resourcepack"),
            "Chance Cubes Resource Pack");
        customSounds.addCustomSounds();
        customSounds.assemble();
    }

    public void loadCustomRewards() {
        for (File f : folder.listFiles()) {
            if (!f.isFile() || !f.getName()
                .contains(".json")) continue;
            if (f.getName()
                .substring(
                    f.getName()
                        .indexOf("."))
                .equalsIgnoreCase(".json")) {
                JsonElement fileJson;
                try {
                    CCubesCore.logger.log(Level.INFO, "Loading custom rewards file " + f.getName());
                    fileJson = json.parse(new FileReader(f));
                } catch (Exception e) {
                    CCubesCore.logger
                        .log(Level.ERROR, "Unable to parse the file " + f.getName() + ". Skipping file loading.");
                    CCubesCore.logger.log(Level.ERROR, "Parse Error: " + e.getMessage());
                    continue;
                }

                for (Entry<String, JsonElement> reward : fileJson.getAsJsonObject()
                    .entrySet()) {
                    CustomEntry<BasicReward, Boolean> parsedReward = this.parseReward(reward);
                    BasicReward basicReward = parsedReward.getKey();
                    if (basicReward == null) {
                        CCubesCore.logger.log(
                            Level.ERROR,
                            "Seems your reward is setup incorrectly, or is disabled for this version of minecraft with a depedency, and Chance Cubes was not able to load the reward "
                                + reward.getKey()
                                + " for the file "
                                + f.getName());
                        continue;
                    }
                    if (parsedReward.getValue()) GiantCubeRegistry.INSTANCE.registerReward(basicReward);
                    else ChanceCubeRegistry.INSTANCE.registerReward(basicReward);
                }
                CCubesCore.logger.log(Level.INFO, "Loaded custom rewards file " + f.getName());
            }
        }
    }

    public void fetchRemoteInfo() {
        String rewardURL = "https://api.theprogrammingturkey.com/chance_cubes/ChanceCubesAPI.php";

        try {
            String today = new SimpleDateFormat("MM/dd").format(new Date());
            // String today = "12/25";
            JsonObject json = HTTPUtil
                .getWebFile(
                    rewardURL,
                    new CustomEntry<String, String>("version", CCubesCore.VERSION),
                    new CustomEntry<String, String>("date", today))
                .getAsJsonObject();
            this.loadDisabledRewards(
                json.get("Disabled Rewards")
                    .getAsJsonArray());
            this.loadHolidayRewards(json.get("Holiday Rewards"));
            System.out.println(json.toString());

        } catch (Exception e) {
            CCubesCore.logger.log(Level.ERROR, "Failed to fetch remote information for the mod!");
            e.printStackTrace();
            return;
        }
    }

    private void loadHolidayRewards(JsonElement json) {
        if (!CCubesSettings.holidayRewards || !json.isJsonObject()) return;

        JsonObject holidays = json.getAsJsonObject();
        if (holidays.has("Texture") && !(holidays.get("Texture") instanceof JsonNull)) {
            CCubesSettings.hasHolidayTexture = true;
            CCubesSettings.holidayTextureName = holidays.get("Texture")
                .getAsString();
        } else {
            CCubesSettings.hasHolidayTexture = false;
            CCubesSettings.holidayTextureName = "";
        }

        if (!CCubesSettings.holidayRewardTriggered) {
            if (holidays.has("Holiday") && !(holidays.get("Holiday") instanceof JsonNull)
                && holidays.has("Reward")
                && !(holidays.get("Reward") instanceof JsonNull)) {
                String holidayName = holidays.get("Holiday")
                    .getAsString();
                BasicReward basicReward = this
                    .parseReward(new CustomEntry<String, JsonElement>(holidayName, holidays.get("Reward")))
                    .getKey();
                if (basicReward != null) {
                    CCubesSettings.doesHolidayRewardTrigger = true;
                    CCubesSettings.holidayReward = basicReward;
                    CCubesCore.logger.log(Level.ERROR, "Custom holiday reward \"" + holidayName + "\" loaded!");
                } else {
                    CCubesCore.logger
                        .log(Level.ERROR, "Failed to load the Custom holiday reward \"" + holidayName + "\"!");
                }
            }
        }
    }

    private void loadDisabledRewards(JsonArray disabledRewards) {
        if (CCubesSettings.disabledRewards) {
            for (JsonElement reward : disabledRewards) {
                boolean removed = ChanceCubeRegistry.INSTANCE.unregisterReward(reward.getAsString());
                if (!removed) removed = GiantCubeRegistry.INSTANCE.unregisterReward(reward.getAsString());
                CCubesCore.logger.log(
                    Level.WARN,
                    "The reward " + reward.getAsString()
                        + " has been disabled by the mod author due to a bug or some other reason.");
            }
        }
    }

    public CustomEntry<BasicReward, Boolean> parseReward(Entry<String, JsonElement> reward) {
        List<IRewardType> rewards = new ArrayList<IRewardType>();
        JsonObject rewardElements = reward.getValue()
            .getAsJsonObject();
        int chance = 0;
        boolean isGiantCubeReward = false;
        for (Entry<String, JsonElement> rewardElement : rewardElements.entrySet()) {
            if (rewardElement.getKey()
                .equalsIgnoreCase("chance")) {
                chance = rewardElement.getValue()
                    .getAsInt();
                continue;
            } else if (rewardElement.getKey()
                .equalsIgnoreCase("dependencies")) {
                    boolean gameversion = false;
                    boolean versionUsed = false;
                    for (Entry<String, JsonElement> dependencies : rewardElement.getValue()
                        .getAsJsonObject()
                        .entrySet()) {
                        if (dependencies.getKey()
                            .equalsIgnoreCase("mod")) {
                            if (!Loader.isModLoaded(
                                dependencies.getValue()
                                    .getAsString()))
                                return null;
                        } else if (dependencies.getKey()
                            .equalsIgnoreCase("mcVersion")) {
                                versionUsed = true;
                                String[] versionsToCheck = dependencies.getValue()
                                    .getAsString()
                                    .split(",");
                                for (String toCheckV : versionsToCheck) {
                                    String currentMCV = CCubesCore.gameVersion;
                                    if (toCheckV.contains("*")) {
                                        currentMCV = currentMCV.substring(0, currentMCV.lastIndexOf("."));
                                        toCheckV = toCheckV.substring(0, toCheckV.lastIndexOf("."));
                                    }
                                    if (currentMCV.equalsIgnoreCase(toCheckV)) gameversion = true;
                                }
                            }
                    }
                    if (!gameversion && versionUsed) return null;
                    continue;
                } else if (rewardElement.getKey()
                    .equalsIgnoreCase("isGiantCubeReward")) {
                        isGiantCubeReward = rewardElement.getValue()
                            .getAsBoolean();
                    }

            try {
                JsonArray rewardTypes = rewardElement.getValue()
                    .getAsJsonArray();
                if (rewardElement.getKey()
                    .equalsIgnoreCase("Item")) this.loadItemReward(rewardTypes, rewards);
                else if (rewardElement.getKey()
                    .equalsIgnoreCase("Block")) this.loadBlockReward(rewardTypes, rewards);
                else if (rewardElement.getKey()
                    .equalsIgnoreCase("Message")) this.loadMessageReward(rewardTypes, rewards);
                else if (rewardElement.getKey()
                    .equalsIgnoreCase("Command")) this.loadCommandReward(rewardTypes, rewards);
                else if (rewardElement.getKey()
                    .equalsIgnoreCase("Entity")) this.loadEntityReward(rewardTypes, rewards);
                else if (rewardElement.getKey()
                    .equalsIgnoreCase("Experience")) this.loadExperienceReward(rewardTypes, rewards);
                else if (rewardElement.getKey()
                    .equalsIgnoreCase("Potion")) this.loadPotionReward(rewardTypes, rewards);
                else if (rewardElement.getKey()
                    .equalsIgnoreCase("Schematic")) this.loadSchematicReward(rewardTypes, rewards);
                else if (rewardElement.getKey()
                    .equalsIgnoreCase("Sound")) this.loadSoundReward(rewardTypes, rewards);
                else if (rewardElement.getKey()
                    .equalsIgnoreCase("Chest")) this.loadChestReward(rewardTypes, rewards);
                else if (rewardElement.getKey()
                    .equalsIgnoreCase("Particle")) this.loadParticleReward(rewardTypes, rewards);
            } catch (Exception ex) {
                CCubesCore.logger
                    .log(Level.ERROR, "Failed to load a custom reward for some reason. I will try better next time.");
                CCubesCore.logger.log(Level.ERROR, ex.getMessage());
                // ex.printStackTrace();
            }
        }
        return new CustomEntry<BasicReward, Boolean>(
            new BasicReward(reward.getKey(), chance, rewards.toArray(new IRewardType[rewards.size()])),
            isGiantCubeReward);
    }

    public List<IRewardType> loadItemReward(JsonArray rawReward, List<IRewardType> rewards) {
        List<ItemPart> items = new ArrayList<ItemPart>();
        for (JsonElement fullelement : rawReward) {
            String itemInfo = fullelement.getAsJsonObject()
                .get("item")
                .getAsJsonObject()
                .get("id")
                .getAsString();
            JsonElement element = fullelement.getAsJsonObject()
                .get("item")
                .getAsJsonObject();
            if (itemInfo.contains(":")) {
                String mod = itemInfo.substring(0, itemInfo.indexOf(":"));
                String name = itemInfo.substring(itemInfo.indexOf(":") + 1);
                int id = Item.getIdFromItem(GameRegistry.findItem(mod, name));
                element.getAsJsonObject()
                    .addProperty("id", id);
            }

            ItemPart stack;

            try {
                String jsonEdited = this.removedKeyQuotes(element.toString());
                NBTBase nbtbase = JsonToNBT.func_150315_a(jsonEdited);

                if (!(nbtbase instanceof NBTTagCompound)) {
                    CCubesCore.logger.log(Level.ERROR, "Failed to convert the JSON to NBT for: " + element.toString());
                    continue;
                } else {
                    ItemStack itemstack = ItemStack.loadItemStackFromNBT((NBTTagCompound) nbtbase);
                    if (itemstack == null) {
                        CCubesCore.logger.log(
                            Level.ERROR,
                            "Failed to create an itemstack from the JSON of: " + jsonEdited
                                + " and the NBT of: "
                                + ((NBTTagCompound) nbtbase).toString());
                        continue;
                    } else stack = new ItemPart(itemstack);
                }
            } catch (NBTException e1) {
                CCubesCore.logger.log(Level.ERROR, e1.getMessage());
                continue;
            }

            if (fullelement.getAsJsonObject()
                .has("delay"))
                stack.setDelay(
                    fullelement.getAsJsonObject()
                        .get("delay")
                        .getAsInt());

            items.add(stack);
        }
        rewards.add(new ItemRewardType(items.toArray(new ItemPart[items.size()])));
        return rewards;
    }

    public List<IRewardType> loadBlockReward(JsonArray rawReward, List<IRewardType> rewards) {
        List<OffsetBlock> blocks = new ArrayList<OffsetBlock>();
        for (JsonElement element : rawReward) {
            int x = element.getAsJsonObject()
                .get("XOffSet")
                .getAsInt();
            int y = element.getAsJsonObject()
                .get("YOffSet")
                .getAsInt();
            int z = element.getAsJsonObject()
                .get("ZOffSet")
                .getAsInt();
            String mod = element.getAsJsonObject()
                .get("Block")
                .getAsString()
                .substring(
                    0,
                    element.getAsJsonObject()
                        .get("Block")
                        .getAsString()
                        .indexOf(":"));
            String blockName = element.getAsJsonObject()
                .get("Block")
                .getAsString()
                .substring(
                    element.getAsJsonObject()
                        .get("Block")
                        .getAsString()
                        .indexOf(":") + 1);
            Block block = GameRegistry.findBlock(mod, blockName);
            boolean falling = element.getAsJsonObject()
                .get("Falling")
                .getAsBoolean();

            OffsetBlock offBlock = new OffsetBlock(x, y, z, block, falling);

            if (element.getAsJsonObject()
                .has("delay"))
                offBlock.setDelay(
                    element.getAsJsonObject()
                        .get("delay")
                        .getAsInt());

            if (element.getAsJsonObject()
                .has("RelativeToPlayer"))
                offBlock.setRelativeToPlayer(
                    element.getAsJsonObject()
                        .get("RelativeToPlayer")
                        .getAsBoolean());

            blocks.add(offBlock);
        }
        rewards.add(new BlockRewardType(blocks.toArray(new OffsetBlock[blocks.size()])));
        return rewards;
    }

    public List<IRewardType> loadMessageReward(JsonArray rawReward, List<IRewardType> rewards) {
        List<MessagePart> msgs = new ArrayList<MessagePart>();
        for (JsonElement element : rawReward) {
            MessagePart message = new MessagePart(
                element.getAsJsonObject()
                    .get("message")
                    .getAsString());

            if (element.getAsJsonObject()
                .has("delay"))
                message.setDelay(
                    element.getAsJsonObject()
                        .get("delay")
                        .getAsInt());
            if (element.getAsJsonObject()
                .has("serverWide"))
                message.setServerWide(
                    element.getAsJsonObject()
                        .get("serverWide")
                        .getAsBoolean());
            if (element.getAsJsonObject()
                .has("range"))
                message.setRange(
                    element.getAsJsonObject()
                        .get("range")
                        .getAsInt());

            msgs.add(message);
        }
        rewards.add(new MessageRewardType(msgs.toArray(new MessagePart[msgs.size()])));
        return rewards;
    }

    public List<IRewardType> loadCommandReward(JsonArray rawReward, List<IRewardType> rewards) {
        List<CommandPart> commands = new ArrayList<CommandPart>();
        for (JsonElement element : rawReward) {
            CommandPart command = new CommandPart(
                element.getAsJsonObject()
                    .get("command")
                    .getAsString());

            if (element.getAsJsonObject()
                .has("delay"))
                command.setDelay(
                    element.getAsJsonObject()
                        .get("delay")
                        .getAsInt());
            commands.add(command);
        }
        rewards.add(new CommandRewardType(commands.toArray(new CommandPart[commands.size()])));
        return rewards;
    }

    public List<IRewardType> loadEntityReward(JsonArray rawReward, List<IRewardType> rewards) {
        List<EntityPart> entities = new ArrayList<EntityPart>();
        for (JsonElement element : rawReward) {
            EntityPart ent;

            try {
                String jsonEdited = this.removedKeyQuotes(
                    element.getAsJsonObject()
                        .get("entity")
                        .getAsJsonObject()
                        .toString());
                NBTBase nbtbase = JsonToNBT.func_150315_a(jsonEdited);

                if (!(nbtbase instanceof NBTTagCompound)) {
                    CCubesCore.logger.log(Level.ERROR, "Failed to convert the JSON to NBT for: " + element.toString());
                    continue;
                } else {
                    ent = new EntityPart((NBTTagCompound) nbtbase);
                }
            } catch (Exception e1) {
                CCubesCore.logger.log(Level.ERROR, "The Entiy loading failed for a custom reward!");
                CCubesCore.logger.log(Level.ERROR, "-------------------------------------------");
                e1.printStackTrace();
                continue;
            }

            if (element.getAsJsonObject()
                .has("delay"))
                ent.setDelay(
                    element.getAsJsonObject()
                        .get("delay")
                        .getAsInt());
            entities.add(ent);
        }
        rewards.add(new EntityRewardType(entities.toArray(new EntityPart[entities.size()])));
        return rewards;
    }

    public List<IRewardType> loadExperienceReward(JsonArray rawReward, List<IRewardType> rewards) {
        List<ExpirencePart> exp = new ArrayList<ExpirencePart>();
        for (JsonElement element : rawReward) {
            ExpirencePart exppart = new ExpirencePart(
                element.getAsJsonObject()
                    .get("experienceAmount")
                    .getAsInt());

            if (element.getAsJsonObject()
                .has("delay"))
                exppart.setDelay(
                    element.getAsJsonObject()
                        .get("delay")
                        .getAsInt());
            if (element.getAsJsonObject()
                .has("numberOfOrbs"))
                exppart.setNumberofOrbs(
                    element.getAsJsonObject()
                        .get("numberOfOrbs")
                        .getAsInt());
            exp.add(exppart);
        }
        rewards.add(new ExperienceRewardType(exp.toArray(new ExpirencePart[exp.size()])));
        return rewards;
    }

    public List<IRewardType> loadPotionReward(JsonArray rawReward, List<IRewardType> rewards) {
        List<PotionPart> potionEffects = new ArrayList<PotionPart>();
        for (JsonElement element : rawReward) {
            PotionPart exppart = new PotionPart(
                new PotionEffect(
                    element.getAsJsonObject()
                        .get("potionid")
                        .getAsInt(),
                    element.getAsJsonObject()
                        .get("duration")
                        .getAsInt() * 20));

            if (element.getAsJsonObject()
                .has("delay"))
                exppart.setDelay(
                    element.getAsJsonObject()
                        .get("delay")
                        .getAsInt());
            potionEffects.add(exppart);
        }
        rewards.add(new PotionRewardType(potionEffects.toArray(new PotionPart[potionEffects.size()])));
        return rewards;
    }

    public List<IRewardType> loadSoundReward(JsonArray rawReward, List<IRewardType> rewards) {
        List<SoundPart> sounds = new ArrayList<SoundPart>();
        for (JsonElement element : rawReward) {
            SoundPart sound = new SoundPart(
                element.getAsJsonObject()
                    .get("sound")
                    .getAsString());

            if (element.getAsJsonObject()
                .has("delay"))
                sound.setDelay(
                    element.getAsJsonObject()
                        .get("delay")
                        .getAsInt());
            if (element.getAsJsonObject()
                .has("serverWide"))
                sound.setServerWide(
                    element.getAsJsonObject()
                        .get("serverWide")
                        .getAsBoolean());
            if (element.getAsJsonObject()
                .has("range"))
                sound.setRange(
                    element.getAsJsonObject()
                        .get("range")
                        .getAsInt());
            if (element.getAsJsonObject()
                .has("playAtPlayersLocation"))
                sound.setAtPlayersLocation(
                    element.getAsJsonObject()
                        .get("playAtPlayersLocation")
                        .getAsBoolean());
            if (element.getAsJsonObject()
                .has("volume"))
                sound.setVolume(
                    element.getAsJsonObject()
                        .get("volume")
                        .getAsInt());
            if (element.getAsJsonObject()
                .has("pitch"))
                sound.setPitch(
                    element.getAsJsonObject()
                        .get("pitch")
                        .getAsInt());

            sounds.add(sound);
        }
        rewards.add(new SoundRewardType(sounds.toArray(new SoundPart[sounds.size()])));
        return rewards;
    }

    public List<IRewardType> loadChestReward(JsonArray rawReward, List<IRewardType> rewards) {
        List<ChestChanceItem> items = Lists.newArrayList();
        for (JsonElement element : rawReward) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("item") && obj.has("chance")) {
                int meta = 0;
                if (obj.has("meta")) meta = obj.get("meta")
                    .getAsInt();

                int amountMin = 0;
                if (obj.has("amountMin")) amountMin = obj.get("amountMin")
                    .getAsInt();

                int amountMax = 8;
                if (obj.has("amountMax")) amountMax = obj.get("amountMax")
                    .getAsInt();

                items.add(
                    new ChestChanceItem(
                        obj.get("item")
                            .getAsString(),
                        meta,
                        obj.get("chance")
                            .getAsInt(),
                        amountMin,
                        amountMax));
            } else {
                CCubesCore.logger.log(Level.ERROR, "A chest reward failed to load do to missing params");
            }

        }
        rewards.add(new ChestRewardType(items.toArray(new ChestChanceItem[items.size()])));
        return rewards;
    }

    public List<IRewardType> loadParticleReward(JsonArray rawReward, List<IRewardType> rewards) {
        List<ParticlePart> particles = new ArrayList<ParticlePart>();
        for (JsonElement element : rawReward) {
            ParticlePart particle = new ParticlePart(
                element.getAsJsonObject()
                    .get("particle")
                    .getAsString());

            if (element.getAsJsonObject()
                .has("delay"))
                particle.setDelay(
                    element.getAsJsonObject()
                        .get("delay")
                        .getAsInt());

            particles.add(particle);
        }
        rewards.add(new ParticleEffectRewardType(particles.toArray(new ParticlePart[particles.size()])));
        return rewards;
    }

    public List<IRewardType> loadSchematicReward(JsonArray rawReward, List<IRewardType> rewards) {
        List<OffsetBlock> blocks = new ArrayList<OffsetBlock>();
        for (JsonElement element : rawReward) {
            String fileName = element.getAsJsonObject()
                .get("fileName")
                .getAsString();
            if (fileName.endsWith(".schematic")) {
                Schematic schem = null;

                try {
                    schem = parseSchematic(
                        element.getAsJsonObject()
                            .get("fileName")
                            .getAsString(),
                        false);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (schem == null) {
                    CCubesCore.logger.log(
                        Level.ERROR,
                        "Failed to load the schematic of " + element.getAsJsonObject()
                            .get("fileName")
                            .getAsString() + ". It seems to be dead :(");
                    continue;
                }

                int multiplier = 0;
                if (element.getAsJsonObject()
                    .has("delay"))
                    multiplier = element.getAsJsonObject()
                        .get("delay")
                        .getAsInt();

                int i = 0;
                short halfLength = (short) (schem.length / 2);
                short halfWidth = (short) (schem.width / 2);

                for (int yy = 0; yy < schem.height; yy++) {
                    for (int zz = 0; zz < schem.length; zz++) {
                        for (int xx = 0; xx < schem.width; xx++) {
                            int j = schem.blocks[i];
                            if (j < 0) j = 128 + (128 + j);

                            Block b = Block.getBlockById(j);
                            if (b != Blocks.air) {
                                boolean falling = false;
                                if (element.getAsJsonObject()
                                    .has("falling"))
                                    falling = element.getAsJsonObject()
                                        .get("falling")
                                        .getAsBoolean();
                                OffsetBlock block = new OffsetBlock(halfWidth - xx, yy, halfLength - zz, b, falling);
                                if (element.getAsJsonObject()
                                    .has("RelativeToPlayer"))
                                    block.setRelativeToPlayer(
                                        element.getAsJsonObject()
                                            .get("RelativeToPlayer")
                                            .getAsBoolean());
                                block.setDelay(i * multiplier);
                                block.setData(schem.data[i]);
                                blocks.add(block);
                            }
                            i++;
                        }
                    }
                }

                if (schem.tileentities != null) {
                    for (int i1 = 0; i1 < schem.tileentities.tagCount(); ++i1) {
                        NBTTagCompound nbttagcompound4 = schem.tileentities.getCompoundTagAt(i1);
                        TileEntity tileentity = TileEntity.createAndLoadEntity(nbttagcompound4);

                        if (tileentity != null) {
                            boolean falling = false;
                            if (element.getAsJsonObject()
                                .has("falling"))
                                falling = element.getAsJsonObject()
                                    .get("falling")
                                    .getAsBoolean();
                            Block b = null;
                            for (OffsetBlock osb : blocks)
                                if (osb.xOff == tileentity.xCoord && osb.yOff == tileentity.yCoord
                                    && osb.zOff == tileentity.zCoord) b = osb.getBlock();
                            if (b == null) b = Blocks.stone;
                            OffsetTileEntity block = new OffsetTileEntity(
                                tileentity.xCoord,
                                tileentity.yCoord,
                                tileentity.zCoord,
                                b,
                                nbttagcompound4,
                                falling);
                            if (element.getAsJsonObject()
                                .has("RelativeToPlayer"))
                                block.setRelativeToPlayer(
                                    element.getAsJsonObject()
                                        .get("RelativeToPlayer")
                                        .getAsBoolean());
                            block.setDelay(i1 * multiplier);
                            block.setData(schem.data[i1]);
                            blocks.add(block);
                        }
                    }
                }
            } else {
                if (!fileName.endsWith(".ccs")) fileName += ".ccs";
                int xoff = 0;
                int yoff = 0;
                int zoff = 0;
                float delay = 0;
                boolean falling = true;
                boolean relativeToPlayer = false;
                if (element.getAsJsonObject()
                    .has("XOffSet"))
                    xoff = element.getAsJsonObject()
                        .get("XOffSet")
                        .getAsInt();
                if (element.getAsJsonObject()
                    .has("YOffSet"))
                    yoff = element.getAsJsonObject()
                        .get("YOffSet")
                        .getAsInt();
                if (element.getAsJsonObject()
                    .has("ZOffSet"))
                    zoff = element.getAsJsonObject()
                        .get("ZOffSet")
                        .getAsInt();
                if (element.getAsJsonObject()
                    .has("delay"))
                    delay = element.getAsJsonObject()
                        .get("delay")
                        .getAsFloat();
                if (element.getAsJsonObject()
                    .has("falling"))
                    falling = element.getAsJsonObject()
                        .get("falling")
                        .getAsBoolean();
                if (element.getAsJsonObject()
                    .has("RelativeToPlayer"))
                    relativeToPlayer = element.getAsJsonObject()
                        .get("RelativeToPlayer")
                        .getAsBoolean();
                blocks.addAll(
                    SchematicUtil.loadCustomSchematic(fileName, xoff, yoff, zoff, delay, falling, relativeToPlayer)
                        .getBlocks());
            }
        }
        rewards.add(new BlockRewardType(blocks.toArray(new OffsetBlock[blocks.size()])));
        return rewards;
    }

    public Schematic parseSchematic(String name, boolean hardcoded) throws IOException {
        if (!name.contains(".schematic")) name = name + ".schematic";
        InputStream is;

        if (hardcoded) {
            is = new FileInputStream(new File(source.getAbsolutePath() + "/assets/chancecubes/schematics/" + name));
        } else {
            File schematic = new File(
                folder.getParentFile()
                    .getAbsolutePath() + "/CustomRewards/Schematics/"
                    + name);
            is = new FileInputStream(schematic);
        }

        NBTTagCompound nbtdata = CompressedStreamTools.readCompressed(is);

        short width = nbtdata.getShort("Width");
        short height = nbtdata.getShort("Height");
        short length = nbtdata.getShort("Length");

        byte[] blocks = nbtdata.getByteArray("Blocks");
        byte[] data = nbtdata.getByteArray("Data");

        NBTTagList tileentities = nbtdata.getTagList("TileEntities", 10);
        is.close();

        return new Schematic(tileentities, width, height, length, blocks, data);
    }

    public class Schematic {

        public NBTTagList tileentities;
        public short width;
        public short height;
        public short length;
        public byte[] blocks;
        public byte[] data;

        public Schematic(NBTTagList tileentities, short width, short height, short length, byte[] blocks, byte[] data) {
            this.tileentities = tileentities;
            this.width = width;
            this.height = height;
            this.length = length;
            this.blocks = blocks;
            this.data = data;
        }
    }

    public String removedKeyQuotes(String raw) {
        StringBuilder sb = new StringBuilder(raw.toString());
        int index = 0;
        while ((index = sb.indexOf("\"", index)) != -1) {
            int secondQuote = sb.indexOf("\"", index + 1);
            if (secondQuote == -1) break;
            if (sb.charAt(secondQuote + 1) == ':') {
                sb.deleteCharAt(index);
                sb.delete(secondQuote - 1, secondQuote);
                index = secondQuote;
            } else {
                index++;
            }
        }
        return sb.toString();
    }

    public List<String> getRewardsFiles() {
        List<String> files = Lists.newArrayList();
        for (File f : folder.listFiles()) {
            if (!f.isFile()) continue;
            if (f.getName()
                .substring(
                    f.getName()
                        .indexOf("."))
                .equalsIgnoreCase(".json")) files.add(f.getName());
        }
        return files;
    }

    public List<String> getRewardsFromFile(String file) {
        List<String> rewards = Lists.newArrayList();

        File rewardsFile = new File(this.folder.getPath() + "\\" + file);

        JsonElement fileJson;
        try {
            fileJson = json.parse(new FileReader(rewardsFile));
        } catch (Exception e) {
            CCubesCore.logger
                .log(Level.ERROR, "Unable to parse the file " + rewardsFile.getName() + ". Skipping file loading.");
            return null;
        }

        for (Entry<String, JsonElement> reward : fileJson.getAsJsonObject()
            .entrySet()) rewards.add(reward.getKey());

        return rewards;
    }

    public List<String> getReward(String file, String rewardName) {
        List<String> rewardinfo = Lists.newArrayList();

        File rewardsFile = new File(this.folder.getPath() + "\\" + file);
        JsonElement fileJson;

        try {
            fileJson = json.parse(new FileReader(rewardsFile));
        } catch (Exception e) {
            CCubesCore.logger
                .log(Level.ERROR, "Unable to parse the file " + rewardsFile.getName() + ". Skipping file loading.");
            return null;
        }

        for (Entry<String, JsonElement> reward : fileJson.getAsJsonObject()
            .entrySet()) {
            JsonObject rewardElements = reward.getValue()
                .getAsJsonObject();
            for (Entry<String, JsonElement> rewardElement : rewardElements.entrySet())
                rewardinfo.add(rewardElement.getKey());
        }
        return rewardinfo;
    }

    public List<String> getRewardType(String file, String s, String type) {
        List<String> rewardinfo = Lists.newArrayList();

        File rewardsFile = new File(this.folder.getPath() + "\\" + file);
        JsonElement fileJson;
        try {
            fileJson = json.parse(new FileReader(rewardsFile));
        } catch (Exception e) {
            CCubesCore.logger
                .log(Level.ERROR, "Unable to parse the file " + rewardsFile.getName() + ". Skipping file loading.");
            return null;
        }

        for (Entry<String, JsonElement> reward : fileJson.getAsJsonObject()
            .entrySet()) {
            JsonObject rewardElements = reward.getValue()
                .getAsJsonObject();
            for (Entry<String, JsonElement> rewardElement : rewardElements.entrySet()) {
                if (rewardElement.getKey()
                    .equalsIgnoreCase(type)) {
                    if (rewardElement.getValue() instanceof JsonArray) {
                        JsonArray rewardTypeArray = rewardElement.getValue()
                            .getAsJsonArray();
                        for (int i = 0; i < rewardTypeArray.size(); i++) rewardinfo.add(
                            rewardTypeArray.get(i)
                                .toString());
                    } else {
                        rewardinfo.add(
                            rewardElement.getValue()
                                .toString());
                    }
                }
            }
        }
        return rewardinfo;
    }

    public int compareDates(Calendar first, Calendar second) {
        int fm = first.get(Calendar.MONTH);
        int sm = second.get(Calendar.MONTH);

        int fd = first.get(Calendar.DAY_OF_MONTH);
        int sd = second.get(Calendar.DAY_OF_MONTH);

        if (fm < sm) return 1;
        else if (fm == sm) return fd == sd ? 0 : fd < sd ? 1 : -1;
        else return -2;
    }

    public File getFolderFile() {
        return this.folder;
    }
}
