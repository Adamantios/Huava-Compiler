package org.hua;

import static java.lang.System.out;
import java_cup.runtime.Symbol;

%%

%class Lexer
%unicode
%public
%final
%integer
%line
%column
%cup

%eofval{
    return createSymbol(sym.EOF);
%eofval}

%{
    private StringBuffer sb = new StringBuffer();

    private Symbol createSymbol(int type) {
        return new Symbol(type, yyline+1, yycolumn+1);
    }

    private Symbol createSymbol(int type, Object value) {
        return new Symbol(type, yyline+1, yycolumn+1, value);
    }
%}

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \type\f]
Comment        = "/*" [^*] ~"*/" | "/*" "*"+ "/"

Identifier     = [:jletter:] [:jletterdigit:]*
IntegerLiteral = 0 | [1-9][0-9]*

Exponent       = [eE][\+\-]?[0-9]+
Float1         = [0-9]+ \. [0-9]+ {Exponent}?
Float2         = \. [0-9]+ {Exponent}?
Float3         = [0-9]+ \. {Exponent}?
Float4         = [0-9]+ {Exponent}
FloatLiteral   = {Float1} | {Float2} | {Float3} | {Float4}

%state STRING

%%

<YYINITIAL> {
    /* reserved keywords */
    "while"                           { return createSymbol(sym.WHILE); }
    "static"                          { return createSymbol(sym.STATIC); }
    "int"                             { return createSymbol(sym.INT); }
    "float"                           { return createSymbol(sym.FLOAT); }
    "string"                          { return createSymbol(sym.STRING); }
    "if"                              { return createSymbol(sym.IF); }
    "else"                            { return createSymbol(sym.ELSE); }
    "write"                           { return createSymbol(sym.WRITE); }
    "void"                            { return createSymbol(sym.VOID); }
    "return"                          { return createSymbol(sym.RETURN); }
    "break"                           { return createSymbol(sym.BREAK); }
    "continue"                        { return createSymbol(sym.CONTINUE); }
    "class"                           { return createSymbol(sym.CLASS); }
    "new"                             { return createSymbol(sym.NEW); }
    "null"                            { return createSymbol(sym.NULL); }
    "this"                            { return createSymbol(sym.THIS); }

    /* identifiers */ 
    {Identifier}                   { return createSymbol(sym.IDENTIFIER, yytext()); }

    {IntegerLiteral}               { return createSymbol(sym.INTEGER_LITERAL, Integer.valueOf(yytext())); }
    {FloatLiteral}                 { return createSymbol(sym.FLOAT_LITERAL, Float.valueOf(yytext())); }

    \"                             { sb.setLength(0); yybegin(STRING); }

    /* operators */
    "="                            { return createSymbol(sym.EQ); }
    "+"                            { return createSymbol(sym.PLUS); }
    "-"                            { return createSymbol(sym.MINUS); }
    "*"                            { return createSymbol(sym.TIMES); }
    "/"                            { return createSymbol(sym.DIVISION); }
    "("                            { return createSymbol(sym.LPAREN); }
    ")"                            { return createSymbol(sym.RPAREN); }
    ";"                            { return createSymbol(sym.SEMICOLON); }
    ">"                            { return createSymbol(sym.GREATER); }
    "<"                            { return createSymbol(sym.LESS); }
    ">="                           { return createSymbol(sym.GREATER_OR_EQUAL); }
    "<="                           { return createSymbol(sym.LESS_OR_EQUAL); }
    "=="                           { return createSymbol(sym.EQUALS); }
    "!="                           { return createSymbol(sym.NOT_EQUALS); }
    "{"                            { return createSymbol(sym.LBRACKET); }
    "}"                            { return createSymbol(sym.RBRACKET); }
    "%"                            { return createSymbol(sym.MOD); }
    "&&"                           { return createSymbol(sym.AND); }
    "||"                           { return createSymbol(sym.OR); }
    "!"                            { return createSymbol(sym.NOT); }
    "."                            { return createSymbol(sym.DOT); }
    ","                            { return createSymbol(sym.COMMA); }

    /* comments */
    {Comment}                      { /* ignore */ }

    /* whitespace */
    {WhiteSpace}                   { /* ignore */ }
}

<STRING> {
    \"                             { yybegin(YYINITIAL);
                                     return createSymbol(sym.STRING_LITERAL, sb.toString());
                                   }

    [^\n\r\"\\]+                   { sb.append(yytext()); }
    \\t                            { sb.append('\t'); }
    \\n                            { sb.append('\n'); }

    \\r                            { sb.append('\r'); }
    \\\"                           { sb.append('\"'); }
    \\                             { sb.append('\\'); }
}

/* error fallback */
[^]                                { throw new RuntimeException((yyline+1) + ":" + (yycolumn+1) + ": illegal character <"+ yytext()+">"); }
