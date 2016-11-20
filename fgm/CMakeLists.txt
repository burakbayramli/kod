cmake_minimum_required(VERSION 2.8)

project(FGM)

set (EXECUTABLE_OUTPUT_PATH ${PROJECT_BINARY_DIR}/bin)
set (LIBRARY_OUTPUT_PATH ${PROJECT_BINARY_DIR}/lib)
set (ARCHIVE_OUTPUT_PATH ${PROJECT_BINARY_DIR}/lib)

if (WIN32)
    if (CMAKE_GENERATOR MATCHES "Win64")
        set (CMAKE_INSTALL_PREFIX ${CMAKE_SOURCE_DIR}/x64)
    else()
        set (CMAKE_INSTALL_PREFIX ${CMAKE_SOURCE_DIR}/x86)
    endif()
else()
    set (CMAKE_INSTALL_PREFIX ${CMAKE_SOURCE_DIR}/linux)
endif()

set (FGM_headers
    src/blas.h
    src/blasp.h
    src/FGM.h
    src/linear.h
    src/MKL.h
    src/ProxMKL.h
    src/random.h
    src/shash.h
    src/stocha_prox_mkl.h
    )
set(FGM_src
    src/daxpy.c
    src/ddot.c
    src/dnrm2.c
    src/dscal.c
    src/FGM.cpp
    src/linear.cpp
    src/MKL.cpp
    src/ProxMKL.cpp
    src/random.cpp
    src/stocha_prox_mkl.cpp
    src/train.cpp
    )

add_executable(FGM ${FGM_headers} ${FGM_src})
set_target_properties(FGM PROPERTIES DEBUG_POSTFIX "d")


set (predict_headers
    src/FGM.h
    src/linear.h
    src/MKL.h
    src/ProxMKL.h
    src/stocha_prox_mkl.h
    )

set (predict_src
    src/FGM.cpp
    src/linear.cpp
    src/MKL.cpp
    src/predict.cpp
    src/ProxMKL.cpp
    src/stocha_prox_mkl.cpp
    )

add_executable(Predict ${predict_headers} ${predict_src})
set_target_properties(Predict PROPERTIES DEBUG_POSTFIX "d")

install(TARGETS FGM Predict
    RUNTIME DESTINATION .
    )
