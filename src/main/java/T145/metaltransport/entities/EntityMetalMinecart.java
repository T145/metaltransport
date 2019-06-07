package T145.metaltransport.entities;

import T145.metaltransport.api.ItemsMT;
import T145.metaltransport.api.SerializersMT;
import T145.metaltransport.api.carts.IMetalMinecart;
import T145.metaltransport.api.carts.IMinecartBlock;
import T145.metaltransport.api.constants.CartType;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class EntityMetalMinecart extends EntityMinecartEmpty implements IMetalMinecart {

	private static final DataParameter<CartType> CART_TYPE = EntityDataManager.createKey(EntityMetalMinecart.class, SerializersMT.CART_TYPE);
	private static final DataParameter<ItemStack> DISPLAY_DATA = EntityDataManager.createKey(EntityMetalMinecart.class, DataSerializers.ITEM_STACK);

	public EntityMetalMinecart(World world) {
		super(world);
	}

	public EntityMetalMinecart(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityMetalMinecart(EntityMinecart cart) {
		this(cart.getEntityWorld(), cart.prevPosX, cart.prevPosY, cart.prevPosZ);

		if (cart instanceof EntityMetalMinecart) {
			this.setCartType(((EntityMetalMinecart) cart).getCartType());
		} else if (cart instanceof EntityMinecartEmpty) {
			this.setCartType(CartType.IRON);
		} else if (cart.hasDisplayTile()) {
			this.setDisplayTile(cart.getDisplayTile());
		}

		this.posX = cart.posX;
		this.posY = cart.posY;
		this.posZ = cart.posZ;
		this.motionX = cart.motionX;
		this.motionY = cart.motionY;
		this.motionZ = cart.motionZ;
		this.rotationPitch = cart.rotationPitch;
		this.rotationYaw = cart.rotationYaw;
	}

	public EntityMetalMinecart setDisplayState(IBlockState state) {
		this.setDisplayTile(state);
		return this;
	}

	public EntityMetalMinecart setDisplayBlock(Block block) {
		if (block instanceof IMinecartBlock) {
			return this.setDisplayState(((IMinecartBlock) block).getDisplayState(this, this.getDisplayData()));
		} else {
			// add any special vanilla block exceptions here
			return this.setDisplayState(block.getDefaultState());
		}
	}

	public EntityMetalMinecart setDisplayItem(Item item) {
		return this.setDisplayBlock(Block.getBlockFromItem(item));
	}

	public EntityMetalMinecart setDisplayStack(ItemStack stack) {
		if (stack.hasTagCompound()) {
			this.dataManager.set(DISPLAY_DATA, stack);
		}
		return this.setDisplayItem(stack.getItem());
	}

	private ItemStack getDisplayData() {
		return this.dataManager.get(DISPLAY_DATA);
	}

	@Override
	public CartType getCartType() {
		return dataManager.get(CART_TYPE);
	}

	@Override
	public EntityMinecart setCartType(CartType type) {
		dataManager.set(CART_TYPE, type);
		return this;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(CART_TYPE, CartType.IRON);
		dataManager.register(DISPLAY_DATA, ItemStack.EMPTY);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setString(TAG_CART_TYPE, getCartType().toString());
		NBTTagCompound stackTag = new NBTTagCompound();
		this.getDisplayData().writeToNBT(stackTag);
		tag.setTag(TAG_DISPLAY_DATA, stackTag);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		setCartType(CartType.valueOf(tag.getString(TAG_CART_TYPE)));
		NBTTagCompound stackTag = tag.getCompoundTag(TAG_DISPLAY_DATA);
		ItemStack stack = new ItemStack(stackTag);
		this.dataManager.set(DISPLAY_DATA, stack);
	}

	@Override
	public ItemStack getCartItem() {
		return new ItemStack(ItemsMT.METAL_MINECART, 1, getCartType().ordinal());
	}

	@Override
	public boolean canBeRidden() {
		return !this.hasDisplayTile();
	}

	@Override
	public String getName() {
		if (hasCustomName()) {
			return getCustomNameTag();
		} else {
			return I18n.format(String.format("item.metaltransport:metal_minecart.%s.name", getCartType().getName()));
		}
	}

	protected void dropDisplayStack() {
		if (!this.world.isRemote) {
			ItemStack data = this.getDisplayData();
			entityDropItem(data.isEmpty() ? new ItemStack(this.getDisplayTile().getBlock()) : data, 0.0F);
		}
	}

	@Override
	public void killMinecart(DamageSource source) {
		super.killMinecart(source);

		if (this.hasDisplayTile() && world.getGameRules().getBoolean("doEntityDrops")) {
			this.dropDisplayStack();
		}
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
		if (super.processInitialInteract(player, hand) || this.isBeingRidden()) {
			return true;
		}

		if (player.isSneaking()) {
			if (this.hasDisplayTile()) {
				this.dropDisplayStack();
				this.setDisplayTile(getDefaultDisplayTile());
				this.setHasDisplayTile(false);
				return true;
			} else {
				return false;
			}
		}

		if (!this.world.isRemote) {
			player.startRiding(this);
		}

		return true;
	}
}
