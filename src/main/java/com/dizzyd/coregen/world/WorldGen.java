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

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class WorldGen implements IWorldGenerator {
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        // Walk through each deposit in the config
        int sample = ThreadLocalRandom.current().nextInt(100);
        for (Deposit d : CoreGen.config.getDeposits().values())
        {
            // If the sample is greater than the chance of the chunk, no generation will be attempted
            if (sample > d.getChunkChance()) {
                continue;
            }

            // Generation _is_ being attempted; check for a min-distance restriction to see if this deposit
            // is outside the range of any other deposits
            int minDepositDistance = d.getRestrictions().getMinDepositDistance();
            if (minDepositDistance > 0 && WorldData.depositInDistance(world, d.getId(), chunkX, chunkZ, minDepositDistance)) {
                continue;
            }

            // Ok - clear to generate!
            WorldData.addDeposit(world, d.getId(), chunkX, chunkZ);
            d.generate(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
            CoreGen.logger.info("Creating deposit {} at {}, {}", d.getId(), chunkX, chunkZ);
        }
    }
}
