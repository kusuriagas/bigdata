// Databricks notebook source
import org.apache.spark.sql.SparkSession

// COMMAND ----------

//Definimos la reserva
var spark = SparkSession.builder.
appName("Mi Aplicacion").
config("spark.driver.memory", "1g").
config("spark.dynamicAllocation.maxExecutors", "10").
config("spark.executor.cores", "2").
config("spark.executor.memory", "20g").
config("spark.executor.memoryOverhead", "2g").
enableHiveSupport().
getOrCreate()

// COMMAND ----------

import org.apache.spark.sql.types.{StructType, StructField}
import org.apache.spark.sql.types.{StringType, IntegerType, DoubleType}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{functions => f}
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame


// COMMAND ----------

var df_order_details = spark.read.format("csv").option("header", "true").option("delimiter", ",").schema(
    StructType(
        Array(
          StructField("order_id", IntegerType, true),
          StructField("product_id", IntegerType, true),
          StructField("unit_price", DoubleType, true),
          StructField("quantity", DoubleType, true),
           StructField("discount", DoubleType, true)
        )
    )
).load("dbfs:///FileStore/dataset/order_details-1.csv")

df_order_details.show()

// COMMAND ----------

var df_orders = spark.read.format("csv").option("header", "true").option("delimiter", ",").schema(
    StructType(
        Array(
          StructField("order_id", IntegerType, true),
          StructField("customer_id", StringType, true),
          StructField("employee_id", IntegerType, true),
          StructField("order_date", StringType, true)
        )
    )
).load("dbfs:///FileStore/dataset/orders-1.csv")

df_orders.show()

// COMMAND ----------

//PASO 1: Agrupamos los datos según la edad
var df1 = df_order_details.groupBy(df_order_details.col("order_id")).agg(
    f.count(df_order_details.col("order_id")).alias("CANTIDAD ORDEN"), 
	f.min(df_order_details.col("quantity")).alias("CANTIDAD MINIMA"), 
	f.sum(df_order_details.col("quantity")).alias("CANTIDAD TOTAL"), 
	f.max(df_order_details.col("quantity")).alias("CANTIDAD MAXIMA")
)

df1.show()


// COMMAND ----------

var df2 = df1.filter(df1.col("CANTIDAD ORDEN") > 5)

// COMMAND ----------

df2.show()

// COMMAND ----------

var df3 = df2.filter(df2.col("CANTIDAD MINIMA") > 1)
df3.show()


// COMMAND ----------

var dfResultado = df3.filter(df3.col("CANTIDAD MINIMA") > 12)
dfResultado.show()

// COMMAND ----------


