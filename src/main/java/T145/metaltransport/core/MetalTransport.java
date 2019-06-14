package T145.metaltransport.core;

import java.io.IOException;

import T145.metaltransport.api.EntitiesMT;
import T145.metaltransport.api.ItemsMT;
import T145.metaltransport.api.SerializersMT;
import T145.metaltransport.api.carts.CartBehaviorRegistry;
import T145.metaltransport.api.constants.CartType;
import T145.metaltransport.api.constants.RegistryMT;
import T145.metaltransport.client.gui.GuiHandler;
import T145.metaltransport.client.render.entities.RenderMetalMinecart;
import T145.metaltransport.entities.EntityMetalMinecart;
import T145.metaltransport.entities.behaviors.ChestBehavior;
import T145.metaltransport.entities.behaviors.DispenserBehavior;
import T145.metaltransport.entities.behaviors.DropperBehavior;
import T145.metaltransport.entities.behaviors.EnderChestBehavior;
import T145.metaltransport.entities.behaviors.FurnaceBehavior;
import T145.metaltransport.entities.behaviors.JukeboxBehavior;
import T145.metaltransport.entities.behaviors.LampBehavior;
import T145.metaltransport.entities.behaviors.MobSpawnerBehavior;
import T145.metaltransport.entities.behaviors.ObserverBehavior;
import T145.metaltransport.entities.behaviors.PistonBehavior;
import T145.metaltransport.entities.behaviors.SimpleGuiBehavior;
import T145.metaltransport.items.ItemMetalMinecart;
import T145.metaltransport.network.MTPacketHandler;
import T145.tbone.core.TBone;
import T145.tbone.dispenser.BehaviorDispenseMinecart;
import T145.tbone.network.TPacketHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod(modid = RegistryMT.ID, name = RegistryMT.NAME, version = RegistryMT.VERSION, updateJSON = RegistryMT.UPDATE_JSON, dependencies = "required-after:tbone;after:metalchests")
@EventBusSubscriber(modid = RegistryMT.ID)
public class MetalTransport {

	public static final TPacketHandler NETWORK = new MTPacketHandler();

	public MetalTransport() {
		TBone.registerMod(RegistryMT.ID, RegistryMT.NAME);
	}

	@Instance(RegistryMT.ID)
	public static MetalTransport instance;

	@EventHandler
	public void metaltransport$preInit(final FMLPreInitializationEvent event) {
		ModMetadata meta = event.getModMetadata();
		meta.authorList.add("T145");
		meta.autogenerated = false;
		meta.credits = "The fans!";
		meta.description = "Metal in Motion!";
		meta.logoFile = "logo.png";
		meta.modId = RegistryMT.ID;
		meta.name = RegistryMT.NAME;
		meta.url = "https://github.com/T145/metaltransport";
		meta.useDependencyInformation = false;
		meta.version = RegistryMT.VERSION;
		NETWORK.registerMessages();
	}

	@EventHandler
	public void metaltransport$init(final FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
		DataFixer fixer = FMLCommonHandler.instance().getDataFixer();

		// does nothing for now, but done for future-proofing
		EntityMinecart.registerFixesMinecart(fixer, EntityMetalMinecart.class);

		fixer.registerWalker(FixTypes.ENTITY, new IDataWalker() {

			@Override
			public NBTTagCompound process(IDataFixer fixer, NBTTagCompound tag, int version) {
				String id = tag.getString("id");

				if (EntityList.getKey(EntityMetalMinecart.class).equals(new ResourceLocation(id))) {
					tag.setString("id", TileEntity.getKey(TileEntityMobSpawner.class).toString());
					fixer.process(FixTypes.BLOCK_ENTITY, tag, version);
					tag.setString("id", id);
				}

				return tag;
			}
		});
	}

	@EventHandler
	public void metaltransport$postInit(final FMLPostInitializationEvent event) {
		BehaviorDispenseMinecart.register(ItemsMT.METAL_MINECART, ItemMetalMinecart.DISPENSER_BEHAVIOR);
		CartBehaviorRegistry.register(Blocks.ENDER_CHEST, new EnderChestBehavior.EnderChestBehaviorFactory());
		CartBehaviorRegistry.register(Blocks.JUKEBOX, new JukeboxBehavior.JukeboxBehaviorFactory());
		CartBehaviorRegistry.register(Blocks.REDSTONE_LAMP, new LampBehavior.LampBehaviorFactory());
		CartBehaviorRegistry.register(Blocks.LIT_REDSTONE_LAMP, new LampBehavior.LampBehaviorFactory());
		CartBehaviorRegistry.register(Blocks.CRAFTING_TABLE, new SimpleGuiBehavior.SimpleGuiBehaviorFactory());
		CartBehaviorRegistry.register(Blocks.ENCHANTING_TABLE, new SimpleGuiBehavior.SimpleGuiBehaviorFactory());
		CartBehaviorRegistry.register(Blocks.ANVIL, new SimpleGuiBehavior.SimpleGuiBehaviorFactory());
		//CartBehaviorRegistry.register(Blocks.FURNACE, new FurnaceBehavior.FurnaceBehaviorFactory());
		CartBehaviorRegistry.register(Blocks.LIT_FURNACE, new FurnaceBehavior.FurnaceBehaviorFactory());
		CartBehaviorRegistry.register(Blocks.MOB_SPAWNER, new MobSpawnerBehavior.MobSpawnerBehaviorFactory());
		//CartBehaviorRegistry.register(Blocks.TNT, new TNTBehavior.TNTBehaviorFactory());
		CartBehaviorRegistry.register(Blocks.CHEST, new ChestBehavior.ChestBehaviorFactory());
		CartBehaviorRegistry.register(Blocks.TRAPPED_CHEST, new ChestBehavior.ChestBehaviorFactory());
		CartBehaviorRegistry.register(Blocks.DISPENSER, new DispenserBehavior.DispenserBehaviorFactory());
		CartBehaviorRegistry.register(Blocks.DROPPER, new DropperBehavior.DropperBehaviorFactory());
		CartBehaviorRegistry.register(Blocks.PISTON, new PistonBehavior.PistonBehaviorFactory());
		CartBehaviorRegistry.register(Blocks.STICKY_PISTON, new PistonBehavior.PistonBehaviorFactory());
		CartBehaviorRegistry.register(Blocks.OBSERVER, new ObserverBehavior.ObserverBehaviorFactory());
	}

	@SubscribeEvent
	public static void metaltransport$updateConfig(final OnConfigChangedEvent event) {
		if (event.getModID().equals(RegistryMT.ID)) {
			ConfigManager.sync(RegistryMT.ID, Config.Type.INSTANCE);
		}
	}

	@SubscribeEvent
	public static void metaltransport$registerSerializers(final RegistryEvent.Register<DataSerializerEntry> event) {
		final IForgeRegistry<DataSerializerEntry> registry = event.getRegistry();

		registry.register(SerializersMT.ENTRY_CART_TYPE = new DataSerializerEntry(
				SerializersMT.CART_TYPE = new DataSerializer<CartType>() {

					@Override
					public void write(PacketBuffer buf, CartType value) {
						buf.writeEnumValue(value);
					}

					@Override
					public CartType read(PacketBuffer buf) throws IOException {
						return buf.readEnumValue(CartType.class);
					}

					@Override
					public DataParameter<CartType> createKey(int id) {
						return new DataParameter<CartType>(id, this);
					}

					@Override
					public CartType copyValue(CartType value) {
						return value;
					}

				}).setRegistryName(RegistryMT.ID, RegistryMT.KEY_CART_TYPE));
	}

	@SubscribeEvent
	public static void metaltransport$registerItems(final RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();

		registry.register(ItemsMT.METAL_MINECART = new ItemMetalMinecart());
	}

	@SubscribeEvent
	public static void metaltransport$registerEntities(final RegistryEvent.Register<EntityEntry> event) {
		final IForgeRegistry<EntityEntry> registry = event.getRegistry();

		registry.register(EntitiesMT.METAL_MINECART = EntityEntryBuilder.create()
				.id(RegistryMT.KEY_METAL_MINECART, 0)
				.name(RegistryMT.KEY_METAL_MINECART)
				.entity(EntityMetalMinecart.class).tracker(80, 3, true).build());
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void metaltransport$registerModels(final ModelRegistryEvent event) {
		for (CartType type : CartType.values()) {
			TBone.registerModel(RegistryMT.ID, ItemsMT.METAL_MINECART, "item_minecart", type.ordinal(),	String.format("item=%s", type.getName()));
		}

		RenderingRegistry.registerEntityRenderingHandler(EntityMetalMinecart.class, manager -> new RenderMetalMinecart(manager));
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void metalchests$registerRecipes(RegistryEvent.Register<IRecipe> event) {
		CartType.registerRecipes();
	}

	public static Block getBlockFromStack(ItemStack stack) {
		return Block.getBlockFromItem(stack.getItem());
	}

	public static boolean isSolidBlock(ItemStack stack) {
		return getBlockFromStack(stack) != Blocks.AIR /* && block is relatively normal && in whitelist || not in blacklist */;
	}

	@SubscribeEvent
	public static void metaltransport$playerInteract(PlayerInteractEvent.EntityInteractSpecific event) {
		Entity target = event.getTarget();
		EntityPlayer player = event.getEntityPlayer();

		if (target instanceof EntityMinecartEmpty && target.getPassengers().isEmpty()) {
			EntityMinecartEmpty cart = (EntityMinecartEmpty) target;
			EnumHand hand = EnumHand.MAIN_HAND;
			ItemStack stack = player.getHeldItemMainhand();

			if (!isSolidBlock(stack)) {
				stack = player.getHeldItemOffhand();
				hand = EnumHand.OFF_HAND;
			}

			if (!cart.hasDisplayTile() && isSolidBlock(stack)) {
				World world = event.getWorld();
				cart = new EntityMetalMinecart((EntityMinecartEmpty) target).setDisplayStack(stack);

				if (!world.isRemote) {
					if (!player.isCreative()) {
						stack.shrink(1);
					}

					target.setDead();
					world.spawnEntity(cart);
				}

				player.swingArm(hand);
				event.setCancellationResult(EnumActionResult.SUCCESS);
				event.setCanceled(true);
			}
		}
	}
}
