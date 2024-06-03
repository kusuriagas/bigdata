#!/usr/bin/bash
# --------------------------------------------------------------------------------------------------------------- #
# =======================================   BCP   =============================================================== #
# --------------------------------------------------------------------------------------------------------------- #
# NOMBRE PROCESO : PRC_EjecutaJobs_ADB_PRM.ksh                                                                    #
# DESCRIPCION    : Proceso del modulo Azure Databricks que obtiene los parametros y genera un                     #
#                  JSON.                                                                                          #
# PARAMETROS     :                                                                                                #
#    PRM_ORA_LOGON                  :   Cadena de Conexion                                                        #
#    PRM_ORA_BD_ADMIN               :   Esquema BD  												              #
#    PRM_NOMBRE_PROCESO             :  	Nombre del Proceso                                                        #
#    PRM_NOMBRE_MODULO              :   Nombre del Modulo                                                         #
#    PRM_PROCESO_TMP                :   Nombre tmp                                                                #
#    PRM_SECUENCIA                  :   Paramaetro secuencia ejecucion								              #
#    PRM_PATH_PROY                  :   Ruta de proyecto                                                          #
#    PRM_ID_RANDOM                  :   Id Random                                                                 #
#    PRM_FECHA_RUTINA               :   Fecha de rutina  		                                                  #
#    PRM_ENABLE_ASYNC_FLAGS         :   Activar uso de banderas                                                   #
# --------------------------------------------------------------------------------------------------------------- #
# VERSION    AUTOR               FECHA           PROVEEDOR       PO/LT          DESCRIPCION                       #
# --------------------------------------------------------------------------------------------------------------- #
# 1          Andres Hernandez    2022-12-05      Microsoft      Richard Tadeo   Creacion del script               #
# 2          Jean Llantoy        2024-03-02      Indra          Lily Lujan      creacion de parametro json array  #
#                                                                               para objeto jar/wheel             #
# --------------------------------------------------------------------------------------------------------------- #

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

# Parametro de nombre de archivo temporal con la cantidad de procesos a ejecutar y los nombres de jobs ADB
PRM_TMP_RUTA_ARCHIVO_EXEC_1_ORA=${PRM_PATH_TMP}${PRM_PROCESO_TMP}.ora_adb_prm

# Valida si el archivo existe, lo elimina.
if [ -f ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.ora_adb_prm ]; then
	rm ${PRM_TMP_RUTA_ARCHIVO_EXEC_1_ORA}
fi

# Valida si el archivo JSON existe, lo elimina.
if [ -f ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adb_json ]; then
	rm ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adb_json
fi

if [ ${PRM_ENABLE_ASYNC_FLAGS} -eq 1 ]; then
	#Conexion a ORACLE - Identificacion de Proceso
	sqlplus ${PRM_ORA_LOGON} <<EOF >${PRM_TMP_RUTA_ARCHIVO_EXEC_1_ORA}

SET linesize 30000;

SELECT
TRIM(PRO.NBRPROCESOCARGA) 
AS JSON_PRM_NBRPROC_${PRM_ID_RANDOM}
FROM ${PRM_ORA_BD_ADMIN}.CT_PROCESOCARGA_LHCL PRO
WHERE TRIM(PRO.DESPROCESOCARGA)       = '${PRM_NOMBRE_PROCESO}'
  AND TRIM(PRO.DESMODULOPROCESOCARGA) = '${PRM_NOMBRE_MODULO}'
  AND PRO.NUMSECUENCIAEJECUCION       =  ${PRM_SECUENCIA}
;

SELECT
  CASE
    WHEN INSTR(TRIM(PRO.NBRPROYECTO), '.') > 0 THEN
      JSON_ARRAY(
        TRIM(PRO.DESPROCESOCARGA),
        TRIM(PRO.DESMODULOPROCESOCARGA),
        TO_CHAR(PRO.NUMSECUENCIAEJECUCION),
        TRIM(PRO.NBRPROYECTO),
        LISTAGG(PRM.CODPARAMETRO || '=' || PRM.DESVALORPARAMETRO, '%')
      )
    ELSE
      JSON_OBJECTAGG (
        KEY PRM.CODPARAMETRO VALUE PRM.DESVALORPARAMETRO
      )
    END AS JSON_PRM_${PRM_ID_RANDOM}
FROM ${PRM_ORA_BD_ADMIN}.CT_PROCESOCARGA_LHCL PRO
JOIN ${PRM_ORA_BD_ADMIN}.CT_PARAMETROPROCESOCARGA_LHCL PRM
ON 
	TRIM(PRO.DESPROCESOCARGA) = TRIM(PRM.DESPROCESOCARGA)
	AND TRIM(PRO.DESMODULOPROCESOCARGA) = TRIM(PRM.DESMODULOPROCESOCARGA)
	AND PRO.NUMSECUENCIAEJECUCION = PRM.NUMSECUENCIAEJECUCION
WHERE TRIM(PRM.DESPROCESOCARGA)       = '${PRM_NOMBRE_PROCESO}'
  AND TRIM(PRM.DESMODULOPROCESOCARGA) = '${PRM_NOMBRE_MODULO}'
  AND PRM.NUMSECUENCIAEJECUCION       =  ${PRM_SECUENCIA}
  AND PRM.CODPARAMETRO LIKE 'PRM_%'
  AND PRM.CODPARAMETRO NOT IN ('PRM_LHCL_FLAG_TIMEOUT','PRM_ENABLE_CONVIVENCIA','PRM_ENABLE_ASYNC_FLAGS')
GROUP BY PRO.DESPROCESOCARGA,PRO.DESMODULOPROCESOCARGA,PRO.NUMSECUENCIAEJECUCION,PRO.NBRPROYECTO
;

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
TRIM(PRO.NBRPROCESOCARGA) 
AS JSON_PRM_NBRPROC_${PRM_ID_RANDOM}
FROM ${PRM_ORA_BD_ADMIN}.CT_PROCESOCARGA_LHCL PRO
WHERE TRIM(PRO.DESPROCESOCARGA)       = '${PRM_NOMBRE_PROCESO}'
  AND TRIM(PRO.DESMODULOPROCESOCARGA) = '${PRM_NOMBRE_MODULO}'
  AND PRO.NUMSECUENCIAEJECUCION       =  ${PRM_SECUENCIA}
;

SELECT
  CASE
    WHEN INSTR(TRIM(PRO.NBRPROYECTO), '.') > 0 THEN
      JSON_ARRAY(
        TRIM(PRO.DESPROCESOCARGA),
        TRIM(PRO.DESMODULOPROCESOCARGA),
        TO_CHAR(PRO.NUMSECUENCIAEJECUCION),
        TRIM(PRO.NBRPROYECTO),
        LISTAGG(PRM.CODPARAMETRO || '=' || PRM.DESVALORPARAMETRO, '%')
      )
    ELSE
      JSON_OBJECTAGG (
        KEY PRM.CODPARAMETRO VALUE PRM.DESVALORPARAMETRO
      )
    END AS JSON_PRM_${PRM_ID_RANDOM}
FROM ${PRM_ORA_BD_ADMIN}.CT_PROCESOCARGA_LHCL PRO
JOIN ${PRM_ORA_BD_ADMIN}.CT_PARAMETROPROCESOCARGA_LHCL PRM
ON 
	TRIM(PRO.DESPROCESOCARGA) = TRIM(PRM.DESPROCESOCARGA)
	AND TRIM(PRO.DESMODULOPROCESOCARGA) = TRIM(PRM.DESMODULOPROCESOCARGA)
	AND PRO.NUMSECUENCIAEJECUCION = PRM.NUMSECUENCIAEJECUCION
WHERE TRIM(PRM.DESPROCESOCARGA)       = '${PRM_NOMBRE_PROCESO}'
  AND TRIM(PRM.DESMODULOPROCESOCARGA) = '${PRM_NOMBRE_MODULO}'
  AND PRM.NUMSECUENCIAEJECUCION       =  ${PRM_SECUENCIA}
  AND PRM.CODPARAMETRO LIKE 'PRM_%'
  AND PRM.CODPARAMETRO NOT IN ('PRM_LHCL_FLAG_TIMEOUT','PRM_ENABLE_CONVIVENCIA','PRM_ENABLE_ASYNC_FLAGS')
GROUP BY PRO.DESPROCESOCARGA,PRO.DESMODULOPROCESOCARGA,PRO.NUMSECUENCIAEJECUCION,PRO.NBRPROYECTO
;

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

# Genera el archivo JSON de parametros y el valo de nbrproceocarga.
grep -A 2 "JSON_PRM_${PRM_ID_RANDOM}" ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.ora_adb_prm | tail -1 >${PRM_PATH_TMP}${PRM_PROCESO_TMP}.tmp_adb_json

grep -A 2 "JSON_PRM_NBRPROC_${PRM_ID_RANDOM}" ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.ora_adb_prm | tail -1 >${PRM_PATH_TMP}${PRM_PROCESO_TMP}.tmp_adb_nbrproc

sed -e 's/#PRM_FECHA_RUTINA#/'${PRM_FECHA_RUTINA}'/g' ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.tmp_adb_json > ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adb_json

#sed -e 's/null/'\"\"'/g' ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adb_json_1 > ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adb_json

if [ ${PRM_ENABLE_ASYNC_FLAGS} -eq 1 ]; then
	# Genera el archivo flags.
	grep -A 2 "PREDECESORES_${PRM_ID_RANDOM}" ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.ora_adb_prm | tail -1 >${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adb_predecessor_flags
fi

# Eliminando Temporales
rm ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.ora_adb_prm
rm ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.tmp_adb_json
#rm ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adb_json_1

Flag_Json=0

# Validamos si se creo el archivo de PARAMETROS
if [ -f ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adb_json ]; then
	# Imprimimos los parametros:
	echo "	-> Parametros:"
	cat ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adb_json
	echo "	-> Ruta JSON: "${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adb_json
	echo "	-> OK - Se creo el archivo de PARAMETROS en formato JSON."
	Flag_Json=0
else
	echo "	-> ERROR - No se pudo crear el archivo de PARAMETROS."
	Flag_Json=1
fi

Flag_predecessor=0

if [ ${PRM_ENABLE_ASYNC_FLAGS} -eq 1 ]; then
	# Validamos si se creo el archivo de banderas predecesoras
	if [ -f ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adb_predecessor_flags ]; then
		# Imprimimos flags:
		echo "	-> Flags:"
		cat ${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adb_predecessor_flags
		echo "	-> Ruta FLAGS: "${PRM_PATH_TMP}${PRM_PROCESO_TMP}.adb_predecessor_flags
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
