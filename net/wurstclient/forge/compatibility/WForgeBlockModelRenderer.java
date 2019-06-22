package net.wurstclient.forge.compatibility;

import java.util.Collections;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.hacks.XRayHack;
import net.minecraft.block.state.IBlockState;
import net.wurstclient.forge.utils.BlockUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;

public class WForgeBlockModelRenderer extends ForgeBlockModelRenderer {
    public WForgeBlockModelRenderer(BlockColors colors) {
        super(colors);
    }

    public boolean renderModel(IBlockAccess blockAccessIn, IBakedModel modelIn, IBlockState blockStateIn, BlockPos blockPosIn, BufferBuilder buffer, boolean checkSides)
    {
        if(ForgeWurst.getForgeWurst().getHax().xRayHack.isEnabled()) {
            if (!isVisible(blockStateIn.getBlock())) {
                return false;
            }

            blockStateIn.getBlock().setLightLevel(100F);
            checkSides = false;
        }

        return super.renderModel(blockAccessIn, modelIn, blockStateIn, blockPosIn, buffer, checkSides);
    }

    private boolean isVisible(Block block) {
        String name = BlockUtils.getName(block);
        int index = Collections.binarySearch(XRayHack.blocks.getBlockNames(), name);

        return index >= 0;
    }
}
