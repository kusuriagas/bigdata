// Databricks notebook source
//COPYRIGHT: BIG DATA ACADEMY [info@bigdataacademy.org]
//AUTHOR: ALONSO MELGAREJO [alonsoraulmgs@gmail.com]

// COMMAND ----------

// DBTITLE 1,1. Importamos las librerías
import org.apache.spark.sql.types.{StructType, StructField}
import org.apache.spark.sql.types.{StringType, IntegerType, DoubleType}
import org.apache.spark.sql.types._

// COMMAND ----------

// DBTITLE 1,2. Procesamiento con Spark SQL
//La mejor forma de aprovechar SPARK es con algún lenguaje de programación
//Por ejemplo podemos usar Java, Scala, Python o R para empezar a programar
//Pero, si no conocemos un lenguaje de programación, ¿podemos usar SPARK?
//Spark tiene un que nos permite procesar los dataframes con la sintaxis SQL
//A este módulo se le conoce como SPARK SQL
//SPARK SQL es ideal para implementar procesamiento de datos estructurados

// COMMAND ----------

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

//Vamos a visualizar el dataframe como si fuera una tabla de base de datos
//Para eso deberemos registrar al dataframe como una "TempView"
dfPersona.createOrReplaceTempView("dfPersona")

// COMMAND ----------

//Visualizamos los dataframes que podemos procesar como tabla
spark.sql("SHOW VIEWS").show()

// COMMAND ----------

//Filtraremos algunos registros según la edad
spark.sql("SELECT * FROM dfpersona WHERE EDAD > 25").show()

// COMMAND ----------

//En ocasiones los SQL pueden ser muy grandes
//Para escribirlos de una mejor manera, utilizaremos la triple comilla doble
//Nos permitira colocar "ENTERS" en las sentencias para ordenar el código
spark.sql("""
SELECT
  T.*
FROM
  dfPersona T
WHERE
  T.EDAD > 25
""").show()

// COMMAND ----------

//También, podemos almacenar el resultado del procesamiento SQL en una variable dataframe
//NOTAR QUE AQUÍ NO AGREGAMOS EL ".show()" AL FINAL
var df1 = spark.sql("""
SELECT
  T.*
FROM
  dfPersona T
WHERE
  T.EDAD > 25
""")

//Vemos el resultado
df1.show()

// COMMAND ----------

//En este otro ejemplo, creamos otro dataframe donde:
//Seleccionamos el campos ID, NOMBRE, CORREO, EDAD, SALARIO
//Las personas tengan más de 25 años Y
//Las personas tengan un salario mayor a 5000 dólares
var df2 = spark.sql("""
SELECT
  T.ID,
  T.NOMBRE,
  T.CORREO,
  T.EDAD,
  T.SALARIO
FROM
  dfPersona T
WHERE
  T.EDAD > 25 AND
  T.SALARIO > 5000
""")

//Vemos el resultado
df2.show()

// COMMAND ----------

// DBTITLE 1,3. Parametrizando el código SQL
//Crearemos dos variables para filtrar por edad y salario
var PARAM_EDAD = 25
var PARAM_SALARIO = 5000

// COMMAND ----------

//Crearemos la misma consulta anterior, pero parametrizada
//Debemos anteponer el caracter f antes de la triple comilla doble
//Para usar un parametro en el codigo debemos de escribir $NOMBRE_PARAMETRO%s
var df3 = spark.sql(f"""
SELECT
  T.ID,
  T.NOMBRE,
  T.CORREO,
  T.EDAD,
  T.SALARIO
FROM
  dfPersona T
WHERE
  T.EDAD > $PARAM_EDAD%s AND
  T.SALARIO > $PARAM_SALARIO%s
""")

//Vemos el resultado
df3.show()

// COMMAND ----------

// DBTITLE 1,4. Escribiendo la resultante en disco duro
//Crearemos una carpeta "output" dentro de "FileStore"

// COMMAND ----------

// MAGIC %fs mkdirs dbfs:///FileStore/output

// COMMAND ----------

//Dentro de este directorio escribiremos la resultante del datframe "df3"
//Es importante notar que para almacenar el dataframe, se creará un directorio y dentro los archivos del dataframe
//Esto se hace así porque un dataframe puede tener billones de registros, para hacer la escritura en paralelo, se escriben dentro del directorio varios archivos de manera paralela
//Podemos elegir dos modos de escritura:
//
// overwrite: si el directorio ya existe y tiene archivos, borra todos los archivos y luego escribe los archivos del dataframe
// append: si el directorio ya existe y tiene archivos, agrega los archivos del dataframe dentro del directorio
df3.write.format("csv").mode("overwrite").option("header", "true").option("delimiter", "|").save("dbfs:///FileStore/output/df3")

// COMMAND ----------

//Para verificar la escritura en disco duro, vamos a listar el contenido de "output"
//Notaremos que se ha creado la carpeta "df3"

// COMMAND ----------

// MAGIC %fs ls dbfs:///FileStore/output

// COMMAND ----------

//Ahora listaremos la carpeta "df3"

// COMMAND ----------

// MAGIC %fs ls dbfs:///FileStore/output/df3

// COMMAND ----------

//Encontraremos los siguientes archivos:
//
// _SUCCESS indica que el proceso de escritura finalizo correctamente, si finalizara con errores colocaría un archivo _ERROR
// _committed_... indica la hora en que el proceso ingresó al clúster
// _started_... indica la hora en que el proceso comenzó a ejecutarse
// part-00000-... son los archivos que tienen el contenido del dataframe

// COMMAND ----------

//Para verificar, leemos el directorio del dataframe en una variable
var df3Leido = spark.read.format("csv").option("header", "true").option("delimiter", "|").schema(
    StructType(
        Array(
            StructField("ID", StringType, true),
            StructField("NOMBRE", StringType, true),
            StructField("CORREO", StringType, true),
            StructField("EDAD", IntegerType, true),
            StructField("SALARIO", DoubleType, true)
        )
    )
).load("dbfs:///FileStore/output/df3")

//Mostramos los datos
df3Leido.show()
