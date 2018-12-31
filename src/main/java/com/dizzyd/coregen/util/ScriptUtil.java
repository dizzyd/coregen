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

import com.dizzyd.coregen.CoreGen;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.script.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ScriptUtil {

    private NashornScriptEngine engine;

    private GetBlockFunc blockFunc = new GetBlockFunc();
    private GetBlockStateFunc blockStateFunc = new GetBlockStateFunc();

    public ScriptUtil() {
        engine = (NashornScriptEngine) new ScriptEngineManager().getEngineByName("nashorn");
    }

    public String run(String script, World world, BlockPos position) {
        File f = new File(script);
        try {
            FileReader reader = new FileReader(f.getAbsoluteFile());

            ScriptContext ctx = engine.getContext();
            Bindings bindings = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("block", blockFunc);
            bindings.put("blockState", blockStateFunc);
            bindings.put("world", world);
            bindings.put("pos", position);

            engine.eval(reader, ctx);
        } catch (FileNotFoundException ex) {
            return String.format("%s not found: %s", script, ex);
        } catch (ScriptException ex) {
            return String.format("%s error: %s", script, ex);
        }

        return String.format("%s: ok", script);
    }

    public CompiledScript compile(String script) {
        File f = new File(script);
        try {
            FileReader reader = new FileReader(f.getAbsoluteFile());
            return engine.compile(reader);
        } catch (FileNotFoundException ex) {
            CoreGen.logger.error("Script not found: {}", script);
        } catch (ScriptException ex) {
            CoreGen.logger.error("Script {} failed to compile: {}", script, ex);
        }
        return null;
    }

    private static class GetBlockFunc implements Function<String, Block> {
        @Override
        public Block apply(String blockName) {
            ResourceLocation loc = new ResourceLocation(blockName);
            if (ForgeRegistries.BLOCKS.containsKey(loc)) {
                return ForgeRegistries.BLOCKS.getValue(loc);
            }
            return null;
        }
    }

    private static class GetBlockStateFunc implements BiFunction<String, Integer, IBlockState> {
        @Override
        public IBlockState apply(String blockName, Integer meta) {
            ResourceLocation loc = new ResourceLocation(blockName);
            if (ForgeRegistries.BLOCKS.containsKey(loc)) {
                return ForgeRegistries.BLOCKS.getValue(loc).getStateFromMeta(meta);
            }
            return null;
        }
    }
}