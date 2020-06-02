parser grammar QueryTokens;

options {
 tokenVocab=QueryLexer;
}

url : path?  query? WS? EOF;

path : path_string? (PATH_SEPARATOR  path_string)* PATH_SEPARATOR? ;

query : QUERY_PATH_SEPARATOR search?;

search: searchparameter (SEARCHPARAMETER_SEPARATOR searchparameter)* ;

searchparameter: keystring (MODIFIER_SEPARATOR modifier)* ( KEYVAL_SEPARATOR values )?;

values: value? (VALUE_SEPARATOR value)* ;

value: QUERY_VALUE_STRING ((BAR_SEPARATOR QUERY_VALUE_STRING)* |  (COMPOSITE_SEPARATOR QUERY_VALUE_STRING)*);

keystring: QUERY_STRING;
path_string: PATH_STRING;
modifier: QUERY_STRING;


