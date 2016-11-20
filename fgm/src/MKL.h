#ifndef FGM_MKL_H_
#define FGM_MKL_H_
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <time.h>
#include <malloc.h>
#include "linear.h"

typedef signed char schar;

#define Malloc(type,n) (type *)malloc((n)*sizeof(type))

//template <class T> static inline void swap(T& x, T& y) { T t=x; x=y; y=t; }

class MKL
{
public:
	MKL(problem *&prob_,const parameter *svm_param_,int num_kernel_,model *&svm_model_)
	{
		num_kernel	= num_kernel_;
		param 		= svm_param_; 
		prob		= prob_;
		bias        = prob->bias;
		model_		= svm_model_;
		l			= prob_->l; 
		w_size		= num_kernel*param->B;
		max_w_size	= svm_param_->max_iteraion*param->B;
		max_num_kernel = svm_param_->max_iteraion;
		eps			=svm_param_->eps;
		if (svm_param_->solver_type == MINLR||svm_param_->solver_type == LRFGM)
		{
			l_alpha = 2*l;
		}
		else
		{
			l_alpha = l;
		}
		MKL_allocate();
	}
	MKL()
	{
	}
	~MKL();

	

	void my_max(double *vet, int size, double *max_value, int *max_index)
	{
		int i;
		double tmp = vet[0];
		max_index[0] = 0;

		for(i=0; i<size; i++){
			if(vet[i] > tmp){
				tmp = vet[i];
				max_index[0] = i;
			}
		}
		max_value[0] = tmp;
	}

	void my_min(double *vet, int size, double *min_value, int *min_index)
	{
		int i;
		double tmp = vet[0];
		min_index[0] = 0;

		for(i=0; i<size; i++){
			if(vet[i] < tmp){
				tmp = vet[i];
				min_index[0] = i;
			}
		}
		min_value[0] = tmp;
	}

	void my_soft_min(double *sigma_new, double * desc, int n_kernel, double *stepmax)
	{
		int i;
		int flag = 1;
		for(i=0; i<n_kernel; i++)
		{
			if(desc[i] < 0)
			{
				if(flag == 1)
				{
					stepmax[0] = -sigma_new[i]/desc[i];
					flag = 0;
				}
				else
				{
					double tmp = -sigma_new[i]/desc[i];
					if(tmp < stepmax[0])
					{
						stepmax[0] = tmp;
					}
				}
			}
		}

	}


	void	MKL_allocate();
	void	MKL_init();
	void	warm_set_model(int num_kernel_);
	void    linear_solver_svc();
	void    linear_solver_svc_match_pursuit(int w_size_temp);
	void    linear_solver_lr();
	void    mkl_svm_train(double *w, double* alpha_, double *sigma_);
	void    mkl_lr_train(double *w, double* alpha_, double *sigma_);

	void	pure_train_one();
	void    train_one(double *w, double* alpha_, double *sigma_);
	void	SimpleMKL();
	void    reset_model();
	void    reset_model(int w_size_temp);  





protected:
	const parameter		*param;
	problem				*prob;
	model               *model_;
	double				*QD;       //Q_ii for each subkernel

	double				*w;

	weight				*w2;
	int					*index;
	int                 l;
	int                 l_alpha;//dimension of alpha
	int					max_iter;

	int					num_kernel;//number of kernels
	int					w_size;
	schar				*y;
	double				*alpha;
	double				Cp;
	double				Cn;
	double				eps;
	double				bias;
	double              mkl_obj;
	int					max_num_kernel;
	int					max_w_size;


};









#endif  //
