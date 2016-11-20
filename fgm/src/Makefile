CXX                ?= c++

CXXFLAGS           +=  -I. -g -O3 -DNDEBUG -fpermissive
CXXFLAGS           += -DVL_USEFASTMATH

CXXFLAGS           += $($(ARCH)_CXXFLAGS)

all: train

$%.o : %.c% 
	$(CXX) $(CXXFLAGS) $< -c -o $@

train : dscal.o dnrm2.o ddot.o daxpy.o train.o FGM.o linear.o macro.o MKL.o ProxMKL.o ProxOMP.o random.o  stocha_prox_mkl.o tron.o
	$(CXX) $(CXXFLAGS) $^ -o $@

clean:
	rm -rf *.o
	


