codigo de spark con scala


var dfcategories=spark.read.format("csv").option("header","true").option("delimiter",",").load("dbfs:///FileStore/dataset/Northwind/*")

dfcategories.show()

mostrar el encabezado
dfcategories.printSchema()
