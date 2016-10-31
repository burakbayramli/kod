## What it is

Mindmeld aims to combine all working parts of blood type diet,
numerology, psychology and astrology (yes!). The methods we utilize
here are mostly unknown to the general public; most people know about
sun signs (Capricorn, Taurus, blah) which has 1 out of 12
possibilities. One method here uses (G. Lewi's) all sun and moon
combinations which has 12x12 = 144 character possibilities. Lewi
method also looks at other planet combinations that can supply
additional information.

Jan Spiller method looks at moon readings differently; the result is
another in-depth character reading. The accuracy of this reading can
be quite shocking. Lastly Millman numerology, another extremely
detailed method to analyze people, is shared here.

Usage:

```
import mindmeld

res =  mindmeld.calculate(mindmeld.conv("10/3/1968"))
print mindmeld.describe(res)
```

The result will look like:

``` {'millman': [28, 10, 2, 8, 1, 0], 'chinese': 'Monkey', 'lewi':
[136, 161, 163, 183, 196, 199, 211, 214, 216, 235, 243, 246, 272,
276], 'spiller': 'Aries', 'cycle': 2} ```

The details of the reading results can be found under the `doc/details` folder. 
For Millman it is at `millman/2810.txt` for example, or for Chinese
`chinese/Monkey.html`, so on..

Cycle is the period your life path is on; according to his method life
proceeds in 9 year cycles, 1 is time to start something 9 to enjoy its
fruits (and not start something new).

Here is a related interesting exercise; starting from the cycle you
are at today, go back in time, year by year; i.e. if current cycle
point says 4, and the current year is 2015,

```
2010 8
2011 9
2012 1
2013 2
2014 3
2015 4
...
```

and try to remember the state of your life at those moments in
time, especially at year 1. The results can be revealing.

## MBTI Test

You can also take the MBTI test under `doc/mbti_en.html` and once you
answer all questions, you can hit the evaluate button and the
radiobutton input (represented as -1,0,+1 values) will be displayed in
a messagebox. Copy these results to clipboard, and from there paste it
in Python code like this,

```
ans = ":1:-1:-1:1:0:0:0:1:0:-1:0:0:-1:-1:1:-1:0:0:1:1:-1:1:1:1:0:0:0:-1:1:0:1:1:0:0:0:0:0:1:-1:-1:1:1:1:1:1:1:-1:1:1:1:0:-1:-1:-1:-1:1:0:0:0:0:-1:0:0:-1:0:-1:-1:-1:0:0"
ans = ans.split(":")[1:]
print mindmeld.calculate_mb(ans)
```

Run this code and you will get the MBTI result of the questionaire.


# Blood Type Diet

Some food ingredients are good for some blood types, bad for
others. See the file `data/food.dat` for the whole list. For example
the column A_S carries the ingredient's benefit status for blood type
A (secretor). For non-secretor it would be A_NS. Details on secretor /
non-secreators are in the file `doc/details/btype/secretor.txt`.

Secretor status determination apparently requires a blood test but it
is possible to guess it. For your blood type find a food item that has
different statuses for sec / non-sec, and ask yourself if you like
that food item, meaning that if the sec/non-sec statuses are
BENEFICIAL, AVOID and you like that food, it probably means you are a
secretor.

##Lewi Files

Now some implementation details; There are some reference files
`mindmeld` uses from the subproject `jlewi`; No need to copy anything
back and forth, bcz `mindmeld` folder as-is already has a recently
updated / working Lewi file copied from this subproject. If you want
to regenerate the lewi file however, all required files are under the
`jlewi` subfolder. This is a Java project, see its README for further
info. The calculation of Lewi numbers and "decans" is the most
beneficial part of this package, doing the same calculation by hand
was extremely time consuming (the decan information required for the
calculation is through the `SwissEph` package which we wrapped with
the Java code found under `jlewi`).

## Summary

In sum, `mindmeld` calculates the following:

* Grant Lewi Numbers (based on decans)

* Dan Millman Numerology

* Jan Spiller Moon North Node Astrology

* Chinese Astrology

* Myers-Briggs Test
