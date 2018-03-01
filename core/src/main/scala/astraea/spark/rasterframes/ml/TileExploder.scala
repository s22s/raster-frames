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

package astraea.spark.rasterframes.ml

import astraea.spark.rasterframes._
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.util.{DefaultParamsReadable, DefaultParamsWritable, Identifiable}
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.gt.types.TileUDT
import org.apache.spark.sql.types._
import astraea.spark.rasterframes.util._

/**
 * SparkML Transformer for expanding tiles into single cell rows with
 * additional columns containing source row/column indexes.
 *
 * @since 9/21/17
 */
class TileExploder(override val uid: String) extends Transformer with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("tile-exploder"))

  override def copy(extra: ParamMap): TileExploder = defaultCopy(extra)

  private def isTile(field: StructField) =
    field.dataType.typeName.equalsIgnoreCase(TileUDT.typeName)

  /** Checks the incoming schema and determines what the output schema will be. */
  def transformSchema(schema: StructType) = {
    val nonTiles = schema.fields.filter(!isTile(_))
    val tiles = schema.fields.filter(isTile)
    val cells = tiles.map(_.copy(dataType = DoubleType, nullable = false))
    val indexes = Seq(
      StructField(COLUMN_INDEX_COLUMN.columnName, IntegerType, false),
      StructField(ROW_INDEX_COLUMN.columnName, IntegerType, false)
    )
    StructType(nonTiles ++ indexes ++ cells)
  }

  def transform(dataset: Dataset[_]) = {
    val nonTiles = dataset.schema.fields.filter(!isTile(_)).map(f ⇒ col(f.name))
    val tiles = dataset.schema.fields.filter(isTile).map(f ⇒ col(f.name))
    val exploder = explodeTiles(tiles: _*)
    dataset.select(nonTiles :+ exploder: _*)
  }
}

object TileExploder extends DefaultParamsReadable[TileExploder]