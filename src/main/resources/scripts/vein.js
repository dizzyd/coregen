
// From: https://github.com/CoFH/CoFHWorld/blob/1.12/src/main/java/cofh/cofhworld/world/generator/WorldGenMinableLargeVein.java

function generate(ctx, cx, cz) {

    pos = ctx.randomPos(cx, cz);
    if (pos.y < 1) {
        return; // Y-level requirement was not met
    }

    var x = pos.x;
    var y = pos.y;
    var z = pos.z;

    var rand = ctx.random;

    var sparse = ctx.config.getBoolean("sparse");
    var spindly = true;

    var veinSize = ctx.config.getInt("count");
    var branchSize = 1 + (veinSize / 30);
    var subBranchSize = 1 + (branchSize / 5);

    for (var blocksVein = 0; blocksVein <= veinSize; ) {
        var posX = x;
        var posY = y;
        var posZ = z;

        var directionChange = rand.nextInt(6);

        var directionX1 = -rand.nextInt(2);
        var directionY1 = -rand.nextInt(2);
        var directionZ1 = -rand.nextInt(2);

        directionX1 += ~directionX1 >>> 31;
        directionY1 += ~directionY1 >>> 31;
        directionZ1 += ~directionZ1 >>> 31;

        for (var blocksBranch = 0; blocksBranch <= branchSize; ) {
            if (directionChange != 1) {
                posX += rand.nextInt(2) * directionX1;
            }
            if (directionChange != 2) {
                posY += rand.nextInt(2) * directionY1;
            }
            if (directionChange != 3) {
                posZ += rand.nextInt(2) * directionZ1;
            }

            if (rand.nextInt(3) == 0) {
                var posX2 = posX;
                var posY2 = posY;
                var posZ2 = posZ;

                var directionChange2 = rand.nextInt(6);

                var directionX2 = -rand.nextInt(2);
                var directionY2 = -rand.nextInt(2);
                var directionZ2 = -rand.nextInt(2);

                directionX2 += ~directionX2 >>> 31;
                directionY2 += ~directionY2 >>> 31;
                directionZ2 += ~directionZ2 >>> 31;

                for (var blocksSubBranch = 0; blocksSubBranch <= subBranchSize; ) {
                    if (directionChange2 != 0) {
                        posX2 += rand.nextInt(2) * directionX2;
                    }
                    if (directionChange2 != 1) {
                        posY2 += rand.nextInt(2) * directionY2;
                    }
                    if (directionChange2 != 2) {
                        posZ2 += rand.nextInt(2) * directionZ2;
                    }

                    ctx.placeBlock(posX2, posY2, posZ2);

                    if (sparse) {
                        blocksVein++;
                        blocksBranch++;
                    }
                    blocksSubBranch++;
                }
            }

            ctx.placeBlock(posX, posY, posZ);

            if (spindly) {
                blocksVein++;
            }
            blocksBranch++;
        }

        x = x + (rand.nextInt(3) - 1);
        y = y + (rand.nextInt(3) - 1);
        z = z + (rand.nextInt(3) - 1);
        if (!spindly) {
            blocksVein++;
        }
    }
}