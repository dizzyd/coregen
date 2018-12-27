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

package com.dizzyd.coregen.world;

import com.dizzyd.coregen.CoreGen;
import com.github.davidmoten.rtree.*;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class WorldData extends WorldSavedData {

    private static final String IDENTIFIER = WorldData.class.getName();
    private static HashMap<Integer, WorldData> DIMENSIONS = new HashMap<Integer, WorldData>();

    private RTree<String, Point> deposits = RTree.create();

    public WorldData(String tag) {
        super(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        byte[] depositsData = compound.getByteArray("deposits");
        if (depositsData.length > 0) {
            try {
                ByteArrayInputStream is = new ByteArrayInputStream(depositsData);
                Serializer<String, Point> serializer = Serializers.flatBuffers().utf8();
                deposits = serializer.read(is, depositsData.length, InternalStructure.DEFAULT);
            } catch (IOException ex) {
                CoreGen.logger.error("Error loading deposits R-Tree: %s", ex);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList chunkList = new NBTTagList();

        // Serialize the rtree, if it contains data
        if (deposits.size() > 0) {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
                Serializer<String, Point> serializer = Serializers.flatBuffers().utf8();
                serializer.write(deposits, os);

                NBTTagByteArray depositsNbt = new NBTTagByteArray(os.toByteArray());
                compound.setTag("deposits", depositsNbt);
                return compound;
            } catch (IOException ex) {
                CoreGen.logger.error("Error saving deposits R-Tree: %s", ex);
                return null;
            }
        }

        return null;
    }

    private static WorldData load(World world) {
        WorldData data = DIMENSIONS.getOrDefault(world.provider.getDimension(), null);
        if (data == null) {
            // No world-data available; load or create it
            data = (WorldData) world.getPerWorldStorage().getOrLoadData(WorldData.class, IDENTIFIER);
            if (data == null) {
                // No data was ever available; create one
                data = new WorldData(IDENTIFIER);
                world.getPerWorldStorage().setData(IDENTIFIER, data);
            }

            DIMENSIONS.put(world.provider.getDimension(), data);
        }

        return data;
    }

    public static void addDeposit(World world, String depositId, int cx, int cz) {
        WorldData wd = load(world);
        wd.deposits = wd.deposits.add(depositId, Geometries.point(cx, cz));
        wd.markDirty();
    }

    public static boolean depositInDistance(World world, String depositId, int cx, int cz, int distance) {
        WorldData wd = load(world);
        return wd.deposits.search(Geometries.point(cx, cz), distance).count().toBlocking().single() > 0;
    }

    public static Point nearestDeposit(World world, String depositId, int cx, int cz) {
        WorldData wd = load(world);
        Entry<String, Point> pt = wd.deposits.nearest(Geometries.point(cx, cz), 1000, 1).firstOrDefault(null).toBlocking().single();
        if (pt == null) {
            return null;
        }
        return pt.geometry();
    }
}
