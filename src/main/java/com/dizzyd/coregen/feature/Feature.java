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

import com.dizzyd.coregen.util.WeightedBlockList;
import com.dizzyd.coregen.ylevel.YLevelDistribution;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.Random;

public abstract class Feature {
    protected String type;
    protected WeightedBlockList blocks;
    protected YLevelDistribution ydist;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void init(WeightedBlockList blocks, YLevelDistribution dist) {
        this.blocks = blocks;
        this.ydist = dist;
    }

    public abstract void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider);

}
