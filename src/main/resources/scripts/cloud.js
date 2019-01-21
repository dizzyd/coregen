
var SimplexNoise = Packages.com.dizzyd.coregen.util.SimplexNoise;

function generate(ctx, cx, cz) {
    var pos = ctx.randomPos(cx, cz);
    if (pos.y < 1) {
        return; // Y-level requirement was not met
    }

    var minRadius = ctx.config.getInt("min-radius");
    var maxRadius = ctx.config.getInt("max-radius");
    var radius = minRadius + ctx.random.nextInt(maxRadius - minRadius);

    // Frequency based on my own experiments
    var freq = 0.1 + (0.05 * ctx.random.nextDouble());

    // Walk over the x/z plane, placing blocks using noise to jitter them
    for (var x = -radius; x < radius; x++) {
        for (var z = -radius; z < radius; z++) {
            var n = SimplexNoise.noise(x * freq, z * freq);
            ctx.placeBlock(pos.x + (x * n), pos.y - (2 * n), pos.z + (z * n));
        }
    }
}

