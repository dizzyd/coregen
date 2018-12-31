
/* 
 Global variables provided by the script feature:
 - log: function that will send a string to all players
 - ydist: YDistributionLevel; provides a random y-level, based on distribution, etc.
 - blockList : WeightedBlockList; use blockList.chooseBlock(random) to get a IBlockState
 - random: java.util.Random
 - world: net.minecraft.World
 - cx: X coordinate of chunk
 - cz: Z coordinate of chunk
 - chunkGen: IChunkGenerator
 - chunkProvider: IChunkProvider 
*/

var ResourceLocation = Java.type("net.minecraft.util.ResourceLocation");
var ForgeRegistries = Java.type("net.minecraftforge.fml.common.registry.ForgeRegistries");
var TextComponentString = Java.type("net.minecraft.util.text.TextComponentString");

function BlockState(id) {
    var rl = new ResourceLocation(id);
    return ForgeRegistries.BLOCKS.getValue(rl).getStateFromMeta(0);
}

// Explicitly select int-based constructor for BlockPos
var BlockPos = Java.type("net.minecraft.util.math.BlockPos")["(int, int, int)"];

Math.radians = function(degrees) {
    return degrees * Math.PI / 180;
};

var count = 100;
var variance = 10;

var size = count + (random.nextGaussian() * variance);

var yaw = Math.radians(random.nextInt(360))
var pitch = Math.radians(300 + random.nextInt(40));

pos = new BlockPos((cx * 16 + 8) + random.nextInt(16), 
                   80, 
                   (cz * 16 + 8) + random.nextInt(16));

var seamWidth = Math.ceil(Math.pow(size, 0.4));
var seamLength = Math.ceil(Math.pow(size, 0.2));
var seamHeight = Math.ceil(Math.pow(size, 0.4));

log("W/L/H: " + seamWidth + " " + seamLength + " " + seamHeight + " vol: " + (seamWidth * seamLength * seamHeight));

var total = 0;

outerLoop:
for (var x = pos.x; x < pos.x + seamWidth; x++) {
    for (var z = pos.z; z < pos.z + seamLength; z++) {
        for (var i = 1; i < seamHeight; i++) {
            xPos = x + Math.ceil((i * Math.cos(pitch) * Math.cos(yaw)));
            zPos = z + Math.ceil((i * Math.cos(pitch) * Math.sin(yaw)));
            yPos = pos.y + Math.ceil((i * Math.sin(pitch)));
            world.setBlockState(new BlockPos(xPos, yPos, zPos), blockList.chooseBlock(random), 2|16);
            if (total++ > size) {
                break outerLoop;
            }
        }
    }
}

log("Size: " + size + " Generated: " + total);

world.setBlockState(new BlockPos(pos.x, pos.y, pos.z), BlockState("minecraft:redstone_ore"), 2|16);

