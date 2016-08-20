/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.autobet.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PolynomialTest
{
    public static final double DELTA = 0.0001;

    @Test
    public void test() {
        Polynomial constant = new Polynomial(new double[] {1, 0});;
        assertEquals(constant.calculate(0), 1, DELTA);
        assertEquals(constant.calculate(-110), 1, DELTA);
        assertEquals(constant.calculate(110), 1, DELTA);

        Polynomial linear = new Polynomial(new double[] {1, 2});;
        assertEquals(linear.calculate(0), 1, DELTA);
        assertEquals(linear.calculate(-110), -219, DELTA);
        assertEquals(linear.calculate(110), 221,  DELTA);

        Polynomial parabola = new Polynomial(new double[] {1, 2, 3});;
        assertEquals(parabola.calculate(0), 1, DELTA);
        assertEquals(parabola.calculate(-2), 9, DELTA);
        assertEquals(parabola.calculate(2), 17,  DELTA);
    }

}
