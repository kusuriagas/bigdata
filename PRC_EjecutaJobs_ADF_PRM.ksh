#!/usr/bin/bash
# ------------------------------------------------------------------------------------------------- #
# =======================================   BCP   ================================================= #
# ------------------------------------------------------------------------------------------------- #
# NOMBRE PROCESO : PRC_EjecutaJobs_ADF_PRM.ksh                                                      #
# DESCRIPCION    : Proceso del modulo Azure Data Factory que obtiene los parametros y genera un     #
#                  JSON.                                                                            #
# PARAMETROS:                                                                                       #
#    PRM_ORA_LOGON              :	Cadena de Conexion                                              #
#    PRM_ORA_BD_ADMIN			:	Esquema BD                                                      #
#    PRM_NOMBRE_PROCESO			:	Nombre del proceso                                              #
#    PRM_NOMBRE_MODULO			:	Nombre del modulo                                               #
#    PRM_PROCESO_TMP			:	Nombre del tmp                                                  #
#    PRM_SECUENCIA				:	Secuencia de ejecucion                                          #
#    PRM_PATH_PROY				:	Ruta de proyecto                                                #
#    PRM_ID_RANDOM				:	Id Random                                                       #
#    PRM_FECHA_RUTINA			:	Fecha de rutina                                                 #
#    PRM_ENABLE_ASYNC_FLAGS		:	Activar uso de banderas                                         #
# ------------------------------------------------------------------------------------------------- #
# VERSION    AUTOR               FECHA           PROVEEDOR      PO/LT          DESCRIPCION          #
# ------------------------------------------------------------------------------------------------- #
# 1          Andres Hernandez    2022-12-05      Microsoft      Richard Tadeo  Creacion del script  #
# ------------------------------------------------------------------------------------------------- #

# Seteo de Parametros
PRM_ORA_LOGON=$1
PRM_ORA_BD_ADMIN=$2
PRM_NOMBRE_PROCESO=$3
PRM_NOMBRE_MODULO=$4
PRM_PROCESO_TMP=$5
PRM_SECUENCIA=$6
# Path inicial del proceso.
PRM_PATH_PROY=$7
PRM_ID_RANDOM=$8
PRM_FECHA_RUTINA=$9
PRM_ENABLE_ASYNC_FLAGS=${10}

# Obtención de Variables de Ambiente necesarias para el proceso a ejecutar.
PRM_PATH_SHE=${PRM_PATH_PROY}/ksh/
PRM_PATH_TMP=${PRM_PATH_PROY}/tmp/

# Parametro de nombre de archivo temporal con la cantidad de procesos a ejecutar y los nombres de pipelines ADF
PRM_TMP_RUTA_ARCHIVO_EXEC_1_ORA=${PRM_PATH_TMP}${PRM_PROCESO_TMP}.ora_adf_prm

# Valida si el archivo existe, lo elimina.
if [ -f ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.ora_adf_prm ]; then
	rm ${PRM_TMP_RUTA_ARCHIVO_EXEC_1_ORA}
fi

# Valida si el archivo JSON existe, lo elimina.
if [ -f ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adf_json ]; then
	rm ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adf_json
fi

if [ ${PRM_ENABLE_ASYNC_FLAGS} -eq 1 ]; then
	#Conexion a ORACLE - Identificacion de Proceso
	sqlplus ${PRM_ORA_LOGON} <<EOF >${PRM_TMP_RUTA_ARCHIVO_EXEC_1_ORA}

SET linesize 30000;

SELECT 
        JSON_OBJECTAGG (
                        KEY PRM.CODPARAMETRO VALUE PRM.DESVALORPARAMETRO
        ) AS JSON_PRM_${PRM_ID_RANDOM}
FROM   ${PRM_ORA_BD_ADMIN}.CT_PARAMETROPROCESOCARGA_LHCL PRM
WHERE TRIM(PRM.DESPROCESOCARGA)       = '${PRM_NOMBRE_PROCESO}'
  AND TRIM(PRM.DESMODULOPROCESOCARGA) = '${PRM_NOMBRE_MODULO}'
  AND PRM.NUMSECUENCIAEJECUCION       =  ${PRM_SECUENCIA}
  AND PRM.CODPARAMETRO NOT IN ('PRM_ENABLE_CONVIVENCIA','PRM_ENABLE_ASYNC_FLAGS')
ORDER BY PRM.CODPARAMETRO;

SELECT 
       Listagg(ppc.desprocesocargapredecesor
               ||'_'
               ||ppc.desmoduloprocesocargapredecesor, ';') AS PREDECESORES_${PRM_ID_RANDOM}
FROM   ${PRM_ORA_BD_ADMIN}.ct_procesocarga_lhcl pc
       inner join ${PRM_ORA_BD_ADMIN}.ct_relprocesocargapredecesor_lhcl PPC
               ON ( pc.desprocesocarga = ppc.desprocesocarga
                    AND pc.desmoduloprocesocarga = ppc. desmoduloprocesocarga
                    AND pc.numsecuenciaejecucion = ppc.numsecuenciaejecucion )
WHERE  pc.desprocesocarga = '${PRM_NOMBRE_PROCESO}'
       AND pc.desmoduloprocesocarga = '${PRM_NOMBRE_MODULO}'
GROUP  BY pc.desprocesocarga,
          pc.desmoduloprocesocarga; 

DISCONNECT;

EOF
else
	#Conexion a ORACLE - Identificacion de Proceso
	sqlplus ${PRM_ORA_LOGON} <<EOF >${PRM_TMP_RUTA_ARCHIVO_EXEC_1_ORA}

SET linesize 30000;

SELECT 
        JSON_OBJECTAGG (
                        KEY PRM.CODPARAMETRO VALUE PRM.DESVALORPARAMETRO
        ) AS JSON_PRM_${PRM_ID_RANDOM}
FROM   ${PRM_ORA_BD_ADMIN}.CT_PARAMETROPROCESOCARGA_LHCL PRM
WHERE TRIM(PRM.DESPROCESOCARGA)       = '${PRM_NOMBRE_PROCESO}'
  AND TRIM(PRM.DESMODULOPROCESOCARGA) = '${PRM_NOMBRE_MODULO}'
  AND PRM.NUMSECUENCIAEJECUCION       =  ${PRM_SECUENCIA}
  AND PRM.CODPARAMETRO NOT IN ('PRM_ENABLE_CONVIVENCIA','PRM_ENABLE_ASYNC_FLAGS')
ORDER BY PRM.CODPARAMETRO;

DISCONNECT;

EOF
fi

# Instanciamos la SHELL con las FUNCIONES GENERICAS
source ${PRM_PATH_SHE}PRC_EjecutaJobs_Functions_Cloud.ksh
# Funcion que valida ejecucion de QUERY
validateEXECUTE_ErrorInsertOracle ${PRM_TMP_RUTA_ARCHIVO_EXEC_1_ORA}
PRM_RETURN_VALUE=$?

# Cancela en caso haya ocurrido un error en la ejecucion ORACLE.
if [ ${PRM_RETURN_VALUE} -gt 0 ]; then
	# ERROR
	exit 1
fi

# Genera el archivo JSON de parametros.
grep -A 2 "JSON_PRM_${PRM_ID_RANDOM}" ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.ora_adf_prm | tail -1 >${PRM_PATH_TMP}${PRM_PROCESO_TMP}.tmp_adf_json

sed -e 's/#PRM_FECHA_RUTINA#/'${PRM_FECHA_RUTINA}'/g' ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.tmp_adf_json > ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adf_json

if [ ${PRM_ENABLE_ASYNC_FLAGS} -eq 1 ]; then
	# Genera el archivo flags.
	grep -A 2 "PREDECESORES_${PRM_ID_RANDOM}" ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.ora_adf_prm | tail -1 >${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adf_predecessor_flags
fi

# Eliminando Temporales
rm ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.ora_adf_prm
rm ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.tmp_adf_json

Flag_Json=0

# Validamos si se creo el archivo de PARAMETROS
if [ -f ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adf_json ]; then
	# Imprimimos los parametros:
	echo "	-> Parametros:"
	cat ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adf_json
	echo "	-> Ruta JSON: "${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adf_json
	echo "	-> OK - Se creo el archivo de PARAMETROS en formato JSON."
	Flag_Json=0
else
	echo "	-> ERROR - No se pudo crear el archivo de PARAMETROS."
	Flag_Json=1
fi

Flag_predecessor=0

if [ ${PRM_ENABLE_ASYNC_FLAGS} -eq 1 ]; then
	# Validamos si se creo el archivo de banderas predecesoras
	if [ -f ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adf_predecessor_flags ]; then
		# Imprimimos flags:
		echo "	-> Flags:"
		cat ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adf_predecessor_flags
		echo "	-> Ruta FLAGS: "${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adf_predecessor_flags
		echo "	-> OK - Se creo el archivo de FLAGS."
		Flag_predecessor=0
	else
		echo "	-> ERROR - No se pudo crear el archivo de FLAGS."
		Flag_predecessor=1
	fi
fi

if [ ${Flag_Json} -ne 0 -o ${Flag_predecessor} -ne 0 ]; then
	# ERROR
	exit 1
else
	# OK
	exit 0
fi
