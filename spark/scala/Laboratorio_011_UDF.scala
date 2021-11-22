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

// DBTITLE 1,3. UDFs
//Hemos visto que SPARK tiene implementado un juego de funciones clásicas que podemos usar
//Si queremos usar nuestras funciones, debemos de implementarlas dentro de los objetos UDF
//Las funciones personalizadas las implementamos con el lenguaje de programación (SCALA)
//El UDF es el objeto que paraleliza el uso de la función

// COMMAND ----------

//Importamos las librerías para implementar UDFs
import org.apache.spark.sql.functions.udf

// COMMAND ----------

// DBTITLE 1,4. Implementación de función
//Funcion para calcular el salario anual
def calcularSalarioAnual(salarioMensual : Double) : Double = {
  	//Calculo del salario anual
	var salarioAnual : Double = salarioMensual*12
	
	//Devolvemos el valor
	return salarioAnual
}

// COMMAND ----------

// DBTITLE 1,5. Definición del UDF
//Creamos la función personalizada
var udfCalcularSalarioAnual = udf((salarioMensual : Double) => calcularSalarioAnual(salarioMensual))

// COMMAND ----------

//Registramos el UDF
spark.udf.register("udfCalcularSalarioAnual", udfCalcularSalarioAnual)

// COMMAND ----------

// DBTITLE 1,6. Uso del UDF
//Aplicamos el UDF
var df1 = dfPersona.select(
  dfPersona.col("NOMBRE").alias("NOMBRE"),
  dfPersona.col("SALARIO").alias("SALARIO_MENSUAL"),
  udfCalcularSalarioAnual(dfPersona.col("SALARIO")).alias("SALARIO_ANUAL")
)

//Mostramos los datos
df1.show()

// COMMAND ----------

// DBTITLE 1,7. Otra forma de aplicar el UDF [Potencial Anti-Patrón]
//Aplicamos el UDF
var df2 = dfPersona.withColumn("SALARIO_ANUAL", udfCalcularSalarioAnual(dfPersona.col("SALARIO")))

//Mostramos los datos
df2.show()
