parser grammar ColumnsTokens;

options {
 tokenVocab=ColumnsLexer;
}

columns: column (COLUMN_SEPARATOR column)*;

column: columnName EXPRESSION_SEPARATOR fhirpathExpression (TYPE_SEPARATOR columnType)?;

columnType: typeName (PAREN_OPEN typeParam? PAREN_CLOSE)?  ;

columnName: COLUMN_STRING;
typeName: TYPE_STRING;
typeParam: (PAREN_STRING | PAREN_OPEN2 | PAREN_CLOSE)*;
fhirpathExpression: FHIRPATH_STRING;


