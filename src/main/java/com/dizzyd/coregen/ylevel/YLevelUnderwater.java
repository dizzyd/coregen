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
package com.dizzyd.coregen.ylevel;

import com.dizzyd.coregen.CoreGen;
import com.dizzyd.coregen.util.BlockStateParser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.IdentityHashMap;
import java.util.List;

public class YLevelUnderwater extends YLevelDistribution {

    private int max;
    private int min;
    private List<String> liquids;
    private IdentityHashMap<IBlockState, Boolean> liquidStates;

    public int getMin() { return min; }

    public void setMin(int min) { this.min = min; }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public List<String> getLiquids() {
        return liquids;
    }

    public void setLiquids(List<String> liquids) {
        this.liquids = liquids;

        // Parse each of the string identifiers into block states
        liquidStates = new IdentityHashMap<IBlockState, Boolean>();
        for (String id : liquids) {
            IBlockState bs = BlockStateParser.parse(id);
            if (!bs.getMaterial().isLiquid()) {
                CoreGen.logger.warn("WARNING: Setting up underwater y-level; {} is not a liquid block!");
            }
            liquidStates.put(bs, true);
        }
    }

    @Override
    public int chooseLevel(World world, int x, int z) {
        // Starting at x,min,z, scan up for first liquid block on our list of liquids
        BlockPos p = new BlockPos(x, min, z);
        while (p.getY() < max && !liquidStates.containsKey(world.getBlockState(p))) {
            p = p.up();
        }

        if (p.getY() >= max) {
            // We've looked beyond our limit; bail
            return 0;
        }

        // Step back down to last non-liquid block
        p = p.down();
        return p.getY();
    }
}
