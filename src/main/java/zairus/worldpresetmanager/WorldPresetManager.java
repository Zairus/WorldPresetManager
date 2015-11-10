package zairus.worldpresetmanager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import zairus.worldpresetmanager.handlers.WPMEventHandler;
import zairus.worldpresetmanager.proxy.CommonProxy;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;

@Mod(modid = WorldPresetManager.MODID, version = WorldPresetManager.VERSION)
public class WorldPresetManager
{
    public static final String MODID = "worldpresetmanager";
    public static final String VERSION = "1.1.5";
    
    @SidedProxy(clientSide="zairus.worldpresetmanager.proxy.ClientProxy", serverSide="zairus.worldpresetmanager.proxy.ServerProxy")
	public static CommonProxy proxy;
    
    @Mod.Instance("WorldPresetManager")
    public static WorldPresetManager instance;
    
    private static final Set<WorldType> worldTypes = new HashSet<WorldType>();
    public static final List<WorldType> managedWorldTypes = new ArrayList<WorldType>();
    public static Logger logger;
    public static Configuration configuration;
    
    private int totalPresets;
    
    @Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
    	WorldPresetManager.proxy.preInit(event);
    	
    	configuration = new Configuration(event.getSuggestedConfigurationFile());
		
		configuration.load();
		
		Property tempProp;
		
		tempProp = configuration.get(Configuration.CATEGORY_GENERAL, "numberOfPresets", 1);
		tempProp.comment = "Total number of presets to add to the world types list.";
		
		totalPresets = tempProp.getInt(1);
		
		configuration.save();
	}
    
	@EventHandler
    public void init(FMLInitializationEvent event)
    {
    	WorldPresetManager.proxy.init(event);
    	
    	WPMEventHandler eventHandler = new WPMEventHandler();
    	FMLCommonHandler.instance().bus().register(eventHandler);
    	MinecraftForge.EVENT_BUS.register(eventHandler);
    	MinecraftForge.TERRAIN_GEN_BUS.register(eventHandler);
    }
    
    @SuppressWarnings("deprecation")
	@EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    	WorldPresetManager.proxy.postInit(event);
    	
    	configuration.load();
    	
    	String wtCode = "";
    	String wtName = "";
    	
    	WorldPresetType currentPreset;
    	
    	Property tempProp;
    	
    	for (int i = 1; i <= totalPresets; ++i)
    	{
    		wtCode = "";
    		wtName = "";
    		
    		currentPreset = null;
    		
    		tempProp = configuration.get("WORLDPRESET" + i, "presetCode", "Preset_" + i);
    		tempProp.comment = "The code of this preset, cant me longer than 16 chars";
    		wtCode = tempProp.getString();
    		
    		tempProp = configuration.get("WORLDPRESET" + i, "presetName", "World Preset " + i);
    		tempProp.comment = "The name as it will appear on the settings screen";
    		wtName = tempProp.getString();
    		
    		configuration.addCustomCategoryComment("WORLDPRESET" + i, "Generator settings for a world preset to manage");
    		
    		currentPreset = new WorldPresetType(wtCode, i);
    		
    		managedWorldTypes.add(currentPreset);
    		
    		LanguageRegistry.instance().addStringLocalization("generator." + wtCode, "en_US", wtName);
    		
    		worldTypes.add(managedWorldTypes.get(i - 1));
    	}
    	
    	configuration.save();
    }
    
    public static void log(String obj)
	{
		if (logger == null) {
			logger = Logger.getLogger("WorldPresetManager");
		}
		if (obj == null) {
			obj = "null";
		}
		logger.info("[" + FMLCommonHandler.instance().getEffectiveSide() + "] " + obj);
	}
    
    public static void overrideOtherGens(boolean value)
	{
		try{
			if (YUNoExists())
				handleOtherGens(value);
		} catch (Exception e)
		{
			;
		}
	}
    
    private static boolean YUNoExists()
	{
		boolean exists = false;
		
		try {
			Class.forName("net.minecraftforge.lex.yunomakegoodmap.YUNoMakeGoodMap");
			exists = true;
		}
		catch (Exception e)
		{
			exists = false;
		}
		
		return exists;
	}
	
	private static void handleOtherGens(boolean value)
	{
		try {
			ReflectionHelper.setPrivateValue(net.minecraftforge.lex.yunomakegoodmap.YUNoMakeGoodMap.class, net.minecraftforge.lex.yunomakegoodmap.YUNoMakeGoodMap.instance, value, "overrideDefault");
		}
		catch (Exception e)
		{
			;
		}
	}
}
