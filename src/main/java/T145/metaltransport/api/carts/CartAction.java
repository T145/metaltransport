package T145.metaltransport.api.carts;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

public abstract class CartAction extends NBTTagCompound {

	protected final String[] blockNames;

	public CartAction(Block[] blocks) {
		this.blockNames = new String[blocks.length];
		byte[] bits = new byte[blocks.length];

		for (short i = 0; i < blocks.length; ++i) {
			bits[i] = (byte) i;
			this.blockNames[i] = blocks[i].getRegistryName().toString();
		}

		if (blocks.length > 1) {
			this.setByteArray("BlockNames", bits);
		} else {
			this.setString("BlockName", this.blockNames[0]);
		}
	}

	public CartAction(Block block) {
		this(new Block[] { block });
	}

	public String[] getBlockNames() {
		return blockNames;
	}

	public boolean hasMultipleBlocks() {
		return blockNames.length > 0;
	}

	public abstract NBTTagCompound serialize();

	public abstract CartAction deserialize();

	public abstract void tick();
}