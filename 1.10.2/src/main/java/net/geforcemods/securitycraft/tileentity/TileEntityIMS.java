package net.geforcemods.securitycraft.tileentity;

import java.util.Iterator;
import java.util.List;

import net.geforcemods.securitycraft.api.CustomizableSCTE;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.blocks.mines.BlockIMS;
import net.geforcemods.securitycraft.entity.EntityIMSBomb;
import net.geforcemods.securitycraft.main.mod_SecurityCraft;
import net.geforcemods.securitycraft.misc.EnumCustomModules;
import net.geforcemods.securitycraft.network.packets.PacketCPlaySoundAtPos;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.geforcemods.securitycraft.util.WorldUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityIMS extends CustomizableSCTE {
	
	/** Number of bombs remaining in storage. **/
	private int bombsRemaining = 4;
	
	/** The targeting option currently selected for this IMS. PLAYERS = players, PLAYERS_AND_MOBS = hostile mobs & players.**/
	private EnumIMSTargetingMode targetingOption = EnumIMSTargetingMode.PLAYERS_AND_MOBS;
	
	private boolean updateBombCount = false;
	
	@Override
	public void update(){
		super.update();
		
		if(!worldObj.isRemote && updateBombCount){
	        BlockUtils.setBlockProperty(worldObj, pos, BlockIMS.MINES, BlockUtils.getBlockPropertyAsInteger(worldObj, pos, BlockIMS.MINES) - 1);
	        updateBombCount = false;
		}
		
		if(this.worldObj.getTotalWorldTime() % 80L == 0L){
            this.launchMine();
        }
	}

    /**
	 * Create a bounding box around the IMS, and fire a mine if a mob or player is found.
	 */	
	private void launchMine() {
		boolean launchedMine = false;
		
		if(bombsRemaining > 0){
			double d0 = mod_SecurityCraft.configHandler.imsRange;
			
			AxisAlignedBB axisalignedbb = BlockUtils.fromBounds(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).expand(d0, d0, d0);
	        List<?> list1 = this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, axisalignedbb);
	        List<?> list2 = this.worldObj.getEntitiesWithinAABB(EntityMob.class, axisalignedbb);
	        Iterator<?> iterator1 = list1.iterator();
	        Iterator<?> iterator2 = list2.iterator();	       
	        
	        while(targetingOption == EnumIMSTargetingMode.PLAYERS_AND_MOBS && iterator2.hasNext()){
	        	EntityLivingBase entity = (EntityLivingBase) iterator2.next();
				int launchHeight = this.getLaunchHeight();

				if(WorldUtils.isPathObstructed(worldObj, pos.getX() + 0.5D, pos.getY() + (((launchHeight - 1) / 3) + 0.5D), pos.getZ() + 0.5D, entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ)){ continue; }
				if(hasModule(EnumCustomModules.WHITELIST) && ModuleUtils.getPlayersFromModule(worldObj, pos, EnumCustomModules.WHITELIST).contains(entity.getName().toLowerCase())){ continue; }

		        double d5 = entity.posX - (pos.getX() + 0.5D);
		        double d6 = entity.getEntityBoundingBox().minY + entity.height / 2.0F - (pos.getY() + 1.25D);
		        double d7 = entity.posZ - (pos.getZ() + 0.5D);

		        this.spawnMine(entity, d5, d6, d7, launchHeight);
		            
		        if(worldObj.isRemote){
		        	mod_SecurityCraft.network.sendToAll(new PacketCPlaySoundAtPos(pos.getX(), pos.getY(), pos.getZ(), "random.bow", 1.0F));
		        }
		        		  
		        this.bombsRemaining--;
		        
		        if(bombsRemaining == 0){
		        	worldObj.scheduleUpdate(pos, BlockUtils.getBlock(worldObj, pos), 140);
		        }
		        
		        launchedMine = true;
		        updateBombCount = true;

		        break;
	        }
	        
	        while(!launchedMine && iterator1.hasNext()){
	        	EntityPlayer entity = (EntityPlayer) iterator1.next();
				int launchHeight = this.getLaunchHeight();

	        	if(entity != null && getOwner().isOwner((entity))){ continue; }
				if(WorldUtils.isPathObstructed(worldObj, pos.getX() + 0.5D, pos.getY() + (((launchHeight - 1) / 3) + 0.5D), pos.getZ() + 0.5D, entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ)){ continue; }
				if(hasModule(EnumCustomModules.WHITELIST) && ModuleUtils.getPlayersFromModule(worldObj, pos, EnumCustomModules.WHITELIST).contains(entity.getName())){ continue; }

		        double d5 = entity.posX - (pos.getX() + 0.5D);
		        double d6 = entity.getEntityBoundingBox().minY + entity.height / 2.0F - (pos.getY() + 1.25D);
		        double d7 = entity.posZ - (pos.getZ() + 0.5D);
					
		        this.spawnMine(entity, d5, d6, d7, launchHeight);
		            
		        if(worldObj.isRemote){
		        	mod_SecurityCraft.network.sendToAll(new PacketCPlaySoundAtPos(pos.getX(), pos.getY(), pos.getZ(), "random.bow", 1.0F));
		        }		        		    
		        
		        this.bombsRemaining--;
		        
		        if(bombsRemaining == 0){
		        	worldObj.scheduleUpdate(pos, BlockUtils.getBlock(worldObj, pos), 140);
		        }
		        
		        updateBombCount = true;
		        		        
		        break;
	        }       
        }
	}
	
    /**
	 * Spawn a mine at the correct position on the IMS model.
	 */
	private void spawnMine(EntityPlayer target, double x, double y, double z, int launchHeight){
		if(bombsRemaining == 4){
			EntityIMSBomb entitylargefireball = new EntityIMSBomb(worldObj, target, pos.getX() + 1.2D, pos.getY(), pos.getZ() + 1.2D, x, y, z, launchHeight);
			worldObj.spawnEntityInWorld(entitylargefireball);
		}else if(bombsRemaining == 3){
			EntityIMSBomb entitylargefireball = new EntityIMSBomb(worldObj, target, pos.getX() + 1.2D, pos.getY(), pos.getZ() + 0.6D, x, y, z, launchHeight);
			worldObj.spawnEntityInWorld(entitylargefireball);
		}else if(bombsRemaining == 2){
			EntityIMSBomb entitylargefireball = new EntityIMSBomb(worldObj, target, pos.getX() + 0.55D, pos.getY(), pos.getZ() + 1.2D, x, y, z, launchHeight);
			worldObj.spawnEntityInWorld(entitylargefireball);
		}else if(bombsRemaining == 1){
			EntityIMSBomb entitylargefireball = new EntityIMSBomb(worldObj, target, pos.getX() + 0.55D, pos.getY(), pos.getZ() + 0.6D, x, y, z, launchHeight);
			worldObj.spawnEntityInWorld(entitylargefireball);
		}
	}
	
    /**
	 * Spawn a mine at the correct position on the IMS model.
	 */
	private void spawnMine(EntityLivingBase target, double x, double y, double z, int launchHeight){
		if(bombsRemaining == 4){
			EntityIMSBomb entitylargefireball = new EntityIMSBomb(worldObj, target, pos.getX() + 1.2D, pos.getY(), pos.getZ() + 1.2D, x, y, z, launchHeight);
			worldObj.spawnEntityInWorld(entitylargefireball);
		}else if(bombsRemaining == 3){
			EntityIMSBomb entitylargefireball = new EntityIMSBomb(worldObj, target, pos.getX() + 1.2D, pos.getY(), pos.getZ() + 0.6D, x, y, z, launchHeight);
			worldObj.spawnEntityInWorld(entitylargefireball);
		}else if(bombsRemaining == 2){
			EntityIMSBomb entitylargefireball = new EntityIMSBomb(worldObj, target, pos.getX() + 0.55D, pos.getY(), pos.getZ() + 1.2D, x, y, z, launchHeight);
			worldObj.spawnEntityInWorld(entitylargefireball);
		}else if(bombsRemaining == 1){
			EntityIMSBomb entitylargefireball = new EntityIMSBomb(worldObj, target, pos.getX() + 0.55D, pos.getY(), pos.getZ() + 0.6D, x, y, z, launchHeight);
			worldObj.spawnEntityInWorld(entitylargefireball);
		}
	}
	
	/**
	 * Returns the amount of ticks the {@link EntityIMSBomb} should float in the air before firing at an entity.
	 */
	private int getLaunchHeight() {
		int height;
		
		for(height = 1; height <= 9; height++){
			if(BlockUtils.getBlock(getWorld(), getPos().up(height)) == null || BlockUtils.getBlock(getWorld(), getPos().up(height)) == Blocks.AIR){
				continue;
			}else{
				break;
			}
		}
		
		return height * 3;
	}

	/**
     * Writes a tile entity to NBT.
	 * @return 
     */
    @Override
	public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound){
        super.writeToNBT(par1NBTTagCompound);
        
        par1NBTTagCompound.setInteger("bombsRemaining", bombsRemaining);
        par1NBTTagCompound.setInteger("targetingOption", targetingOption.modeIndex);
        par1NBTTagCompound.setBoolean("updateBombCount", updateBombCount);
        return par1NBTTagCompound;
    }

    /**
     * Reads a tile entity from NBT.
     */
    @Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound){
        super.readFromNBT(par1NBTTagCompound);

        if (par1NBTTagCompound.hasKey("bombsRemaining"))
        {
            this.bombsRemaining = par1NBTTagCompound.getInteger("bombsRemaining");
        }
        
        if (par1NBTTagCompound.hasKey("targetingOption"))
        {
            this.targetingOption = EnumIMSTargetingMode.values()[par1NBTTagCompound.getInteger("targetingOption")];
        }
        
        if (par1NBTTagCompound.hasKey("updateBombCount"))
        {
            this.updateBombCount = par1NBTTagCompound.getBoolean("updateBombCount");
        }
    }

	public int getBombsRemaining() {
		return bombsRemaining;
	}

	public void setBombsRemaining(int bombsRemaining) {
		this.bombsRemaining = bombsRemaining;
	}

	public EnumIMSTargetingMode getTargetingOption() {
		return targetingOption;
	}

	public void setTargetingOption(EnumIMSTargetingMode targetingOption) {
		this.targetingOption = targetingOption;
	}

	@Override
	public EnumCustomModules[] acceptedModules() {
		return new EnumCustomModules[]{EnumCustomModules.WHITELIST};
	}
	
	@Override
	public Option<?>[] customOptions() {
		return null;
	}

public static enum EnumIMSTargetingMode {
	
	PLAYERS(0),
	PLAYERS_AND_MOBS(1);
	
	public final int modeIndex;
	
	private EnumIMSTargetingMode(int index){
		this.modeIndex = index;
	}
	
	
}

}
