grammar SQL;

// ============
// Parser rules
// ============

statement
  : selectStatement
  | insertStatement
  | dropTableStatement
  | createTableStatement
  ;
  
dropTableStatement
  : DROP TABLE ID ';'
  ;
  
createTableStatement
  : CREATE TABLE ID '(' columnDefinitionList ')' ';'
  ;
  
columnDefinitionList
  : (columnDefinition (',' columnDefinition)*)?
  ;
  
columnDefinition
  : ID type=(INTEGER|STRING)
  ;
  
insertStatement
  : INSERT INTO ID '(' insertStatementColumns ')' VALUES '(' insertStatementValue (',' insertStatementValue)* ')' ';'
  ;
  
insertStatementColumns
  : ID (',' ID)*
  ;
  
insertStatementValue
  : NULL
  | INTEGER_LITERAL
  | STRING_LITERAL
  ;
  
selectStatement
  : SELECT selectStatementColumns selectStatementFromClause ';'
  ;
  
selectStatementColumns
  : selectStatementColumn (',' selectStatementColumn)*
  ;
  
selectStatementColumn
  : column=(STAR|ID)
  ;

selectStatementFromClause
  : FROM ID
  ;

// ===========
// Lexer rules
// ===========

NEWLINE: '\r'? '\n' -> skip;
WS: ( ' ' | '\t' | '\n' | '\r' )+ -> skip;

CREATE: C R E A T E;
DROP: D R O P;
INSERT: I N S E R T;
VALUES: V A L U E S;
INTO: I N T O;
TABLE: T A B L E;
INTEGER: I N T E G E R;
STRING: S T R I N G;
SELECT: S E L E C T;
FROM: F R O M;

NULL: N U L L;
TRUE: T R U E;
FALSE: F A L S E;
INTEGER_LITERAL: '0' .. '9'+;
STRING_LITERAL: '\'' (~('\'' | '\r' | '\n') | '\'' '\'')* '\'';

STAR: '*';

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
