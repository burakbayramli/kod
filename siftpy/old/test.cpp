// file:        sift-driver.cpp
// author:      Andrea Vedaldi
// description: SIFT command line utility implementation

// AUTORIGTHS

#include<sift.hpp>
#include<stdio.h>
#include<string>
#include<iostream>
    
using namespace std ;

size_t const not_found = numeric_limits<size_t>::max() - 1 ;

std::ostream&
bla(std::ostream& os,
                 VL::float_t const * descr_pt,
                 bool binary,
                 bool fp )
{

    printf("else 2\n");

    VL::uint8_t idescr_pt [128] ;

    for(int i = 0 ; i < 128 ; ++i)
      idescr_pt[i] = VL::uint8_t(float_t(512) * descr_pt[i]) ;
    

      
      printf("else 3\n");
      
      for(int i = 0 ; i < 128 ; ++i) 
        os << ' ' 
           << VL::uint32_t( idescr_pt[i] ) ;
  return os ;
}


