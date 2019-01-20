// ***************************************************************************
//
//  Copyright 2018-2019 David (Dizzy) Smith, dizzyd@dizzyd.com
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// ***************************************************************************

package com.dizzyd.coregen.feature;

import com.dizzyd.coregen.util.BlockStateParser;
import com.dizzyd.coregen.util.WeightedBlockList;
import com.dizzyd.coregen.ylevel.YLevelDistribution;
import com.typesafe.config.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;

public abstract class Feature {
    protected String type;
    protected WeightedBlockList blocks;
    protected YLevelDistribution ydist;
    protected Config config;

    protected IdentityHashMap<IBlockState, Boolean> targets;
    private List<String> rawTargets;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public WeightedBlockList getBlocks() {
        return blocks;
    }

    public void setTargets(List<String> targets) {
        this.targets = new IdentityHashMap<IBlockState, Boolean>();
        this.rawTargets = new ArrayList<>();

        for (String id: targets) {
            this.targets.put(BlockStateParser.parse(id), true);
            this.rawTargets.add(id);
        }
    }

    public List<String> getTargets() {
        return this.rawTargets;
    }

    public void init(Config config, WeightedBlockList blocks, YLevelDistribution dist) {
        this.config = config;
        this.blocks = blocks;
        this.ydist = dist;
    }

    public boolean placeBlock(World world, Random random, double x, double y, double z) {
        return placeBlock(world, x, y, z, blocks.chooseBlock(random));
    }

    public boolean placeBlock(World world, double x, double y, double z, IBlockState block) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState curr = world.getBlockState(pos);
        if ((targets.isEmpty() && curr.getMaterial().isSolid()) || targets.containsKey(curr)) {
            world.setBlockState(pos, block, 2 | 16);
            return true;
        }
        return false;
    }

    public IBlockState blockFromString(String blockResource) {
        try {
            return BlockStateParser.parse(blockResource);
        } catch (BlockStateParser.InvalidBlockId e) {
            return null;
        }
    }

    public void chatLog(World world, String msg) {
        world.getMinecraftServer().getPlayerList().sendMessage(new TextComponentString(msg));
    }

    public abstract int generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider);

}
