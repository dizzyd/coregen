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

import com.dizzyd.coregen.CoreGen;
import com.dizzyd.coregen.feature.Feature;
import com.dizzyd.coregen.world.WorldData;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Deposit {
    private String id;
    private int chunkChance;
    private Restrictions restrictions;
    private List<Feature> features = new ArrayList<Feature>();

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

    public boolean generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider, boolean force) {
        if (!force && !canGenerate(world, chunkX, chunkZ)) {
            return false;
        }

        CoreGen.logger.info("Creating deposit {} at {}, {}", id, chunkX, chunkZ);

        for (Feature f : features) {
            f.generate(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
        }

        return true;
    }

    private boolean canGenerate(World world, int chunkX, int chunkZ) {
        // Check for a min-distance restriction to see if this deposit
        // is outside the range of any other deposits
        int minDepositDistance = restrictions.getMinDepositDistance();
        if (minDepositDistance > 0 && WorldData.depositInDistance(world, id, chunkX, chunkZ, minDepositDistance)) {
            return false;
        }

        return true;
    }
}
