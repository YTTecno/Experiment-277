package com.tecno.experiment.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

public class FakeLightBlock extends Block {

    public FakeLightBlock() {
        // Properties: Invisible, no collision, easily replaceable, emits max light
        super(BlockBehaviour.Properties.of()
                .replaceable()
                .noCollission()
                .noLootTable()
                .lightLevel(state -> 15)
                .pushReaction(PushReaction.DESTROY)
                .air());
    }

    // This makes the block completely invisible
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    // When the block is placed, we tell it to tick (update) after 3 ticks
    @Override
    public void onPlace(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 20);
        }
    }

    // When the tick fires, the block deletes itself (turning back into air)
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }
}