

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{StructType, StructField}
import org.apache.spark.sql.types.{StringType, IntegerType, DoubleType}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{functions => f}


//Definimos la reserva
var spark = SparkSession.builder.
appName("Mi Aplicacion").
config("spark.driver.memory", "1g").
config("spark.dynamicAllocation.maxExecutors", "10").
config("spark.executor.cores", "2").
config("spark.executor.memory", "2g").
config("spark.executor.memoryOverhead", "1g").
enableHiveSupport().
getOrCreate()


//lectura
var dfcategories = spark.read.format("csv").option("header", "true").option("delimiter", ",").schema(
    StructType(
        Array(
            StructField("category_id", IntegerType, false),
            StructField("category_name", StringType, true),
            StructField("description", StringType, true)
        )
    )
).load("gs://repositorio_bigdata/datalake/landingtemp/categories/categories.csv")

dfcategories.show()


var dfproducts = spark.read.format("csv").option("header", "true").option("delimiter", ",").schema(
    StructType(
        Array(
            StructField("product_id", IntegerType, false),
            StructField("product_name", StringType, true),
            StructField("supplier_id", IntegerType, true),
            StructField("category_id", IntegerType, true),
            StructField("quantity_per_unit", StringType, true),
            StructField("unit_price", DoubleType, true),
            StructField("units_in_stock", IntegerType, true)
        )
    )
).load("gs://repositorio_bigdata/datalake/landingtemp/products/products.csv")

dfproducts.show()



var dforders_details = spark.read.format("csv").option("header", "true").option("delimiter", ",").schema(
    StructType(
        Array(
            StructField("order_id", IntegerType, false),
            StructField("product_id", IntegerType, true),
            StructField("unit_price", DoubleType, true),
            StructField("quantity", IntegerType, true),
            StructField("discount", DoubleType, true)
        )
    )
).load("gs://repositorio_bigdata/datalake/landingtemp/orders_details/order_details.csv")


dforders_details.show()

var dforders = spark.read.format("csv")
                        .option("header", "true")
                        .option("delimiter", ",")
                        .option("encoding", "ISO-8859-1")
                        .schema(
    StructType(
        Array(
            StructField("order_id", IntegerType, true),
            StructField("customer_id", IntegerType, true),
            StructField("employee_id", StringType, true),
            StructField("order_date", StringType, true),
            StructField("required_date", StringType, true),
            StructField("shipped_date", StringType, true),
           // StructField("ship_via", IntegerType, true),
            //StructField("freight", DoubleType, true),
            //StructField("ship_name", StringType, true),
            //StructField("ship_address", StringType, true),
            //StructField("ship_city", StringType, true),
            //StructField("ship_region", StringType, true),
            //StructField("ship_postal_code", StringType, true),
            StructField("ship_country", StringType, true)
        )
    )
).load("gs://repositorio_bigdata/datalake/landingtemp/orders/orders.csv")


dforders.show()


dforders.printSchema()


var dfcustomers = spark.read.format("csv").option("header", "true").option("delimiter", ",").schema(
    StructType(
        Array(
            StructField("customer_id", StringType, true),
            StructField("company_name", StringType, true),
            StructField("contact_name", StringType, true),
            StructField("contact_title", StringType, true),
            StructField("address", StringType, true),
            StructField("city", StringType, true),
            StructField("region", StringType, true),
            StructField("postal_code", StringType, true),
            StructField("country", StringType, true),
            StructField("phone", StringType, true),
            StructField("fax", StringType, true)
        )
    )
).load("gs://repositorio_bigdata/datalake/landingtemp/customers/customers.csv")


dfcustomers.show()


var dfemployees = spark.read.format("csv").option("header", "true").option("delimiter", ",").schema(
    
    
    StructType(
        Array(
            StructField("employee_id", StringType, true),
            StructField("last_name", StringType, true),
            StructField("first_name", StringType, true),
            StructField("title", StringType, true),
            StructField("title_of_courtesy", StringType, true),
            StructField("birth_date", StringType, true),
            StructField("hire_date", StringType, true),
            StructField("address", StringType, true),
            StructField("city", StringType, true),
            StructField("region", StringType, true),
            StructField("postal_code", StringType, true),
            StructField("country", StringType, true),
            StructField("home_phone", StringType, true)
            
        )
    )
).load("gs://repositorio_bigdata/datalake/landingtemp/employees/employees.csv")


dfemployees.show()

