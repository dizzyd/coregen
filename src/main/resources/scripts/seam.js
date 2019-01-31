
var SimplexNoise = Packages.com.dizzyd.coregen.util.SimplexNoise;

function generate(ctx, cx, cz) {
    // Get a random start position
    var pos = ctx.randomPos(cx, cz);
    if (pos.y < 1) {
        return; // Y-level requirement was not met
    }

    // Frequency based on my own experiments
    var freq = 0.2 + (0.05 * ctx.random.nextDouble());

    // Starting with a length (x), width (z) and depth (y), place blocks using simplex noise; this generates
    // a seam running east-west, from 10 - 26 blocks long, 3 wide and up to 50 blocks deep
    // (This is a _lot_ of ore! - expect around 1k ore blocks :))
    var length = 10 + (16 * ctx.random.nextDouble());
    var width = 3;
    var depth = Math.min(50, pos.y);

    for (var x = pos.x; x < pos.x + length; x++) {
        for (var z = pos.z; z < pos.z + width; z++) {
            for (var y = pos.y; y > pos.y - depth; y--) {
                var n = SimplexNoise.noise(x * freq, y * freq, z * freq);
                if (n > (0.1 * ctx.random.nextDouble())) {
                    ctx.placeBlock(x, y, z);
                }
            }
        }
    }
}
