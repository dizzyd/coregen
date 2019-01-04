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
package com.dizzyd.coregen.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BlockStateParser {
    public static IBlockState parse(String id) {
        // <block id>; meta
        String[] parts = id.split(";", 2);
        switch (parts.length) {
            case 1:
                // Only a resource was provided
                return parse(parts[0], 0);
            case 2:
                // Resource and meta were provided
                return parse(parts[0], Integer.valueOf(parts[1]));
            default:
                // Yikes
                throw new InvalidBlockId(id);
        }
    }

    public static IBlockState parse(String id, int meta) {
        ResourceLocation loc = new ResourceLocation(id);
        if (ForgeRegistries.BLOCKS.containsKey(loc)) {
            return ForgeRegistries.BLOCKS.getValue(loc).getStateFromMeta(meta);
        }
        throw new InvalidBlockId(id);
    }

    public static class InvalidBlockId extends RuntimeException {
        public String blockId;
        public int blockMeta;

        public InvalidBlockId(String id, int meta) {
            this.blockId = id;
            this.blockMeta = meta;
        }

        public InvalidBlockId(String id) {
            this.blockId = id;
        }
    }
}
