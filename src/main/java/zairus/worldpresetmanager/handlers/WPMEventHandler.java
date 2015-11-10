package zairus.worldpresetmanager.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.AnvilConverterException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.SaveFormatComparator;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.WorldEvent;
import zairus.worldpresetmanager.WorldPresetManager;
import zairus.worldpresetmanager.WorldPresetType;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class WPMEventHandler
{
	private boolean guiSelectWorldCanceled = false;
	private boolean guiSelectWorldOpened = false;
	private boolean beforeLoadWorldProcessDone = false;
	private GuiSelectWorld guiSelectWorld;
	
	@SubscribeEvent
	public void onLivingEvent(LivingEvent event)
	{
		;
	}
	
	@SubscribeEvent
	public void onLivingSpawnEvent(LivingSpawnEvent.CheckSpawn event)
	{
		;
	}
	
	@SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
		if (event.world.getWorldInfo().getTerrainType() instanceof WorldPresetType && !event.world.isRemote)
		{
			;
		}
    }
	
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event)
	{
		Configuration YUNOConfig = new Configuration(new File(".\\config\\YUNoMakeGoodMap.cfg"));
		
		YUNOConfig.load();
		
		boolean overrideDefault = YUNOConfig.get(Configuration.CATEGORY_GENERAL, "overrideDefault", false).getBoolean();
		
		WorldPresetManager.overrideOtherGens(overrideDefault);
		
		YUNOConfig.save();
		
		guiSelectWorldCanceled = false;
		guiSelectWorldOpened = false;
		beforeLoadWorldProcessDone = false;
	}
	
	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event)
	{
		if (event.world.getWorldInfo().getTerrainType() instanceof WorldPresetType && !event.world.isRemote)
		{
			WorldPresetType worldPreset = (WorldPresetType)event.world.getWorldInfo().getTerrainType();
			
			try {
				NBTTagCompound leveldat = CompressedStreamTools.readCompressed(new FileInputStream(new File(event.world.getSaveHandler().getWorldDirectory(), "level.dat")));
				leveldat.getCompoundTag("Data").setString("generatorOptions", worldPreset.getGeneratorSettingsFromConfig());
				CompressedStreamTools.writeCompressed(leveldat, new FileOutputStream(new File(event.world.getSaveHandler().getWorldDirectory(), "level.dat")));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SubscribeEvent
	public void onActionPerformed(ActionPerformedEvent event)
	{
		if (event.gui instanceof GuiSelectWorld)
		{
			if (event.button.id == 0)
			{
				guiSelectWorldCanceled = true;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void checkOpenWorld(GuiSelectWorld selectWorldScreen) throws AnvilConverterException, FileNotFoundException, IOException
	{
		ISaveFormat isaveformat = Minecraft.getMinecraft().getSaveLoader();
		
		@SuppressWarnings("rawtypes")
		List saveList = isaveformat.getSaveList();
		
		Collections.sort(saveList);
		
		int listIndex = ReflectionHelper.getPrivateValue(GuiSelectWorld.class, selectWorldScreen, "field_146640_r");
		
		String fileName = ((SaveFormatComparator)saveList.get(listIndex)).getFileName();
		
		File worldDir = new File(FMLClientHandler.instance().getSavesDir().getPath() + "\\" + fileName);
		
		NBTTagCompound leveldat = CompressedStreamTools.readCompressed(new FileInputStream(new File(worldDir, "level.dat")));
		
		String generatorName = leveldat.getCompoundTag("Data").getString("generatorName");
		
		for (int i = 0; i < WorldPresetManager.managedWorldTypes.size(); ++i)
		{
			if (generatorName.compareTo(WorldPresetManager.managedWorldTypes.get(i).getWorldTypeName()) == 0)
			{
				WorldPresetManager.overrideOtherGens(false);
				break;
			}
		}
	}
	
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event)
	{
		if (guiSelectWorldOpened && event.gui instanceof GuiMainMenu)
		{
			if (guiSelectWorldCanceled)
			{
				guiSelectWorldCanceled = false;
				guiSelectWorldOpened = false;
				beforeLoadWorldProcessDone = false;
			}
			else
			{
				if (!beforeLoadWorldProcessDone)
				{
					beforeLoadWorldProcessDone = true;
					
					try {
						checkOpenWorld(guiSelectWorld);
					} catch (AnvilConverterException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		else
		{
			if (guiSelectWorldOpened)
				guiSelectWorldOpened = false;
		}
		
		if (!guiSelectWorldOpened && event.gui instanceof GuiSelectWorld)
		{
			guiSelectWorld = (GuiSelectWorld)event.gui;
			guiSelectWorldOpened = true;
		}
	}
}
