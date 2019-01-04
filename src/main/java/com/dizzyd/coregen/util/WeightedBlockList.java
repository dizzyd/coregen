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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WeightedBlockList {
    private IBlockState singleBlock;

    private ArrayList<IBlockState> blocks = new ArrayList<IBlockState>();

    public void setBlock(String blockId) {
        this.singleBlock = BlockStateParser.parse(blockId);
    }

    public void setBlocks(Map<String, Object> blockMap) {
        for (Map.Entry<String, Object> e : blockMap.entrySet()) {
            IBlockState bs = BlockStateParser.parse(e.getKey());
            for (int i = 0; i < (Integer)e.getValue(); i++) {
                blocks.add(bs);
            }
        }
        this.singleBlock = null;
    }

    public void setBlocks(List<String> blockIds) {
        for (String id : blockIds) {
            blocks.add(BlockStateParser.parse(id));
        }
        this.singleBlock = null;
    }

    public IBlockState chooseBlock(Random r) {
        if (this.singleBlock != null) {
            return this.singleBlock;
        }

        return this.blocks.get(r.nextInt(this.blocks.size()));
    }
}
