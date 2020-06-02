parser grammar ColumnsTokens;

options {
 tokenVocab=ColumnsLexer;
}

columns: column (COLUMN_SEPARATOR column)*;

column: columnName (TYPE_SEPARATOR columnType)? EXPRESSION_SEPARATOR fhirpathExpression?;

columnType: typeName (PAREN_OPEN typeParam? PAREN_CLOSE)?  ;

columnName: COLUMN_STRING;
typeName: TYPE_STRING;
typeParam: PAREN_STRING;
fhirpathExpression: FHIRPATH_STRING;


