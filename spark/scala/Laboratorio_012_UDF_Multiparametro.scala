// Databricks notebook source
//COPYRIGHT: BIG DATA ACADEMY [info@bigdataacademy.org]
//AUTHOR: ALONSO MELGAREJO [alonsoraulmgs@gmail.com]

// COMMAND ----------

// DBTITLE 1,1. Librerías
import org.apache.spark.sql.types.{StructType, StructField}
import org.apache.spark.sql.types.{StringType, IntegerType, DoubleType}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{functions => f}
import org.apache.spark.sql.functions.udf

// COMMAND ----------

// DBTITLE 1,2. Lectura de datos
//Leemos el archivo de cliente
var dfCliente = spark.read.format("csv").option("header", "true").option("separator", ",").schema(
    StructType(
        Array(
            StructField("ID_CLIENTE", StringType, true),
            StructField("SEXO", StringType, true),
            StructField("EDAD", IntegerType, true),
            StructField("ESTATURA", DoubleType, true),
            StructField("ESTADO_CIVIL", StringType, true),
            StructField("TELEFONO", StringType, true)
        )
    )
).load("dbfs:///FileStore/dataset/CLIENTE.csv")

dfCliente.show()

//Mostramos los datos
dfCliente.show()

// COMMAND ----------

// DBTITLE 1,3. Implementación de función
//Funcion para calcular el salario anual
def clasificarCliente(sexo : String, edad : Integer) : String = {
  //Aquí colocaremos la respuesta
  var respuesta : String = ""
  
  if(sexo == "MASCULINO"){
    if(edad < 30){
      respuesta = "REGULAR"
    }else if(edad >= 30 && edad < 50){
      respuesta = "TOP"
    }else{
      respuesta = "PREMIUM"
    }
  }else{
    if(edad < 25){
      respuesta = "REGULAR"
    }else if(edad >= 25 && edad < 40){
      respuesta = "TOP"
    }else{
      respuesta = "PREMIUM"
    }
  }
  
  //Devolvemos el valor
  return respuesta
}

// COMMAND ----------

// DBTITLE 1,4. Definición del UDF
//Creamos la función personalizada
val udfClasificarCliente = udf((sexo : String, edad : Integer) => clasificarCliente(sexo, edad))

// COMMAND ----------

//Registramos el UDF
spark.udf.register("udfClasificarCliente", udfClasificarCliente)

// COMMAND ----------

// DBTITLE 1,5. Uso del UDF
//Aplicamos la función
var df1 = dfCliente.select(
  dfCliente.col("ID_CLIENTE"),
  dfCliente.col("TELEFONO").alias("CONTACTO_CLIENTE"),
  udfClasificarCliente(dfCliente.col("SEXO"), dfCliente.col("EDAD")).alias("CATEGORIA_CLIENTE")
)

// COMMAND ----------

//Vemos los datos
df1.show()

// COMMAND ----------

// DBTITLE 1,6. Uso del UDF con withcolumn
//Aplicamos el UDF
var df2 = dfCliente.withColumn("CATEGORIA_CLIENTE", udfClasificarCliente(dfCliente.col("SEXO"), dfCliente.col("EDAD")))

//Mostramos los datos
df2.show()

//¿Negocio realmente necesita todas esas columnas?, recordemos que desperdiciamos uso de memoria RAM

// COMMAND ----------

// DBTITLE 1,7. Uso del UDF con SQL
//Luego de definir el UDF, lo hemos registrado de esta manera
spark.udf.register("udfClasificarCliente", udfClasificarCliente)

// COMMAND ----------

//Al hacer esto, podemos usarlo dentro de SPARK SQL

//Vamos a registrar al "dfCliente" como una vista temporal
dfCliente.createOrReplaceTempView("dfCliente")

// COMMAND ----------



// COMMAND ----------

//Procesémoslo con SPARK SQL haciendo uso de nuestro UDF
var df3 = spark.sql("""
SELECT
  ID_CLIENTE,
  TELEFONO CONTACTO_CLIENTE,
  udfClasificarCliente(SEXO, EDAD) CATEGORIA_CLIENTE
FROM
  dfCliente
""")

//Mostramos los datos
df3.show()
