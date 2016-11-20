
#ifndef _PROXMKL_H_
#define _PROXMKL_H_
#include <malloc.h>
#include <math.h>
#include <time.h>
#include "linear.h"
#include <stdlib.h>
//typedef struct 
//{
//	double val;
//	int ind;
//}
//doubleIntPair;


class ProxMKL
{
public:
	ProxMKL();
	ProxMKL(problem *&prob_,const parameter *svm_param_,int num_kernel_,model *&svm_model_)
	{
		num_kernel	= num_kernel_;
		svm_param 	= svm_param_; 
		prob		= prob_;
		bias        = prob->bias;
		svm_model	= svm_model_;
		l			= prob_->l; 
		B           = svm_param->B;
		w_size		= num_kernel*svm_param->B;
		max_num_kernel = svm_param->max_iteraion;
		C           = svm_param->C;
		max_w_size	= max_num_kernel*svm_param->B;
		ProxMKL_allocate();
		numEpochs = 10;
		eta0      = 1.0;
		L_f       = 10;
		TAU_K     = 0.01;
		
	}
	inline static void veccpy(double *y, const double *x, const int n)
	{
		int i;

		for (i = 0;i < n;++i) {
			y[i] = x[i];
		}
	}
	~ProxMKL();
	int proximity_L1squared(int d, double* x0, double lambda, double* x);
	int compare (const void * a, const void * b);
	//int mkl_online_linear();
	void ProxMKL_allocate();
    void sort_rs(doubleIntPair* x0abs,int n_rsi);
	void warm_start_model(int num_kernel_);
	void smoothmkl_init();
	void calculate_alpha();
	void return_model();
	void mkl_svm_train();
	double calculate_gradient_fast(double *gw, double *v);
	double calculate_gradient_logistic_fast(double *gw, double *v);

    int mkl_online_linear_l1svm();
	int mkl_online_linear_l2svm();
	int mkl_batch_linear_l2svm();
	int mkl_prox_linear_l2svm();
	void calucate_gwx(double *gw);

	
	void calculate_wx_gwx(double s);
	double calculate_gradient(double *gw,double *w);
    double line_search(double *v, double *gw, double *normsq, double *normsq_search,	
		double tau, double *b0,double *b, double *upd_t, double fv,double lambda,double &f_w_,double o_mega);
	double line_search_complex(double *v, double *gw, 
							double *normsq,	double *normsq_search,double tau, double *b0,double *b, double *upd_t, double fv,double lambda);

	int mkl_online_linear_logistic();
	double calculate_gradient_logistic(double *gw, double *v);
	int mkl_prox_linear_logistic();
	double line_search_logistic(double *v, double *gw, double *normsq,	double *normsq_search,
		double tau, double *b0,double *b, double *upd_t, double fv,double lambda,double &f_w_,double o_mega);

	void smoothmkl_omp_init();


	int mkl_prox_lasso_logistic();
	int proximity_group_lasso(int d, double* x0, double lambda, double* x);
	double line_search_lasso_logistic(double *v, double *gw, double *normsq,	double *normsq_search, double tau, double *b0,
									 double *b, double *upd_t, double fv,double lambda,double &f_w_, double f_omega);



	protected:
	const parameter		*svm_param;
	problem				*prob;
	model               *svm_model;

	int                 l;
	int					max_iter;
	int					max_inner_iter;
	double				innereps;
	double				innereps_min;
	//double              upper_bound[3];
	double				Cp;
	double				Cn;
	double				eps;
	double				bias;
	double              ptr_fx; //objective value



	double				*grad_alpha;//gradient for each kernel:h_'(z)
	double				**QD;       //Q_ii for each sub_kernel
	double				*betas;
	double				*gwTgw;//qt+h_t(z)
	double              *wTgw;
	double				*w;
	double				*gw;
	double				*G;
	double				*v_tau;
	double              *v;
	double              *w_temp;
	int					*index;
	int				    *y;
	double				*alpha_sum;
	int					num_kernel;//number of kernels
	int					w_size;


	//for functions
	double				*lambda_f; //for objective
	double				*fval_f; //qt+h_t(z)

	//other parameters
	double				beta;
	double				*bt;
	double              mu;
	double              q; //q = 1/mu

	double				epsf;
	int					max_num_kernel;
	int					max_w_size;
	double              C2; //to control the noise 
	double              C;
	double              gp;
	double              eta_mu; //eta_ = 0.8;
	double              upper_bound[3];
	double              **wtTxt;
	double              **gw_wtTxt;
	double              **wx_gwx;
	int                 numEpochs;
	double              eta0;
	double				yita; 
	double              L_f;
	double              TAU_K;
	double              tau_k;
	double              tau;
	int                  B;
	double t_minus;
	double t_zero ;
	double fw_temp;
	
};



#endif