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
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;

import java.util.Random;

public class DefaultFeature extends Feature {
    private String ore;
    private int count;
    private int maxHeight;
    private int minHeight;
    private WorldGenMinable genMinable;

    public String getOre() {
        return ore;
    }

    public void setOre(String ore) {
        this.ore = ore.toUpperCase();

        ChunkGeneratorSettings cs = CoreGen.config.getChunkGeneratorSettings();

        switch (ore) {
            case "COAL":
                count = cs.coalCount;
                maxHeight = cs.coalMaxHeight;
                minHeight = cs.coalMinHeight;
                genMinable = new WorldGenMinable(Blocks.COAL_ORE.getDefaultState(), cs.coalSize);
                break;
            case "GOLD":
                count = cs.goldCount;
                maxHeight = cs.goldMaxHeight;
                minHeight = cs.goldMinHeight;
                genMinable = new WorldGenMinable(Blocks.GOLD_ORE.getDefaultState(), cs.goldSize);
                break;
            case "IRON":
                count = cs.ironCount;
                maxHeight = cs.ironMaxHeight;
                minHeight = cs.ironMinHeight;
                genMinable = new WorldGenMinable(Blocks.IRON_ORE.getDefaultState(), cs.ironSize);
                break;
            case "LAPIS":
                count = cs.lapisCount;
                maxHeight = cs.lapisCenterHeight + cs.lapisSpread;
                minHeight = cs.lapisCenterHeight - cs.lapisSpread;
                genMinable = new WorldGenMinable(Blocks.LAPIS_ORE.getDefaultState(), cs.lapisSize);
                break;
            case "DIAMOND":
                count = cs.diamondCount;
                maxHeight = cs.diamondMaxHeight;
                minHeight = cs.diamondMinHeight;
                genMinable = new WorldGenMinable(Blocks.DIAMOND_ORE.getDefaultState(), cs.diamondSize);
                break;
            case "REDSTONE":
                count = cs.redstoneCount;
                maxHeight = cs.redstoneMaxHeight;
                minHeight = cs.redstoneMinHeight;
                genMinable = new WorldGenMinable(Blocks.REDSTONE_ORE.getDefaultState(), cs.redstoneSize);
                break;
//            case QUARTZ:
//            case EMERALD:
            default:
                CoreGen.logger.error("Ignoring default feature: %s - NOT GENERATED!", ore);
        }
    }

    @Override
    public int generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {

        if (genMinable == null)
            return 0;

        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        boolean generated = false;

        for (int i = 0; i < count; i++) {
            BlockPos bpos = pos.getBlock(random.nextInt(16), random.nextInt(maxHeight - minHeight) + minHeight, random.nextInt(16));
            generated |= genMinable.generate(world, random, bpos);
        }

        return generated ? 1 : 0;
    }
}
