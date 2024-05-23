-- ************************************************************************************************************************************
-- || PROYECTO       : BCP - Evolución Data Lake
-- || NOMBRE         : DML_H_INTERACCIONBANCAMOVIL_ORACLE_PROD.sql
-- || TABLA DESTINO  : ADMIN.CT_PARAMETROPROCESOCARGA_LHCL
-- || TABLAS FUENTES : DML
-- || OBJETIVO       : POBLAR TABLA ADMIN.CT_PARAMETROPROCESOCARGA_LHCL
-- || TIPO           : SQL
-- || REPROCESABLE   : NA
-- || OBSERVACION    : NA
-- || SCHEDULER      : NA
-- || JOB            : NA
-- || ----------------------------------------------------------------------------------------------------------
-- || VERSION     DESARROLLADOR        PROVEEDOR        PO       				FECHA          DESCRIPCION
-- || ----------------------------------------------------------------------------------------------------------
-- || 1                                                                                       Creación de Script 
-- *************************************************************************************************************

---------------------------------------------------------------------------------------------------------------------------------------------------------------
--PARAMETROS DE CARGA A LA TABLA ADMIN.CT_PARAMETROPROCESOCARGA_LHCL
---------------------------------------------------------------------------------------------------------------------------------------------------------------

SET ECHO ON;
SET TIMING ON;

WHENEVER SQLERROR EXIT 1;

INSERT INTO ADMIN.CT_PARAMETROPROCESOCARGA_LHCL (DESPROCESOCARGA, DESMODULOPROCESOCARGA, NUMSECUENCIAEJECUCION, CODPARAMETRO, DESVALORPARAMETRO) VALUES ('H_INTERACCIONBANCAMOVIL_ORACLE','OUDV',1,'PRM_ENABLE_CONVIVENCIA','0');
INSERT INTO ADMIN.CT_PARAMETROPROCESOCARGA_LHCL (DESPROCESOCARGA, DESMODULOPROCESOCARGA, NUMSECUENCIAEJECUCION, CODPARAMETRO, DESVALORPARAMETRO) VALUES ('H_INTERACCIONBANCAMOVIL_ORACLE','OUDV',1,'PRM_ENABLE_ASYNC_FLAGS','1');


COMMIT;

DISCONNECT;
EXIT;