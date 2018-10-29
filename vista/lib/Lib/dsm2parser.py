from gov.ca.dsm2.input.parser import InputTable, Parser, Tables;
def parse(echofile):
    """
    Parses an echo file and returns a Tables object that can retrieve InputTables by name
    """
    parser = Parser();
    return parser.parseModel(echofile);
def get_table_named(name, tables):
    return tables.getTableNamed(name)
