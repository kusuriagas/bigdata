// Databricks notebook source
//COPYRIGHT: BIG DATA ACADEMY [info@bigdataacademy.org]
//AUTHOR: ALONSO MELGAREJO [alonsoraulmgs@gmail.com]

// COMMAND ----------

// DBTITLE 1,1. Librerías
import org.apache.spark.sql.types.{StructType, StructField}
import org.apache.spark.sql.types.{StringType, IntegerType, DoubleType}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{functions => f}

// COMMAND ----------

// DBTITLE 1,2. Lectura
//Leemos el archivo de persona
var dfPersona = spark.read.format("csv").option("header", "true").option("delimiter", "|").schema(
    StructType(
        Array(
            StructField("ID", StringType, true),
            StructField("NOMBRE", StringType, true),
            StructField("TELEFONO", StringType, true),
            StructField("CORREO", StringType, true),
            StructField("FECHA_INGRESO", StringType, true),
            StructField("EDAD", IntegerType, true),
            StructField("SALARIO", DoubleType, true),
            StructField("ID_EMPRESA", StringType, true)
        )
    )
).load("dbfs:///FileStore/dataset/persona.data")

//Mostramos los datos
dfPersona.show()

// COMMAND ----------

//Vamos a colocar el contenido de todos los archivos de la carpeta en un dataframe
//Solo deberemos indicar la ruta asociada
//Para este ejemplo, el archivo tiene un delimitador de coma ","
var dfTransaccion = spark.read.format("csv").option("header", "true").option("delimiter", ",").schema(
    StructType(
        Array(
            StructField("ID_PERSONA", StringType, true),
            StructField("ID_EMPRESA", StringType, true),
            StructField("MONTO", DoubleType, true),
            StructField("FECHA", StringType, true)
        )
    )
).load("dbfs:///FileStore/dataset/transaccion")

//Vemos el contenido
dfTransaccion.show()

// COMMAND ----------

// DBTITLE 1,3. Procesamiento
//PASO 1: Agrupamos los datos según la edad
var df1 = dfPersona.groupBy(dfPersona.col("EDAD")).agg(
	f.count(dfPersona.col("EDAD")).alias("CANTIDAD"), 
	f.min(dfPersona.col("FECHA_INGRESO")).alias("FECHA_CONTRATO_MAS_RECIENTE"), 
	f.sum(dfPersona.col("SALARIO")).alias("SUMA_SALARIOS"), 
	f.max(dfPersona.col("SALARIO")).alias("SALARIO_MAYOR")
)

//Mostramos los datos
df1.show()

// COMMAND ----------

//PASO 2: Filtramos por una EDAD
var df2 = df1.filter(df1.col("EDAD") > 35)

//Mostramos los datos
df2.show()

// COMMAND ----------

//PASO 3: Filtramos por SUMA_SALARIOS
var df3 = df2.filter(df2.col("SUMA_SALARIOS") > 20000)

//Mostramos los datos
df3.show()

// COMMAND ----------

//PASO 4: Filtramos por SALARIO_MAYOR
var dfResultado = df3.filter(df3.col("SALARIO_MAYOR") > 1000)

//Mostramos los datos
dfResultado.show()

// COMMAND ----------

// DBTITLE 1,4. Escritura de resultante final TEXTFILE [Si queremos compartirlo para ser analizado con herramientas NO BIG DATA]
//Escribimos el dataframe de la resultante final en disco duro
dfResultado.write.format("csv").mode("overwrite").option("header", "true").option("delimiter", "|").save("dbfs:///FileStore/output/dfResultado")

// COMMAND ----------

// MAGIC %fs ls dbfs:///FileStore/output/dfResultado

// COMMAND ----------

//Si descargamos el archivo csv:
//
// - RUTA_A_MI_ARCHIVO: /output/dfResultado/
// - NOMBRE_ARCHIVO: part-00000-tid-7659704571732851131-ff72a61c-03a6-4c7e-98d2-6e32a6c0bac4-145-1-c000.csv
// - ID_CUENTA: 4458995983973520
//
//la URL sería:
//
// https://community.cloud.databricks.com/files/output/dfResultado/part-00000-tid-7659704571732851131-ff72a61c-03a6-4c7e-98d2-6e32a6c0bac4-145-1-c000.csv?o=4458995983973520

// COMMAND ----------

// DBTITLE 1,5. Escritura de resultante final PARQUET [Si queremos compartirlo para ser analizado con herramientas BIG DATA]
//Escribiremos el mismo dataframe, pero en formato PARQUET
//Lo escribiremos en otra ruta (dfResultadoParquet)
dfResultado.write.mode("overwrite").format("parquet").option("compression", "snappy").save("dbfs:///FileStore/output/dfResultadoParquet")

// COMMAND ----------

// MAGIC %fs ls dbfs:///FileStore/output/dfResultadoParquet

// COMMAND ----------

//Si descargamos el archivo parquet:
//
// - RUTA_A_MI_ARCHIVO: /output/dfResultadoParquet/
// - NOMBRE_ARCHIVO: part-00000-tid-1444626084385412988-b6a237d4-f57b-4d12-b649-bd16641b043d-147-1-c000.snappy.parquet
// - ID_CUENTA: 4458995983973520
//
//la URL sería:
//
// https://community.cloud.databricks.com/files/output/dfResultadoParquet/part-00000-tid-1444626084385412988-b6a237d4-f57b-4d12-b649-bd16641b043d-147-1-c000.snappy.parquet?o=4458995983973520
