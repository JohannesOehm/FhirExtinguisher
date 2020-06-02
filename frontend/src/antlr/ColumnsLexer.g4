lexer grammar ColumnsLexer;


TYPE_SEPARATOR: '@' -> pushMode(TYPE);
EXPRESSION_SEPARATOR: ':' -> pushMode(EXPRESSION);
COLUMN_STRING: (~[:@]|'\\@'|'\\:')+;


mode TYPE;
PAREN_OPEN: '(' -> pushMode(PAREN);
EXPRESSION_SEPARATOR2: ':' -> popMode, type(EXPRESSION_SEPARATOR), pushMode(EXPRESSION);
TYPE_STRING : ~[:(]+ ;


mode EXPRESSION;
FHIRPATH_STRING: (~[,]|'\\,')+ ;
COLUMN_SEPARATOR: ',' -> popMode;


mode PAREN;
PAREN_STRING: (~[)]|'\\)')+;
PAREN_CLOSE: ')' -> popMode;

WS : [\r\n] + ;