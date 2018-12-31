// ***************************************************************************
//
//  Copyright 2018-2019 David (Dizzy) Smith, dizzyd@dizzyd.com
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// ***************************************************************************
package com.dizzyd.coregen.ylevel;

import java.util.Random;

public class YLevelUniform extends YLevelDistribution {

    private int center;
    private int spread;

    @Override
    public int chooseLevel(Random r) {
        return (int)((center - spread) + (r.nextFloat() * (spread * 2)));
    }

    public int getCenter() {
        return center;
    }

    public void setCenter(int center) {
        this.center = center;
    }

    public int getSpread() {
        return spread;
    }

    public void setSpread(int spread) {
        this.spread = spread;
    }
}
