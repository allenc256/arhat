grammar SQL;

// ============
// Parser rules
// ============

topLevelStatement
  : statement ';'
  ;

statement
  : selectStatement
  | insertStatement
  | dropTableStatement
  | createTableStatement
  ;
  
dropTableStatement
  : DROP TABLE ID
  ;
  
createTableStatement
  : CREATE TABLE ID '(' columnDefinitionList ')'
  ;
  
columnDefinitionList
  : (columnDefinition (',' columnDefinition)*)?
  ;
  
columnDefinition
  : ID type=(INTEGER|STRING|BOOLEAN)
  ;
  
insertStatement
  : INSERT INTO ID '(' insertStatementColumns ')' VALUES '(' insertStatementValues ')'
  ;
  
insertStatementColumns
  : ID (',' ID)*
  ;
  
insertStatementValues
  : literal (',' literal)*
  ;

literal
  : NULL_LITERAL                        # literalNull
  | INTEGER_LITERAL                     # literalInteger
  | STRING_LITERAL                      # literalString
  | TRUE_LITERAL                        # literalTrue
  | FALSE_LITERAL                       # literalFalse
  ;
  
selectStatement
  : SELECT selectStatementColumns selectStatementFromClause selectStatementWhereClause?
  ;
  
selectStatementColumns
  : selectStatementColumn (',' selectStatementColumn)*
  ;
  
selectStatementColumn
  : STAR_SYMBOL                         # selectStatementColumnStar
  | expression (AS ID)?                 # selectStatementColumnExpression
  ;

selectStatementFromClause
  : FROM selectStatementFromTableOrSubquery (ID)?
  ;
  
selectStatementFromTableOrSubquery
  : ID                                  # selectStatementFromTable
  | '(' selectStatement ')'             # selectStatementFromSubquery
  ;

selectStatementWhereClause
  : WHERE expression
  ;

// N.B., order below is important for associativity rules.
expression
  : (ID '.')? ID                        # expressionId
  | literal                             # expressionLiteral
  | '(' expression ')'                  # expressionParens
  | expression CONCAT_SYMBOL expression # expressionConcat
  | expression op=('*'|'/') expression  # expressionMultDiv
  | expression op=('+'|'-') expression  # expressionPlusMinus
  | expression op=('<'|'>'|'<='|'>='|'=') expression # expressionCompare
  | NOT expression                      # expressionNot
  | expression op=(AND|OR) expression   # expressionAndOr
  ;

// ===========
// Lexer rules
// ===========

NEWLINE: '\r'? '\n' -> skip;
WS: ( ' ' | '\t' | '\n' | '\r' )+ -> skip;

AS: A S;
AND: A N D;
BOOLEAN: B O O L E A N;
CREATE: C R E A T E;
DROP: D R O P;
FROM: F R O M;
INSERT: I N S E R T;
INTEGER: I N T E G E R;
INTO: I N T O;
TABLE: T A B L E;
SELECT: S E L E C T;
STRING: S T R I N G;
NOT: N O T;
OR: O R;
WHERE: W H E R E;
VALUES: V A L U E S;

NULL_LITERAL: N U L L;
TRUE_LITERAL: T R U E;
FALSE_LITERAL: F A L S E;
INTEGER_LITERAL: '0' .. '9'+;
STRING_LITERAL: '\'' (~('\'' | '\r' | '\n') | '\'' '\'')* '\'';

STAR_SYMBOL: '*';
DIV_SYMBOL: '/';
PLUS_SYMBOL: '+';
MINUS_SYMBOL: '-';
EQ_SYMBOL: '=';
LT_SYMBOL: '<';
LTE_SYMBOL: '<=';
GT_SYMBOL: '>';
GTE_SYMBOL: '>=';
CONCAT_SYMBOL: '||';

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
