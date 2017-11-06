# Try this script in a `pyscript` REPL
from pyspark.sql import SparkSession
from pyrasterframes import *

# you can also tweak app name and master here.
spark = SparkSession.builder.getOrCreate()
spark.sparkContext.setLogLevel('WARN')

# Get access to Raster Frames goodies.
spark.withRasterFrames()
from pyrasterframes.functions import *

# Read a local file
rf = spark.rf.readGeoTiff("src/test/resources/L8-B8-Robinson-IL.tiff")
print("Tile columns: ", rf.tileColumns())
print("Spatial key column: ", rf.spatialKeyColumn())
print("Temporal key column: ", rf.temporalKeyColumn())
rf.select(
    rf.spatialKeyColumn(),
    tileDimensions("tile"),
    cellType("tile"),
    dataCells("tile"),
    noDataCells("tile"),
    tileMin("tile"),
    tileMean("tile"),
    tileMax("tile"),
    tileSum("tile"),
    renderAscii("tile"),
).show()

rf.agg(
    aggMean("tile"),
    aggDataCells("tile"),
    aggNoDataCells("tile"),
    aggStats("tile").alias("stat"),
    aggHistogram("tile")
).show()

rf.createOrReplaceTempView("r")
# Demo of using local operations and SQL
spark.sql("SELECT rf_tileMean(rf_localAdd(tile, rf_makeConstantTile(1, 128,128, 'uint16'))) AS AndOne, rf_tileMean(tile) "
          "AS t FROM r").show()
