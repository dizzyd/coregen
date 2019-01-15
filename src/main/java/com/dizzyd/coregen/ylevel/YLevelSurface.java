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

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class YLevelSurface extends YLevelDistribution {

    private int max;

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    @Override
    public int chooseLevel(World w, int x, int z) {
        // Starting at x,60,z, scan up for first air-gapped (aka "surface")
        // N.B. "sea-level" is 64, so start slightly below that
        BlockPos p = new BlockPos(x, 60, z);
        while (p.getY() < max && !w.isAirBlock(p)) {
            p = p.up();
        }

        if (p.getY() >= max) {
            // We've looked beyond our limit; bail
            return 0;
        }

        // Step back down to last non-air block
        p = p.down();
        if (!w.getBlockState(p).isSideSolid(w, p, EnumFacing.UP)) {
            // Top block is not solid on top; bail
            return 0;
        }

        return p.getY();
    }
}
