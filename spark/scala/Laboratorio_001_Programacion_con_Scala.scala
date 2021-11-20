// Databricks notebook source
//COPYRIGHT: BIG DATA ACADEMY [info@bigdataacademy.org]
//AUTHOR: ALONSO MELGAREJO [alonsoraulmgs@gmail.com]

// COMMAND ----------

// DBTITLE 1,1. Creación de variables
//En Scala la sintaxis para la definición de variables es la siguiente:
// var nombreVariable : TipoDeDatos = ValorInicial

//Definamos una variable entera
var edad : Int = 25

// COMMAND ----------

//Imprimimos
print(edad)

// COMMAND ----------

//La función "print" imprime los mensajes uno tras otro, esto puede ser un problema al leer los datos
print(edad)
print(edad)
print(edad)

// COMMAND ----------

//En su lugar usaremos "println", que agrega un salto de línea al imprimir
println(edad)
println(edad)
println(edad)

// COMMAND ----------

//Podemos reasignarle un valor
edad = 30
println(edad)

// COMMAND ----------

//Declaracion de una variable sin un valor inicial
var edad2 : Int = _
println(edad2)

// COMMAND ----------

// DBTITLE 1,2. Creación de constantes
//Una constante es una variable a la cual sólo se le puede asignar un valor un vez
//Una vez asignado, ya no puede modificarse

//Va antecedida por la palabra reservada "val"
val estatura : Double = 1.75

// COMMAND ----------

//Si tratamos de reasignarle un valor, el intérprete de Scala nos mostrará un error
//Descomentar esta linea para ver el error
//estatura = 1.76

// COMMAND ----------

// DBTITLE 1,3. Definición de cadenas de caracteres
//Las cadenas de caracteres van entre comillas dobles, NO SE PERMITEN COMILLAS SIMPLES

//Definicion con comillas dobles
//El tipo de datos es String
var nombre : String = "Alonso Raul"
println(nombre)

// COMMAND ----------

//También es posible crear una variable String omitiendo su declaración
//En lugar de var apellido : String = "Melgarejo Galvan", omitiremos "String"
var apellido = "Melgarejo Galvan"
println(apellido)

// COMMAND ----------

//Si nuestra cadena de caracteres tiene sólo un caracter, si se pueden usar comillas SIMPLES
//El tipo de datos es Char
var inicial : Char = 'A'
println(inicial)

// COMMAND ----------

//Para efectos prácticos, omitiremos siempre el tipo de datos "String" y usaremos siempre las comillas dobles

// COMMAND ----------

// DBTITLE 1,4. Operaciones con String
//Concatenacion de strings
var nombreCompleto = nombre + " " + apellido
println(nombreCompleto)

// COMMAND ----------

//Substring
//Primer parametro: Desde que posicion se extrae la subcadena
//Segunda parametro: Cuantos caracteres se extraen
//Desde el caracter 0, se extraen 3 caracteres:
var subcadena = nombre.substring(0, 3)
println(subcadena)

// COMMAND ----------

var otroNombre = nombreCompleto.replaceAll("Alonso", "Daniel")
println(otroNombre)

// COMMAND ----------

// DBTITLE 1,5. Importar clases en paquetes
//En Scala, hay muchas funciones utilitarias ya implementadas
//Las funciones se colocan en objetos conocidos como "class" (clases)
//Para ordenar las clases, estas se colocan en "package" (paquetes), podemos verlas como directorios
//Por ejemplo, existe una clase con funciones utilitarias llamada "Math", que se encuentra dentro del paquete "java", subpaquete "lang"
//Para importarlo usamos la palabra reservada "import"
import java.lang.Math

// COMMAND ----------

//La clase "Math" tiene una función llamada "random"
//La función "random" genera un número aleatorio entre 0 y 1
var aleatorio : Double = Math.random()
println(aleatorio)

// COMMAND ----------

//Otra función dentro de "Math" es "pow"
//Sirve para calcular potencias
//Por ejemplo, 2 al cubo
var potencia : Double = Math.pow(2, 3)
println(potencia)

// COMMAND ----------

// DBTITLE 1,6. Condicionales
//Definimos una variable
var salario : Double = 10000
println(salario)

// COMMAND ----------

//Verificamos el salario con una condición
if(salario > 5000){
  println("Salario alto")
}else{
  println("Salario bajo")
}

// COMMAND ----------

//Colocamos un nuevo valor al salario
salario = 3500
println(salario)

// COMMAND ----------

//Verificamos el salario con las siguientes condiciones
//Si es mayor a 5000, es un salario alto
//Si es menor o igual 5000 y mayor a 3000, es un salario medio
//Si es menor o igual a 3000, es un salario bajo

if(salario > 5000){
  println("Salario alto")
}else if(salario <= 5000 && salario > 3000){
  println("Salario medio")
}else{
  println("Salario bajo")
}

// COMMAND ----------

// DBTITLE 1,7. Creación de funciones
//Crearemos una función que encapsule el codigo anterior
//Recibirá el salario (Double) y devolverá un mensaje (String)

//Definimos el nombre de la funcion (verificaSalario), los parametros recibidos (salario : Double) y el tipo de dato devuelto (String)
def verificaSalario(salario : Double) : String = {
  //Definimos la variable que retorna la función
  //Lo ideal sería crear la variable así:
  //var resultado : String = _
  //Sin embargo, variables dentro de funciones no pueden inicializarse así
  //Colocaremos el valor inicial en "nulo"
  var resultado : String = null
  
  //Implementamos la logica
  if(salario > 5000){
    resultado = "Salario alto"
  }else if(salario <= 5000 && salario > 3000){
    resultado = "Salario medio"
  }else{
    resultado = "Salario bajo"
  }
  
  //Devolvemos el resultado
  return resultado
}

// COMMAND ----------

//Usamos la función
var resultado1 = verificaSalario(1000)
println(resultado1)

var resultado2 = verificaSalario(3500)
println(resultado2)

var resultado3 = verificaSalario(20000)
println(resultado3)

// COMMAND ----------

//También podemos crear funciones con varios parámetros
def suma(a : Double, b : Double) : Double = {
  var resultado : Double = 0.0
  
  //Sumamos
  resultado = a + b
  
  return resultado
}

// COMMAND ----------

//Usamos la función
print(suma(1, 2))

// COMMAND ----------

// DBTITLE 1,8. Arrays y bucles
//Definimos un array de enteros de 3 elementos
var numeros : Array[Int] = new Array[Int](3)

// COMMAND ----------

//Colocamos valores a cada elemento
//Para acceder a cada elemento usamos los paréntesis
numeros(0) = 1000
numeros(1) = 3500
numeros(2) = 20000

// COMMAND ----------

//Podemos imprimir manualmente el valor de cada elemento
println(numeros(0))
println(numeros(1))
println(numeros(2))

// COMMAND ----------

//Podemos averiguar la longitud del array
println(numeros.length)

// COMMAND ----------

//Vamos a imprimir los valores del array iterándolo en un bucle
for(elemento <- numeros){
  println(elemento)
}

// COMMAND ----------

//Si queremos el índice de la iteración, podemos iterar de la siguiente manera
for(i <- 0 until numeros.length){
  println("Iteración "+i)
  println(numeros(i))
}

// COMMAND ----------

// DBTITLE 1,9. Excepciones
//Sabemos que hay codigos que producen errores
//Si tratamos de dividir entre 0, se produce un error

//Descomentar para ver el error
//var division : Double = 28111989/0

// COMMAND ----------

//Para que el código no se detenga, podemos encerrar el código que potencialmente pueda producir errores

var division : Double = 200
var edad : Int = 28

try{
  println("Trataremos de dividir entre 0")
  division = 28111989/0
  println("Esta linea nunca se imprimira, porque antes se produce una excepcion")
}catch{
  case exception: Throwable => {
    println("Se produjo una excepcion, asignaremos algun valor a la variable")
    division = 1
  }
}finally{
  println("Este codigo siempre se ejecuta, independientemente de si se ejecuta el try o el catch")
  edad = 30
}

// COMMAND ----------

//Imprimimos las variables
println(division)
println(edad)
