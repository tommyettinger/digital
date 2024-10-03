# digital

![digital](docs/logo.png)

Utilities for handling math and showing numbers in Java.

## What is it?

There are only a few classes here, and any given application
only is likely to use even fewer of them. Still, they're
useful as a package, since they often depend on each other.

BitConversion allows converting float and double values to
int and long versions of their underlying bits (and it does
this in a way that works on GWT efficiently). While code that
does this is available in the regular JDK in Float and Double,
the implementations for those provided by GWT are extremely
slow. BitConversion was initially meant just to provide a
faster route for those conversions on GWT, but it also has
some methods that convert float-int and double-long with
reversed byte order (using a fast intrinsic on desktop JDKs
and a special trick on GWT), and others that get only the low
or high half of a double's bits as an int. It is modeled after
the NumberUtils class from libGDX, but offers extra methods.
It also has, purely for GWT purposes, an `imul()` method that
acts just like integer multiplication on most platforms, but
compiles down to a call to the JavaScript `Math.imul()`
function on GWT, which automatically handles overflow in the
same way a desktop JVM would. Similarly, `countLeadingZeros()`
compiles to the JavaScript `Math.clz32()` function on GWT (or
for `long` arguments, some small code that calls that), while
using `Integer.numberOfLeadingZeros()` (or its `Long`
counterpart) on non-GWT platforms. Counting leading zeros is
an operation that shows up in a surprising assortment of
places and is supposed to be fast, so having an alternative to
[this monstrosity](https://github.com/gwtproject/gwt/blob/main/user/super/com/google/gwt/emul/java/lang/Integer.java#L118)
is more than welcome. There's also `countTrailingZeros()` for
int and long arguments, which compiles into a single call to
`Integer.numberOfTrailingZeros()` (or using `Long`) on most
platforms, or a JS two-liner on GWT that uses `Math.clz32()`.
Also purely for GWT support, `lowestOneBit()` works around a
bug that was present in GWT 2.8.2 and may still be present,
where its built-in `Long.lowestOneBit()` method could return
very wrong results for larger inputs.

Base is much larger, and allows converting any Java primitive
number type to a specific base/radix/number-system. Here,
Bases are flexible enough to be able to be generated randomly
for obfuscation purposes (such as to mangle a high score in a
save file). There are several Base constants already defined.
Each one can write numbers as signed (variable-length) or
unsigned (fixed-length), as well as read back either type from
one method. Parsing is significantly more relaxed than in the
JDK, and invalid numbers tend to be returned as 0 rather than
requiring an Exception to be caught. Base has some options
when writing (or reading) a `float` or `double`. Like other
numeric types, you can use `signed()` or `unsigned()` to write
using the digits the Base usually uses, though here it writes
the bits that compose the float or double for higher accuracy.
Unlike other numeric types, there are base-10 `decimal()`,
`scientific()`, `general()`, and `friendly()` methods to write
floats/doubles with different rules for when to switch to
scientific notation, if at all. These can be read back with
`readFloat()` and `readDouble()`, while the signed/unsigned
output needs `readFloatExact()` or `readDoubleExact()`. If you
use a scrambled base (a random one, as mentioned before), then
you need to use `signed()`/`unsigned()`/`readFloatExact()`/
`readDoubleExact()` to use the right scrambled digits.
`decimal()` can take an exact length to limit large output to,
or zero-pad small output to, as well as a precision for how
many digits to show after the decimal point. Base
can also combine the String representations of an array of
primitives (or part of such an array, since 0.3.1) using
`join()` or `appendJoined()`, and split apart Strings into 
arrays of primitives with methods like `longSplit()`,
`intSplit()`, and `floatSplit()`. There are also now static
methods with `readable` in their names; these output text
that can be used in Java source code as numeric or character
literals, for code generation. The `Base.BASE10.readLong()`
method can read a long produced by `Base.readable(long)` just
fine, because readLong() ignores invalid characters after the
number (such as `L` in longs). Reading in the char literals
this can produce requires using `Base.readCharReadable()`.

TrigTools tries to be as complete as possible at covering
trigonometric functions, offering sin, cos, tan, asin, acos,
atan, and atan2 in radians, degrees, and turns. It also allows
access to the lookup tables used by sin and cos. Much of
TrigTools can be seen as similar to what libGDX's MathUtils
class offers, but allowing access to the lookup tables permits
a few novel features (see its docs). It supports float and
double arguments/returns for all functions. It also provides
"smooth" sin and cos approximations that aren't table-based,
and "smoother" sin/cos/tan versions that interpolate between
entries in the lookup table for very high precision. It should
be pointed out that very few libraries support trigonometric
functions that take angles in turns, but turns can be very
useful for a variety of cases. For example, if you want to
store a hue (which is essentially an angle) into a limited
format such as one channel of a color to be sent to the GPU,
storing the hue as an angle in turns keeps it in the 0.0
to 1.0 range, but using radians or degrees would not. There's
also a few extra methods, such as `atan2Deg360()`, which acts
like the degree version of atan2, but always returns an angle
between 0 (inclusive) and 360 (exclusive), since negative
angles are less intuitive and sometimes not supported.

MathTools offers a wild grab bag of math functions and
constants, from simple lerp, floor, ceil, and clamp methods to
an optimized cube root function and parameterized splines. It
is also based on MathUtils from libGDX. It supports float and
double arguments/returns for most functions; some make sense
for float only, like the optimized cube root. There's also a
lot of commonly-defined constants, such as the square root of
2 and the golden ratio, as floats and doubles. Some methods
here are useful in other mathematical code, like gamma and
greatestCommonDivisor; others are more esoteric, like
modularMultiplicativeInverse. There are some of the standard
signal processing functions, such as triangle waves, square
waves, and sawtooth waves. There are also a few functions
here, "sway" and relatives, that look like sine waves when
graphed, but are simpler to calculate internally. You can do
some bitwise operations with MathTools, such as interleaving
the bits of two numbers (also called a Morton code, or a
position on the Z-Order Curve). There are ways to bring a
float or double closer to 0.0 by the smallest possible amount.
There's the fract() methods, familiar to shader programmers,
that get the fractional part of a float or double. There's
Freya Holmer's recently-discovered method to make a float
approach another value at an even rate using interpolation.
In general, if you need a math function that isn't
trigonometry-related and doesn't do something with text, you
may want to look here first.

ArrayTools provides common code for dealing with 2D arrays, and
also sometimes 1D or 3D arrays. It allows copying, inserting,
and filling 2D arrays, and creating ranges of 1D arrays. It also
has a lot of methods for shuffling 1D arrays, 2D arrays, and
sections of 1D arrays, for all primitive types and for objects.
There are also some "filler supplies" in ArrayTools -- methods
for filling up char, int, or String arrays with contents that
are guaranteed to be distinct up to a certain limit. The same
data that can be used to fill up arrays with ArrayTools also
gets used by Hasher for seeding its predefined instances. The
filler supplies include the names of Greek letters, the chemical
elements of the periodic table, and the listing of names of
demons from the Ars Goetia (many of which are familiar names
from games that also used that list).

Hasher is... large. It provides fast, high-quality hashing
functions for primitive arrays (and arrays of objects, if they
implement hashCode()), and has 64-bit and 32-bit variants. The
specific hashing algorithm it uses is a somewhat-hardened
version of [wyhash](https://github.com/wangyi-fudan/wyhash) that
doesn't use 128-bit math.

While Hasher can hash all types of 1D
primitive array and most types of 2D primitive array, it can't
do much with 3D or higher sizes at the moment. However, the only
change that would be needed to add a `hash()` method for, say, a
`float[][][]` is to copy the `hash(float[][], int, int)` method and change
the first parameter to be a `float[][][]`, so if you have the source,
you can add this yourself with a quick copy and paste. Hasher
allows you to specify the seed when you call `hash()` or
`hash64()` (as static methods), but it also has a fairly-large
array of `predefined` hash functions with instance methods for
`hash()` and `hash64()`. Starting in 0.3.0, Hasher can either
process a whole input array or part of one, specified by a start
index and a length.

Hasher also has a few unary hashes that
can be used as quick and dirty random number generators when
applied to numbers in a sequence. The unary hashes can output
longs, bounded ints, floats, and doubles, so they may be useful in
a lot of cases. They are named like `randomize1()`, `randomize2()`,
`randomize3()`, and so on, with higher numbers being typically
slightly slower but also higher-quality (and more permissive of
sets of inputs with atypical patterns). Most usage is probably
served best by `randomize3()` and its bounded int, float, and
double variants, since it uses a proven algorithm (MX3) and isn't
much different on runtime performance.

While Hasher should usually
change only very rarely, if at all, I was concerned about a few
properties of the hashing code that might have meant some values
would be returned less frequently, though this wasn't detectable to
the [SMHasher test suite](https://github.com/rurban/smhasher). In
version 0.4.0, Hasher's output for any given seed will be different
from the same seed in previous versions. You can get the older
Hasher instances either from Git history or the test folder's
[OldHasher v020](src/test/java/com/github/tommyettinger/digital/v020/OldHasher.java)
or [OldHasher v037](src/test/java/com/github/tommyettinger/digital/v037/OldHasher.java) files,
if you need to reproduce the results of older seeds. Newer versions
of SMHasher do find statistical failures in at least some `hash()` and
`hash64()` methods in `Hasher`, but the recently-added `hashBulk()`
and `hashBulk64()` methods don't have detectable issues.

AlternateRandom is a quick micro-port of a random number generator
from the closely-related [juniper](https://github.com/tommyettinger/juniper)
library. It is used only in ArrayTools here, as the default when
no `Random` object is specified. The alternative would be to use
a `java.util.Random` object, but that can't produce as many
possible shuffles of mid-size arrays, and is slower, both of which
AlternateRandom solves to some extent. If you don't use juniper,
then AlternateRandom is a pretty good replacement for `Random`;
if you do use juniper, then its `AceRandom` or `PasarRandom`
generators are similar to or the same as AlternateRandom's
algorithm, and offer many more features.

ShapeTools provides some predefined mathematical constants for
the vertices and faces of 3D polyhedra (currently, just the 5
Platonic solids). It could be useful for code that needs 3D shapes
in code for something like continuous noise, but doesn't have
access to 3D models.

TextTools provides some features that are similar to ones in Base,
but different enough to belong in their own class. It operates on
CharSequences usually, but sometimes needs Strings, so I'll just
call this vague type "text." This class includes many different
ways to search text for something, code to count the occurrences
of text in a larger piece of text, code to join/split arrays of
text and larger texts (this also works for boolean arrays), code
for padding text, and code for replacing text with literals. This
class mostly exists to avoid duplicating similar code that occurs
often throughout my projects, and is related to code here.

Interpolations, along with its nested classes InterpolationFunction
and Interpolator, provide a way to store and look up functions to
smoothly interpolate between floats. This code is very similar to
the Interpolation class in libGDX, and all the instances in
Interpolation have a counterpart in Interpolations (there are some
more here, as well). Creating an Interpolator registers its name
in Interpolations' registry, where it can be looked up on its own
with `get(String)` or as a group with `getInterpolatorArray()`.
There are also ways to create one of the building blocks of an
Interpolator, an InterpolationFunction, with methods in
Interpolations. This makes creating new Interpolators with different
parameters as easy as assigning a name to the generated function.
[Here's a sample page that shows how each Interpolator looks graphed.](https://tommyettinger.github.io/digital/interpolators.html)
Using that page of graphs is a good idea when you're deciding which
of the many Interpolator varieties to use.

RoughMath has a variety of "rough" and "rougher" approximations to a
variety of functions. These tend to be faster but less precise.
The logistic functions `logisticRough()` and `logisticRougher()` are
actually not that imprecise for small-ish inputs, and are a handy
tool that is only available in RoughMath. There's also hyperbolic
trigonometric functions, which aren't anywhere else either, plus
`expRough()` and a variant `pow2Rough()`. MathTools also has
approximations to `Math.exp()`, including one that has a limited
domain (it returns 0 outside that domain, instead of returning
gradually smaller numbers; this can sometimes be useful).

Stringf is a somewhat experimental class that exists mostly to be a
partial polyfill for the missing `String.format()` method on GWT and
on some versions of TeaVM. It doesn't have anywhere near the full
functionality of `String.format()`, or even the functionality of
[Formic](https://github.com/tommyettinger/formic)'s `Stringf.format()`,
but unlike Formic it should work on TeaVM. It does have enough support
for the simpler usages of `%d` and `%f`, as well as common conveniences
such as `%08X` to print a 32-bit int as 8 hex digits, and always 8.
You could also use `Base.BASE16.unsigned(num)` for that.

Distributor is a relatively narrow class that exists to redistribute
int, long, and double inputs to float or double normal-distributed
values, while preserving patterns in the input data. The code that takes
int or long input acts like the more-well-known Ziggurat algorithm, but
is faster and almost certainly less precise. These are the `normal()` and
`normalF()` methods, and each works by looking up a precalculated normal
value from a table and interpolating linearly. The table's size is the main
saving grace for how simple the algorithm is; with 1024 entries, the
severity of problems introduced by linear interpolation is greatly reduced.
This class calculates the table using `probit()` and `probitHighPrecision()`
(it also exposes those for external use). Those take a double in the 0.0 to
1.0 range and return a normal-distributed double. There are probably all
sorts of creative uses for mapping int or long inputs to normal-distributed
outputs; you can find some yourself!

## How do I get it?

This library needs Java language level 8, but does not rely on any
APIs introduced in Java 8. Targeting level 8 means this will work
even if your project uses the newest Java versions (20 and later do
not support targeting Java 7). Android projects should be able to
use digital even without needing core library desugaring, as long
as they target release 8 or higher. GWT can use digital without
issue from GWT 2.8.0 up; GWT compatibility is a major focus of this
library. RoboVM, for iOS, can use digital because no APIs are used
from Java 8.

In a libGDX project, **you must make sure** the sourceCompatibility is
8 or higher in your core module and any other modules that use digital.
This is currently not the default for gdx-setup projects (which is no
longer the official setup tool), but is the default for
[gdx-liftoff](https://github.com/libgdx/gdx-liftoff) projects (which is
the offical setup tool). Liftoff also lets you just check a box to
depend on digital.

To depend on digital with Gradle, add this to your dependencies (in
your core module's `build.gradle`, for libGDX projects):

```groovy
api "com.github.tommyettinger:digital:0.5.2"
```

If you target GWT using libGDX, you will also need this in your
html module's `build.gradle`:

```groovy
api "com.github.tommyettinger:digital:0.5.2:sources"
```

GWT needs to be told about these changes in your `GdxDefinition.gwt.xml`
file. For digital 0.1.7 and later, use:

```xml
<inherits name="com.github.tommyettinger.digital" />
```

If you are using 0.1.6 or older, **there are probably some GWT
compatibility issues**, though you can try using this. You should
**update to 0.1.7 or later instead of using this**:

```xml
<inherits name="digital" />
```

You can also use JitPack to get a recent commit; in that case,
follow [its instructions here](https://jitpack.io/#tommyettinger/digital/).
This also has instructions for Maven and other build tools.

## License

[Apache 2.0](LICENSE). This includes some modified code from
[libGDX](https://github.com/libgdx/libgdx), 
[SquidLib](https://github.com/yellowstonegames/SquidLib),
[SquidSquad](https://github.com/yellowstonegames/SquidSquad),
[Uncommon Maths](https://maths.uncommons.org/),
[wyhash](https://github.com/wangyi-fudan/wyhash),
[Apache Commons Lang](https://github.com/apache/commons-lang),
[fastapprox](https://code.google.com/archive/p/fastapprox/),
and [Ryu](https://github.com/ulfjack/ryu). More code
is not from a particular repository (for example, some is from
Wikipedia); see each file for specific author credits. The Ryu
code is more substantial than the other projects, and even
though its license is also Apache 2.0, I included its license
here as [LICENSE-RYU.txt](LICENSE-RYU.txt). The Ryu code that
is relevant to what we use here is also replicated with only
minor compatibility changes in the `src/test` folder.
There's some cases where an individual method was ported from
another permissively-licensed repo, such as
`a_cbrt` from [Stand-alone-junk](https://github.com/Marc-B-Reynolds/Stand-alone-junk)
(Marc B. Reynolds' in-progress code), or `probit` from
[P.J. Acklam's site](https://web.archive.org/web/20151030215612/http://home.online.no/~pjacklam/notes/invnorm/)
(archived), and for most of these I
have tried to provide credit and a link to the source in the
documentation for that method.
