package net.geforcemods.securitycraft.handlers;

import java.util.HashMap;
import java.util.Random;

import net.geforcemods.securitycraft.api.CustomizableSCTE;
import net.geforcemods.securitycraft.api.EnumLinkedAction;
import net.geforcemods.securitycraft.api.INameable;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.IPasswordProtected;
import net.geforcemods.securitycraft.blocks.BlockLaserBlock;
import net.geforcemods.securitycraft.blocks.BlockOwnable;
import net.geforcemods.securitycraft.blocks.BlockSecurityCamera;
import net.geforcemods.securitycraft.entity.EntitySecurityCamera;
import net.geforcemods.securitycraft.gui.GuiHandler;
import net.geforcemods.securitycraft.ircbot.SCIRCBot;
import net.geforcemods.securitycraft.items.ItemModule;
import net.geforcemods.securitycraft.main.mod_SecurityCraft;
import net.geforcemods.securitycraft.misc.CustomDamageSources;
import net.geforcemods.securitycraft.misc.SCSounds;
import net.geforcemods.securitycraft.network.packets.PacketCPlaySoundAtPos;
import net.geforcemods.securitycraft.tileentity.TileEntityOwnable;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.GuiUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ForgeEventHandler {
	
	private static HashMap<String, String> tipsWithLink = new HashMap<String, String>();
	
	public ForgeEventHandler()
	{
		tipsWithLink.put("trello", "https://trello.com/b/dbCNZwx0/securitycraft");
		tipsWithLink.put("patreon", "https://www.patreon.com/Geforce");
	}
	
	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerLoggedInEvent event){
		mod_SecurityCraft.instance.createIrcBot(event.player.getName());

        String tipKey = getRandomTip();
		
		ITextComponent TextComponentString;
		if(tipsWithLink.containsKey(tipKey.split("\\.")[2])) {
			TextComponentString = new TextComponentString("[" + TextFormatting.GOLD + "SecurityCraft" + TextFormatting.WHITE + "] " + I18n.format("messages.thanks").replace("#", mod_SecurityCraft.getVersion()) + " " + I18n.format("messages.tip") + " " + I18n.format(tipKey) + " ").appendSibling(ForgeHooks.newChatWithLinks(tipsWithLink.get(tipKey.split("\\.")[2])));
		}
		else {
			TextComponentString = new TextComponentString("[" + TextFormatting.GOLD + "SecurityCraft" + TextFormatting.WHITE + "] " + I18n.format("messages.thanks").replace("#", mod_SecurityCraft.getVersion()) + " " + I18n.format("messages.tip") + " " + I18n.format(tipKey));
		}
    	
		if(mod_SecurityCraft.configHandler.sayThanksMessage){
			event.player.addChatComponentMessage(TextComponentString);	
		}
	}
	
	@SubscribeEvent
	public void onDamageTaken(LivingHurtEvent event)
	{
		if(event.getEntityLiving() != null && PlayerUtils.isPlayerMountedOnCamera(event.getEntityLiving())){
			event.setCanceled(true);
			return;
		}
		
		if(event.getSource() == CustomDamageSources.electricity)
			mod_SecurityCraft.network.sendToAll(new PacketCPlaySoundAtPos(event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ, SCSounds.ELECTRIFIED.path, 0.25F));
	}
	
	@SubscribeEvent
	public void onBucketUsed(FillBucketEvent event){
		ItemStack result = fillBucket(event.getWorld(), event.getTarget().getBlockPos());
		if(result == null){ return; }
		event.setFilledBucket(result);
		event.setResult(Result.ALLOW);	
	}
	
	@SubscribeEvent
	public void onServerChatEvent(ServerChatEvent event)
	{
		SCIRCBot bot = mod_SecurityCraft.instance.getIrcBot(event.getPlayer().getName());
		
		if(bot != null && bot.getMessageMode())
		{
			event.setCanceled(true);
			bot.sendMessage("> " + event.getMessage());
			bot.sendMessageToPlayer(TextFormatting.GRAY + "<" + event.getPlayer().getName() + " --> IRC> " + event.getMessage(), event.getPlayer());
		}
	}
	
	@SubscribeEvent 
	public void onPlayerLoggedOut(PlayerLoggedOutEvent event){
		if(mod_SecurityCraft.configHandler.disconnectOnWorldClose && mod_SecurityCraft.instance.getIrcBot(event.player.getName()) != null){
			mod_SecurityCraft.instance.getIrcBot(event.player.getName()).disconnect();
			mod_SecurityCraft.instance.removeIrcBot(event.player.getName());
		}		
	}
	
	@SubscribeEvent 
	public void onRightClickBlock(RightClickBlock event){
		if(event.getHand() == EnumHand.MAIN_HAND)
		{
			if(!event.getEntityPlayer().worldObj.isRemote){
				World world = event.getEntityPlayer().worldObj;
				TileEntity tileEntity = event.getEntityPlayer().worldObj.getTileEntity(event.getPos());
				Block block = event.getEntityPlayer().worldObj.getBlockState(event.getPos()).getBlock();
				
				if(PlayerUtils.isHoldingItem(event.getEntityPlayer(), mod_SecurityCraft.codebreaker) && handleCodebreaking(event)) {
					event.setCanceled(true);
					return;
				}
				
				if(tileEntity != null && tileEntity instanceof CustomizableSCTE && PlayerUtils.isHoldingItem(event.getEntityPlayer(), mod_SecurityCraft.universalBlockModifier)){
					event.setCanceled(true);
					
					if(!((IOwnable) tileEntity).getOwner().isOwner(event.getEntityPlayer())){
						PlayerUtils.sendMessageToPlayer(event.getEntityPlayer(), I18n.format("item.universalBlockModifier.name"), I18n.format("messages.notOwned").replace("#", ((TileEntityOwnable) tileEntity).getOwner().getName()), TextFormatting.RED);
						return;
					}
					
					event.getEntityPlayer().openGui(mod_SecurityCraft.instance, GuiHandler.CUSTOMIZE_BLOCK, world, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());	
					return;
				}
				
				if(tileEntity instanceof INameable && ((INameable) tileEntity).canBeNamed() && PlayerUtils.isHoldingItem(event.getEntityPlayer(), Items.NAME_TAG) && event.getEntityPlayer().inventory.getCurrentItem().hasDisplayName()){
					event.setCanceled(true);
					
					for(String character : new String[]{"(", ")"}) {
						if(event.getEntityPlayer().inventory.getCurrentItem().getDisplayName().contains(character)) {
							PlayerUtils.sendMessageToPlayer(event.getEntityPlayer(), "Naming", I18n.format("messages.naming.error").replace("#n", event.getEntityPlayer().inventory.getCurrentItem().getDisplayName()).replace("#c", character), TextFormatting.RED);
							return;
						}
					}		
					
					if(((INameable) tileEntity).getCustomName().matches(event.getEntityPlayer().inventory.getCurrentItem().getDisplayName())) {
						PlayerUtils.sendMessageToPlayer(event.getEntityPlayer(), "Naming", I18n.format("messages.naming.alreadyMatches").replace("#n", ((INameable) tileEntity).getCustomName()), TextFormatting.RED);
						return;
					}
	
					event.getEntityPlayer().inventory.getCurrentItem().stackSize--;
	
					((INameable) tileEntity).setCustomName(event.getEntityPlayer().inventory.getCurrentItem().getDisplayName());
					return;
				}
				
				if(tileEntity != null && isOwnableBlock(block, tileEntity) && PlayerUtils.isHoldingItem(event.getEntityPlayer(), mod_SecurityCraft.universalBlockRemover)){
					event.setCanceled(true);
	
					if(!((IOwnable) tileEntity).getOwner().isOwner(event.getEntityPlayer())){
						PlayerUtils.sendMessageToPlayer(event.getEntityPlayer(), I18n.format("item.universalBlockRemover.name"), I18n.format("messages.notOwned").replace("#", ((TileEntityOwnable) tileEntity).getOwner().getName()), TextFormatting.RED);
						return;
					}
	
					if(block == mod_SecurityCraft.laserBlock){
						world.destroyBlock(event.getPos(), true);
						BlockLaserBlock.destroyAdjacentLasers(event.getWorld(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
						event.getEntityPlayer().inventory.getCurrentItem().damageItem(1, event.getEntityPlayer());
					}else{
						world.destroyBlock(event.getPos(), true);
						world.removeTileEntity(event.getPos());
						event.getEntityPlayer().inventory.getCurrentItem().damageItem(1, event.getEntityPlayer());
					}
				}
			}
		}
	}
	
	@SubscribeEvent
    public void onConfigChanged(OnConfigChangedEvent event) {
        if(event.getModID().equals("securitycraft")){
        	mod_SecurityCraft.configFile.save();
        	
        	mod_SecurityCraft.configHandler.setupConfiguration();
        }
    }
	
	@SubscribeEvent
	public void onBlockPlaced(PlaceEvent event) {
		handleOwnableTEs(event);
	}
	
	@SubscribeEvent
	public void onBlockBroken(BreakEvent event){
		if(!event.getWorld().isRemote){
			if(event.getWorld().getTileEntity(event.getPos()) != null && event.getWorld().getTileEntity(event.getPos()) instanceof CustomizableSCTE){
				CustomizableSCTE te = (CustomizableSCTE) event.getWorld().getTileEntity(event.getPos());

				for(int i = 0; i < te.getNumberOfCustomizableOptions(); i++){
					if(te.itemStacks[i] != null){
						ItemStack stack = te.itemStacks[i];
						EntityItem item = new EntityItem(event.getWorld(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), stack);
						event.getWorld().spawnEntityInWorld(item);
						
						te.onModuleRemoved(stack, ((ItemModule) stack.getItem()).getModule());
						te.createLinkedBlockAction(EnumLinkedAction.MODULE_REMOVED, new Object[]{ stack, ((ItemModule) stack.getItem()).getModule() }, te);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event)
	{
		if(event.getTarget() != null && event.getTarget() instanceof EntityPlayer && event.getTarget() != event.getEntityLiving().getAttackingEntity())
		{
			if(PlayerUtils.isPlayerMountedOnCamera(event.getTarget()))
				((EntityLiving)event.getEntityLiving()).setAttackTarget(null);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPlayerRendered(RenderPlayerEvent.Pre event) {
		if(PlayerUtils.isPlayerMountedOnCamera(event.getEntityPlayer())){
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderGameOverlay(RenderGameOverlayEvent event) {
		if(Minecraft.getMinecraft().thePlayer != null && PlayerUtils.isPlayerMountedOnCamera(Minecraft.getMinecraft().thePlayer)){
			if(event.getType() == RenderGameOverlayEvent.ElementType.EXPERIENCE && ((BlockUtils.getBlock(Minecraft.getMinecraft().theWorld, BlockUtils.toPos((int)Math.floor(Minecraft.getMinecraft().thePlayer.getRidingEntity().posX), (int)(Minecraft.getMinecraft().thePlayer.getRidingEntity().posY - 1.0D), (int)Math.floor(Minecraft.getMinecraft().thePlayer.getRidingEntity().posZ))) instanceof BlockSecurityCamera))){
				GuiUtils.drawCameraOverlay(Minecraft.getMinecraft(), Minecraft.getMinecraft().ingameGUI, event.getResolution(), Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().theWorld, BlockUtils.toPos((int)Math.floor(Minecraft.getMinecraft().thePlayer.getRidingEntity().posX), (int)(Minecraft.getMinecraft().thePlayer.getRidingEntity().posY - 1.0D), (int)Math.floor(Minecraft.getMinecraft().thePlayer.getRidingEntity().posZ)));
			}
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void fovUpdateEvent(FOVUpdateEvent event){
		if(PlayerUtils.isPlayerMountedOnCamera(event.getEntity())){
			event.setNewfov(((EntitySecurityCamera) event.getEntity().getRidingEntity()).getZoomAmount());
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderHandEvent(RenderHandEvent event){
		if(PlayerUtils.isPlayerMountedOnCamera(Minecraft.getMinecraft().thePlayer)){
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onMouseClicked(MouseEvent event) {
		if(Minecraft.getMinecraft().theWorld != null)
		{
			if(PlayerUtils.isPlayerMountedOnCamera(Minecraft.getMinecraft().thePlayer))
			{
				event.setCanceled(true);
			}
		}
	}

	private ItemStack fillBucket(World world, BlockPos pos){
		Block block = world.getBlockState(pos).getBlock();
		
		if(block == mod_SecurityCraft.bogusWater){
			world.setBlockToAir(pos);
			return new ItemStack(mod_SecurityCraft.fWaterBucket, 1);
		}else if(block == mod_SecurityCraft.bogusLava){
			world.setBlockToAir(pos);
			return new ItemStack(mod_SecurityCraft.fLavaBucket, 1);
		}else{
			return null;
		}
	}
	
	private void handleOwnableTEs(PlaceEvent event) {
		if(event.getWorld().getTileEntity(event.getPos()) instanceof IOwnable) {
			String name = event.getPlayer().getName();
			String uuid = event.getPlayer().getGameProfile().getId().toString();

			((IOwnable) event.getWorld().getTileEntity(event.getPos())).getOwner().set(uuid, name);
		}		
	}
	
	private boolean handleCodebreaking(PlayerInteractEvent event) {
		World world = event.getEntityPlayer().worldObj;
		TileEntity tileEntity = event.getEntityPlayer().worldObj.getTileEntity(event.getPos());
		
		if(tileEntity != null && tileEntity instanceof IPasswordProtected) {
			return ((IPasswordProtected) tileEntity).onCodebreakerUsed(world.getBlockState(event.getPos()), event.getEntityPlayer(), !mod_SecurityCraft.configHandler.allowCodebreakerItem);
		}
		
		return false;
	}
	
	private String getRandomTip(){
		String[] tips = {
				"messages.tip.scHelp",
				"messages.tip.scConnect",
				"messages.tip.trello",
				"messages.tip.patreon"
		};

		return tips[new Random().nextInt(tips.length)];
	}
  
	private boolean isOwnableBlock(Block block, TileEntity tileEntity){
		return (tileEntity instanceof TileEntityOwnable || tileEntity instanceof IOwnable || block instanceof BlockOwnable);
    }
	
}
