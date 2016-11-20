#ifndef _SHASH_H_
#define _SHASH_H_

#include <stdio.h>
//#include <conio.h>
//#include <iostream>

//#include <map>

#include <hash_map>

//#include <algorithm>

 
//
//#include <ace/OS.h>
//
//#include <ace/Time_Value.h>

 

const size_t TEST_NUMBER         = 200000;

const size_t INIT_BUCKETS_NUMBER = 262144;

 

class test_hash_compare

{

public:

   //注意这个地方，我重新定义了这2个参数，【注意】

   static const size_t bucket_size = 1;

   static const size_t min_buckets = INIT_BUCKETS_NUMBER;

   //static const size_t min_buckets = 8;

   test_hash_compare()

   {

   }

   size_t operator( )( const size_t& Key ) const

   {

       return Key;

   }

   bool operator( )( 

      const size_t& _Key1,

      const size_t& _Key2

   ) const

   {

       return (_Key1<_Key2)?true:false;

   }

};

 

void test_hash_map()

{

 

//    ACE_Time_Value tvStart(0);
//
//    ACE_Time_Value tvEnd(0);
//
//    ACE_Time_Value tvPassTime(0);
//
// 
//
//tvStart = ACE_OS::gettimeofday(); 

 

    //这是使用STLPORT后才有的宏

#if defined _STLPORT_VERSION

    std::hash_map<size_t,int>   int_hash_map;

    //注意这行代码,VS.NET默认的STL没有这个函数的，而STLPort的实现有这个函数

    int_hash_map.resize(INIT_BUCKETS_NUMBER); 

#else 

    stdext::hash_map<size_t,double,test_hash_compare>   int_hash_map;

#endif //

 

    //测试20万次

    //顺序插入一组数据

    for (size_t i= 0;i<TEST_NUMBER;++i)
    {
        int_hash_map[i]=1.0;
    }

 

    //查询40万次，一半能查询到，一半不能查询到
    double v = 0.0;
    for (size_t i= 0;i<2*TEST_NUMBER;++i)

    {
        int_hash_map.find(i);
		v = int_hash_map[i];
		int_hash_map[i] = v + 0.5;
		v = int_hash_map[i];
		int_hash_map.erase(i);
		if(int_hash_map[i])
		{
			v = int_hash_map[i];
		}

    }

 

    //得到毫秒的时间差

 

   /* tvEnd = ACE_OS::gettimeofday(); 

    tvPassTime = tvEnd - tvStart;

 

    std::cout<<"test_hash_map gettimeofday :"<<tvPassTime.msec()<<" "<<std::endl;*/

};
#endif