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

// DBTITLE 1,3. Actions para ejecutar pasos de procesamiento
//DEFINIMOS LOS PASOS DE PROCESAMIENTO

//PASO 1: Agrupamos los datos según la edad
var df1 = dfPersona.groupBy(dfPersona.col("EDAD")).agg(
	f.count(dfPersona.col("EDAD")).alias("CANTIDAD"), 
	f.min(dfPersona.col("FECHA_INGRESO")).alias("FECHA_CONTRATO_MAS_RECIENTE"), 
	f.sum(dfPersona.col("SALARIO")).alias("SUMA_SALARIOS"), 
	f.max(dfPersona.col("SALARIO")).alias("SALARIO_MAYOR")
)

//PASO 2: Filtramos por una EDAD
var df2 = df1.filter(df1.col("EDAD") > 35)

//PASO 3: Filtramos por SUMA_SALARIOS
var df3 = df2.filter(df2.col("SUMA_SALARIOS") > 20000)

//PASO 4: Filtramos por SALARIO_MAYOR
var dfResultado = df3.filter(df3.col("SALARIO_MAYOR") > 1000)

// COMMAND ----------

//Al ejecutar la sección de código anterior, solamente hemos definido cómo se crearán los dataframes "df1", "df2", "df3" y "dfResultado"
//Los dataframes aún no se han creado y no ocupan memoria RAM en el clúster
//Para que se cree, deberemos llamar a un action
//Generalmente con los dataframes queremos hacer dos cosas, verlos o almacenarlos
//Estos son "actions" que ejecutan la cadena de procesos

// COMMAND ----------

//Al ejecutar "dfResultado.show()", la cadena de procesos asociada se ejecuta y se cargan en RAM todos los dataframes
dfResultado.show()

// COMMAND ----------

//Si volvemos a llamar al "action", nuevamente se tendrá que recalcular toda la cadena de procesos
//Si la cadena de procesos se demora 1 hora, para que el action "show" muestre los datos, cada vez que lo ejecutemos deberemos esperar 1 hora
dfResultado.show()

// COMMAND ----------

// DBTITLE 1,4. Procesamiento encadenado
//Definimos los pasos de procesamiento en una única cadena de proceso
var dfResultado2 = dfPersona.groupBy(dfPersona.col("EDAD")).agg(
	f.count(dfPersona.col("EDAD")).alias("CANTIDAD"), 
	f.min(dfPersona.col("FECHA_INGRESO")).alias("FECHA_CONTRATO_MAS_RECIENTE"), 
	f.sum(dfPersona.col("SALARIO")).alias("SUMA_SALARIOS"), 
	f.max(dfPersona.col("SALARIO")).alias("SALARIO_MAYOR")
).alias("P1").
filter(f.col("P1.EDAD") > 35).alias("P2").
filter(f.col("P2.SUMA_SALARIOS") > 20000).alias("P3").
filter(f.col("P3.SALARIO_MAYOR") > 1000)

// COMMAND ----------

//Al ejecutar la sección de código anterior, solamente hemos definido cómo se creará el dataframe "dfResultado2"
//Cada paso, también crea en memoria RAM un dataframe ("P1", "P2", "P3"), sólo que ya no lo tenemos en una variable
//Eso significa que encadenar procesos utiliza la misma cantidad de memoria RAM que el hacerlo en variables separadas
//Los dataframes aún no se han creado y no ocupan memoria RAM en el clúster
//Para que se cree, deberemos llamar a un action

// COMMAND ----------

//Al ejecutar "dfResultado2.show()", la cadena de procesos asociada se ejecuta y se cargan en RAM todos los dataframes
dfResultado2.show()

// COMMAND ----------

// DBTITLE 1,5. Posible colapso de memoria RAM
//Nuestros procesamiento independientemente de que estén en pasos separados o en una única cadena de procesamiento ocupa la misma cantidad de memoria RAM
//Mientras más pasos tenga el código, más dataframes ocuparán espacio en la memoria RAM
//La memoria RAM es limitada, si tenemos un script con muchos pasos de procesamiento, es probable que en algún momento termine colapsando y nuestro script no se ejecute
//Esto lo solucionaremos en laboratorios posteriores
