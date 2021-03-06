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

package com.dizzyd.coregen.config;

import com.dizzyd.coregen.feature.Feature;
import com.dizzyd.coregen.world.WorldData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Random;

public class Deposit {
    private String id;
    private int chunkChance;
    private Restrictions restrictions;
    private List<Feature> features = new ArrayList<Feature>();
    private boolean enabled;
    private LongSummaryStatistics stats = new LongSummaryStatistics();

    public LongSummaryStatistics getStats() {
        return stats;
    }

    public void resetStats() {
        stats = new LongSummaryStatistics();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getChunkChance() {
        return chunkChance;
    }

    public void setChunkChance(int chunkChance) {
        this.chunkChance = chunkChance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Restrictions getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(Restrictions restrictions) {
        this.restrictions = restrictions;
    }

    public void addFeature(Feature f) {
        features.add(f);
    }

    public boolean generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        long startTime = System.currentTimeMillis();

        int blocksPlaced = 0;
        for (Feature f : features) {
            blocksPlaced += f.generate(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
        }

        synchronized (stats) {
            stats.accept(System.currentTimeMillis() - startTime);
        }

        return blocksPlaced > 0;
    }

    public boolean canGenerate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        // If this deposit is not enabled, don't allow it to generate automatically
        // N.B. if the deposit generation is being forced this flag will be ignored
        if (!enabled) {
            return false;
        }

        // See if the odds are in our favor...
        int sample = random.nextInt(100);
        if (sample > getChunkChance()) {
            return false;
        }

        // Check dimension restrictions
        if (!restrictions.isValidDimension(world.provider.getDimension())) {
            return false;
        }

        // Check biome restrictions on this chunk; note that we only check to see if ANY biomes
        // in this chunk are allowed for generation.
        if (!restrictions.hasValidBiomes(chunkProvider.getLoadedChunk(chunkX, chunkZ))) {
            return false;
        }

        // Check for a min-distance restriction to see if this deposit
        // is outside the range of any other like deposits
        int minDepositDistance = restrictions.getMinDepositDistance();
        if (minDepositDistance > 0 && WorldData.depositInDistance(world, id, chunkX, chunkZ, minDepositDistance)) {
            return false;
        }

        // Calculate the distance to spawn (in chunks)
        // N.B. we convert distance to approximate chunks by dividing by 16
        BlockPos spawnPos = world.getSpawnPoint();
        double spawnDist = spawnPos.getDistance(chunkX * 16 + 8, spawnPos.getY(), chunkZ * 16 + 8) / 16;

        // If we're too far away from spawn, generation is blocked
        if (restrictions.getMaxSpawnDistance() > 0 && spawnDist > restrictions.getMaxSpawnDistance()) {
            return false;
        }

        // If we're too close to spawn, generation is blocked
        if (restrictions.getMinSpawnDistance() > 0 && spawnDist < restrictions.getMinSpawnDistance()) {
            return false;
        }

        return true;
    }
}
