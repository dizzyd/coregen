

var ResourceLocation = Java.type("net.minecraft.util.ResourceLocation");
var ForgeRegistries = Java.type("net.minecraftforge.fml.common.registry.ForgeRegistries");
var TextComponentString = Java.type("net.minecraft.util.text.TextComponentString");

function BlockState(id) {
    var rl = new ResourceLocation(id);
    return ForgeRegistries.BLOCKS.getValue(rl).getStateFromMeta(0);
}

// Explicitly select int-based constructor for BlockPos
var BlockPos = Java.type("net.minecraft.util.math.BlockPos")["(int, int, int)"];

Math.radians = function (degrees) {
    return degrees * Math.PI / 180;
};

function generate(ctx, cx, cz) {

    var count = 4;
    var variance = 1;

    var size = count + (ctx.random.nextGaussian() * variance);

    var yaw = Math.radians(ctx.random.nextInt(360))
    var pitch = Math.radians(270);

    pos = new BlockPos((cx * 16 + 8) + ctx.random.nextInt(16),
        80,
        (cz * 16 + 8) + ctx.random.nextInt(16));

    var seamWidth = 1; //Math.ceil(Math.pow(size, 0.2));
    var seamLength = 1; //Math.ceil(Math.pow(size, 0.2));
    var seamHeight = size; //Math.ceil(Math.pow(size, 0.4));

    var total = 0;

    outerLoop:
    for (var x = pos.x; x < pos.x + seamWidth; x++) {
        for (var z = pos.z; z < pos.z + seamLength; z++) {
            for (var i = 1; i < seamHeight; i++) {
                xPos = x + Math.ceil((i * Math.cos(pitch) * Math.cos(yaw)));
                zPos = z + Math.ceil((i * Math.cos(pitch) * Math.sin(yaw)));
                yPos = pos.y + Math.ceil((i * Math.sin(pitch)));
                ctx.world.setBlockState(new BlockPos(xPos, yPos, zPos), ctx.feature.blocks.chooseBlock(ctx.random), 2 | 16);
                if (total++ > size) {
                    break outerLoop;
                }
            }
        }
    }

    ctx.world.setBlockState(new BlockPos(pos.x, pos.y, pos.z), BlockState("minecraft:redstone_ore"), 2 | 16);

}