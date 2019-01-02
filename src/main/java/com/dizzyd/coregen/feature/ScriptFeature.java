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
import com.typesafe.config.Config;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;
import java.util.Random;

public class ScriptFeature extends Feature {
    private CompiledScript script;
    private int errorCount = 0;

    private ThreadLocal<Bindings> bindingsThreadLocal = new ThreadLocal<>();
    private ThreadLocal<Context> ctxThreadLocal = new ThreadLocal<>();

    private String filename;


    public ScriptFeature() {
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;

        // Try to load/compile the script
        script = CoreGen.scriptUtil.compile(filename);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {

        if (script == null) {
            return;
        }

        // The ScriptEngine and CompiledScript are thread-safe, but bindings are most definitely not;
        // saw a lot of random hangs in initial development because of this. To avoid per-chunk Binding object
        // creation, we cache the bindings in thread-local variables
        Bindings bindings = bindingsThreadLocal.get();
        if (bindings == null) {
            bindings = script.getEngine().createBindings();
            bindingsThreadLocal.set(bindings);
        }

        bindings.put("ctx", getContext(world, this, random));
        bindings.put("cx", chunkX);
        bindings.put("cz", chunkZ);
        bindings.put("chunkGen", chunkGenerator);
        bindings.put("chunkProvider", chunkProvider);

        // Evaluate the compiled script. We do track sequential failures to avoid spamming the engine
        // and the console. The errorCount is reset on each successful run and incremented on each failed
        // run, such that after ~4 sequential errors, it will disable the chunk generator completely.
        try {
            script.eval(bindings);
            errorCount = 0; // Successful invocation, reset error counter
        } catch (ScriptException e) {
            CoreGen.logger.error("Failed to generate chunk {}, {} using {}: {}", chunkX, chunkZ, filename, e.getMessage());
            if (errorCount < 3) {
                errorCount++;
            } else {
                CoreGen.logger.error("Disabling chunk gen {}; too many sequential failures!", filename);
                script = null;
            }
        }
    }

    private Context getContext(World world, Feature feature, Random random) {
        Context ctx = ctxThreadLocal.get();
        if (ctx == null) {
            ctx = new Context();
            ctxThreadLocal.set(ctx);
        }

        ctx.world = world; ctx.feature = feature; ctx.random = random;
        return ctx;
    }

    public class Context {
        World world;
        Feature feature;
        Random random;

        public Feature getFeature() {
            return feature;
        }

        public Random getRandom() {
            return random;
        }

        public Config getConfig() {
            return feature.config;
        }

        public BlockPos randomPos(int chunkX, int chunkZ) {
            return new BlockPos((chunkX * 16 + 8) + random.nextInt(16),
                                ydist.chooseLevel(random),
                                (chunkZ * 16 + 8) + random.nextInt(16));
        }

        public void placeBlock(double x, double y, double z) {
            world.setBlockState(new BlockPos(x, y, z), feature.blocks.chooseBlock(random), 2|16);
        }

        public void log(String msg) {
            world.getMinecraftServer().getPlayerList().sendMessage(new TextComponentString(msg));
        }
    }
}
