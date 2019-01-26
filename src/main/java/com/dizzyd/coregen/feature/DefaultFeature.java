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
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DefaultFeature extends Feature {

    private List<String> ores = new ArrayList<>();
    private List<Generator> generators = new ArrayList<>();

    public List<String> getOres() {
        return ores;
    }

    public void setOres(List<String> ores) {

        ChunkGeneratorSettings cs = CoreGen.config.getChunkGeneratorSettings();

        for (String ore: ores) {
            switch (ore) {
                case "COAL":
                    generators.add(new Generator(Blocks.COAL_ORE.getDefaultState(), cs.coalSize, cs.coalCount,
                            cs.coalMaxHeight, cs.coalMinHeight));
                    break;
                case "GOLD":
                    generators.add(new Generator(Blocks.GOLD_ORE.getDefaultState(), cs.goldSize, cs.goldCount,
                            cs.goldMaxHeight, cs.goldMinHeight));
                    break;
                case "IRON":
                    generators.add(new Generator(Blocks.IRON_ORE.getDefaultState(), cs.ironSize, cs.ironCount,
                            cs.ironMaxHeight, cs.ironMinHeight));
                    break;
                case "LAPIS":
                    int maxHeight = cs.lapisCenterHeight + cs.lapisSpread;
                    int minHeight = cs.lapisCenterHeight - cs.lapisSpread;
                    generators.add(new Generator(Blocks.LAPIS_ORE.getDefaultState(), cs.lapisSize, cs.lapisCount,
                            maxHeight, minHeight));
                    break;
                case "DIAMOND":
                    generators.add(new Generator(Blocks.DIAMOND_ORE.getDefaultState(), cs.diamondSize, cs.diamondCount,
                            cs.diamondMaxHeight, cs.diamondMinHeight));
                    break;
                case "REDSTONE":
                    generators.add(new Generator(Blocks.REDSTONE_ORE.getDefaultState(), cs.redstoneSize, cs.redstoneCount,
                            cs.redstoneMaxHeight, cs.redstoneMinHeight));
                    break;
                default:
                    CoreGen.logger.error("Ignoring default feature: %s - NOT GENERATED!", ore);
            }
        }
    }

    @Override
    public int generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {

        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        boolean generated = false;

        for (Generator g: generators) {
            generated |= g.generate(world, random, pos);
        }

        return generated ? 1 : 0;
    }

    private static class Generator {
        int count;
        int maxHeight;
        int minHeight;
        WorldGenMinable gen;

        public Generator(IBlockState bs, int size, int count, int maxHeight, int minHeight) {
            this.count = count;
            this.maxHeight = maxHeight;
            this.minHeight = minHeight;
            gen = new WorldGenMinable(bs, size);
        }

        public boolean generate(World world, Random random, ChunkPos cpos) {
            boolean generated = false;

            for (int i = 0; i < count; i++) {
                BlockPos bpos = cpos.getBlock(random.nextInt(16), random.nextInt(maxHeight - minHeight) + minHeight, random.nextInt(16));
                generated |= gen.generate(world, random, bpos);
            }

            return generated;
        }
    }
}
