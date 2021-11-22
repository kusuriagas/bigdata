// Databricks notebook source
//COPYRIGHT: BIG DATA ACADEMY [info@bigdataacademy.org]
//AUTHOR: ALONSO MELGAREJO [alonsoraulmgs@gmail.com]

// COMMAND ----------

// DBTITLE 1,1. Importamos las librerías
import org.apache.spark.sql.types.{StructType, StructField}
import org.apache.spark.sql.types.{StringType, IntegerType, DoubleType}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{functions => f}

// COMMAND ----------

// DBTITLE 1,2. Lectura de datos
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

// DBTITLE 1,3. Procesamiento
//Definimos los pasos de procesamiento en una única cadena de proceso
var dfResultado = dfPersona.groupBy(dfPersona.col("EDAD")).agg(
	f.count(dfPersona.col("EDAD")).alias("CANTIDAD"), 
	f.min(dfPersona.col("FECHA_INGRESO")).alias("FECHA_CONTRATO_MAS_RECIENTE"), 
	f.sum(dfPersona.col("SALARIO")).alias("SUMA_SALARIOS"), 
	f.max(dfPersona.col("SALARIO")).alias("SALARIO_MAYOR")
).alias("P1").
filter(f.col("P1.EDAD") > 35).alias("P2").
filter(f.col("P2.SUMA_SALARIOS") > 20000).alias("P3").
filter(f.col("P3.SALARIO_MAYOR") > 1000)

//Visualizamos los datos
dfResultado.show()

// COMMAND ----------

// DBTITLE 1,4. Almacenamiento en TEXTFILE
//Anteriormente habíamos almacenado un dataframe en un archivo de CSV, es decir delimitado
dfResultado.write.format("csv").mode("overwrite").option("header", "true").option("delimiter", "|").save("dbfs:///FileStore/output/dfResultado")

// COMMAND ----------

//Verificamos la ruta para ver si el archivo se escribió, veremos el archivo CSV

// COMMAND ----------

// MAGIC %fs ls dbfs:///FileStore/output/dfResultado

// COMMAND ----------

//Para verificar, leemos el directorio del dataframe en una variable
var dfResultadoLeido = spark.read.format("csv").option("header", "true").option("delimiter", "|").load("dbfs:///FileStore/output/dfResultado")

//Mostramos los datos
dfResultadoLeido.show()

// COMMAND ----------

//Si consultamos el esquema asociado, el archivo habrá escrito todos los campos como "STRING"
dfResultadoLeido.printSchema()

// COMMAND ----------

//Estos archivos delimitados se conocen como archivos TEXTFILE (texto plano)
//Generalmente cuando queramos procesar algo, el archivo o los archivos a procesar nos los dejarán como TEXTFILE
//Cargaremos los TEXTFILE a DATAFRAMES y comenzaremos a procesarlos como lo hemos estado haciendo
//Cuando termina el procesamiento, el DATAFRAME resultante lo podemos escribir en un archivo TEXTFILE
//Gracias a esto, podemos descargar el archivo TEXTFILE y compartirselo a alguien para que lo cargue a un excel o una tabla clásica de base de datos
//También ese archivo TEXTFILE resultante puede servir para ser analizado por otra herramienta de Big Data orientada a procesar datos en Disco Duro como Hive o Impala
//El problema de los TEXTFILE es que son extremadamente lentos, si una herramienta de Big Data que ejecuta parte de su proceso en disco duro trata de procesar un archivo TEXTFILE, no podrá procesarlo rápidamente
//Es mejor usar formatos de archivo orientados a Big Data, como por ejemplo PARQUET y AVRO

// COMMAND ----------

// DBTITLE 1,5. Almacenamiento en PARQUET
//Escribiremos el mismo dataframe, pero en formato PARQUET
//Lo escribiremos en otra ruta (dfResultadoParquet)
dfResultado.write.mode("overwrite").format("parquet").option("compression", "snappy").save("dbfs:///FileStore/output/dfResultadoParquet")

// COMMAND ----------

//Verificamos la ruta para ver si el archivo se escribió, veremos el archivo PARQUET

// COMMAND ----------

// MAGIC %fs ls dbfs:///FileStore/output/dfResultadoParquet

// COMMAND ----------

//Para verificar, leemos el directorio del dataframe en una variable
var dfResultadoLeidoParquet = spark.read.format("parquet").load("dbfs:///FileStore/output/dfResultadoParquet")

//Mostramos los datos
dfResultadoLeidoParquet.show()

// COMMAND ----------

//Si consultamos el esquema asociado, el archivo almacena metadata de los tipos de datos
dfResultadoLeidoParquet.printSchema()

// COMMAND ----------

//Si descargamos el archivo parquet:
//
// - RUTA_A_MI_ARCHIVO: /output/dfResultadoParquet/
// - NOMBRE_ARCHIVO: part-00000-tid-1156910903102427309-a43a026a-d609-4a35-a50e-bee584306355-45-1-c000.snappy.parquet
// - ID_CUENTA: 4458995983973520
//
//la URL sería:
//
// https://community.cloud.databricks.com/files/output/dfResultadoParquet/part-00000-tid-1156910903102427309-a43a026a-d609-4a35-a50e-bee584306355-45-1-c000.snappy.parquet?o=4458995983973520

// COMMAND ----------

//A diferencia de un TEXTFILE, al ser PARQUET un formato binarizado, ya no podremos ver el contenido del archivo

// COMMAND ----------

// DBTITLE 1,6. Almacenamiento en AVRO
//Escribiremos el mismo dataframe, pero en formato AVRO
//Lo escribiremos en otra ruta (dfResultadoAvro)
dfResultado.write.mode("overwrite").format("avro").option("compression", "snappy").save("dbfs:///FileStore/output/dfResultadoAvro")

// COMMAND ----------

//Verificamos la ruta para ver si el archivo se escribió, veremos el archivo AVRO

// COMMAND ----------

// MAGIC %fs ls dbfs:///FileStore/output/dfResultadoAvro

// COMMAND ----------

//Para verificar, leemos el directorio del dataframe en una variable
var dfResultadoLeidoAvro = spark.read.format("avro").load("dbfs:///FileStore/output/dfResultadoAvro")

//Mostramos los datos
dfResultadoLeidoAvro.show()

// COMMAND ----------

//Si consultamos el esquema asociado, el archivo almacena metadata de los tipos de datos
dfResultadoLeidoAvro.printSchema()

// COMMAND ----------

//De la misma manera que PARQUET, a diferencia de un TEXTFILE, al ser AVRO un formato binarizado, ya no podremos ver el contenido del archivo

// COMMAND ----------

// DBTITLE 1,7. ¿Cuándo usar PARQUET y AVRO?
//Debemos saber que:
//
// - Parquet es el formato de procesamiento más rápido
// - Avro es el formato de procesamiento más flexible
//
//Generalmente se utilizan para la construcción de tablas de datos en un Datalake, en una clase dedicada a todas las herramientas de Big Data se estudiarían a detalle
//Para el caso del procesamiento SPARK, lo usaremos para almacenar resultantes intermedias en Disco Duro y liberar memoria RAM
//Usaremos el formato más rápido: PARQUET
