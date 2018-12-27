
// Explicitly select int-based constructor for BlockPos
var BlockPos = Java.type("net.minecraft.util.math.BlockPos")["(int, int, int)"];

Math.radians = function(degrees) {
    return degrees * Math.PI / 180;
};


var ironOre = blockState("minecraft:iron_ore", 0)

yaw = Math.radians(90)
pitch = Math.radians(180)

startX = pos.x
startY = pos.y
startZ = pos.z

for (var x = pos.x; x < pos.x + 20; x++) {
    for (var z = pos.z; z < pos.z + 3; z*=2) {
        for (var i = 1; i < 50; i++) {
            xPos = x + (i * Math.cos(pitch) * Math.cos(yaw));
            zPos = z + (i * Math.cos(pitch) * Math.sin(yaw));
            yPos = startY + (i * Math.sin(pitch));
            world.setBlockState(new BlockPos(xPos, yPos, zPos), ironOre, 2|16);
        }
    }
}

