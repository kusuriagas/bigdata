// Databricks notebook source
//COPYRIGHT: BIG DATA ACADEMY [info@bigdataacademy.org]
//AUTHOR: ALONSO MELGAREJO [alonsoraulmgs@gmail.com]

// COMMAND ----------

// DBTITLE 1,1. Librerías
import org.apache.spark.sql.types.{StructType, StructField}
import org.apache.spark.sql.types.{StringType, IntegerType, DoubleType}
import org.apache.spark.sql.types._

// COMMAND ----------

// DBTITLE 1,2. Lectura de dataframes
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

// DBTITLE 1,3. Registro de dataframes como TempViews
//Registramos los dataframes como TempViews
dfPersona.createOrReplaceTempView("dfPersona")
dfTransaccion.createOrReplaceTempView("dfTransaccion")

// COMMAND ----------

//Verificamos que las TempViews se hayan creado
spark.sql("SHOW VIEWS").show()

// COMMAND ----------

// DBTITLE 1,4. Procesamos con SQL
//PASO 1: Agregaremos los nombres y edades de las personas que realizaron transacciones
var df1 = spark.sql("""
SELECT
  T.ID_PERSONA,
  P.NOMBRE NOMBRE_PERSONA,
  P.EDAD EDAD_PERSONA,
  T.ID_EMPRESA,
  T.MONTO,
  T.FECHA
FROM
  dftransaccion T
    JOIN dfpersona P ON T.ID_PERSONA = P.ID
""")

//Mostramos los datos
df1.show()

//Guardamos el dataframe como TempView
df1.createOrReplaceTempView("df1")

// COMMAND ----------

//PASO 2: Filtramos por edad

//Crearemos una variable para filtrar por edad
var PARAM_EDAD = 25

//Procesamos
var df2 = spark.sql(f"""
SELECT
  T.ID_PERSONA,
  T.NOMBRE_PERSONA,
  T.EDAD_PERSONA,
  T.ID_EMPRESA,
  T.MONTO,
  T.FECHA
FROM
  df1 T
WHERE
  T.EDAD_PERSONA > $PARAM_EDAD%s
""")

//Vemos el resultado
df2.show()

//Guardamos el dataframe como TempView
df2.createOrReplaceTempView("df2")

// COMMAND ----------

//PASO 3: Nos quedamos con las transacciones hechas por personas cuya inicial es la letra A

//Crearemos una variable para filtrar por la inicial
var PARAM_INICIAL = "A"

//Procesamos
var df3 = spark.sql(f"""
SELECT
  T.ID_PERSONA,
  T.NOMBRE_PERSONA,
  T.EDAD_PERSONA,
  T.ID_EMPRESA,
  T.MONTO,
  T.FECHA
FROM
  df2 T
WHERE
  SUBSTRING(T.NOMBRE_PERSONA, 0, 1) = '$PARAM_INICIAL%s'
""")

//Vemos el resultado
df3.show()

//Guardamos el dataframe como TempView
df3.createOrReplaceTempView("df3")

// COMMAND ----------

// DBTITLE 1,5. Escritura de resultante final
//Escribimos el dataframe de la resultante final en disco duro
df3.write.format("csv").mode("overwrite").option("header", "true").option("delimiter", "|").save("dbfs:///FileStore/output/df3")

// COMMAND ----------

//Verificamos la ruta para ver si el archivo se escribió

// COMMAND ----------

// MAGIC %fs ls dbfs:///FileStore/output/df3

// COMMAND ----------

// DBTITLE 1,6. Verificamos
//Para verificar, leemos el directorio del dataframe en una variable
//Como solo quiero verificar que esta escrito, puedo omitir la definición del esquema
var df3Leido = spark.read.format("csv").option("header", "true").option("delimiter", "|").load("dbfs:///FileStore/output/df3")

//Mostramos los datos
df3Leido.show()

// COMMAND ----------

// DBTITLE 1,7. Descargando el archivo de la resultante final
//Para descargar el archivo, vamos a listar el directorio en donde se encuentra

// COMMAND ----------

// MAGIC %fs ls dbfs:///FileStore/output/df3

// COMMAND ----------

//Para descargar un archivo debemos de acceder a esta URL y darle la ruta del archivo
//
// https://community.cloud.databricks.com/files/RUTA_A_MI_ARCHIVO/NOMBRE_ARCHIVO?o=ID_CUENTA
//
//Donde:
//
// - RUTA_A_MI_ARCHIVO: Ruta en donde se encuentra mi archivo, sin considerar "FileStore" (/output/df3/)
// - NOMBRE_ARCHIVO: Nombre del archivo que se descarga (part-00000-tid-9064573699596065895-5d893d3d-f57d-4696-b12f-a814632af95b-29-1-c000.csv)
// - ID_CUENTA: Identificador de la cuenta, lo podemos saber mirando la URL del Notebook y viendo el valor del parámetro "o" (4458995983973520)
//
//Entonces, para descargar este archivo:
//
// dbfs:///FileStore/output/df3/part-00000-tid-9064573699596065895-5d893d3d-f57d-4696-b12f-a814632af95b-29-1-c000.csv
//
//Los valores serían:
//
// - RUTA_A_MI_ARCHIVO: /output/df3/
// - NOMBRE_ARCHIVO: part-00000-tid-9064573699596065895-5d893d3d-f57d-4696-b12f-a814632af95b-29-1-c000.csv
// - ID_CUENTA: 4458995983973520
//
//Y la URL sería:
//
// https://community.cloud.databricks.com/files/output/df3/part-00000-tid-9064573699596065895-5d893d3d-f57d-4696-b12f-a814632af95b-29-1-c000.csv?o=4458995983973520
