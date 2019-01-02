/* 
 Global variables provided by the script feature:
 - ctx: Object containing helper methods and generator state
 - cx: X coordinate of chunk
 - cz: Z coordinate of chunk
 - chunkGen: IChunkGenerator
 - chunkProvider: IChunkProvider 
*/

function inArea(x, z, size) {
    return x * x + z * z <= radius * radius;
}

// Choose a random position in the chunk, using the y-distribution
// defined in the feature config
var pos = ctx.randomPos(cx, cz);

// Configuration settings for scripts need to be read directly
// from the config object
var radius = ctx.config.getInt("radius")
var height = ctx.config.getInt("height");

for (var x = pos.x - radius; x <= pos.x + radius; ++x) {
    var areaX = x - pos.x;
    for (var z = pos.z - radius; z <= pos.z + radius; ++z) {
        var areaZ = z - pos.z;
        if (inArea(areaX, areaZ, radius)) {
            for (var y = pos.y - height; y <= pos.y + height; ++y) {
                // Place a block in the world at the specified location; this uses
                // the weighted blocks list from the feature config to choose the
                // appropriate block
                ctx.placeBlock(x, y, z);
            }
        }
    }
}
