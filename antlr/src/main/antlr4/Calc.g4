grammar Calc;

prog : stat +;

stat : expr NEWLINE                     #printExpr
     | ID '=' expr NEWLINE              #assign
     | NEWLINE                          #blank
     | 'clear' NEWLINE                  #clear
     ;
expr : expr op=('*'|'/') expr           #MulDiv
     | expr op=('-'|'+') expr           #AddSub
     | INT                              #int
     | ID                               #id
     | '('expr')'                       #parens
     ;

ID : [a-z]+;

INT : [0-9]+;

NEWLINE : '\r'?'\n';

WS : [ \t] -> skip;

MUL : '*';
DIV : '/';
ADD : '+';
SUB : '-';


