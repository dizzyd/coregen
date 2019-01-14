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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ScriptUtil {

    public String run(String script, World world, BlockPos position) {
        File f = new File(script);
        try {
            FileReader reader = new FileReader(f.getAbsoluteFile());

            Context ctx = Context.enter();
            Scriptable scope = ctx.initStandardObjects();

            scope.put("log", scope, CoreGen.logger);

            ctx.evaluateReader(scope, reader, f.getName(), 0, null);
        } catch (FileNotFoundException ex) {
            CoreGen.logger.error("Script `file not found {}: {}", script, ex.getMessage());
            return String.format("%s not found: %s", script, ex);
        } catch (IOException ex) {
            CoreGen.logger.error("Error invoking script {}: {}", script, ex.getMessage());
            return String.format("%s error: %s", script, ex);
        } finally {
            Context.exit();
        }

        return String.format("%s: ok", script);
    }

    public Script compile(String filename) {
        Script result = null;
        File f = new File(filename);
        try {
            Context ctx = Context.enter();
            FileReader reader = new FileReader(f.getAbsoluteFile());
            result = ctx.compileReader(reader, filename, 0, null);
        } catch (FileNotFoundException e) {
            CoreGen.logger.error("Script not found: {}", filename);
        } catch (IOException e) {
            CoreGen.logger.error("Script {} failed to compile: {}", filename, e);
        } finally {
            Context.exit();
        }
        return result;
    }

}