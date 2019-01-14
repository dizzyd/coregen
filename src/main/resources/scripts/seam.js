
var Position = Packages.com.dizzyd.coregen.scripting.Position;

Math.radians = function (degrees) {
    return degrees * Math.PI / 180;
};

function generate(ctx, cx, cz) {

    var count = 4;
    var variance = 1;

    var size = count + (ctx.random.nextGaussian() * variance);

    var yaw = Math.radians(ctx.random.nextInt(360))
    var pitch = Math.radians(270);

    pos = new Position((cx * 16 + 8) + ctx.random.nextInt(16),
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
                ctx.placeBlock(xPos, yPos, zPos)
                if (total++ > size) {
                    break outerLoop;
                }
            }
        }
    }

    ctx.placeBlock(pos.x, pos.y, pos.z, ctx.blockFromString("minecraft:redstone_ore"));
}