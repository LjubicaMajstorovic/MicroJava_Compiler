package rs.ac.bg.etf.pp1;

import java_cup.runtime.Symbol;
import org.apache.log4j.*;

%%

%{
	private Logger log = Logger.getLogger(Yylex.class);

	private Symbol new_symbol(int type) {
		return new Symbol(type, yyline + 1, yycolumn);
	}
	
	private Symbol new_symbol(int type, Object value) {
		return new Symbol(type, yyline + 1, yycolumn, value);
	}
%}

%cup
%line
%column

%xstate COMMENT

%eofval{
	return new_symbol(sym.EOF);
%eofval}

%%

" " 	{ }
"\b" 	{ }
"\t" 	{ }
"\r\n"  { }
"\f" 	{ }

"+"   { return new_symbol(sym.PLUS, yytext()); }
"-"   { return new_symbol(sym.MINUS, yytext()); }
"*"   { return new_symbol(sym.ASTERISK, yytext()); }
"/"   { return new_symbol(sym.DIVIDE, yytext()); }
"%"   { return new_symbol(sym.MODULO, yytext()); }
"=="  { return new_symbol(sym.DBL_EQUAL, yytext()); }
"!="  { return new_symbol(sym.NOT_EQUAL, yytext()); }
">"   { return new_symbol(sym.GREATER, yytext()); }
">="  { return new_symbol(sym.GREATER_EQUAL, yytext()); }
"<"   { return new_symbol(sym.LESS, yytext()); }
"<="  { return new_symbol(sym.LESS_EQUAL, yytext()); }
"&&"  { return new_symbol(sym.AND, yytext()); }
"||"  { return new_symbol(sym.OR, yytext()); }
"="   { return new_symbol(sym.EQUAL, yytext()); }
"++"  { return new_symbol(sym.INC, yytext()); }
"--"  { return new_symbol(sym.DEC, yytext()); }
";"   { return new_symbol(sym.SEMI, yytext()); }
":"   { return new_symbol(sym.COLON, yytext()); }
","   { return new_symbol(sym.COMMA, yytext()); }
"("   { return new_symbol(sym.LPAREN, yytext()); }
")"   { return new_symbol(sym.RPAREN, yytext()); }
"["   { return new_symbol(sym.LBRACKET, yytext()); }
"]"   { return new_symbol(sym.RBRACKET, yytext()); }
"{"   { return new_symbol(sym.LBRACE, yytext()); }
"}"   { return new_symbol(sym.RBRACE, yytext()); }

"program"   { return new_symbol(sym.PROGRAM, yytext()); }
"break"     { return new_symbol(sym.BREAK, yytext()); }
"const"     { return new_symbol(sym.CONST, yytext()); }
"else"      { return new_symbol(sym.ELSE, yytext()); }
"if"        { return new_symbol(sym.IF, yytext()); }
"new"       { return new_symbol(sym.NEW, yytext()); }
"print"     { return new_symbol(sym.PRINT, yytext()); }
"read"      { return new_symbol(sym.READ, yytext()); }
"return"    { return new_symbol(sym.RETURN, yytext()); }
"void"      { return new_symbol(sym.VOID, yytext()); }
"continue"  { return new_symbol(sym.CONTINUE, yytext()); }
"for"       { return new_symbol(sym.FOR, yytext()); }

<YYINITIAL> "//"  { yybegin(COMMENT); }
<COMMENT> .       { yybegin(COMMENT); }
<COMMENT> "\r\n"  { yybegin(YYINITIAL); }

[0-9]+                 { return new_symbol(sym.NUM, Integer.valueOf(yytext())); }
"true"                 { return new_symbol(sym.BOOL, true); }
"false"                { return new_symbol(sym.BOOL, false); }
['].[']                { return new_symbol(sym.CHAR, yytext().charAt(1)); }
[a-zA-Z][a-zA-Z0-9_]*  { return new_symbol (sym.IDENT, yytext()); }

. { log.error("Leksicka greska: '" + yytext() + "' (linija " + (yyline + 1) + ", kolona " + (yycolumn + 1) + ")."); }