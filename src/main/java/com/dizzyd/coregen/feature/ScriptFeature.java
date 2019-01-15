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
import com.dizzyd.coregen.scripting.Position;
import com.dizzyd.coregen.util.BlockStateParser;
import com.typesafe.config.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import org.apache.logging.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import java.util.Random;

public class ScriptFeature extends Feature {
    private Script script;
    private int errorCount = 0;
    private ThreadLocal<ScriptingContext> ctxThreadLocal = new ThreadLocal<>();
    private String filename;


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;

        // Try to load/compile the script; this can be safely shared
        // across
        script = CoreGen.scriptUtil.compile(filename);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {

        if (script == null) {
            return;
        }

        // Invoke the "generate" function in the compiled script. We do track sequential failures to avoid spamming the engine
        // and the console. The errorCount is reset on each successful run and incremented on each failed
        // run, such that after ~4 sequential errors, it will disable the chunk generator completely.
        try {
            ScriptingContext ctx = getContext(world, this, random);
            ctx.generate(chunkX, chunkZ);
            errorCount = 0; // Successful invocation, reset error counter
        } catch (RuntimeException e) {
            CoreGen.logger.error("Failed to generate chunk {}, {} using {}: {}", chunkX, chunkZ, filename, e.getMessage());
            if (errorCount < 3) {
                errorCount++;
            } else {
                CoreGen.logger.error("Disabling chunk gen {}; too many sequential failures!", filename);
                script = null;
            }
        }
    }

    private ScriptingContext getContext(World world, Feature feature, Random random) {
        ScriptingContext ctx = ctxThreadLocal.get();
        if (ctx == null) {
            ctx = new ScriptingContext(script);
            ctxThreadLocal.set(ctx);
        }

        ctx.world = world; ctx.feature = feature; ctx.random = random;
        return ctx;
    }

    public class ScriptingContext implements com.dizzyd.coregen.scripting.Context {
        World world;
        Feature feature;
        Random random;

        Context ctx;
        Scriptable scope;
        Function generateFn;

        ScriptingContext(Script script) {
            ctx = Context.enter();
            scope = ctx.initStandardObjects();

            // Evaluate the script so that the scope has our generate function
            script.exec(ctx, scope);
            Object fn = scope.get("generate", scope);
            if (fn instanceof Function) {
                generateFn = (Function)fn;
            }

        }

        protected void generate(int cx, int cz) {
            com.dizzyd.coregen.scripting.Context runCtx = this;
            generateFn.call(ctx, scope, scope, new Object[]{runCtx, cx, cz});
        }

        public Logger getLogger() {
            return CoreGen.logger;
        }

        public Feature getFeature() {
            return feature;
        }

        public Random getRandom() {
            return random;
        }

        public Config getConfig() {
            return feature.config;
        }

        public Position randomPos(int chunkX, int chunkZ) {
            int x = (chunkX * 16 + 8) + random.nextInt(16);
            int z = (chunkZ * 16 + 8) + random.nextInt(16);
            int y = feature.ydist.chooseLevel(world, x, z);
            return new Position(x, y, z);
        }

        public void placeBlock(double x, double y, double z) {
            feature.placeBlock(world, random, x, y, z);
        }

        public void placeBlock(double x, double y, double z, IBlockState block) {
            feature.placeBlock(world, x, y, z, block);
        }

        public IBlockState blockFromString(String blockResource) {
            return feature.blockFromString(blockResource);
        }

        public void chatLog(String msg) {
            feature.chatLog(world, msg);
        }
    }

}
