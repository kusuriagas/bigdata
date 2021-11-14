
import org.apache.spark.sql.SparkSession
val spark = SparkSession.builder.appName("miApp").master("local").getOrCreate()

val sc = spark.sparkContext

// Cargamos un fichero como RDD
val lineas = sc.textFile("/home/vmuser/ficheros/strangersCharacters.txt")
lineas.collect()

// El RDD, actualmente no tiene ninguna estructura. Podemos crearla.
case class Personaje(nombre: String, edad: Long, sexo: String)

import org.apache.spark.sql.Row
import spark.implicits._

val partes = lineas.map(_.split(","))
partes.collect()

// Lo transformamos en un DataSet
val personajes = partes.map(atr => Personaje(atr(0), atr(1).trim.toInt, atr(2))).toDS()

personajes.show()
personajes.select($"nombre").first

// Tambien tenemos disponibles las transformaciones y acciones que usamos
// en los RDD
personajes.foreach(p => println("El personaje " +  p(0) + " tiene " + p(1) + " años"))

// Persistencia
import org.apache.spark.storage.StorageLevel
personajes.persist(StorageLevel.MEMORY_AND_DISK_2)

// Transformaciones 
personajes.where($"edad" > "13").show()
personajes.agg(max($"edad")).show()
personajes.agg(sum($"edad")).show()

// Podemos transformarlo en un DF asignando nombres a las columnas
val personajesDF = personajes.toDF("name","age","sexo")
personajesDF.select($"age").show()

// El metodo takeAsList nos devuelve una lista de elementos
personajes.takeAsList(5)

// Podemos pasar de DS a RDD
personajes.rdd

// Por ultimo, vamos a crear ficheros
val nombresEdad = personajesDF.select("name","age")

//Por defecto escribe en formato parquet
nombresEdad.write.save("/home/vmuser/personajesParquet")
//Aunque podemos establecerlo nosotros
nombresEdad.write.json("/home/vmuser/personajesJSON")
