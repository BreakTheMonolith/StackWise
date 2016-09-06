grammar Stackwise;

threads : dateTimeLine? (line)* ;

//threadIdLine : QuotedStringLiteral StringLiteral* LF ;

textContent : (SPACE | StringCharacter | LEFT_PAREN | RIGHT_PAREN | NUMERIC_LITERAL | HYPHEN | PERIOD | HASH | EQUAL | ALT_LOCK_BEGIN_DELIMITER | ALT_LOCK_END_DELIMITER)+ ;

dateTimeLine : date SPACE time LF ;
date : NUMERIC_LITERAL HYPHEN NUMERIC_LITERAL HYPHEN NUMERIC_LITERAL ;
time : NUMERIC_LITERAL COLON NUMERIC_LITERAL COLON NUMERIC_LITERAL ;

line : LF? (THREAD_STATE | STACK_LINE_START | LOCKED_OWNABLE_SYNC | JNI | HYPHEN | QuotedStringLiteral) textContent LF ;

NUMERIC_LITERAL : [0-9]+ ;

// Keywords
THREAD_STATE : 'java.lang.Thread.State' ;
STACK_LINE_START : 'at' ;
FULL_THREAD_DUMP : 'Full thread dump' ;
LOCKED : 'locked' ;
LOCKED_OWNABLE_SYNC : 'Locked ownable synchronizers' ;
JNI : 'JNI global references' ;

// Special Characters
LOCK_BEGIN_DELIMITER : '<' ;
LOCK_END_DELIMITER : '>' ;
ALT_LOCK_BEGIN_DELIMITER : '[' ;
ALT_LOCK_END_DELIMITER : ']' ;
COLON : ':' ;
EQUAL : '=' ;
HASH : '#' ;
SLASH : '/' ;
AT : '@' ;
LEFT_PAREN : '(' ;
RIGHT_PAREN : ')' ;
PERIOD : '.' ;
HYPHEN : '-' ;
QUOTE : '"' ;
LF  : '\n' ;
SPACE : ' ' ;

// §3.10.5 String Literals
QuotedStringLiteral
    :   '"' StringCharacter+ '"'
    ;
    
fragment
StringCharacter
    :   ~["\\] ;
    
// §3.8 Identifiers (must appear after all keywords in the grammar)

Identifier
    :   JavaLetter JavaLetterOrDigit*
    ;

fragment
JavaLetter
    :   [a-zA-Z$_] // these are the "java letters" below 0x7F
    |   // covers all characters above 0x7F which are not a surrogate
        ~[\u0000-\u007F\uD800-\uDBFF]
        {Character.isJavaIdentifierStart(_input.LA(-1))}?
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
        {Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;

fragment
JavaLetterOrDigit
    :   [a-zA-Z0-9$_] // these are the "java letters or digits" below 0x7F
    |   // covers all characters above 0x7F which are not a surrogate
        ~[\u0000-\u007F\uD800-\uDBFF]
        {Character.isJavaIdentifierPart(_input.LA(-1))}?
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
        {Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;

// Whitespace
JNI_LINE : JNI .*? '\n' ->skip ;
FULL_THREAD_DUMP_LINE : FULL_THREAD_DUMP .*? '\n' ->skip ;
//EMPTY_LINE : '\r'? '\n' -> skip ;
WS  : [\t\r]+ -> skip ;