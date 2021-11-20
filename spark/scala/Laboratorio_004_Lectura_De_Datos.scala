// Databricks notebook source
//COPYRIGHT: BIG DATA ACADEMY [info@bigdataacademy.org]
//AUTHOR: ALONSO MELGAREJO [alonsoraulmgs@gmail.com]

// COMMAND ----------

// DBTITLE 1,1. Leyendo un archivo dentro de una variable en memoria RAM
//Leemos un ARCHIVO desde DISCO DURO y lo colocamos en una DATAFRAME en memoria RAM
var dfPersona = spark.read.format("csv").option("header", "true").option("delimiter", "|").load("dbfs:///FileStore/dataset/persona.data")

// COMMAND ----------

//Vemos el contenido
dfPersona.show()

// COMMAND ----------

//Databricks ofrece una función especial que nos permite visualizar de una mejor manera el contenido de un dataframe
display(dfPersona)

// COMMAND ----------

//IMPORTANTE: Aunque es cierto que lo imprime de una mejor manera, esta función es propia de Databricks, así que esta función no podrá ejecutarse en otros entornos de SPARK (p.e., Cloudera, Hortonworks, HDInsight [Azure], EMR [AWS], Dataproc [GCP]), solo hay que usarla si sabemos que nuestro código se ejecutará siempre en Databricks

// COMMAND ----------

// DBTITLE 1,2. Definición de esquema
//Veremos los campos que tiene el dataframe
dfPersona.printSchema()

// COMMAND ----------

//Todos los datos están como "STRING", colocaremos "EDAD" y "SALARIO" a "INTEGER" y "DOUBLE"

// COMMAND ----------

//Importamos los objetos "StructType" y el "StructField"
//Estos objetos nos ayudarán a definir la metadata
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.StructField

// COMMAND ----------

//Si importamos varios objetos de un mismo paquete, podemos escribirlo así
import org.apache.spark.sql.types.{StructType, StructField}

// COMMAND ----------

//Importamos los tipos de datos que usaremos
import org.apache.spark.sql.types.{StringType, IntegerType, DoubleType}

// COMMAND ----------

//Esta librería tiene otros tipos de datos
//Para importarlos todos usamos la siguiente linea
import org.apache.spark.sql.types._

// COMMAND ----------

//Leemos el archivo indicando el esquema
var dfPersona2 = spark.read.format("csv").option("header", "true").option("delimiter", "|").schema(
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

// COMMAND ----------

//Mostramos la data
dfPersona2.show()

// COMMAND ----------

//Mostramos el esquema de la data
dfPersona2.printSchema()

// COMMAND ----------

// DBTITLE 1,3. Leyendo todos los archivos dentro de un directorio
//Recordemos que la carpeta "transaccion" hemos colocado tres archivos

// COMMAND ----------

// MAGIC %fs ls dbfs:///FileStore/dataset/transaccion

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

// COMMAND ----------

//Vemos el contenido
dfTransaccion.show()
