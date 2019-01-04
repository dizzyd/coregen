
var SimplexNoise = Java.type("com.dizzyd.coregen.util.SimplexNoise");

function generate(ctx, cx, cz) {
    var pos = ctx.randomPos(cx, cz);
    var depth = 1 + ctx.random.nextInt(ctx.config.getInt("depth"));
    var radius = 3 + ctx.random.nextInt(ctx.config.getInt("radius"));

    if (pos.y - depth < 1) {
        print("Too deep!");
        return
    }

    // Choose a random factor and scale to between 0.0 and 0.3
    // Use the random factor to choose a height/length; scaling ensures
    // we keep it approproxmiately circular
    var factor = 0.3 * ctx.random.nextDouble();
    var invfactor = 0.3 - factor;
    var width, length;
    if (ctx.random.nextDouble()) {
        width = radius - (radius * factor);
        length = radius + (radius * invfactor);
    } else {
        width = radius + (radius * factor);
        length = radius - (radius * invfactor);
    }

    // Select a jitter value per axis; we use this to ensure the noise is unique per-lake instance
    // N.B. we don't jitter the Y-level so that it moves a little more independently
    var jx = ctx.random.nextDouble(); jz = ctx.random.nextDouble();
    for (var x = -width; x < width; x++) {
        for (var z = -length; z < length; z++) {
            for (var y = 0; y < depth; y++) {
                // Normalize coordinates to 0..1, relative to total width/length
                var nx = x / width;
                var nz = z / length;
                var ny = y / depth;

                // Calculate the normalized distance-square
                var ds = nx * nx + nz * nz + ny * ny;

                // Calculate noise, using the normalized coordinates w/
                // pre-selected jitter; scale the result to between 0..1
                var n = SimplexNoise.noise(nx * jx, ny, nz * jz) / 2 + 0.5;

                // If the normalized noise exceeds the distance-square, place our block;
                // the further away a block is from the center of mass, the less likely it is
                // we'll place a block
                if (n > ds) {
                    ctx.placeBlock(pos.x + x, pos.y - y, pos.z + z);
                }
            }
        }
    }
}


