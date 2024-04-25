import configparser
import logging
import sys
import os

from azure.identity import ClientSecretCredential
from azure.mgmt.datafactory import DataFactoryManagementClient

class ADF_Client(object):
    def __init__(self, secret_ini_file):
        self.secret_ini_file = secret_ini_file

        config = configparser.ConfigParser()
        with open(self.secret_ini_file) as stream:
            config.read_string(stream.read())

        self.client_id = os.getenv('AZCOPY_SPA_APP_ID')
        self.client_secret = os.getenv('AZCOPY_SPA_CLIENT_SECRET')
        self.tenant_id = os.getenv('AZCOPY_SPA_TENANT_ID')

        self.subscription_id = config.get('INI_PARAMETERS', 'SUBSCRIPTION_ID')
        self.rg_name = config.get('INI_PARAMETERS', 'RG_NAME')
        self.df_name = config.get('INI_PARAMETERS', 'DF_NAME')

        self.adf_client = self.create_adf_client()

    def create_adf_client(self):
        credentials = ClientSecretCredential(client_id=self.client_id, client_secret=self.client_secret, tenant_id=self.tenant_id)
        adf_client = DataFactoryManagementClient(credentials, self.subscription_id)

        return adf_client

def test_adf_connection(secret_ini_file):
    try:
        adf_client = ADF_Client(secret_ini_file)
        pipelines = list(adf_client.adf_client.pipelines.list_by_factory(adf_client.rg_name, adf_client.df_name))
        print("Conexión exitosa a Azure Data Factory")
    except Exception as e:
        print("Error al conectar a Azure Data Factory:", e)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Debe proporcionar la ruta al archivo de configuración .ini como argumento.")
        sys.exit(1)
    test_adf_connection(sys.argv[1])
