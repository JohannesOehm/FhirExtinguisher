lexer grammar QueryLexer;

PATH_STRING : ~[/?]+ ;
PATH_SEPARATOR: '/';
QUERY_PATH_SEPARATOR: '?' -> pushMode(QUERY_KEY);

WS : [\r\n] + ;

mode QUERY_KEY;
QUERY_STRING: ~[=:&]+;
MODIFIER_SEPARATOR: ':';
SEARCHPARAMETER_SEPARATOR: '&';
KEYVAL_SEPARATOR: '=' -> pushMode(QUERY_VALUE);



mode QUERY_VALUE;
VALUE_SEPARATOR: ',';
COMPOSITE_SEPARATOR: '$';
BAR_SEPARATOR: '|';
SEARCHPARAMETER_SEPARATOR2: '&' -> popMode, type(SEARCHPARAMETER_SEPARATOR);
QUERY_VALUE_STRING: (~[,&$|]|'\\,'|'\\$'|'\\|')+;
