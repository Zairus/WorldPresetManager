package zairus.worldpresetmanager.world;

import java.util.List;
import java.util.Random;

import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.WorldChunkManager;

public class WPMWorldManager
	extends WorldChunkManager
{
	private World world;
	
	public WPMWorldManager(World world)
	{
		super(world);
		this.world = world;
	}
	
	@Override
	public ChunkPosition findBiomePosition(int x, int z, int range, @SuppressWarnings("rawtypes") List biomes, Random rand)
	{
		ChunkPosition chunkPos = super.findBiomePosition(x, z, range, biomes, rand);
		
		if (x == 0 && z == 0 && !world.getWorldInfo().isInitialized())
		{
			if (chunkPos == null)
				chunkPos = new ChunkPosition(0, 0, 0);
		}
		
		return chunkPos;
	}
}
