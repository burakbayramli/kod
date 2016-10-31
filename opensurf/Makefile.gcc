# Indicates compilator to use
CC      = g++

# Specifies compilator options
CFLAGS  = -O3 -Wall `pkg-config --cflags opencv` -D LINUX -fPIC
LDFLAGS = 
LDLIBS  = `pkg-config --libs opencv`

# Files extensions .cpp, .o
SUFFIXES = .cpp .o 
.SUFFIXES: $(SUFFIXES) .

# Name of the main program
PROG  = surf

# Name of the library
LIB = libsurf.so

# Object files .o necessary to build the main program
OBJS  = fasthessian.o integral.o main.o surf.o utils.o ipoint.o

# Object files .o necessary to build the library
LIBOBJS  = fasthessian.o integral.o surf.o utils.o ipoint.o
 
all: $(PROG) $(LIB)

# Compilation and link
$(PROG): $(OBJS)
	$(CC) $(LDFLAGS) -o $(PROG) $(OBJS) $(LDLIBS)

$(LIB): $(LIBOBJS)
	$(CC) $(LIBOBJS) -o $(LIB) -ldl -shared -fPIC

%.o: $(addprefix ./src/, %.cpp)
	$(CC)  $(CFLAGS) -c $< -o $@

clean:
	-rm -f $(PROG)
	-rm -f $(LIB)
	-rm -f *.o
