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

import com.dizzyd.coregen.CoreGen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.Random;

public class ClusterFeature extends Feature {
    private int count;
    private int variance;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getVariance() {
        return variance;
    }

    public void setVariance(int variance) {
        this.variance = variance;
    }

    protected void generate(Random random, BlockPos pos, World world, int yawDeg, int pitchDeg, double wfactor, double lfactor, double hfactor) {
        double yaw = Math.toRadians(yawDeg);
        double pitch = Math.toRadians(pitchDeg);

        int size = (int) (count + (random.nextGaussian() * variance));

        int width = (int) Math.floor(Math.pow(size, wfactor));
        int length = (int) Math.floor(Math.pow(size, lfactor));
        int height = (int) Math.ceil(Math.pow(size, hfactor));

        int total = 0;

        outerloop:
        for (int x = pos.getX(); x < pos.getX() + width; x++) {
            for (int z = pos.getZ(); z < pos.getZ() + length; z++) {
                for (int i = 1; i < height; i++) {
                    double xPos = x + (i * Math.cos(pitch) * Math.cos(yaw));
                    double zPos = z + (i * Math.cos(pitch) * Math.sin(yaw));
                    double yPos = pos.getY() + (i * Math.sin(pitch));
                    world.setBlockState(new BlockPos(xPos, yPos, zPos),
                                        blocks.chooseBlock(random), 2 | 16);
                    if (total++ > size) {
                        break outerloop;
                    }
                }
            }
        }

        CoreGen.logger.info("Generated deposit at {} - {} blocks ({} expected); {},{},{} ", pos, total, size, width, length, height);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        BlockPos pos = new BlockPos((chunkX * 16 + 8) + random.nextInt(16),
                                    ydist.chooseLevel(random),
                                    (chunkZ * 16 + 8) + random.nextInt(16));
        generate(random, pos, world, 0, 90, 0.4, 0.2, 0.4);
    }

}