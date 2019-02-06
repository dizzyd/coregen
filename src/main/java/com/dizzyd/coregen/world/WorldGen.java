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

package com.dizzyd.coregen.world;

import com.dizzyd.coregen.CoreGen;
import com.dizzyd.coregen.config.Deposit;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.LongSummaryStatistics;
import java.util.Random;

public class WorldGen implements IWorldGenerator {

    private LongSummaryStatistics stats = new LongSummaryStatistics();

    public LongSummaryStatistics getStats() {
        return stats;
    }

    public void resetStats() {
        stats = new LongSummaryStatistics();
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        boolean generated = false;
        long startTime = System.currentTimeMillis();

        // Ensure there are no stale entries in the deposit R-Tree for this chunk
        WorldData.deleteAllDeposits(world, chunkX, chunkZ);

        // Walk through each deposit in the config
        for (Deposit d : CoreGen.config.getDeposits().values())
        {
            // If the deposit can't be generated, move on to next deposit
            if (!d.canGenerate(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider)) {
                continue;
            }

            // Place a hold on the deposit so that any cascading chunks don't overlap
            WorldData.addDeposit(world, d.getId(), chunkX, chunkZ);

            // If the generation doesn't succeed, back out the change in the world data
            if (!d.generate(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider)) {
                WorldData.deleteDeposit(world, d.getId(), chunkX, chunkZ);
            } else {
                // Generation succeeded; make sure we note that something generated this run
                generated |= true;
            }
        }

        // Update generation stats if generation was attempted
        if (generated) {
            synchronized (stats) {
                stats.accept(System.currentTimeMillis() - startTime);
            }
        }
    }
}
