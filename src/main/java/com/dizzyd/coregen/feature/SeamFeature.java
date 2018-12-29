package com.dizzyd.coregen.feature;

import com.dizzyd.coregen.CoreGen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.Random;

public class SeamFeature extends Feature {
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {

        int seamWidth = 10 + random.nextInt(20);
        int seamLength = 10 + random.nextInt(20);
        int seamHeight = 5 + random.nextInt(20);

        double yaw = Math.toRadians(random.nextInt(360));
        double pitch = Math.toRadians(random.nextInt(75));

        BlockPos pos = new BlockPos(chunkX * 16 + 8, 80, chunkZ * 16 + 8);

        for (int x = pos.getX(); x < pos.getX() + seamWidth; x++) {
            for (int z = pos.getZ(); z < pos.getZ() + seamLength; z*=2) {
                for (int i = 1; i < seamHeight; i++) {
                    double xPos = x + (i * Math.cos(pitch) * Math.cos(yaw));
                    double zPos = z + (i * Math.cos(pitch) * Math.sin(yaw));
                    double yPos = pos.getY() + (i * Math.sin(pitch));
                    world.setBlockState(new BlockPos(xPos, yPos, zPos), blocks.chooseBlock(random), 2|16);
                }
            }
        }
    }

}