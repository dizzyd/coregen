
/* 
 Global variables provided by the script feature:
 - blockList : WeightedBlockList; use blockList.chooseBlock(random) to get a IBlockState
 - random: java.util.Random
 - world: net.minecraft.World
 - cx: X coordinate of chunk
 - cz: Z coordinate of chunk
 - chunkGen: IChunkGenerator
 - chunkProvider: IChunkProvider 
*/



// Explicitly select int-based constructor for BlockPos
var BlockPos = Java.type("net.minecraft.util.math.BlockPos")["(int, int, int)"];

Math.radians = function(degrees) {
    return degrees * Math.PI / 180;
};


yaw = Math.radians(random.nextInt(360))
pitch = Math.radians(random.nextInt(75))

pos = new BlockPos(cx * 16 + 8, 80, cz * 16 + 8)

startX = pos.x
startY = pos.y
startZ = pos.z

for (var x = pos.x; x < pos.x + 20; x++) {
    for (var z = pos.z; z < pos.z + 3; z*=2) {
        for (var i = 1; i < 50; i++) {
            xPos = x + (i * Math.cos(pitch) * Math.cos(yaw));
            zPos = z + (i * Math.cos(pitch) * Math.sin(yaw));
            yPos = startY + (i * Math.sin(pitch));
            world.setBlockState(new BlockPos(xPos, yPos, zPos), blockList.chooseBlock(random), 2|16);
        }
    }
}

