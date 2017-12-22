/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright 2017 Astraea, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     [http://www.apache.org/licenses/LICENSE-2.0]
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package astraea.spark.rasterframes.jts

import geotrellis.util.LazyLogging
import org.apache.spark.sql.catalyst.plans.logical.{Filter, LogicalPlan}
import org.apache.spark.sql.catalyst.rules.Rule

/**
 * Logical plan manipulations to handle spatial queries on tile components.
 *
 * @author sfitch 
 * @since 12/21/17
 */
object SpatialRules extends Rule[LogicalPlan] with LazyLogging {
  def apply(plan: LogicalPlan): LogicalPlan = {
    logger.debug(s"Evaluating $plan")
    plan.transform {
      //case f @ Filter(cond, lp) ⇒ println(f); f
      case lp: LogicalPlan ⇒ lp.transformExpressionsDown {
        case s ⇒ s
      }
    }
  }
}
