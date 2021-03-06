package net.geforcemods.securitycraft.blocks;

import java.util.List;
import java.util.Random;

import net.geforcemods.securitycraft.api.CustomizableSCTE;
import net.geforcemods.securitycraft.api.IPasswordProtected;
import net.geforcemods.securitycraft.imc.waila.ICustomWailaDisplay;
import net.geforcemods.securitycraft.items.ItemModule;
import net.geforcemods.securitycraft.main.mod_SecurityCraft;
import net.geforcemods.securitycraft.misc.EnumCustomModules;
import net.geforcemods.securitycraft.tileentity.TileEntityKeypad;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockKeypad extends BlockContainer implements ICustomWailaDisplay {
	
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool POWERED = PropertyBool.create("powered");

	public BlockKeypad(Material par2Material) {
		super(par2Material);
	}
	
	public boolean isOpaqueCube() {
		return false;
	}
	
	@SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }
	
	@SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        BlockPos keypadPos = pos.offset(side.getOpposite());
        
		if(worldIn.getTileEntity(keypadPos) == null) return true;
        CustomizableSCTE tileEntity = (CustomizableSCTE) worldIn.getTileEntity(keypadPos);
        
        if(tileEntity.hasModule(EnumCustomModules.DISGUISE))
        {
        	ItemStack disguiseModule = tileEntity.getModule(EnumCustomModules.DISGUISE);
        	List<Block> blocks = ((ItemModule) disguiseModule.getItem()).getBlockAddons(disguiseModule.getTagCompound());
        	
        	if(blocks.size() != 0)
        	{
	        	Block blockToDisguiseAs = blocks.get(0);
	        	
	        	// If the keypad has a disguise module added with a transparent block inserted.
	        	if(!blockToDisguiseAs.isOpaqueCube() || !blockToDisguiseAs.isFullCube())
	        	{        		      			        
	        		return checkForSideTransparency(worldIn, keypadPos, worldIn.getBlockState(keypadPos.offset(side)).getBlock(), side);  
	        	}
        	}
        }

        return true;
    }
	
	public boolean checkForSideTransparency(IBlockAccess world, BlockPos keypadPos, Block neighborBlock, EnumFacing side) {
		if(neighborBlock == Blocks.air) {
    		return true;
		}
		
		// Slightly cheating here, checking if the block is an instance of BlockBreakable
		// and a vanilla block instead of checking for specific blocks, since all vanilla
		// BlockBreakable blocks are transparent.
		if(neighborBlock instanceof BlockBreakable && neighborBlock.toString().startsWith("net.minecraft.block")) {
			return false;
		}
		
		return true;
	}
	
	public int getRenderType(){
		return 3;
	}
    
	public boolean onBlockActivated(World par1World, BlockPos pos, IBlockState state, EntityPlayer par5EntityPlayer, EnumFacing side, float par7, float par8, float par9){
    	if(par1World.isRemote){
    		return true;
    	}
    	else {
			if(((Boolean) state.getValue(POWERED)).booleanValue()){
				return false;
			}

			if(ModuleUtils.checkForModule(par1World, pos, par5EntityPlayer, EnumCustomModules.WHITELIST) || ModuleUtils.checkForModule(par1World, pos, par5EntityPlayer, EnumCustomModules.BLACKLIST)){
				activate(par1World, pos);
				return true;
			}

			((IPasswordProtected) par1World.getTileEntity(pos)).openPasswordGUI(par5EntityPlayer);

			return true;       		 	    	     	
		}
    }
    
    public static void activate(World par1World, BlockPos pos){
    	BlockUtils.setBlockProperty(par1World, pos, POWERED, true);
		par1World.notifyNeighborsOfStateChange(pos, mod_SecurityCraft.keypad);
		par1World.scheduleUpdate(pos, mod_SecurityCraft.keypad, 60);
	}
    
    public void updateTick(World par1World, BlockPos pos, IBlockState state, Random par5Random){
    	BlockUtils.setBlockProperty(par1World, pos, POWERED, false);
		par1World.notifyNeighborsOfStateChange(pos, mod_SecurityCraft.keypad);
    }
    
    /**
     * Called whenever the block is added into the world. Args: world, x, y, z
     */
    public void onBlockAdded(World par1World, BlockPos pos, IBlockState state)
    {
        this.setDefaultFacing(par1World, pos, state);
    }   
    
    private void setDefaultFacing(World par1World, BlockPos pos, IBlockState state) {
    	Block block = par1World.getBlockState(pos.north()).getBlock();
        Block block1 = par1World.getBlockState(pos.south()).getBlock();
        Block block2 = par1World.getBlockState(pos.west()).getBlock();
        Block block3 = par1World.getBlockState(pos.east()).getBlock();
        EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);

        if (enumfacing == EnumFacing.NORTH && block.isFullBlock() && !block1.isFullBlock())
        {
            enumfacing = EnumFacing.SOUTH;
        }
        else if (enumfacing == EnumFacing.SOUTH && block1.isFullBlock() && !block.isFullBlock())
        {
            enumfacing = EnumFacing.NORTH;
        }
        else if (enumfacing == EnumFacing.WEST && block2.isFullBlock() && !block3.isFullBlock())
        {
            enumfacing = EnumFacing.EAST;
        }
        else if (enumfacing == EnumFacing.EAST && block3.isFullBlock() && !block2.isFullBlock())
        {
            enumfacing = EnumFacing.WEST;
        }

        par1World.setBlockState(pos, state.withProperty(FACING, enumfacing), 2);		
	}

    public boolean canProvidePower(){
        return true;
    }
    
    /**
     * Returns true if the block is emitting indirect/weak redstone power on the specified side. If isBlockNormalCube
     * returns true, standard redstone propagation rules will apply instead and this will not be called. Args: World, X,
     * Y, Z, side. Note that the side is reversed - eg it is 1 (up) when checking the bottom of the block.
     */
    public int isProvidingWeakPower(IBlockAccess par1IBlockAccess, BlockPos pos, IBlockState state, EnumFacing side){
    	if(((Boolean) state.getValue(POWERED)).booleanValue()){
    		return 15;
    	}else{
    		return 0;
    	}
    }
    
    /**
     * Returns true if the block is emitting direct/strong redstone power on the specified side. Args: World, X, Y, Z,
     * side. Note that the side is reversed - eg it is 1 (up) when checking the bottom of the block.
     */
    public int isProvidingStrongPower(IBlockAccess par1IBlockAccess, BlockPos pos, IBlockState state, EnumFacing side){  	
    	if(((Boolean) state.getValue(POWERED)).booleanValue()){
    		return 15;
    	}else{
    		return 0;
    	}
    }
    
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(POWERED, false);
    }
    
    @SideOnly(Side.CLIENT)
    public IBlockState getStateForEntityRender(IBlockState state)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.SOUTH);
    }

    public IBlockState getStateFromMeta(int meta)
    {
		if(meta == 15) return this.getDefaultState();

        if(meta <= 5){
        	return this.getDefaultState().withProperty(FACING, EnumFacing.values()[meta].getAxis() == EnumFacing.Axis.Y ? EnumFacing.NORTH : EnumFacing.values()[meta]).withProperty(POWERED, false);
        }else{
        	return this.getDefaultState().withProperty(FACING, EnumFacing.values()[meta - 6]).withProperty(POWERED, true);
        }
    }

    public int getMetaFromState(IBlockState state)
    {
    	if(state.getProperties().containsKey(POWERED) && ((Boolean) state.getValue(POWERED)).booleanValue()){
    		return (((EnumFacing) state.getValue(FACING)).getIndex() + 6);
    	}else{
    		if(!state.getProperties().containsKey(FACING)) return 15;
    		
    		return ((EnumFacing) state.getValue(FACING)).getIndex();
    	}
    }
    
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IBlockState disguisedState = getDisguisedBlockState(world, pos);
    	
        return disguisedState != null ? disguisedState : state;
    }
    
    public IBlockState getDisguisedBlockState(IBlockAccess world, BlockPos pos) {
    	if(world.getTileEntity(pos) instanceof TileEntityKeypad) {
        	TileEntityKeypad te = (TileEntityKeypad) world.getTileEntity(pos);
            
        	ItemStack module = te.hasModule(EnumCustomModules.DISGUISE) ? te.getModule(EnumCustomModules.DISGUISE) : null;
            
        	if(module != null && !((ItemModule) module.getItem()).getBlockAddons(module.getTagCompound()).isEmpty()) {
                ItemStack disguisedStack = ((ItemModule) module.getItem()).getAddons(module.getTagCompound()).get(0);
                Block block = Block.getBlockFromItem(disguisedStack.getItem());
                boolean hasMeta = disguisedStack.getHasSubtypes();
                
                IBlockState disguisedModel = block.getStateFromMeta(hasMeta ? disguisedStack.getItemDamage() : getMetaFromState(world.getBlockState(pos)));
                
                if (block != this) {
                    return block.getActualState(disguisedModel, world, pos);
                }
            }     	
        }
        
        return null;
    }
    
    public ItemStack getDisguisedStack(IBlockAccess world, BlockPos pos) {
    	if(world.getTileEntity(pos) instanceof TileEntityKeypad) {
        	TileEntityKeypad te = (TileEntityKeypad) world.getTileEntity(pos);
            
        	ItemStack stack = te.hasModule(EnumCustomModules.DISGUISE) ? te.getModule(EnumCustomModules.DISGUISE) : null;
            
        	if(stack != null && !((ItemModule) stack.getItem()).getBlockAddons(stack.getTagCompound()).isEmpty()) {
                ItemStack disguisedStack = ((ItemModule) stack.getItem()).getAddons(stack.getTagCompound()).get(0);
                
                if(Block.getBlockFromItem(disguisedStack.getItem()) != this) {
                    return disguisedStack;
                }
            }      	
        }
        
        return null;
    }
    
    @SideOnly(Side.CLIENT)
    public Item getItem(World worldIn, BlockPos pos) {
    	return null;
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] {FACING, POWERED});
    }

    /**
     * Returns a new instance of a block's tile entity class. Called on placing the block.
     */
    public TileEntity createNewTileEntity(World par1World, int par2){
        return new TileEntityKeypad();
    }

	public ItemStack getDisplayStack(World world, IBlockState state, BlockPos pos) {
		ItemStack stack = getDisguisedStack(world, pos);
		
		return stack != null ? stack : new ItemStack(this);	
	}

	public boolean shouldShowSCInfo(World world, IBlockState state, BlockPos pos) {
		return !(getDisguisedStack(world, pos) != null);
	}

}
