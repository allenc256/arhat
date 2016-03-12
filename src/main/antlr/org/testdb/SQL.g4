grammar SQL;

// ============
// Parser rules
// ============

statement
  : selectStatement
  | createTableStatement
  ;
  
createTableStatement
  : CREATE TABLE ID '(' columnDefinitionList ')'
  ;
  
columnDefinitionList
  : columnDefinition (',' columnDefinition)*
  ;
  
columnDefinition
  : ID columnDefinitionType
  ;
  
columnDefinitionType
  : INTEGER
  | STRING
  ;
  
selectStatement
  : SELECT columnList fromClause
  ;

columnList
  : column (',' column)*
  ;

column
  : ID        # identifier
  | '*'       # star
  ;

fromClause
  : FROM table
  ;

table
  : ID
  ;

// ===========
// Lexer rules
// ===========

NEWLINE: '\r'? '\n' -> skip;
WS: ( ' ' | '\t' | '\n' | '\r' )+ -> skip;

CREATE: C R E A T E;
TABLE: T A B L E;
INTEGER: I N T E G E R;
STRING: S T R I N G;
SELECT: S E L E C T;
FROM: F R O M;

ID: ('a' .. 'z' | 'A' .. 'Z' | '_') ('a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9')*;

fragment A: [aA];
fragment B: [bB];
fragment C: [cC];
fragment D: [dD];
fragment E: [eE];
fragment F: [fF];
fragment G: [gG];
fragment H: [hH];
fragment I: [iI];
fragment J: [jJ];
fragment K: [kK];
fragment L: [lL];
fragment M: [mM];
fragment N: [nN];
fragment O: [oO];
fragment P: [pP];
fragment Q: [qQ];
fragment R: [rR];
fragment S: [sS];
fragment T: [tT];
fragment U: [uU];
fragment V: [vV];
fragment W: [wW];
fragment X: [xX];
fragment Y: [yY];
fragment Z: [zZ];
