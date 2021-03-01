lexer grammar ColumnsLexer;


EXPRESSION_SEPARATOR: ':' -> pushMode(EXPRESSION);
COLUMN_STRING: (~[:]|'\\:')+;


mode TYPE;
PAREN_OPEN: '(' -> pushMode(PAREN);
EXPRESSION_SEPARATOR2: ':' -> popMode, type(EXPRESSION_SEPARATOR), pushMode(EXPRESSION);
COLUMN_SEPARATOR2: ',' -> type(COLUMN_SEPARATOR), popMode;
TYPE_STRING : ~[,(]+ ;


mode EXPRESSION;
FHIRPATH_STRING: (~[,@]|'\\,'|'\\@')+ ;
COLUMN_SEPARATOR: ',' -> popMode;
TYPE_SEPARATOR: '@' -> popMode, pushMode(TYPE);


mode PAREN;
PAREN_STRING: (~[()])+;
PAREN_OPEN2: '(' -> pushMode(PAREN);
PAREN_CLOSE: ')' -> popMode;

WS : [\r\n] + ;