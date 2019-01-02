

// From: https://github.com/CoFH/CoFHWorld/blob/1.12/src/main/java/cofh/cofhworld/world/generator/WorldGenMinableCluster.java

function generate(ctx, cx, cz) {
    // Choose a random position in the chunk, using the y-distribution
    // defined in the feature config
    var pos = ctx.randomPos(cx, cz);

    var rand = ctx.random;
    var blocks = ctx.config.getInt("count");
    var f = rand.nextFloat() * Math.PI;

    // despite naming, these are not exactly min/max. more like direction
    var xMin = pos.x + (Math.sin(f) * blocks) / 8;
    var xMax = pos.x - (Math.sin(f) * blocks) / 8;
    var zMin = pos.z + (Math.cos(f) * blocks) / 8;
    var zMax = pos.z - (Math.cos(f) * blocks) / 8;
    var yMin = (pos.y + rand.nextInt(3)) - 2;
    var yMax = (pos.y + rand.nextInt(3)) - 2;

    // optimization so this subtraction doesn't occur every time in the loop
    xMax -= xMin;
    yMax -= yMin;
    zMax -= zMin;

    for (var i = 0; i <= blocks; i++) {
        var xCenter = xMin + (xMax * i) / blocks;
        var yCenter = yMin + (yMax * i) / blocks;
        var zCenter = zMin + (zMax * i) / blocks;

        // preserved as nextDouble to ensure the rand gets ticked the same amount
        var size = (rand.nextDouble() * blocks) / 16;
        var hMod = ((Math.sin((i * Math.PI) / blocks) + 1) * size + 1) * 0.5;
        var vMod = ((Math.sin((i * Math.PI) / blocks) + 1) * size + 1) * 0.5;

        var xStart = Math.floor(xCenter - hMod);
        var yStart = Math.floor(yCenter - vMod);
        var zStart = Math.floor(zCenter - hMod);

        var xStop = Math.floor(xCenter + hMod);
        var yStop = Math.floor(yCenter + vMod);
        var zStop = Math.floor(zCenter + hMod);

        for (var blockX = xStart; blockX <= xStop; blockX++) {
            var xDistSq = ((blockX + .5) - xCenter) / hMod;
            xDistSq *= xDistSq;
            if (xDistSq >= 1) {
                continue;
            }
            for (var blockY = yStart; blockY <= yStop; blockY++) {
                var yDistSq = ((blockY + .5) - yCenter) / vMod;
                yDistSq *= yDistSq;
                var xyDistSq = yDistSq + xDistSq;
                if (xyDistSq >= 1) {
                    continue;
                }
                for (var blockZ = zStart; blockZ <= zStop; blockZ++) {
                    var zDistSq = ((blockZ + .5) - zCenter) / hMod;
                    zDistSq *= zDistSq;
                    if (zDistSq + xyDistSq >= 1) {
                        continue;
                    }
                    ctx.placeBlock(blockX, blockY, blockZ);
                }
            }
        }
    }
}