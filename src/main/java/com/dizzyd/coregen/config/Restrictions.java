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

import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class Restrictions {
    private int minDepositDistance = 0;
    private int minSpawnDistance = 0; // Minimum distance from spawn
    private int maxSpawnDistance = 0; // Maximum distance from spawn

    private Set<String> biomes;
    private BitSet      cachedBiomes;

    private Set<Integer> dimensions;

    public int getMinDepositDistance() {
        return minDepositDistance;
    }

    public void setMinDepositDistance(int minDepositDistance) {
        this.minDepositDistance = minDepositDistance;
    }

    public int getMinSpawnDistance() {
        return minSpawnDistance;
    }

    public void setMinSpawnDistance(int minSpawnDistance) {
        this.minSpawnDistance = minSpawnDistance;
    }

    public int getMaxSpawnDistance() {
        return maxSpawnDistance;
    }

    public void setMaxSpawnDistance(int maxSpawnDistance) {
        this.maxSpawnDistance = maxSpawnDistance;
    }

    public List<String> getBiomes() {
        return new ArrayList<String>(biomes);
    }

    public void setBiomes(List<String> biomes) {
        // Use a Java stream to convert the list of strings into a set of lower-cased equivalents
        this.biomes = biomes.stream().map(String::toLowerCase).collect(Collectors.toSet());

    }

    public List<Integer> getDimensions() {
        return new ArrayList<Integer>(dimensions);
    }

    public void setDimensions(List<Integer> dimensions) {
        this.dimensions = new HashSet<Integer>(dimensions);
    }

    public void prepare() {
        // Walk over every biome and note if it's allowed
        if (!biomes.isEmpty()) {
            cachedBiomes = new BitSet();
            for (Biome b : ForgeRegistries.BIOMES) {
                cacheBiomeRestriction(b);
            }
        }
    }

    public boolean hasValidBiomes(Chunk c) {
        if (cachedBiomes == null) {
            return true;
        }

        BitSet chunkBiomes = new BitSet();
        for (byte id : c.getBiomeArray()) {
            chunkBiomes.set(Byte.toUnsignedInt(id));
        }

        return chunkBiomes.intersects(cachedBiomes);
    }

    public boolean isValidDimension(int d) {
        if (dimensions == null || dimensions.isEmpty()) {
            return true;
        }

        return dimensions.contains(d);
    }

    private void cacheBiomeRestriction(Biome b) {
        int id = Biome.getIdForBiome(b);

        Set<BiomeDictionary.Type> biomeTypes = BiomeDictionary.getTypes(b);

        // If there is no list of biomes, assume all biomes are permitted
        if (biomes.isEmpty()) {
            cachedBiomes.set(id);
        }

        // Check for biome by name (lower-casing it as necessary)
        if (biomes.contains(b.getRegistryName().getResourcePath().toLowerCase())) {
            cachedBiomes.set(id);
        } else {
            // Check biomes type
            for (BiomeDictionary.Type t : biomeTypes) {
                if (biomes.contains(t.getName().toLowerCase())) {
                    cachedBiomes.set(id);
                    break;
                }
            }
        }
    }
}
