# digital

![digital](docs/logo.png)

Utilities for handling math and showing numbers in Java.

## What is it?

There are only a few classes here, and any given application
only is likely to use even fewer of them. Still, they're
useful as a package, since they often depend on each other.

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

TrigTools tries to be as complete as possible at covering
trigonometric functions, offering sin, cos, tan, asin, acos,
atan, and atan2 in radians, degrees, and turns. It also allows
access to the lookup table used by sin, cos, and tan. Much of
TrigTools can be seen as similar to what libGDX's MathUtils
class offers, but allowing access to the lookup table permits
a few novel features (see its docs).

MathTools offers a wild grab bag of math functions and
constants, from simple lerp, floor, ceil, and clamp methods to
an optimized cube root function and a parameterized spline. It
is also based on MathUtils from libGDX.

ArrayTools provides common code for dealing with 2D arrays, and
also sometimes 1D arrays. It allows copying, inserting, and
filling 2D arrays, and creating ranges of 1D arrays.

Hasher is... large. It provides fast, high-quality hashing
functions for primitive arrays (and arrays of objects, if they
implement hashCode()), and has 64-bit and 32-bit variants. The
specific hashing algorithm it uses is a somewhat-hardened
version of [wyhash](https://github.com/wangyi-fudan/wyhash) that
doesn't use 128-bit math. While it can hash all types of 1D
primitive array and most types of 2D primitive array, it can't
do much with 3D or higher sizes at the moment. However, the only
change that would be needed to add a `hash()` method for, say, a
`float[][][]` is to copy the `hash(float[][])` method and change
the parameter to be a `float[][][]`, so if you have the source,
you can add this yourself with a quick copy and paste.
Hasher also has a few unary hashes that can be used as quick and
dirty random number generators when applied to numbers in a
sequence. The unary hashes can output longs, bounded ints,
floats, and doubles, so they may be useful in a lot of cases.

## How do I get it?

With Gradle, add this to your dependencies (in your core module,
for libGDX projects):

```groovy
api "com.github.tommyettinger:digital:0.0.3"
```

If you target GWT using libGDX, you will also need this in your
html module:

```groovy
api "com.github.tommyettinger:digital:0.0.3:sources"
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