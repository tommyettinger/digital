# digital

Small utilities for handling numbers in Java.

This includes just two classes currently.

BitConversion allows converting float and double values to
int and long versions of their underlying bits (and it does
this in a way that works on GWT efficiently).

Base is much larger, and allows converting any Java primitive
number type to a specific base/radix/number-system. Here,
Bases are flexible enough to be able to be generated randomly
for obfuscation purposes (such as to mangle a high score in a
save file). There are several Base constants already defined.
Each one can write numbers as signed (variable-length) or
unsigned (fixed-length), as well as read back either type from
one method. Parsing is significantly more relaxed than in the
JDK, and invalid numbers tend to be returned as 0 rather than
requiring an Exception to be caught.