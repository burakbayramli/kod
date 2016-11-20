#include "macro.h"

static void info(const char *fmt,...)
{
	va_list ap;
	va_start(ap,fmt);
	vprintf(fmt,ap);
	va_end(ap);
}
static void info_flush()
{
	fflush(stdout);
}