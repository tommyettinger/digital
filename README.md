# digital

Utilities for handling math and showing numbers in Java.

## What is it?

BitConversion allows converting float and double values to
int and long versions of their underlying bits (and it does
this in a way that works on GWT efficiently). It also has
some methods that convert between float and int with reversed
byte order (using a fast intrinsic on desktop JDKs and a
special trick on GWT), and others that get only the low or
high half of a double's bits as an int. It is modeled after
the NumberUtils class from libGDX, but offers extra methods.

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

TrigTools and MathTools provide various mathematical functions.
On one hand, TrigTools tries to be as complete as possible,
offering sin, cos, tan, asin, acos, atan, and atan2 in radians,
degrees, and turns. On the other hand, MathTools offers a wild
grab bag of math functions and constants, from simple lerp,
floor, ceil, and clamp methods to an optimized cube root
function and a parameterized spline.

ArrayTools provides common code for dealing with 2D arrays, and
also sometimes 1D arrays. It allows copying, inserting, and
filling 2D arrays, and creating ranges of 1D arrays.

Hasher is... large. It provides fast, high-quality hashing
functions for primitive arrays (and arrays of objects, if they
implement hashCode()), and has 64-bit and 32-bit variants. The
specific hashing algorithm it uses is a somewhat-hardened
version of [wyhash](https://github.com/wangyi-fudan/wyhash) that
doesn't use 128-bit math. It
also has a few unary hashes that can be used as quick and dirty
random number generators when applied to numbers in a sequence.

## How do I get it?

With Gradle, add this to your dependencies (in your core module,
for libGDX projects):

```groovy
api "com.github.tommyettinger:digital:0.0.2"
```

If you target GWT using libGDX, you will also need this in your
html module:

```groovy
api "com.github.tommyettinger:digital:0.0.2:sources"
```

and this in your GdxDefinition.gwt.xml file:

```xml
<inherits name="digital" />
```

You can also use JitPack to get a recent commit; in that case,
follow [its instructions here](https://jitpack.io/#tommyettinger/digital/).

## License

[Apache 2.0](LICENSE). This includes some modified code from
[libGDX](https://github.com/libgdx/libgdx), 
[SquidLib](https://github.com/yellowstonegames/SquidLib),
[SquidSquad](https://github.com/yellowstonegames/SquidSquad),
[Uncommon Maths](https://maths.uncommons.org/), and
[wyhash](https://github.com/wangyi-fudan/wyhash).