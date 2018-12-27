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

package com.dizzyd.coregen.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WeightedBlockList {
    private IBlockState singleBlock;

    private ArrayList<IBlockState> blocks = new ArrayList<IBlockState>();

    public void setBlock(String blockId) {
        this.singleBlock = blockIdToState(blockId);
    }

    public void setBlocks(Map<String, Object> blockMap) {
        for (Map.Entry<String, Object> e : blockMap.entrySet()) {
            IBlockState bs = blockIdToState(e.getKey());
            for (int i = 0; i < (Integer)e.getValue(); i++) {
                blocks.add(bs);
            }
        }
        this.singleBlock = null;
    }

    public void setBlocks(List<String> blockIds) {
        for (String id : blockIds) {
            blocks.add(blockIdToState(id));
        }
        this.singleBlock = null;
    }

    public IBlockState chooseBlock(Random r) {
        if (this.singleBlock != null) {
            return this.singleBlock;
        }

        return this.blocks.get(r.nextInt(this.blocks.size()));
    }

    private IBlockState blockIdToState(String id) {
        // <block id>; meta
        String[] parts = id.split(";", 2);
        switch (parts.length) {
            case 1:
                // Only a resource was provided
                return blockIdToState(parts[0], 0);
            case 2:
                // Resource and meta were provided
                return blockIdToState(parts[0], Integer.valueOf(parts[1]));
            default:
                // Yikes
                throw new InvalidBlockId(id);
        }
    }

    private IBlockState blockIdToState(String id, int meta) {
        ResourceLocation loc = new ResourceLocation(id);
        if (ForgeRegistries.BLOCKS.containsKey(loc)) {
            return ForgeRegistries.BLOCKS.getValue(loc).getStateFromMeta(meta);
        }
        throw new InvalidBlockId(id);
    }

    public class InvalidBlockId extends RuntimeException {
        public String blockId;
        public int blockMeta;

        public InvalidBlockId(String id, int meta) {
            this.blockId = id;
            this.blockMeta = meta;
        }

        public InvalidBlockId(String id) {
            this.blockId = id;
        }
    }
}
