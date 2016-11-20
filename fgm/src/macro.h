#ifndef __MACROH__
#define __MACROH__

#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <time.h>

#ifndef __MARO__
#define __MARO__
typedef signed char schar;


#define min2(a, b)      ((a) <= (b) ? (a) : (b))
#define max2(a, b)      ((a) >= (b) ? (a) : (b))
#define max3(a, b, c)   max2(max2((a), (b)), (c));

template <class T> inline void swap(T& x, T& y) { T t=x; x=y; y=t; }
#ifndef min
template <class T> inline T min(T x,T y) { return (x<y)?x:y; }
#endif
#ifndef max
template <class T> inline T max(T x,T y) { return (x>y)?x:y; }
#endif
template <class S, class T> inline void clone(T*& dst, S* src, int n)
{   
	dst = new T[n];
	memcpy((void *)dst,(void *)src,sizeof(T)*n);
}
#define Malloc(type,n) (type *)malloc((n)*sizeof(type))
#define INF HUGE_VAL


//
//
//

#if 1
static void info(const char *fmt,...);
static void info_flush();
#else
static void info(char *fmt,...) {}
static void info_flush() {}
#endif

#endif

#endif