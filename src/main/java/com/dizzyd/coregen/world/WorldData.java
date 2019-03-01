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
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.internal.Comparators;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldSavedData;

import java.io.*;
import java.util.HashMap;

public class WorldData extends WorldSavedData {

    private static final String IDENTIFIER = WorldData.class.getName();

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
        // No world-data available; load or create it
        WorldData data = (WorldData) world.getPerWorldStorage().getOrLoadData(WorldData.class, IDENTIFIER);
        if (data == null) {
            // No data was ever available; create one
            data = new WorldData(IDENTIFIER);
            world.getPerWorldStorage().setData(IDENTIFIER, data);
        }
        return data;
    }

    public static synchronized void reconcileMissingChunks(World world) {
        // Copy over all the RTree entries that actually have existing chunks in them
        // TODO: Reconsider if a more efficient save format would improve memory usage
        WorldData data = (WorldData) world.getPerWorldStorage().getOrLoadData(WorldData.class, IDENTIFIER);
        if (data != null) {
            RTree<String, Point> newDeposits = RTree.create();
            Iterable<Entry<String, Point>> it = data.deposits.entries().filter(e ->
                world.isChunkGeneratedAt((int)e.geometry().x(), (int)e.geometry().y())).
                    toBlocking().toIterable();
            for(Entry<String,Point> e : it) {
                newDeposits = newDeposits.add(e);
            }
            CoreGen.logger.info("{} missing chunks dropped", data.deposits.size() - newDeposits.size());
            data.deposits = newDeposits;
            data.markDirty();
        }
    }

    public static synchronized void addDeposit(World world, String depositId, int cx, int cz) {
        WorldData wd = load(world);
        wd.deposits = wd.deposits.add(depositId, Geometries.point(cx, cz));
        wd.markDirty();
    }

    public static synchronized void deleteDeposit(World world, String depositId, int cx, int cz) {
        WorldData wd = load(world);
        wd.deposits = wd.deposits.delete(depositId, Geometries.point(cx, cz));
        wd.markDirty();
    }

    public static synchronized void deleteAllDeposits(World world, int cx, int cz) {
        WorldData wd = load(world);
        for (Entry e : wd.deposits.search(Geometries.point(cx, cz)).toBlocking().toIterable()) {
            wd.deposits = wd.deposits.delete(e);
        }
        wd.markDirty();
    }

    public static synchronized boolean depositInDistance(World world, String depositId, int cx, int cz, int distance) {
        WorldData wd = load(world);
        return wd.deposits.search(Geometries.point(cx, cz), distance).
                filter(entry -> depositId.equals(entry.value())).
                count().toBlocking().single() > 0;
    }

    public static synchronized Entry<String, Point> nearestDeposit(World world, String depositId, int cx, int cz) {
        WorldData wd = load(world);
        Point center = Geometries.point(cx, cz);
        Entry<String, Point> pt = wd.deposits.search(center, 1000)
                .filter(entry -> depositId.equals(entry.value()))
                .sorted((lhs, rhs) ->
                        Double.compare(lhs.geometry().distance(center), rhs.geometry().distance(center)))
                .firstOrDefault(null).toBlocking().single();
        if (pt != null) {
            return pt;
        }
        return null;
    }

    public static synchronized Entry<String, Point> nearestDeposit(World world, int cx, int cz) {
        WorldData wd = load(world);
        Point center = Geometries.point(cx, cz);
        Entry<String, Point> pt = wd.deposits.nearest(center, 1000, 1)
                .firstOrDefault(null).toBlocking().single();
        if (pt != null) {
            return pt;
        }
        return null;
    }

    public static synchronized int dumpDeposits(World world) throws IOException {
        int count = 0;
        try(Writer w = new BufferedWriter(new FileWriter("coregen.deposits.txt"))) {
            WorldData wd = load(world);
            for (Entry<String, Point> entry : wd.deposits.entries().toBlocking().toIterable()) {
                ChunkPos pos = new ChunkPos((int)entry.geometry().x(), (int)entry.geometry().y());
                w.write(String.format("%s %d %d\n", entry.value(), pos.getXStart(), pos.getZStart()));
                count++;
            }
        }
        return count;
    }
}
