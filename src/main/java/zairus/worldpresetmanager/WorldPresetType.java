package zairus.worldpresetmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import zairus.worldpresetmanager.world.WPMWorldManager;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderFlat;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraftforge.common.config.Property;

public class WorldPresetType
	extends WorldType
{
	private final String worldType;
	
	private String customGeneratorSettings = "";
	private int presetIndex = 1;
	private int biome = 0;
	
	public WorldPresetType(String name, int index)
	{
		super(name);
		this.worldType = name;
		this.presetIndex = index;
		
		customGeneratorSettings = getGeneratorSettingsFromConfig();
	}
	
	public void setPresetIndex (int index)
	{
		presetIndex = index;
	}
	
	public String getWorldTypeName()
	{
		return this.worldType;
	}
	
	@Override
	public WorldChunkManager getChunkManager(World world)
	{
		String genSettings = customGeneratorSettings;
		
		if (genSettings == "")
			this.getGeneratorSettingsFromConfig();
		
		if (genSettings == "")
		{
			WorldPresetManager.log("Could not load settings from config, getting default Chunk Manager.");
			genSettings = world.getWorldInfo().getGeneratorOptions();
		}
		
		FlatGeneratorInfo flatgeneratorinfo = FlatGeneratorInfo.createFlatGeneratorFromString(genSettings);
		
		if (biome == -1)
			return new WPMWorldManager(world);
		else
			return new WorldChunkManagerHell(BiomeGenBase.getBiome(flatgeneratorinfo.getBiome()), 0.5F);
	}
	
	@Override
	public IChunkProvider getChunkGenerator(World world, String generatorOptions)
	{
		String genSettings = customGeneratorSettings;
		
		if (genSettings == "")
			this.getGeneratorSettingsFromConfig();
		
		if (genSettings == "")
		{
			WorldPresetManager.log("Could not get settings from config, getting default Chunk Generator.");
			genSettings = world.getWorldInfo().getGeneratorOptions();
		}
		
		return new ChunkProviderFlat(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), genSettings);
	}
	
	@Override
    public int getSpawnFuzz()
    {
    	return 1;
    }
	
	public int getMinimumSpawnHeight(World world)
	{
		return 4;
	}
	
	public double getHorizon(World world)
	{
		return 0.0D;
	}
	
	public boolean hasVoidParticles(boolean flag)
	{
		return false;
	}
	
	public double voidFadeMagnitude()
	{
		return 1.0D;
	}
	
	public boolean handleSlimeSpawnReduction(Random random, World world)
	{
		return random.nextInt(4) != 1;
	}
	
	public void onGUICreateWorldPress()
	{
		WorldPresetManager.overrideOtherGens(false);
		
		customGeneratorSettings = getGeneratorSettingsFromConfig();
	}
	
	public boolean isCustomizable()
	{
		return false;
	}
	
	public String getGeneratorSettingsFromConfig()
	{
		String generatorSettings = "";
		
		WorldPresetManager.configuration.load();
		
		int i = presetIndex;
		int layers = 1;
		boolean genFeature = false;
		
		Property tempProp;
		
		tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "generatorVersion", "2");
		tempProp.comment = "Left for future upgrades.";
		generatorSettings = tempProp.getString() + ";";
		
		tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "layers", "4");
		tempProp.comment = "Indicates the total number of block layers to configure in the settings layer1, layer2 ... etc.";
		layers = tempProp.getInt();
		
		List<String> defaultLayers = new ArrayList<String>();
		defaultLayers.add("minecraft:bedrock");
		defaultLayers.add("4*minecraft:stone");
		defaultLayers.add("53*minecraft:gravel");
		defaultLayers.add("5*minecraft:sand");
		
		String layerDefault = "";
		
		for (int il = 1; il <= layers; ++ il)
		{
			if (defaultLayers.size() >= il)
				layerDefault = defaultLayers.get(il - 1);
			else
				layerDefault = "minecraft:dirt";
			
			tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "layer" + il, layerDefault);
			
			if (il == 1)
				tempProp.comment = "For each layer indicate the number and type of block example: 3*minecraft:stone";
			
			try {
    			if (tempProp.getString().split("\\*").length > 1)
    			{
    				generatorSettings += tempProp.getString().split("\\*")[0] + 'x' + Block.getIdFromBlock(Block.getBlockFromName(tempProp.getString().split("\\*")[1]));
    			}
    			else
    			{
    				generatorSettings += Block.getIdFromBlock(Block.getBlockFromName(tempProp.getString())); 
    			}
    			
    			if (il < layers)
    				generatorSettings += ",";
			} catch (Exception e)
			{
				WorldPresetManager.log("Could not get block Id for: " + tempProp.getString());
				generatorSettings += "";
			}
		}
		
		generatorSettings += ";";
		
		tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "biome", "-1");
		tempProp.comment = "Biome ID for one biome world, use -1 for all biomes (It Works!)";
		biome = tempProp.getInt();
		
		if (biome == -1)
			generatorSettings += "1;";
		else
			generatorSettings += tempProp.getString() + ";";
		
		tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "village", true);
		tempProp.comment = "Indicate wether you want villages or nor.";
		if (tempProp.getBoolean())
		{
			genFeature = true;
			generatorSettings += "village";
			generatorSettings += "(";
			
			tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "villageSize", "1");
			tempProp.comment = "Indicates the size of villages, the Vanilla default is 1";
			generatorSettings += "size=" + tempProp.getString() + ",";
			
			tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "villageDistance", "32");
			tempProp.comment = "Indicates how separated are the villages, Vanilla default is 32";
			generatorSettings += "distance=" + tempProp.getString() + "";
			
			generatorSettings += ")";
		}
		
		tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "mineshaft", true);
		tempProp.comment = "Indicate wether you want Mineshafts or not";
		if (tempProp.getBoolean())
		{
			if (genFeature)
				generatorSettings += ",";
			genFeature = true;
			generatorSettings += "mineshaft";
			generatorSettings += "(";
			
			tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "mineshaftChance", "0.01");
			tempProp.comment = "Probability of mineshafts to generate, Vanilla default is 0.01";
			generatorSettings += "chance=" + tempProp.getString();
			
			generatorSettings += ")";
		}
		
		tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "stronghold", true);
		tempProp.comment = "Indicate wether you want Strongholds or not";
		if (tempProp.getBoolean())
		{
			if (genFeature)
				generatorSettings += ",";
			genFeature = true;
			generatorSettings += "stronghold";
			generatorSettings += "(";
			
			tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "strongholdDistance", "3");
			tempProp.comment = "Distance between strongholds, Vanilla default is 3";
			generatorSettings += "distance=" + tempProp.getString() + ",";
			
			tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "strongholdCount", "3");
			tempProp.comment = "Number of strongholds in the world, default is 3";
			generatorSettings += "count=" + tempProp.getString() + ",";
			
			tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "strongholdSpread", "3");
			tempProp.comment = "How spread are strongholds, Vanilla default us 3";
			generatorSettings += "spread=" + tempProp.getString() + "";
			
			generatorSettings += ")";
		}
		
		tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "biome_1", true);
		tempProp.comment = "Refers to biome specifict features like trees, temples, etc.";
		if (tempProp.getBoolean())
		{
			if (genFeature)
				generatorSettings += ",";
			genFeature = true;
			generatorSettings += "biome_1";
			generatorSettings += "(";
			
			tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "biome_1Distance", "32");
			tempProp.comment = "How separated are the biome features, Vanilla default is 32";
			generatorSettings += "distance=" + tempProp.getString();
			
			generatorSettings += ")";
		}
		
		tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "dungeon", true);
		tempProp.comment = "Indicate wether you want dungeons or not";
		if (tempProp.getBoolean())
		{
			if (genFeature)
				generatorSettings += ",";
			genFeature = true;
			generatorSettings += "dungeon";
		}
		
		tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "decoration", true);
		tempProp.comment = "Refers to decorations like grass, flowers, etc. Very unstable if you use a modded block as surface.";
		if (tempProp.getBoolean())
		{
			if (genFeature)
				generatorSettings += ",";
			genFeature = true;
			generatorSettings += "decoration";
		}
		
		tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "lake", true);
		tempProp.comment = "Indicate wether you want small lakes or not";
		if (tempProp.getBoolean())
		{
			if (genFeature)
				generatorSettings += ",";
			genFeature = true;
			generatorSettings += "lake";
		}
		
		tempProp = WorldPresetManager.configuration.get("WORLDPRESET" + i, "lava_lake", true);
		tempProp.comment = "Indicate wether you want lava lakes or not";
		if (tempProp.getBoolean())
		{
			if (genFeature)
				generatorSettings += ",";
			genFeature = true;
			generatorSettings += "lava_lake";
		}
		
		WorldPresetManager.configuration.save();
		
		return generatorSettings;
	}
}
