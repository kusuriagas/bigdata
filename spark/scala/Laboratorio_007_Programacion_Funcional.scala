// Databricks notebook source
//COPYRIGHT: BIG DATA ACADEMY [info@bigdataacademy.org]
//AUTHOR: ALONSO MELGAREJO [alonsoraulmgs@gmail.com]

// COMMAND ----------

// DBTITLE 1,1. Programación Funcional
//Para entender los primeros conceptos hemos estado usando SQL como lenguaje de programación
//SQL es un buen lenguaje para procesamiento estructurado batch, pero es mu limitado
//La programación funcional nos permite utilizar la sintaxis de una lenguaje de programación
//Nos permitirá implementar nuestras propias funciones, por eso se le llama "Programación Funcional"

// COMMAND ----------

// DBTITLE 1,2. Importamos las librerías
import org.apache.spark.sql.types.{StructType, StructField}
import org.apache.spark.sql.types.{StringType, IntegerType, DoubleType}
import org.apache.spark.sql.types._

// COMMAND ----------

// DBTITLE 1,3. Lectura de datos
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

// DBTITLE 1,4. Transformations y actions
//Vamos a empezar a procesar los dataframes con SCALA
//Existen dos tipos de funciones para procesar, los transformations y los actions
//Los transformations permiten definir las operaciones que queremos realizar, pero no las ejecutan
//Los actions ejecutan las operaciones que hemos definido

// COMMAND ----------

// DBTITLE 1,5. Transformation "select"
//Por ejemplo, vamos a crear un nuevo dataframe con algunos campos del dataframe "dfPersona"

//EQUIVALENTE SQL: SELECT ID, NOMBRE, EDAD FROM dfPersona
var df1 = dfPersona.select("ID", "NOMBRE", "EDAD")

//Vemos los datos
//Para verlos, llamamos al "action" show
//Más adelante detallaremos los "actions", por ahora sólo los usaremos para ver los datos
df1.show()

// COMMAND ----------

//Aunque el código anterior funciona, se recomienda SIEMPRE llamar a los campos incluyendo el nombre del dataframe de la siguiente manera

// COMMAND ----------

//EQUIVALENTE SQL: SELECT ID, NOMBRE, EDAD FROM dfPersona
//Lo hacemos así porque en el futuro procesaremos varios dataframes a la vez y para tener trazabilidad del código debemos saber de qué dataframe proviene la columna
df1 = dfPersona.select(dfPersona.col("ID"), dfPersona.col("NOMBRE"), dfPersona.col("EDAD"))

//Vemos los datos
df1.show()

// COMMAND ----------

// DBTITLE 1,6. Transformation "filter"
//OPERACION EQUIVALENTE EN SQL:
//SELECT * FROM dfPersona WHERE EDAD > 60

//Hacemos un filtro
var df2 = dfPersona.filter(dfPersona.col("EDAD") > 60)

//Mostramos los datos
df2.show()

// COMMAND ----------

//Hacemos un filtro con un "and"
//SELECT * FROM dfPersona WHERE EDAD > 60 AND SALARIO > 20000
var df3 = dfPersona.filter(
  (dfPersona.col("EDAD") > 60) &&
  (dfPersona.col("SALARIO") > 20000)
)

//Mostramos los datos
df3.show()

// COMMAND ----------

//Hacemos un filtro con un "or"
//SELECT * FROM dfPersona WHERE EDAD > 60 OR EDAD < 20
var df4 = dfPersona.filter(
  (dfPersona.col("EDAD") > 60) || 
  (dfPersona.col("EDAD") < 20)
)

//Mostramos los datos
df4.show()

// COMMAND ----------

// DBTITLE 1,7. Transformation "groupBy"
//OPERACION EQUIVALENTE EN SQL:
//SELECT 
//	EDAD
//	COUNT(EDAD)
//	MIN(FECHA_INGRESO)
//	SUM(SALARIO)
//	MAX(SALARIO)
//FROM
//	dfData
//GROUP BY
//	EDAD

// COMMAND ----------

//Notemos que queremos usar algunas funciones "estándar" como:
//
// - COUNT: Para contar registros
// - MIN: Para saber el valor mínimo de un campo
// - SUM: Para sumar todos los valores de un campo
// - MAX: Para saber el valor máximo de un campo
//
//Todas estas funciones son funciones "clásicas" que los desarrolladores usan
//SPARK ya las tiene implementadas en una librería
import org.apache.spark.sql.functions._

// COMMAND ----------

//Hacemos un GROUP BY para agrupar a las personas por su edad
//Usamos las funciones estándar que tiene SPARK
var df5 = dfPersona.groupBy(dfPersona.col("EDAD")).agg(
	count(dfPersona.col("EDAD")), 
	min(dfPersona.col("FECHA_INGRESO")), 
	sum(dfPersona.col("SALARIO")), 
	max(dfPersona.col("SALARIO"))
)

//Mostramos los datos
df5.show()

// COMMAND ----------

//En ocasiones podemos usar funciones que vengan desde diferentes librerías
//Podemos colocar todas las funciones en una variable para saber de qué librería viene
import org.apache.spark.sql.{functions => f}

// COMMAND ----------

//Y usarlas haciendo referencia a la variable
var df5 = dfPersona.groupBy(dfPersona.col("EDAD")).agg(
	f.count(dfPersona.col("EDAD")), 
	f.min(dfPersona.col("FECHA_INGRESO")), 
	f.sum(dfPersona.col("SALARIO")), 
	f.max(dfPersona.col("SALARIO"))
)

//Mostramos los datos
df5.show()

// COMMAND ----------

//Revisemos el esquema, notamos que las columnas reciben nombre "extraños"
df5.printSchema()

// COMMAND ----------

//Colocaremos un alias a las columnas
var df6 = dfPersona.groupBy(dfPersona.col("EDAD")).agg(
	f.count(dfPersona.col("EDAD")).alias("CANTIDAD"), 
	f.min(dfPersona.col("FECHA_INGRESO")).alias("FECHA_CONTRATO_MAS_RECIENTE"), 
	f.sum(dfPersona.col("SALARIO")).alias("SUMA_SALARIOS"), 
	f.max(dfPersona.col("SALARIO")).alias("SALARIO_MAYOR")
)

//Mostramos los datos
df6.show()

// COMMAND ----------

//Revisamos el esquema
df6.printSchema()

// COMMAND ----------

// DBTITLE 1,8. Transformation "JOIN"
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

//Leemos las transacciones
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

//Mostramos los datos
dfTransaccion.show()

// COMMAND ----------

//Implementacion del JOIN
var dfJoin = dfTransaccion.alias("T").join(
  dfPersona.alias("P"), 
  f.col("T.ID_PERSONA") === f.col("P.ID"),
  "inner"
).select(
  "P.NOMBRE", 
  "P.EDAD", 
  "P.SALARIO", 
  "T.MONTO", 
  "T.FECHA"
)

//Mostramos los datos
dfJoin.show()

// COMMAND ----------



// COMMAND ----------

// DBTITLE 1,6. Actions "show" y "save"
//Generalmente con los dataframes queremos hacer dos cosas, verlos o almacenarlos
//Estos son "actions" que ejecutan la cadena de procesos

// COMMAND ----------

//Al ejecutar "df1", la cadena de procesos asociada se ejecuta y se carga en memoria RAM
df1.show()

// COMMAND ----------

//Si volvemos a llamar al "action", la cadena de procesos se ejecuta nuevamente
df1.show()
