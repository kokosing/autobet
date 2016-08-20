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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class Polynomial
{
    private final double coefficients[];

    public Polynomial(double[] coefficients)
    {
        checkArgument(coefficients.length > 0, "Empty coefficients table");
        this.coefficients = requireNonNull(coefficients, "coefficients is null");
    }

    public double calculate(double x)
    {
        double result = coefficients[0];
        for (int i = 1; i < coefficients.length; i++) {
            result += x * coefficients[i];
            x *= x;
        }
        return result;
    }
}
