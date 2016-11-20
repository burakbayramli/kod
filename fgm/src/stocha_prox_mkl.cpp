#include "stocha_prox_mkl.h"
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "float.h"
#include "linear.h"
#include <iostream>
void StochProxMKL::StochProxMKL_allocate()
{
	index		= new int[l];		
	alpha_sum		= new double[l]; // store alpha and C - alpha
	y			= new int[l];	
	w			= new double[max_w_size]; 
	gw			= new double[max_w_size]; 
	G			= new double[max_w_size]; 
	v			= new double[max_w_size];
	v_tau			= new double[max_w_size];
	w_temp			= new double[max_w_size];
	gwTgw		= new double[max_num_kernel];
	wTgw    = new double[max_num_kernel];
	betas		= new double[max_num_kernel];
	grad_alpha	= new double[max_num_kernel];        //gradient for each kernel
	QD			= new double*[max_num_kernel];       //Q_ii for each sub-kernels
	bt			= new double[max_num_kernel];

	fval_f		= new double[max_num_kernel];
	lambda_f	= new double[max_num_kernel];
	gw_wtTxt   = new double*[max_num_kernel];
	wtTxt      = new double*[max_num_kernel];
	wx_gwx      = new double*[max_num_kernel];
	work_set    = new int [work_set_size];

	int i = 0;
	for (i=0;i<max_num_kernel;i++)
	{

		QD[i]		= new double[l];
		wtTxt[i]	= new double[l];
		gw_wtTxt[i]	= new double[l];
		wx_gwx[i]    = new double[l];
	}
}


StochProxMKL::~StochProxMKL()
{	
	delete [] index;
	delete [] alpha_sum;
	delete [] y;
	delete [] w;
	delete [] gw;
	delete [] G;
	delete [] v;
	delete [] v_tau;
	delete [] w_temp;
	delete [] gwTgw;
	delete [] wTgw;
	delete [] betas;
	delete [] grad_alpha;
	delete [] bt;

	//
	delete [] lambda_f;
	delete [] fval_f;

	for (int i=0;i<max_num_kernel;i++)
	{
		delete [] QD[i]; 
		delete [] wtTxt[i]; 
		delete [] gw_wtTxt[i]; 
		delete [] wx_gwx[i];
	}
	delete [] QD;
	delete [] wtTxt;
	delete [] gw_wtTxt;
	delete [] wx_gwx;
	delete [] work_set;
}

//int StochProxMKL::compare (const void * a, const void * b)
//{
//	return ( (*(doubleIntPair*)a).val - (*(doubleIntPair*)b).val );
//}

int compare (const void * a, const void * b)
{
	return ( (*(doubleIntPair*)a).val - (*(doubleIntPair*)b).val );
}

void StochProxMKL::sort_rs(doubleIntPair* x0abs,int n_rsi)
{
	int i;
	int j;
	doubleIntPair x_temp;
	for (i=0; i< n_rsi-1; i++)
	{
		for(j=i+1; j < n_rsi; j++)
		{
			if (x0abs[i].val>x0abs[j].val)
			{
				x_temp.ind = x0abs[i].ind;
				x_temp.val = x0abs[i].val;

				x0abs[i].ind = x0abs[j].ind;
				x0abs[i].val = x0abs[j].val;

				x0abs[j].ind = x_temp.ind;
				x0abs[j].val = x_temp.val;
			}
		}
	}
}

void StochProxMKL::smoothmkl_init()
{
	
	Cn = Cp = svm_param->C;
	upper_bound[0] = Cn;
	upper_bound[1] = 0;
	upper_bound[2] = Cp;
	beta	= 0.5;
	eps = 0.1;

	TAU_K = 0.01;
	tau_k = 0.01;
	int i = 0;
	int p = 0;
	for(i=0; i<w_size; i++)
		w[i] = 0;

	for(i=0; i<l; i++)
	{
		if(prob->y[i] > 0)
		{
			y[i] = +1; 
		}
		else
		{
			y[i] = -1;
		}
		for(p=0; p<num_kernel; p++)
		{
			QD[p][i] = 0.0;
			wtTxt[p][i]=0;
			feature_node *xi = prob->xsp[p][i];
			while(xi->index!=-1)
			{
				QD[p][i] += (xi->value)*(xi->value);
				xi++;
			}
		}
		index[i] = i;
	}
}

int StochProxMKL::proximity_L1squared(int d, double* x0, double lambda, double* x)
{
	int i, l;
	doubleIntPair* x0abs = new doubleIntPair[d];
	double sum = 0.0;
	double tau;

	for (i = 0; i < d; i++)
	{
		x0abs[i].val = (x0[i] < 0)? -x0[i] : x0[i];
		x0abs[i].ind = i;
		x[i] = x0abs[i].val;
		sum += x0abs[i].val;
	}

	//sort by descend order
	sort_rs (x0abs, d);

	for (i = 0; i < d; i++)
	{
		tau = sum * lambda / (1 + (d - i) * lambda);
		if (x0abs[i].val > tau)
			break;
		sum -= x0abs[i].val;
	}

	for (i = 0; i < d; i++)
	{
		if (x[i] - tau < 0)
		{
			x[i] = 0.0;
		}
		else
		{
			x[i] -= tau;
			if (x0[i] < 0)
				x[i] = -x[i];
		}
	}

	delete[] x0abs;

	return 0;
}

void StochProxMKL::warm_start_model(int num_kernel_)
{
	num_kernel	= num_kernel_;
	w_size		= num_kernel*svm_param->B;
	int q = num_kernel-1;
	for(int i=0; i<l; i++)
	{
		for (int p=0; p<num_kernel; p++)
		{
			wtTxt[p][i]=0;
		}
		for(int p=q; p<num_kernel; p++)
		{
			QD[p][i] = 0.0;
			feature_node *xi = prob->xsp[p][i];
			while(xi->index!=-1)
			{
				QD[p][i] += (xi->value)*(xi->value);
				xi++;
			}
		}
		index[i] = i;
	}
}


//m: number of instances;
//p: number of kernels;
//C: parameter;
//numEpochs: iterations
//eta0: learning steps
//betas: weights for kernel
//alpha: dual variable for kernel
//y(x) = \sum\limits_i^{m}alpha_i y_i x_i'x = \sum\limits_i^{m}alpha_i y_i K(x_i,x)
//In linear case, it becomes much easier, 
//we maintain w for each kernel with w_t = \sum y_i alpha_i x_i
//and have y_t(x) = w_t' x
//and y(x) = \sum y_x(x)
int StochProxMKL::mkl_online_linear_logistic()
{
	int i, j, t, k;
	int B = svm_model->B;
	int p = num_kernel;
	int m = prob->l;
	int iter;
	int ind;
	double eta;
	double lambda = 1/(C*m);//*m
	double* alpha;
	double* K;
	double* preds = new double[p];
	double* normsq = new double[p];
	double* b0 = new double[p];
	double* b = new double[p];
	//double* w = new double[p*B];
	double pred;
	double upd;
	double btot;
	double C = Cp;
    float expcoef = 0.0;
	srand ( time(NULL));
	//wtTxt = new (double*)[p];	
	for (k = 0; k < p; k++)
	{
		//	wtTxt[k] = new double[m];
		for(i = 0; i < B; i++)
		{
			w[k*B+i] = 0;              
		}
		for (i = 0; i < m; i++)
			wtTxt[k][i] = 0.0;
	}

	for (k = 0; k < p; k++)
	{
		normsq[k] = 0.0;
	}

	//for (k = 0; k < p; k++)
	//{
	//	//	wtTxt[k] = new double[m];
	//	for(i = 0; i < B; i++)
	//	{
	//		w[k*B+i] = 0;              
	//	}
	//}
   /// numEpochs = 100;
	double normsq_temp;
	for (t = 0; t < m * numEpochs; t++)
	{
		iter = t / m;
		//printf("%d... ", iter+1);

		eta = eta0 / sqrt(float(t+1));

		i = rand() % m; //t % m; 

		//printf("%d\n", i);

		pred = 0.0;
	
		for (k = 0; k < p; k++)
		{
			
			//alpha = wtTxt[k];
			
			preds[k] = 0.0;
			feature_node *xi = prob->xsp[k][i];

			while (xi->index!=-1)
			{
				preds[k] += w[k*B+xi->index-1]*xi->value;
				xi++;
			}
			pred += preds[k];
		}

		//if (y[i]*pred < 1.0) for hinge loss only y[i]*pred < 1.0 we have sub-gradient
		//but for logistic regression, we have
		//calculate exp(-y_i*w'*x)/(1+exp(-y_i*w'*x))*expcoef
		double expywx = exp(-y[i]*pred);
        expcoef = expywx/(1+expywx);
		//if (y[i]*pred < 1.0)
		{
			for (k = 0; k < p; k++)
			{
				//printf("%lf\n", preds[k]);
				//alpha = wtTxt[k];
				//alpha[i] += y[i] *expcoef*eta;
				feature_node *xj = prob->xsp[k][i];
				while(xj->index!=-1)
				{
					w[k*B+xj->index-1] += y[i]*eta*expcoef*xj->value;
					xj++;
				}
				//K = KK[k]; K[ind] = K(xi,xi)=i*m+i
				//ind = i + m * i; 
				//||w+dw||^2 = ||w||^2 + ||dw||^2 + 2*w'dw
				// = ||w||^2 + ||dw||^2 + 2*y[i]*eta*\sum w_j*x_j 
				// = ||w||^2 + ||dw||^2 + 2*y[i]*eta*preds[k]
				normsq_temp = normsq[k];
				normsq[k] += expcoef*expcoef*eta * eta * QD[k][i] + 2*expcoef*eta*y[i]*preds[k];
				//printf("%lf\n", normsq[k]);
			}
		}
     
        if(t%100==0)
		{
			for (k = 0; k < p; k++)
				b0[k] = sqrt(normsq[k]);	

			proximity_L1squared(p, b0, eta * lambda, b);
			//deal with w
			//proximal step
			for (k = 0; k < p; k++)
			{
				//printf("%lf\n", normsq[k]);
				normsq[k] = b[k] * b[k];
				upd = (b0[k] > 0)? b[k] / b0[k] : 1.0;
				//alpha = wtTxt[k];
				for (j=0;j<B;j++)
				{
					w[k*B+j] *= upd;
				}
				/*for (j = 0; j < m; j++)
				alpha[j] *= upd;	*/			
			}	
		}
		//printf("\n");
	}

	//calculate obj
    
	//btot = 0.0;
	//for (k = 0; k < p; k++)
	//{
	//	b0[k] = sqrt(normsq[k]);				
	///*	alpha = wtTxt[k];
	//	for (j = 0; j < m; j++)
	//		alpha[j] *= y[j];*/
	//	btot += b0[k];				
	//}

	//for (k = 0; k < p; k++)
	//{
	//	betas[k] = b0[k] / btot;
	//}
 //   //calculate_alpha()
	//double loss=0;
	//for (i=0; i<m; i++)
	//{
	//	pred = 0.0;
	//	for (k = 0; k < p; k++)
	//	{
	//		preds[k] = 0.0;
	//		feature_node *xi = prob->xsp[k][i];
	//		while (xi->index!=-1)
	//		{
	//			preds[k] += w[k*B+xi->index-1]*xi->value;
	//			xi++;
	//		}
	//		pred += preds[k];
	//	}
	//	alpha_sum[i] = exp(-pred*y[i]);
	//	loss += log(1+alpha_sum[i]);
	//	alpha_sum[i] = alpha_sum[i]/(1+alpha_sum[i]);
	//}




	    
	btot = 0.0;
	for (k = 0; k < p; k++)
	{
		b0[k] = sqrt(normsq[k]);				
	/*	alpha = wtTxt[k];
		for (j = 0; j < m; j++)
			alpha[j] *= y[j];*/
		btot += b0[k];				
	}

	for (k = 0; k < p; k++)
	{
		betas[k] = b0[k] / btot;
	}
    //calculate_alpha()
	double loss=0;
	for (i=0; i<m; i++)
	{
		pred = 0.0;
		for (k = 0; k < p; k++)
		{
			preds[k] = 0.0;
			feature_node *xi = prob->xsp[k][i];
			while (xi->index!=-1)
			{
				preds[k] += w[k*B+xi->index-1]*xi->value;
				xi++;
			}
			pred += preds[k];
		}
		alpha_sum[i] = exp(-pred*y[i]);
		loss += log(1+alpha_sum[i]);
		alpha_sum[i] = alpha_sum[i]/(1+alpha_sum[i]);
	}

	ptr_fx = 0.5*btot*btot + C*loss;

	delete[] preds;
	delete[] normsq;
	delete[] b;
	delete[] b0;

	//for (k = 0; k < p; k++)
	//{
	//	alpha_sum[i] += betas[k]* wtTxt[k][i];
	//}
	//for (k = 0; k < p; k++)
	//{
	//	delete[] wtTxt[k];
	//}
	//delete[] wtTxt;

	return 0;
}




#ifndef max
template <class T> inline T max(T x,T y) { return (x>y)?x:y; }
#endif
#ifndef min
template <class T> inline T min(T x,T y) { return (x<y)?x:y; }
#endif
int StochProxMKL::mkl_online_linear_l1svm()
{
	int i, j, t, k;
	int B = svm_model->B;
	int p = num_kernel;
	int m = prob->l;
	int iter;
	int ind;
	double eta;
	double lambda = 1/(C*m);//*m
	double* alpha;
	double* K;
	double* preds = new double[p];
	double* normsq = new double[p];
	double* b0 = new double[p];
	double* b = new double[p];
	//double* w = new double[p*B];
	double pred;
	double upd;
	double btot;
	double C = Cp;
    float expcoef = 0.0;
	srand ( time(NULL));
	//wtTxt = new (double*)[p];	
	for (k = 0; k < p; k++)
	{
		//	wtTxt[k] = new double[m];
		for(i = 0; i < B; i++)
		{
			w[k*B+i] = 0;              
		}
		for (i = 0; i < m; i++)
			wtTxt[k][i] = 0.0;
	}

	for (k = 0; k < p; k++)
	{
		normsq[k] = 0.0;
	}

	//for (k = 0; k < p; k++)
	//{
	//	//	wtTxt[k] = new double[m];
	//	for(i = 0; i < B; i++)
	//	{
	//		w[k*B+i] = 0;              
	//	}
	//}
    //numEpochs = 3;
	double normsq_temp;
	for (t = 0; t < m * numEpochs; t++)
	{
		iter = t / m;
		//printf("%d... ", iter+1);

		eta = eta0 / sqrt(float(t+1));

		i = rand() % m; //t % m; 

		//printf("%d\n", i);

		pred = 0.0;
	
		for (k = 0; k < p; k++)
		{
			
			//alpha = wtTxt[k];
			
			preds[k] = 0.0;
			feature_node *xi = prob->xsp[k][i];

			while (xi->index!=-1)
			{
				preds[k] += w[k*B+xi->index-1]*xi->value;
				xi++;
			}
			pred += preds[k];
		}

		//if (y[i]*pred < 1.0) for hinge loss only y[i]*pred < 1.0 we have sub-gradient
		//but for logistic regression, we have
		//calculate exp(-y_i*w'*x)/(1+exp(-y_i*w'*x))*expcoef
		//double expywx = exp(-y[i]*pred);
        //expcoef = expywx/(1+expywx);
		if (y[i]*pred < 1.0)
		{
			for (k = 0; k < p; k++)
			{
				//printf("%lf\n", preds[k]);
				//alpha = wtTxt[k];
				//alpha[i] += y[i] *expcoef*eta;
				feature_node *xj = prob->xsp[k][i];
				while(xj->index!=-1)
				{
					w[k*B+xj->index-1] += y[i]*eta*xj->value;
					xj++;
				}
				//K = KK[k]; K[ind] = K(xi,xi)=i*m+i
				//ind = i + m * i; 
				//||w+dw||^2 = ||w||^2 + ||dw||^2 + 2*w'dw
				// = ||w||^2 + ||dw||^2 + 2*y[i]*eta*\sum w_j*x_j 
				// = ||w||^2 + ||dw||^2 + 2*y[i]*eta*preds[k]
				normsq_temp = normsq[k];
				normsq[k] += eta * eta * QD[k][i] + 2*eta*y[i]*preds[k];
				//printf("%lf\n", normsq[k]);
			}
		}
     
        if(t%100==0)
		{
			for (k = 0; k < p; k++)
				b0[k] = sqrt(normsq[k]);	

			proximity_L1squared(p, b0, eta * lambda, b);
			//deal with w
			//proximal step
			for (k = 0; k < p; k++)
			{
				//printf("%lf\n", normsq[k]);
				normsq[k] = b[k] * b[k];
				upd = (b0[k] > 0)? b[k] / b0[k] : 1.0;
				//alpha = wtTxt[k];
				for (j=0;j<B;j++)
				{
					w[k*B+j] *= upd;
				}
				/*for (j = 0; j < m; j++)
				alpha[j] *= upd;	*/			
			}	
		}
		//printf("\n");
	}

	//calculate obj
    
	btot = 0.0;
	for (k = 0; k < p; k++)
	{
		b0[k] = sqrt(normsq[k]);				
	/*	alpha = wtTxt[k];
		for (j = 0; j < m; j++)
			alpha[j] *= y[j];*/
		btot += b0[k];				
	}

	for (k = 0; k < p; k++)
	{
		betas[k] = b0[k] / btot;
	}
    //calculate_alpha()
	double loss=0;
	for (i=0; i<m; i++)
	{
		pred = 0.0;
		for (k = 0; k < p; k++)
		{
			preds[k] = 0.0;
			feature_node *xi = prob->xsp[k][i];
			while (xi->index!=-1)
			{
				preds[k] += w[k*B+xi->index-1]*xi->value;
				xi++;
			}
			pred += preds[k];
		}
		alpha_sum[i] = max(0.0, 1-pred);
		loss += alpha_sum[i];
		//alpha_sum[i] = alpha_sum[i]/(1+alpha_sum[i]);
	}

	ptr_fx = 0.5*btot*btot + C*loss;
	delete[] preds;
	delete[] normsq;
	delete[] b;
	delete[] b0;


	return 0;
}


int StochProxMKL::mkl_online_linear_l2svm()
{
	int i, j, t, k;
	int B = svm_model->B;
	int p = num_kernel;
	int m = prob->l;
	int iter;
	int ind;
	double eta;
	double lambda = 1/(C*m);//*m
	double* alpha;
	double* K;
	double* preds = new double[p];
	double* normsq = new double[p];
	double* b0 = new double[p];
	double* b = new double[p];
	//double* w = new double[p*B];
	double pred;
	double upd;
	double btot;
	double C = Cp;
    float expcoef = 0.0;
	srand ( time(NULL));
	//wtTxt = new (double*)[p];	
	for (k = 0; k < p; k++)
	{
		//	wtTxt[k] = new double[m];
		for(i = 0; i < B; i++)
		{
			w[k*B+i] = 0;              
		}
		for (i = 0; i < m; i++)
			wtTxt[k][i] = 0.0;
	}

	for (k = 0; k < p; k++)
	{
		normsq[k] = 0.0;
	}

	//for (k = 0; k < p; k++)
	//{
	//	//	wtTxt[k] = new double[m];
	//	for(i = 0; i < B; i++)
	//	{
	//		w[k*B+i] = 0;              
	//	}
	//}
    //numEpochs = 3;
	double normsq_temp;
	for (t = 0; t < m * numEpochs; t++)
	{
		iter = t / m;
		//printf("%d... ", iter+1);

		eta = eta0 / sqrt(float(t+1));

		i = rand() % m; //t % m; 

		//printf("%d\n", i);

		pred = 0.0;
	
		for (k = 0; k < p; k++)
		{
			
			//alpha = wtTxt[k];
			
			preds[k] = 0.0;
			feature_node *xi = prob->xsp[k][i];

			while (xi->index!=-1)
			{
				preds[k] += w[k*B+xi->index-1]*xi->value;
				xi++;
			}
			pred += preds[k];
		}

		//if (y[i]*pred < 1.0) for hinge loss only y[i]*pred < 1.0 we have sub-gradient
		//but for logistic regression, we have
		//calculate exp(-y_i*w'*x)/(1+exp(-y_i*w'*x))*expcoef
		double expywx = (1-y[i]*pred);
        expcoef = expywx;
		if (y[i]*pred < 1.0)
		{
			for (k = 0; k < p; k++)
			{
				//printf("%lf\n", preds[k]);
				//alpha = wtTxt[k];
				//alpha[i] += y[i] *expcoef*eta;
				feature_node *xj = prob->xsp[k][i];
				while(xj->index!=-1)
				{
					w[k*B+xj->index-1] += y[i]*eta*expcoef*xj->value;
					xj++;
				}
				//K = KK[k]; K[ind] = K(xi,xi)=i*m+i
				//ind = i + m * i; 
				//||w+dw||^2 = ||w||^2 + ||dw||^2 + 2*w'dw
				// = ||w||^2 + ||dw||^2 + 2*y[i]*eta*\sum w_j*x_j 
				// = ||w||^2 + ||dw||^2 + 2*y[i]*eta*preds[k]
				normsq_temp = normsq[k];
				normsq[k] += expcoef*expcoef*eta * eta * QD[k][i] + 2*expcoef*eta*y[i]*preds[k];
				//printf("%lf\n", normsq[k]);
			}
		}
     
        if(t%100==0)
		{
			for (k = 0; k < p; k++)
				b0[k] = sqrt(normsq[k]);	

			proximity_L1squared(p, b0, eta * lambda, b);
			//deal with w
			//proximal step
			for (k = 0; k < p; k++)
			{
				//printf("%lf\n", normsq[k]);
				normsq[k] = b[k] * b[k];
				upd = (b0[k] > 0)? b[k] / b0[k] : 1.0;
				//alpha = wtTxt[k];
				for (j=0;j<B;j++)
				{
					w[k*B+j] *= upd;
				}
				/*for (j = 0; j < m; j++)
				alpha[j] *= upd;	*/			
			}	
		}
		//printf("\n");
	/*btot = 0.0;
	for (k = 0; k < p; k++)
	{
		btot += b[k];				
	}

	for (k = 0; k < p; k++)
	{
		betas[k] = b0[k] / btot;
	}
	double loss=0;
	for (i=0; i<m; i++)
	{
		pred = 0.0;
		for (k = 0; k < p; k++)
		{
			preds[k] = 0.0;
			feature_node *xi = prob->xsp[k][i];
			while (xi->index!=-1)
			{
				preds[k] += w[k*B+xi->index-1]*xi->value;
				xi++;
			}
			pred += preds[k];
		}
		alpha_sum[i] = max(0.0,1-pred*y[i]);
		loss += alpha_sum[i];
	}
	ptr_fx = 0.5*btot*btot + C*loss;
	printf("objective value is:%f\n",ptr_fx);*/
	}


	//calculate obj
	btot = 0.0;
	for (k = 0; k < p; k++)
	{
	//	b0[k] = sqrt(normsq[k]);				
	/*	alpha = wtTxt[k];
		for (j = 0; j < m; j++)
			alpha[j] *= y[j];*/
		btot += b[k];				
	}

	for (k = 0; k < p; k++)
	{
		betas[k] = b[k] / btot;
	}
    //calculate_alpha()
	double loss=0;
	for (i=0; i<m; i++)
	{
		pred = 0.0;
		for (k = 0; k < p; k++)
		{
			preds[k] = 0.0;
			feature_node *xi = prob->xsp[k][i];
			while (xi->index!=-1)
			{
				preds[k] += w[k*B+xi->index-1]*xi->value;
				xi++;
			}
			pred += preds[k];
		}
		alpha_sum[i] = max(0.0,1-pred*y[i]);
		loss += alpha_sum[i];
	}

	ptr_fx = 0.5*btot*btot + C*loss;
	delete[] preds;
	delete[] normsq;
	delete[] b;
	delete[] b0;



	return 0;
}


void StochProxMKL::calucate_gwx(double *gw)
{
	int i, j, k; 
	int B = svm_param->B;
	for (i=0; i<prob->l; i++)
	{
		for (k = 0; k < num_kernel; k++)
		{
			feature_node *xi = prob->xsp[k][i];
			gw_wtTxt[k][i]=0; //y_i*w_t*xi_t
			while (xi->index!=-1)
			{
				gw_wtTxt[k][i] += gw[k*B+xi->index-1]*xi->value;
				xi++;
			}
		}
	}
}
void StochProxMKL::calucate_gwx(double *gw,int *work_set)
{
	int i, j, k; 
	int B = svm_param->B;
	for (i=0; i<work_set_size; i++)
	{
		for (k = 0; k < num_kernel; k++)
		{
			feature_node *xi = prob->xsp[k][work_set[i]];
			gw_wtTxt[k][work_set[i]]=0; //y_i*w_t*xi_t
			while (xi->index!=-1)
			{
				gw_wtTxt[k][work_set[i]] += gw[k*B+xi->index-1]*xi->value;
				xi++;
			}
		}
	}
}

void StochProxMKL::calculate_wx_gwx(double s)
{
	int i, j, k; 
	int B = svm_param->B;
	for (i=0; i<prob->l; i++)
	{
		for (k = 0; k < num_kernel; k++)
		{
			wx_gwx[k][i] = wtTxt[k][i] - s*gw_wtTxt[k][i];
		}
	}
}

void StochProxMKL::calculate_wx_gwx(double s, int *work_set)
{
	int i, j, k; 
	int B = svm_param->B;
	for (i=0; i<work_set_size; i++)
	{
		for (k = 0; k < num_kernel; k++)
		{
			wx_gwx[k][work_set[i]] = wtTxt[k][work_set[i]] - s*gw_wtTxt[k][work_set[i]];
		}
	}
}


//1: calculate the gradient max(0,1-yi*w'x)yixi = -(yi-w'x)xi
//2: and calculat f(v) = loss(v);
double StochProxMKL::calculate_gradient(double *gw, double *v)
{
	int m = prob->l;
	int i, k;
	double pred;
	double fv = 0;
	int p = num_kernel;
	int B = svm_param->B;
	for (i=0; i<m; i++)
	{
		//calculate f_t  = alpha_t = y_i*w_t*xi_t
		pred = 0;
		for (k = 0; k < p; k++)
		{
			feature_node *xi = prob->xsp[k][i];
			wtTxt[k][i]=0; //y_i*w_t*xi_t
			while (xi->index!=-1)
			{
				wtTxt[k][i] += v[k*B+xi->index-1]*xi->value;
				xi++;
			}
			pred += wtTxt[k][i];
		}

		pred = 1-y[i]*pred; //1-yi*w'xi
		if(pred>0) //max(0,1-yiw'xi)
		{
			fv += pred*pred;
			for (k = 0; k < p; k++)
			{
				feature_node *xi = prob->xsp[k][i];
				while (xi->index!=-1)
				{
					gw[k*B+xi->index-1] += pred*y[i]*xi->value;
					xi++;
				}
			}
		}
	}//finish gradient calculation
	return fv;
}

//1: calculate the gradient max(0,1-yi*w'x)yixi = -(yi-w'x)xi
//2: and calculat f(v) = loss(v);
double StochProxMKL::calculate_gradient(double *gw, double *v, int *work_set)
{
	int m = prob->l;
	int i, k;
	double pred;
	double fv = 0;
	int p = num_kernel;
	int B = svm_param->B;
	for (i=0; i<work_set_size; i++)
	{
		//calculate f_t  = alpha_t = y_i*w_t*xi_t
		pred = 0;
		for (k = 0; k < p; k++)
		{
			feature_node *xi = prob->xsp[k][work_set[i]];
			wtTxt[k][work_set[i]]=0; //y_i*w_t*xi_t
			while (xi->index!=-1)
			{
				wtTxt[k][work_set[i]] += v[k*B+xi->index-1]*xi->value;
				xi++;
			}
			pred += wtTxt[k][work_set[i]];
		}

		pred = 1-y[work_set[i]]*pred; //1-yi*w'xi
		if(pred>0) //max(0,1-yiw'xi)
		{
			fv += pred*pred;
			for (k = 0; k < p; k++)
			{
				feature_node *xi = prob->xsp[k][work_set[i]];
				while (xi->index!=-1)
				{
					gw[k*B+xi->index-1] += pred*y[work_set[i]]*xi->value;
					xi++;
				}
			}
		}
	}//finish gradient calculation
	return fv;
}






//0: for logistic regression,
//1: calculate the gradient max(0,1-yi*w'x)yixi = -(yi-w'x)xi
//2: and calculat f(v) = loss(v);
double StochProxMKL::calculate_gradient_logistic(double *gw, double *v)
{
	int m = prob->l;
	int i, k;
	double pred;
	double fv = 0;
	int p = num_kernel;
	int B = svm_param->B;
	double expywx, expcoef; 
	for (i=0; i<m; i++)
	{
		//calculate f_t  = alpha_t = y_i*w_t*xi_t
		pred = 0;
		for (k = 0; k < p; k++)
		{
			feature_node *xi = prob->xsp[k][i];
			wtTxt[k][i]=0; //y_i*w_t*xi_t
			while (xi->index!=-1)
			{
				wtTxt[k][i] += v[k*B+xi->index-1]*xi->value;
				xi++;
			}
			pred += wtTxt[k][i];
		}

		//pred =  -y[i]*pred; //1-yi*w'xi
		expywx	= exp(-y[i]*pred);
        expcoef = expywx/(1+expywx);
		//if(pred>0) //max(0,1-yiw'xi)
		{
			fv += log(1+expywx);
			for (k = 0; k < p; k++)
			{
				feature_node *xi = prob->xsp[k][i];
				while (xi->index!=-1)
				{
					gw[k*B+xi->index-1] += expcoef*y[i]*xi->value;
					xi++;
				}
			}
		}
	}//finish gradient calculation

	return fv;
}
double StochProxMKL::calculate_gradient_logistic(double *gw, double *v,int *work_set)
{
	int m = prob->l;
	int i, k;
	double pred;
	double fv = 0;
	int p = num_kernel;
	int B = svm_param->B;
	double expywx, expcoef; 
	for (i=0; i<work_set_size; i++)
	{
		//calculate f_t  = alpha_t = y_i*w_t*xi_t
		pred = 0;
		for (k = 0; k < p; k++)
		{
			feature_node *xi = prob->xsp[k][work_set[i]];
			wtTxt[k][work_set[i]]=0; //y_i*w_t*xi_t
			while (xi->index!=-1)
			{
				wtTxt[k][work_set[i]] += v[k*B+xi->index-1]*xi->value;
				xi++;
			}
			pred += wtTxt[k][work_set[i]];
		}

		//pred =  -y[i]*pred; //1-yi*w'xi
		expywx	= exp(-y[work_set[i]]*pred);
        expcoef = expywx/(1+expywx);
		//if(pred>0) //max(0,1-yiw'xi)
		{
			fv += log(1+expywx);
			for (k = 0; k < p; k++)
			{
				feature_node *xi = prob->xsp[k][work_set[i]];
				while (xi->index!=-1)
				{
					gw[k*B+xi->index-1] += expcoef*y[work_set[i]]*xi->value;
					xi++;
				}
			}
		}
	}//finish gradient calculation

	return fv;
}

//do line search for tau
//gw: gradient
//tau: approximated Lf
//to find best tau such that G = Y-tau^-1 gw
//double StochProxMKL::line_search(double *v, double *gw, double *normsq,	double *normsq_search,
//							double tau, double *b0,double *b, double *upd_t, double fv,double lambda,double &f_w_)
//{
//	int i, k, j;
//	double f_w, Q_tau;
//	int p = num_kernel;
//	int B = svm_param->B;
//	double btot, xi, pred;
//	int m = prob->l;
//	double tau_k;
//	yita = 0.8;
//	double pred_temp1;
//	double *SG = new double [w_size];
//	double Q_tau1;
//	tau = yita*tau;
//	for(i=0; i<40; i++)
//	{
//		double s = 1/tau;
//		for(k=0; k<p; k++)
//		{
//			//compute G = v-tau^-1*gw;
//			for(j=0; j<B; j++)
//			{
//				G[k*B+j] = v[k*B+j] -  s*gw[k*B+j];
//			}
//			//normsq[k]: norm(v)
//			grad_alpha[k] = normsq[k] + s * s *gwTgw[k]
//			- 2*s*wTgw[k];
//		}
//
//		//calculate wx-tau*gwx
//		calculate_wx_gwx(s);
//
//
//		// do shrinking for G, and compute SG
//		for (k = 0; k < p; k++)
//			b0[k] = sqrt(grad_alpha[k]);	
//		proximity_L1squared(p, b0, s, b);
//
//		//deal with w
//		//proximal step
//		for (k = 0; k < p; k++)
//		{
//			//printf("%lf\n", normsq[k]);
//			normsq_search[k] = b[k] * b[k];
//			upd_t[k] = (b0[k] > 0)? b[k] / b0[k] : 1.0;
//			for (j=0;j<B;j++)
//			{
//				SG[k*B+j] = G[k*B+j]*upd_t[k];
//			}
//
//		}	
//
//		//calculate f(w) and Q_tau(w,v)
//		//f(w)
//		pred = 0;
//		for(j=0; j<m; j++)
//		{
//			double pred_temp = 0;
//			for(k=0; k<p; k++)
//			{
//				pred_temp += upd_t[k]*wx_gwx[k][j];
//			}
//			xi = max(0.0,1-y[j]*pred_temp); 
//			pred += xi*xi;
//		}
//		pred = C*pred;
//        pred_temp1 = 0;
//		for(j=0; j<m; j++)
//		{
//			double pred_temp = 0;
//			for(k=0; k<p; k++)
//			{
//				feature_node *xi = prob->xsp[k][j];
//				while (xi->index!=-1)
//				{
//					pred_temp += SG[k*B+xi->index-1]*xi->value;
//					xi++;
//				}
//			}
//			xi = max(0.0,1-y[j]*pred_temp); 
//			pred_temp1 += xi*xi;
//		}
//		pred_temp1 = C*pred_temp1;
//        if(abs(pred_temp1-pred)>1e-5)
//		{
//			printf("Difference happens\n");
//		}
//		btot = 0;
//		for (k = 0; k < p; k++)
//		{
//			//b0[k] = sqrt(normsq_search[k]);				
//			btot += b[k];				
//		}
//		f_w = 0.5*btot*btot + pred;
//		f_w_ = f_w;
//		//printf("Objective value is %f\n",f_w);
//		//Q_tau(w,v) = f(v) + <d_y, w-y> + \tau/2 ||w-y||^2 + p(w)
//		//f(v) = \sum_i loss(v,x_i) 
//		//<d_y, w-y> 
//		double dw_proj = 0;
//		double dw_diff = 0;
//		double G_SG = 0;
//		double normSY = 0;
//		for(k=0; k<p; k++)
//		{
//			for(j=0; j< B; j++)
//			{
//				dw_proj += gw[k*B+j]*(upd_t[k]*G[k*B+j]-v[k*B+j]);
//				dw_diff += (upd_t[k]*G[k*B+j]-v[k*B+j])*(upd_t[k]*G[k*B+j]-v[k*B+j]);
//				G_SG    += 0.5*tau*(G[k*B+j]-SG[k*B+j])*(G[k*B+j]-SG[k*B+j]);
//				normSY  += 0.5/tau*gw[k*B+j]*gw[k*B+j];
//			}
//		}
//		//0.5*btot*btot = Omega(SG)
//		Q_tau = dw_proj + tau*dw_diff/2 + fv + 0.5*btot*btot;
//		Q_tau1 = G_SG + fv + 0.5*btot*btot - normSY;
//		if (f_w<Q_tau)
//		{
//			tau_k = tau;
//			break;
//		}
//		else
//		{
//			tau = tau/yita;
//		}
//	}
//	printf("Objective value is %f\n",f_w);
//	delete [] SG;
//	return tau;
//}


double StochProxMKL::line_search(double *v, double *gw,double *normsq,	double *normsq_search,
							double tau, double *b0,double *b, double *upd_t, double fv,double lambda,double &f_w_, int *work_set)
{
	int i, k, j;
	double f_w, Q_tau;
	int p = num_kernel;
	int B = svm_param->B;
	double btot, xi, pred;
	int m = prob->l;
	double tau_k;
	yita = 0.8;
	double pred_temp1;
	double *SG = new double [w_size];
	double Q_tau1;
	tau = yita*tau;
	for(i=0; i<40; i++)
	{
		double s = 1/tau;
		for(k=0; k<p; k++)
		{
			//compute G = v-tau^-1*gw;
			for(j=0; j<B; j++)
			{
				G[k*B+j] = v[k*B+j] -  s*gw[k*B+j];
			}
			//normsq[k]: norm(v)
			grad_alpha[k] = normsq[k] + s * s *gwTgw[k]
			- 2*s*wTgw[k];
		}

		//calculate wx-tau*gwx
		calculate_wx_gwx(s,work_set);


		// do shrinking for G, and compute SG
		for (k = 0; k < p; k++)
			b0[k] = sqrt(grad_alpha[k]);	
		proximity_L1squared(p, b0, s, b);

		//deal with w
		//proximal step
		for (k = 0; k < p; k++)
		{
			//printf("%lf\n", normsq[k]);
			normsq_search[k] = b[k] * b[k];
			upd_t[k] = (b0[k] > 0)? b[k] / b0[k] : 1.0;
			for (j=0;j<B;j++)
			{
				SG[k*B+j] = G[k*B+j]*upd_t[k];
			}
		}	

		//calculate f(w) and Q_tau(w,v)
		//f(w)
		pred = 0;
		for(j=0; j<work_set_size; j++)
		{
			double pred_temp = 0;
			for(k=0; k<p; k++)
			{
				pred_temp += upd_t[k]*wx_gwx[k][work_set[j]];
			}
			xi = max(0.0,1-y[work_set[j]]*pred_temp); 
			pred += xi*xi;
		}
		pred = C*pred;
        pred_temp1 = 0;
		for(j=0; j<work_set_size; j++)
		{
			double pred_temp = 0;
			for(k=0; k<p; k++)
			{
				feature_node *xi = prob->xsp[k][work_set[j]];
				while (xi->index!=-1)
				{
					pred_temp += SG[k*B+xi->index-1]*xi->value;
					xi++;
				}
			}
			xi = max(0.0,1-y[work_set[j]]*pred_temp); 
			pred_temp1 += xi*xi;
		}
		pred_temp1 = C*pred_temp1;
		//pred = pred_temp1;
		if(abs(pred_temp1-pred)>1e-4)
		{
			printf("The difference happened\n");
		}
		btot = 0;
		for (k = 0; k < p; k++)
		{
			//b0[k] = sqrt(normsq_search[k]);				
			btot += b[k];				
		}
		f_w = 0.5*btot*btot + pred;
		f_w_ = f_w;
		//printf("Objective value is %f\n",f_w);
		//Q_tau(w,v) = f(v) + <d_y, w-y> + \tau/2 ||w-y||^2 + p(w)
		//f(v) = \sum_i loss(v,x_i) 
		//<d_y, w-y> 
		double dw_proj = 0;
		double dw_diff = 0;
		double G_SG = 0;
		double normSY = 0;
		for(k=0; k<p; k++)
		{
			for(j=0; j< B; j++)
			{
				dw_proj += gw[k*B+j]*(upd_t[k]*G[k*B+j]-v[k*B+j]);
				dw_diff += (upd_t[k]*G[k*B+j]-v[k*B+j])*(upd_t[k]*G[k*B+j]-v[k*B+j]);
				G_SG    += 0.5*tau*(G[k*B+j]-SG[k*B+j])*(G[k*B+j]-SG[k*B+j]);
				normSY  += 0.5/tau*gw[k*B+j]*gw[k*B+j];
			}
		}
		//0.5*btot*btot = Omega(SG)
		Q_tau = dw_proj + tau*dw_diff/2 + fv + 0.5*btot*btot;
		//Q_tau1 = G_SG + fv + 0.5*btot*btot - normSY;
		if (f_w<Q_tau)
		{
			tau_k = tau;
			break;
		}
		else
		{
			tau = tau/yita;
		}
	}
	//if(i>40)
	{
		printf("Objective value is %f\n",f_w);
	}
	delete [] SG;
	return tau;
}


double StochProxMKL::line_search(double *v, double *gw,double *normsq,	double *normsq_search,
							double tau, double *b0,double *b, double *upd_t, double fv,double lambda,double &f_w_)
{
	int i, k, j;
	double f_w, Q_tau;
	int p = num_kernel;
	int B = svm_param->B;
	double btot, xi, pred;
	int m = prob->l;
	double tau_k;
	yita = 0.8;
	double pred_temp1;
	double *SG = new double [w_size];
	double Q_tau1;
	tau = yita*tau;
	for(i=0; i<40; i++)
	{
		double s = 1/tau;
		for(k=0; k<p; k++)
		{
			//compute G = v-tau^-1*gw;
			for(j=0; j<B; j++)
			{
				G[k*B+j] = v[k*B+j] -  s*gw[k*B+j];
			}
			//normsq[k]: norm(v)
			grad_alpha[k] = normsq[k] + s * s *gwTgw[k]
			- 2*s*wTgw[k];
		}

		//calculate wx-tau*gwx
		calculate_wx_gwx(s);


		// do shrinking for G, and compute SG
		for (k = 0; k < p; k++)
			b0[k] = sqrt(grad_alpha[k]);	
		proximity_L1squared(p, b0, s, b);

		//deal with w
		//proximal step
		for (k = 0; k < p; k++)
		{
			//printf("%lf\n", normsq[k]);
			normsq_search[k] = b[k] * b[k];
			upd_t[k] = (b0[k] > 0)? b[k] / b0[k] : 1.0;
			for (j=0;j<B;j++)
			{
				SG[k*B+j] = G[k*B+j]*upd_t[k];
			}
		}	

		//calculate f(w) and Q_tau(w,v)
		//f(w)
		pred = 0;
		for(j=0; j<m; j++)
		{
			double pred_temp = 0;
			for(k=0; k<p; k++)
			{
				pred_temp += upd_t[k]*wx_gwx[k][j];
			}
			xi = max(0.0,1-y[j]*pred_temp); 
			pred += xi*xi;
		}
		pred = C*pred;
     /*  pred_temp1 = 0;
		for(j=0; j<m; j++)
		{
			double pred_temp = 0;
			for(k=0; k<p; k++)
			{
				feature_node *xi = prob->xsp[k][j];
				while (xi->index!=-1)
				{
					pred_temp += SG[k*B+xi->index-1]*xi->value;
					xi++;
				}
			}
			xi = -y[j]*pred_temp; 
			pred_temp1 += log(1+exp(xi));
		}
		pred_temp1 = C*pred_temp1;
		if(abs(pred_temp1-pred)>1e-4)
		{
			printf("The difference happened\n");
		}*/
		btot = 0;
		for (k = 0; k < p; k++)
		{
			//b0[k] = sqrt(normsq_search[k]);				
			btot += b[k];				
		}
		f_w = 0.5*btot*btot + pred;
		f_w_ = f_w;
		//printf("Objective value is %f\n",f_w);
		//Q_tau(w,v) = f(v) + <d_y, w-y> + \tau/2 ||w-y||^2 + p(w)
		//f(v) = \sum_i loss(v,x_i) 
		//<d_y, w-y> 
		double dw_proj = 0;
		double dw_diff = 0;
		double G_SG = 0;
		double normSY = 0;
		for(k=0; k<p; k++)
		{
			for(j=0; j< B; j++)
			{
				dw_proj += gw[k*B+j]*(upd_t[k]*G[k*B+j]-v[k*B+j]);
				dw_diff += (upd_t[k]*G[k*B+j]-v[k*B+j])*(upd_t[k]*G[k*B+j]-v[k*B+j]);
				G_SG    += 0.5*tau*(G[k*B+j]-SG[k*B+j])*(G[k*B+j]-SG[k*B+j]);
				normSY  += 0.5/tau*gw[k*B+j]*gw[k*B+j];
			}
		}
		//0.5*btot*btot = Omega(SG)
		Q_tau = dw_proj + tau*dw_diff/2 + fv + 0.5*btot*btot;
		//Q_tau1 = G_SG + fv + 0.5*btot*btot - normSY;
		if (f_w<Q_tau)
		{
			tau_k = tau;
			break;
		}
		else
		{
			tau = tau/yita;
		}
	}
	//if(i>40)
	/*{
		printf("Objective value is %f\n",f_w);
	}*/
	delete [] SG;
	return tau;
}
//do line search for tau
//gw: gradient
//tau: approximated Lf
//to find best tau such that G = Y-tau^-1 gw
double StochProxMKL::line_search_logistic(double *v, double *gw, 
							double *normsq,	double *normsq_search,double tau, double *b0,double *b, double *upd_t, double fv,double lambda,double &f_w_)
{
	int i, k, j;
	double f_w, Q_tau;
	int p = num_kernel;
	int B = svm_param->B;
	double btot, xi, pred;
	int m = prob->l;
	double tau_k;
	yita = 0.8;
	double pred_temp1;
	double *SG = new double [w_size];
	double Q_tau1;
	tau = yita*tau;
	for(i=0; i<40; i++)
	{
		double s = 1/tau;
		for(k=0; k<p; k++)
		{
			//compute G = v-tau^-1*gw;
			for(j=0; j<B; j++)
			{
				G[k*B+j] = v[k*B+j] -  s*gw[k*B+j];
			}
			//normsq[k]: norm(v)
			grad_alpha[k] = normsq[k] + s * s *gwTgw[k]
			- 2*s*wTgw[k];
		}

		//calculate wx-tau*gwx
		calculate_wx_gwx(s);


		// do shrinking for G, and compute SG
		for (k = 0; k < p; k++)
			b0[k] = sqrt(grad_alpha[k]);	
		proximity_L1squared(p, b0, s, b);

		//deal with w
		//proximal step
		for (k = 0; k < p; k++)
		{
			//printf("%lf\n", normsq[k]);
			normsq_search[k] = b[k] * b[k];
			upd_t[k] = (b0[k] > 0)? b[k] / b0[k] : 1.0;
			for (j=0;j<B;j++)
			{
				SG[k*B+j] = G[k*B+j]*upd_t[k];
			}

		}	

		//calculate f(w) and Q_tau(w,v)
		//f(w)
		pred = 0;
		for(j=0; j<m; j++)
		{
			double pred_temp = 0;
			for(k=0; k<p; k++)
			{
				pred_temp += upd_t[k]*wx_gwx[k][j];
			}
			xi = -y[j]*pred_temp; 
			pred += log(1+exp(xi));
		}
		pred = C*pred;
     /*  pred_temp1 = 0;
		for(j=0; j<m; j++)
		{
			double pred_temp = 0;
			for(k=0; k<p; k++)
			{
				feature_node *xi = prob->xsp[k][j];
				while (xi->index!=-1)
				{
					pred_temp += SG[k*B+xi->index-1]*xi->value;
					xi++;
				}
			}
			xi = -y[j]*pred_temp; 
			pred_temp1 += log(1+exp(xi));
		}
		pred_temp1 = C*pred_temp1;
		if(abs(pred_temp1-pred)>1e-4)
		{
			printf("The difference happened\n");
		}*/
		btot = 0;
		for (k = 0; k < p; k++)
		{
			//b0[k] = sqrt(normsq_search[k]);				
			btot += b[k];				
		}
		f_w = 0.5*btot*btot + pred;
		f_w_ = f_w;
		//printf("Objective value is %f\n",f_w);
		//Q_tau(w,v) = f(v) + <d_y, w-y> + \tau/2 ||w-y||^2 + p(w)
		//f(v) = \sum_i loss(v,x_i) 
		//<d_y, w-y> 
		double dw_proj = 0;
		double dw_diff = 0;
		double G_SG = 0;
		double normSY = 0;
		for(k=0; k<p; k++)
		{
			for(j=0; j< B; j++)
			{
				dw_proj += gw[k*B+j]*(upd_t[k]*G[k*B+j]-v[k*B+j]);
				dw_diff += (upd_t[k]*G[k*B+j]-v[k*B+j])*(upd_t[k]*G[k*B+j]-v[k*B+j]);
				G_SG    += 0.5*tau*(G[k*B+j]-SG[k*B+j])*(G[k*B+j]-SG[k*B+j]);
				normSY  += 0.5/tau*gw[k*B+j]*gw[k*B+j];
			}
		}
		//0.5*btot*btot = Omega(SG)
		Q_tau = dw_proj + tau*dw_diff/2 + fv + 0.5*btot*btot;
		//Q_tau1 = G_SG + fv + 0.5*btot*btot - normSY;
		if (f_w<Q_tau)
		{
			tau_k = tau;
			break;
		}
		else
		{
			tau = tau/yita;
		}
	}
	//if(i>40)
	{
		printf("Objective value is %f\n",f_w);
	}
	delete [] SG;
	return tau;
}



double StochProxMKL::line_search_logistic(double *v, double *gw, double *normsq,	double *normsq_search,
										  double tau, double *b0,double *b, double *upd_t, double fv,double lambda,double &f_w_, int *work_set)
{
	int i, k, j;
	double f_w, Q_tau;
	int p = num_kernel;
	int B = svm_param->B;
	double btot, xi, pred;
	int m = prob->l;
	double tau_k;
	yita = 0.8;
	double pred_temp1;
	double *SG = new double [w_size];
	double Q_tau1;
	tau = yita*tau;
	for(i=0; i<40; i++)
	{
		double s = 1/tau;
		for(k=0; k<p; k++)
		{
			//compute G = v-tau^-1*gw;
			for(j=0; j<B; j++)
			{
				G[k*B+j] = v[k*B+j] -  s*gw[k*B+j];
			}
			//normsq[k]: norm(v)
			grad_alpha[k] = normsq[k] + s * s *gwTgw[k]
			- 2*s*wTgw[k];
		}

		//calculate wx-tau*gwx
		calculate_wx_gwx(s,work_set);


		// do shrinking for G, and compute SG
		for (k = 0; k < p; k++)
			b0[k] = sqrt(grad_alpha[k]);	
		proximity_L1squared(p, b0, s, b);

		//deal with w
		//proximal step
		for (k = 0; k < p; k++)
		{
			//printf("%lf\n", normsq[k]);
			normsq_search[k] = b[k] * b[k];
			upd_t[k] = (b0[k] > 0)? b[k] / b0[k] : 1.0;
			for (j=0;j<B;j++)
			{
				SG[k*B+j] = G[k*B+j]*upd_t[k];
			}

		}	

		//calculate f(w) and Q_tau(w,v)
		//f(w)
		pred = 0;
		for(j=0; j<work_set_size; j++)
		{
			double pred_temp = 0;
			for(k=0; k<p; k++)
			{
				pred_temp += upd_t[k]*wx_gwx[k][work_set[j]];
			}
			xi = -y[work_set[j]]*pred_temp; 
			pred += log(1+exp(xi));
		}
		pred = C*pred;
     /*  pred_temp1 = 0;
		for(j=0; j<m; j++)
		{
			double pred_temp = 0;
			for(k=0; k<p; k++)
			{
				feature_node *xi = prob->xsp[k][j];
				while (xi->index!=-1)
				{
					pred_temp += SG[k*B+xi->index-1]*xi->value;
					xi++;
				}
			}
			xi = -y[j]*pred_temp; 
			pred_temp1 += log(1+exp(xi));
		}
		pred_temp1 = C*pred_temp1;
		if(abs(pred_temp1-pred)>1e-4)
		{
			printf("The difference happened\n");
		}*/
		btot = 0;
		for (k = 0; k < p; k++)
		{
			//b0[k] = sqrt(normsq_search[k]);				
			btot += b[k];				
		}
		f_w = 0.5*btot*btot + pred;
		f_w_ = f_w;
		//printf("Objective value is %f\n",f_w);
		//Q_tau(w,v) = f(v) + <d_y, w-y> + \tau/2 ||w-y||^2 + p(w)
		//f(v) = \sum_i loss(v,x_i) 
		//<d_y, w-y> 
		double dw_proj = 0;
		double dw_diff = 0;
		double G_SG = 0;
		double normSY = 0;
		for(k=0; k<p; k++)
		{
			for(j=0; j< B; j++)
			{
				dw_proj += gw[k*B+j]*(upd_t[k]*G[k*B+j]-v[k*B+j]);
				dw_diff += (upd_t[k]*G[k*B+j]-v[k*B+j])*(upd_t[k]*G[k*B+j]-v[k*B+j]);
				G_SG    += 0.5*tau*(G[k*B+j]-SG[k*B+j])*(G[k*B+j]-SG[k*B+j]);
				normSY  += 0.5/tau*gw[k*B+j]*gw[k*B+j];
			}
		}
		//0.5*btot*btot = Omega(SG)
		Q_tau = dw_proj + tau*dw_diff/2 + fv + 0.5*btot*btot;
		//Q_tau1 = G_SG + fv + 0.5*btot*btot - normSY;
		if (f_w<Q_tau)
		{
			tau_k = tau;
			break;
		}
		else
		{
			tau = tau/yita;
		}
	}
	//if(i>40)
	{
		//printf("Objective value is %f\n",f_w);
	}
	delete [] SG;
	return tau;
}


double StochProxMKL::line_search_complex(double *v, double *gw, 
							double *normsq,	double *normsq_search,double tau, double *b0,double *b, double *upd_t, double fv,double lambda)
{
	int i, k, j;
	double f_w, Q_tau;
	int p = num_kernel;
	int B = svm_param->B;
	double btot, xi, pred;
	int m = prob->l;
	double tau_k;
	yita = 0.8;
	double pred_temp1;
	double *SG = new double [w_size];
	double Q_tau1;
	for(i=0; i<40; i++)
	{
		double s = 1/tau;
		for(k=0; k<p; k++)
		{
			//compute G = v-tau^-1*gw;
			for(j=0; j<B; j++)
			{
				G[k*B+j] = v[k*B+j] -  s*gw[k*B+j];
			}
			//normsq[k]: norm(v)
			grad_alpha[k] = normsq[k] + s * s *gwTgw[k]
			- 2*s*wTgw[k];
		}

		//calculate wx-tau*gwx
		calculate_wx_gwx(s);


		// do shrinking for G, and compute SG
		for (k = 0; k < p; k++)
			b0[k] = sqrt(grad_alpha[k]);	
		proximity_L1squared(p, b0, s, b);

		//deal with w
		//proximal step
		for (k = 0; k < p; k++)
		{
			//printf("%lf\n", normsq[k]);
			normsq_search[k] = b[k] * b[k];
			upd_t[k] = (b0[k] > 0)? b[k] / b0[k] : 1.0;
			for (j=0;j<B;j++)
			{
				SG[k*B+j] = G[k*B+j]*upd_t[k];
			}

		}	

		//calculate f(w) and Q_tau(w,v)
		//f(w)
		/*pred = 0;
		for(j=0; j<m; j++)
		{
			double pred_temp = 0;
			for(k=0; k<p; k++)
			{
				pred_temp += upd_t[k]*wx_gwx[k][j];
			}
			xi = max(0.0,1-y[j]*pred_temp); 
			pred += xi*xi;
		}*/
        pred_temp1 = 0;
		for(j=0; j<m; j++)
		{
			double pred_temp = 0;
			for(k=0; k<p; k++)
			{
				feature_node *xi = prob->xsp[k][j];
				while (xi->index!=-1)
				{
					pred_temp += SG[k*B+xi->index-1]*xi->value;
					xi++;
				}
			}
			xi = max(0.0,1-y[j]*pred_temp); 
			pred_temp1 += xi*xi;
		}
		pred_temp1 = C*pred_temp1;
		pred = pred_temp1;
		btot = 0;
		for (k = 0; k < p; k++)
		{
			//b0[k] = sqrt(normsq_search[k]);				
			btot += b[k];				
		}
		f_w = 0.5*btot*btot + pred;
		//printf("Objective value is %f\n",f_w);
		//Q_tau(w,v) = f(v) + <d_y, w-y> + \tau/2 ||w-y||^2 + p(w)
		//f(v) = \sum_i loss(v,x_i) 
		//<d_y, w-y> 
		double dw_proj = 0;
		double dw_diff = 0;
		double G_SG = 0;
		double normSY = 0;
		for(k=0; k<p; k++)
		{
			for(j=0; j< B; j++)
			{
				dw_proj += gw[k*B+j]*(upd_t[k]*G[k*B+j]-v[k*B+j]);
				dw_diff += (upd_t[k]*G[k*B+j]-v[k*B+j])*(upd_t[k]*G[k*B+j]-v[k*B+j]);
				G_SG    += 0.5*tau*(G[k*B+j]-SG[k*B+j])*(G[k*B+j]-SG[k*B+j]);
				normSY  += 0.5/tau*gw[k*B+j]*gw[k*B+j];
			}
		}
		//0.5*btot*btot = Omega(SG)
		Q_tau = dw_proj + tau*dw_diff/2 + fv + 0.5*btot*btot;
		Q_tau1 = G_SG + fv + 0.5*btot*btot - normSY;
		if (f_w<Q_tau)
		{
			tau_k = tau;
			break;
		}
		else
		{
			tau = tau/yita;
		}
	}
	delete [] SG;
	return tau;
}
#ifndef INF
#define INF HUGE_VAL
#endif
template <class T> inline void swap(T& x, T& y) { T t=x; x=y; y=t; }

int StochProxMKL::stoch_mkl_prox_linear_l2svm()
{
	int i, j, t, k;
	int B = svm_model->B;
	int p = num_kernel;
	int m = prob->l;
	int active_size = m;
	int iter;
	int ind;
	double eta;
 
	double lambda = 1/(C);
	double* alpha;
	double* K;
	double* preds = new double[p];
	double* normsq = new double[p];
	double* normsq_search = new double[p];
	double* b0 = new double[p];
	double* b = new double[p];
	double* upd_t = new double[p];
	//double* w = new double[p*B];
	double pred;
	double upd;
	double btot;
	double C = Cp;
	float expcoef = 0.0;
	double t_zero = 1;
	double t_minus = 1;
	double tau = 1;

	//numEpochs = 3;
	double normsq_temp;
	double t_ratio;
	//double tau_k = 1;
	double yita = 0.8;
	double fv = 0;
	double omgega_v = 0;
	double Q_tau;
	double f_w;
	double tk; 
	double f_w_temp = 0;
	//v: search points
	//w: solution wk
	//w_temp: store w_{k-1}
	double xi = 0;


	srand ( time(NULL));
	for (k = 0; k < p; k++)
	{
		normsq[k] = 0.0;
	}

	//using wtTxt[k] to store x_ti'w_t
	for (k = p-1; k < p; k++)
	{
		for(i = 0; i < B; i++)
		{
			w[k*B+i] = 0;  
			w_temp[k*B+i] = 0; 
		}
	}

	
	tau_k = 0.01;
	numEpochs = 3;
	f_w_temp = 1;
	int out_t = 0;
	int out_numEpochs = 40;
	int start = 0;
	int flag_stop = 0;
	tau = tau_k;
	for (out_t = 0; out_t < out_numEpochs; out_t++)
	{
		//prepare for work_set, at first randomly perm the data points
		for (i=0; i<active_size; i++)
		{
			int j = i+rand()%(active_size-i);
			swap(index[i], index[j]);
		}
        printf("out_t = %d\n",out_t);
		start = 0;
		if(flag_stop==1)
		{
			break;
		}
		flag_stop = 0;
		
		while((start+work_set_size)<active_size-1)
		{
			//prepare for work_set
			for(int z = 0; z < work_set_size; z++)
			{
				work_set[z] = index[start++];
			}
			//learning
			for(t = 0; t < numEpochs; t++)
			{
                //printf("inner t = %d\n",t);
				t_ratio = (t_minus-1)/(t_zero);

				//v=w + t_ratio(w-w_temp);
				for (k = 0; k < p; k++)
				{
					for(i = 0; i < B; i++)
					{
						v[k*B+i] =w[k*B+i] + t_ratio*(w[k*B+i]-w_temp[k*B+i]);     
						gw[k*B+i] = 0;
					}
				}

				//calculate gradient and the loss(v)
				fv = calculate_gradient(gw, v, work_set);

				//omgega_v = (sum(||v_t||))^2
				omgega_v = 0;
				for(k=0; k<p; k++)
				{
					normsq[k] = 0.0;
					for(i=0; i<B; i++)
					{  
						gw[k*B+i] = -2*C*gw[k*B+i] ;
						normsq[k] += v[k*B+i]*v[k*B+i];
					}
					omgega_v = sqrt(normsq[k]);
				}
				fv = C*fv ;

				//calculate wt'gw and gw'gw
				//G = v - s*gw
				//S_g // G
				//||S_g||^2 = c *||G|| = ||v||^2 - 2*s*wt'gw + s^2||gw||^2
				for(k=0; k<p; k++)
				{
					wTgw[k] = 0;//wTgw = wt'*gw
					gwTgw[k] = 0; // gwTgw[k] = gw'gw;
					for(i=0; i<B; i++)
					{
						wTgw[k] += v[k*B+i]*gw[k*B+i]; 
						gwTgw[k] += gw[k*B+i]*gw[k*B+i]; 
					}
				}

				//calculate gw'x
				//Sg'x = s*G'x = v'x-s*gw'x
				calucate_gwx(gw,work_set);

				//tau = yita*tau_k;
				//linear_search for tau
				//if(t==0)
				{
					tau = line_search(v, gw, normsq, normsq_search, tau, b0, b, upd_t, fv, lambda, f_w, work_set);
					//printf("step size is %f\n", 1.0/tau);
				}
				if(t==0)
				{
					//give a proper guess of f_w_temp
					f_w_temp = f_w + f_w + 100;
				}
				//if (abs(f_w_temp-f_w)/abs(f_w_temp)<1e-3)
				//{
				//	//printf("can be stopped!\n");
				//	flag_stop = 1;
				//	break;	
				//}else
				//{
				//	flag_stop = 0;
				//	f_w_temp = f_w;
				//}

				//update w, fv, normsq, omgega_v, and tau
				btot = 0;
				for (k = 0; k < p; k++)
				{
					//printf("%lf\n", normsq[k]);
					normsq[k] = b[k] * b[k];
					btot += normsq[k];

					//upd_t[k] = (b0[k] > 0)? b[k] / b0[k] : 1.0;
					for (j=0; j<B; j++)
					{
						w_temp[k*B+j]   = w[k*B+j];
						w[k*B+j] = G[k*B+j]*upd_t[k];
					}
					omgega_v = 0.5*btot*btot;
					tau_k = tau;
				}	

				//update t_zero
				t_minus = t_zero;
				t_zero = 0.5*(1+sqrt(1+4*t_zero*t_zero));
			}

		}

	}
	


	//calculate obj
	btot = 0.0;
	for (k = 0; k < p; k++)
	{
		b0[k] = sqrt(normsq[k]);				
		btot += b[k];				
	}

	double loss=0;
	for (i=0; i<m; i++)
	{
		pred = 0.0;
		for (k = 0; k < p; k++)
		{
			preds[k] = 0.0;
			feature_node *xi = prob->xsp[k][i];
			while (xi->index!=-1)
			{
				preds[k] += w[k*B+xi->index-1]*xi->value;
				xi++;
			}
			pred += preds[k];
		}
		alpha_sum[i] = max(0.0,1-pred*y[i]);
		loss += alpha_sum[i]*alpha_sum[i];
	}

	ptr_fx = 0.5*btot*btot + C*loss;

	for (k = 0; k < p; k++)
	{
		betas[k] = b[k] / btot;
	}
	delete[] preds;
	delete[] normsq;
	delete[] normsq_search;
	delete[] b;
	delete[] b0;
	delete[] upd_t;
	return 0;
}

int StochProxMKL::mkl_prox_linear_l2svm()
{
	int i, j, t, k;
	int B = svm_model->B;
	int p = num_kernel;
	int m = prob->l;
	int iter;
	int ind;
	double eta;

	double lambda = 1/(C);
	double* alpha;
	double* K;
	double* preds = new double[p];
	double* normsq = new double[p];
	double* normsq_search = new double[p];
	double* b0 = new double[p];
	double* b = new double[p];
	double* upd_t = new double[p];
	//double* w = new double[p*B];
	double pred;
	double upd;
	double btot;
	double C = Cp;
	float expcoef = 0.0;
	double t_zero = 1;
	double t_minus = 1;
	double tau = 1;

	//numEpochs = 3;
	double normsq_temp;
	double t_ratio;
	//double tau_k = 1;
	double yita = 0.8;
	double fv = 0;
	double omgega_v = 0;
	double Q_tau;
	double f_w;
	double tk; 
	double f_w_temp = 0;
	//v: search points
	//w: solution wk
	//w_temp: store w_{k-1}
	double xi = 0;


	srand ( time(NULL));
	for (k = 0; k < p; k++)
	{
		normsq[k] = 0.0;
	}

	//using wtTxt[k] to store x_ti'w_t
	for (k = p-1; k < p; k++)
	{
		for(i = 0; i < B; i++)
		{
			w[k*B+i] = 0;  
			w_temp[k*B+i] = 0; 
		}
	}

	
	tau_k = 0.01;
	numEpochs = 40;
	f_w_temp = 1;
	for(t = 0; t < numEpochs; t++)
	{
		t_ratio = (t_minus-1)/(t_zero);

		//v=w + t_ratio(w-w_temp);
		for (k = 0; k < p; k++)
		{
			for(i = 0; i < B; i++)
			{
				v[k*B+i] =w[k*B+i] + t_ratio*(w[k*B+i]-w_temp[k*B+i]);     
				gw[k*B+i] = 0;
			}
		}

		//calculate gradient and the loss(v)
       fv = calculate_gradient(gw, v);
       
	   //omgega_v = (sum(||v_t||))^2
		omgega_v = 0;
		for(k=0; k<p; k++)
		{
			normsq[k] = 0.0;
			for(i=0; i<B; i++)
			{  
				gw[k*B+i] = -2*C*gw[k*B+i] ;
				normsq[k] += v[k*B+i]*v[k*B+i];
			}
			omgega_v = sqrt(normsq[k]);
		}
		fv = C*fv ;

		//calculate wt'gw and gw'gw
		//G = v - s*gw
		//S_g // G
		//||S_g||^2 = c *||G|| = ||v||^2 - 2*s*wt'gw + s^2||gw||^2
		for(k=0; k<p; k++)
		{
			wTgw[k] = 0;//wTgw = wt'*gw
			gwTgw[k] = 0; // gwTgw[k] = gw'gw;
			for(i=0; i<B; i++)
			{
				wTgw[k] += v[k*B+i]*gw[k*B+i]; 
				gwTgw[k] += gw[k*B+i]*gw[k*B+i]; 
			}
		}

		//calculate gw'x
		//Sg'x = s*G'x = v'x-s*gw'x
		calucate_gwx(gw);

		//tau = yita*tau_k;
		//linear_search for tau
        tau = line_search(v, gw, normsq, normsq_search, tau, b0, b, upd_t, fv,lambda,f_w);
		if(t==0)
		{
			//give a proper guess of f_w_temp
			f_w_temp = f_w + f_w + 100;
		}
		if (abs(f_w_temp-f_w)/abs(f_w_temp)<1e-4)
		{
			break;	
		}else
		{
			f_w_temp = f_w;
		}

		//update w, fv, normsq, omgega_v, and tau
		btot = 0;
		for (k = 0; k < p; k++)
		{
			//printf("%lf\n", normsq[k]);
			normsq[k] = b[k] * b[k];
			btot += normsq[k];

			//upd_t[k] = (b0[k] > 0)? b[k] / b0[k] : 1.0;
			for (j=0; j<B; j++)
			{
				w_temp[k*B+j]   = w[k*B+j];
				w[k*B+j] = G[k*B+j]*upd_t[k];
			}
			omgega_v = 0.5*btot*btot;
			tau_k = tau;
		}	

		//update t_zero
		t_minus = t_zero;
		t_zero = 0.5*(1+sqrt(1+4*t_zero*t_zero));
	}


	//calculate obj
	btot = 0.0;
	for (k = 0; k < p; k++)
	{
		b0[k] = sqrt(normsq[k]);				
		btot += b[k];				
	}

	double loss=0;
	for (i=0; i<m; i++)
	{
		pred = 0.0;
		for (k = 0; k < p; k++)
		{
			preds[k] = 0.0;
			feature_node *xi = prob->xsp[k][i];
			while (xi->index!=-1)
			{
				preds[k] += w[k*B+xi->index-1]*xi->value;
				xi++;
			}
			pred += preds[k];
		}
		alpha_sum[i] = max(0.0,1-pred*y[i]);
		loss += alpha_sum[i]*alpha_sum[i];
	}

	ptr_fx = 0.5*btot*btot + C*loss;

	for (k = 0; k < p; k++)
	{
		betas[k] = b[k] / btot;
	}
	delete[] preds;
	delete[] normsq;
	delete[] normsq_search;
	delete[] b;
	delete[] b0;
	delete[] upd_t;
	return 0;
}

int StochProxMKL::mkl_prox_linear_logistic()
{
	int i, j, t, k;
	int B = svm_model->B;
	int p = num_kernel;
	int m = prob->l;
	int iter;
	int ind;
	double eta;

	double lambda = 1/(C);//
	double* alpha;
	double* K;
	double* preds = new double[p];
	double* normsq = new double[p];
	double* normsq_search = new double[p];
	double* b0 = new double[p];
	double* b = new double[p];
	double* upd_t = new double[p];
	//double* w = new double[p*B];
	double pred;
	double upd;
	double btot;
	double C = Cp;
	float expcoef = 0.0;
	double t_zero = 1;
	double t_minus = 1;
	double tau = 1;

	//numEpochs = 3;
	double normsq_temp;
	double t_ratio;
	//double tau_k = 1;
	double yita = 0.8;
	double fv = 0;
	double omgega_v = 0;
	double Q_tau;
	double f_w;
	double tk; 
	double f_w_temp = 0;
	//v: search points
	//w: solution wk
	//w_temp: store w_{k-1}
	double xi = 0;


	srand ( time(NULL));
	for (k = 0; k < p; k++)
	{
		normsq[k] = 0.0;
	}

	//using wtTxt[k] to store x_ti'w_t
	for (k = p-1; k < p; k++)
	{
		for(i = 0; i < B; i++)
		{
			w[k*B+i] = 0;  
			w_temp[k*B+i] = 0; 
		}
	}

	
	tau_k = 0.01;
	numEpochs = 100;
	f_w_temp = 1;
	for(t = 0; t < numEpochs; t++)
	{
		t_ratio = (t_minus-1)/(t_zero);

		//v=w + t_ratio(w-w_temp);
		for (k = 0; k < p; k++)
		{
			for(i = 0; i < B; i++)
			{
				v[k*B+i] =w[k*B+i] + t_ratio*(w[k*B+i]-w_temp[k*B+i]);     
				gw[k*B+i] = 0;
			}
		}

		//calculate gradient and the loss(v)
       fv = calculate_gradient_logistic(gw, v);
       
		
	   //omgega_v = (sum(||v_t||))^2
		omgega_v = 0;
		for(k=0; k<p; k++)
		{
			normsq[k] = 0.0;
			for(i=0; i<B; i++)
			{  
				gw[k*B+i] = -C*gw[k*B+i] ;
				normsq[k] += v[k*B+i]*v[k*B+i];
			}
			omgega_v = sqrt(normsq[k]);
		}
		fv = C*fv ;

		//calculate wt'gw and gw'gw
		//G = v - s*gw
		//S_g // G
		//||S_g||^2 = c *||G|| = ||v||^2 - 2*s*wt'gw + s^2||gw||^2
		for(k=0; k<p; k++)
		{
			wTgw[k] = 0;//wTgw = wt'*gw
			gwTgw[k] = 0; // gwTgw[k] = gw'gw;
			for(i=0; i<B; i++)
			{
				wTgw[k] += v[k*B+i]*gw[k*B+i]; 
				gwTgw[k] += gw[k*B+i]*gw[k*B+i]; 
			}
		}


		//calculate gw'x
		//Sg'x = s*G'x = v'x-s*gw'x
		calucate_gwx(gw);

		//tau = yita*tau_k;
		//linear_search for tau
        tau = line_search_logistic(v, gw, normsq, normsq_search, tau, b0, b, upd_t, fv,lambda,f_w);
		if(t==0)
		{
			//give a proper guess of f_w_temp
			f_w_temp = f_w + f_w + 100;
		}
		if (abs(f_w_temp-f_w)/abs(f_w_temp)<1e-5)
		{
			break;	
		}else
		{
			f_w_temp = f_w;
		}

		//update w, fv, normsq, omgega_v, and tau
		btot = 0;
		for (k = 0; k < p; k++)
		{
			//printf("%lf\n", normsq[k]);
			normsq[k] = b[k] * b[k];
			btot += normsq[k];

			//upd_t[k] = (b0[k] > 0)? b[k] / b0[k] : 1.0;
			for (j=0; j<B; j++)
			{
				w_temp[k*B+j]   = w[k*B+j];
				w[k*B+j] = G[k*B+j]*upd_t[k];
			}
			omgega_v = 0.5*btot*btot;
			tau_k = tau;
		}	

		//update t_zero
		t_minus = t_zero;
		t_zero = 0.5*(1+sqrt(1+4*t_zero*t_zero));
	}


	//calculate obj    
	btot = 0.0;
	for (k = 0; k < p; k++)
	{
		b0[k] = sqrt(normsq[k]);				
	/*	alpha = wtTxt[k];
		for (j = 0; j < m; j++)
			alpha[j] *= y[j];*/
		btot += b0[k];				
	}

	for (k = 0; k < p; k++)
	{
		betas[k] = b0[k] / btot;
	}
    //calculate_alpha()
	double loss=0;
	for (i=0; i<m; i++)
	{
		pred = 0.0;
		for (k = 0; k < p; k++)
		{
			preds[k] = 0.0;
			feature_node *xi = prob->xsp[k][i];
			while (xi->index!=-1)
			{
				preds[k] += w[k*B+xi->index-1]*xi->value;
				xi++;
			}
			pred += preds[k];
		}
		alpha_sum[i] = exp(-pred*y[i]);
		loss += log(1+alpha_sum[i]);
		alpha_sum[i] = alpha_sum[i]/(1+alpha_sum[i]);
	}

	ptr_fx = 0.5*btot*btot + C*loss;

	for (k = 0; k < p; k++)
	{
		betas[k] = b[k] / btot;
	}
	delete[] preds;
	delete[] normsq;
	delete[] normsq_search;
	delete[] b;
	delete[] b0;
	delete[] upd_t;
	return 0;
}



int StochProxMKL::stoch_mkl_prox_linear_lr()
{
	int i, j, t, k;
	int B = svm_model->B;
	int p = num_kernel;
	int m = prob->l;
	int iter;
	int ind;
	double eta;

	double lambda = 1/(C);//
	double* alpha;
	double* K;
	double* preds = new double[p];
	double* normsq = new double[p];
	double* normsq_search = new double[p];
	double* b0 = new double[p];
	double* b = new double[p];
	double* upd_t = new double[p];
	//double* w = new double[p*B];
	double pred;
	double upd;
	double btot;
	double C = Cp;
	float expcoef = 0.0;
	double t_zero = 1;
	double t_minus = 1;
	double tau = 1;

	//numEpochs = 3;
	double normsq_temp;
	double t_ratio;
	//double tau_k = 1;
	double yita = 0.8;
	double fv = 0;
	double omgega_v = 0;
	double Q_tau;
	double f_w;
	double tk; 
	double f_w_temp = 0;
	//v: search points
	//w: solution wk
	//w_temp: store w_{k-1}
	double xi = 0;


	srand ( time(NULL));
	for (k = 0; k < p; k++)
	{
		normsq[k] = 0.0;
	}

	//using wtTxt[k] to store x_ti'w_t
	for (k = p-1; k < p; k++)
	{
		for(i = 0; i < B; i++)
		{
			w[k*B+i] = 0;  
			w_temp[k*B+i] = 0; 
		}
	}


	tau_k = 0.01;
	numEpochs = 100;
	f_w_temp = 1;
	int out_t;
	int out_numEpochs = 40;
	int start = 0;
	int active_size = prob->l;
	for(out_t = 0; out_t<out_numEpochs; out_t++)
	{
		start = 0;
		for (i=0; i<active_size; i++)
		{
			int j = i+rand()%(active_size-i);
			swap(index[i], index[j]);
		}
		while(start+work_set_size<prob->l-1)
		{
			for(int z = 0; z<work_set_size; z++)
			{
				work_set[z] = index[start++];
			}
			//optimize the subproblem:
			for(t = 0; t < numEpochs; t++)
			{
				t_ratio = (t_minus-1)/(t_zero);

				//v=w + t_ratio(w-w_temp);
				for (k = 0; k < p; k++)
				{
					for(i = 0; i < B; i++)
					{
						v[k*B+i] =w[k*B+i] + t_ratio*(w[k*B+i]-w_temp[k*B+i]);     
						gw[k*B+i] = 0;
					}
				}

				//calculate gradient and the loss(v)
				fv = calculate_gradient_logistic(gw, v,work_set);


				//omgega_v = (sum(||v_t||))^2
				omgega_v = 0;
				for(k=0; k<p; k++)
				{
					normsq[k] = 0.0;
					for(i=0; i<B; i++)
					{  
						gw[k*B+i] = -C*gw[k*B+i] ;
						normsq[k] += v[k*B+i]*v[k*B+i];
					}
					omgega_v = sqrt(normsq[k]);
				}
				fv = C*fv ;

				//calculate wt'gw and gw'gw
				//G = v - s*gw
				//S_g // G
				//||S_g||^2 = c *||G|| = ||v||^2 - 2*s*wt'gw + s^2||gw||^2
				for(k=0; k<p; k++)
				{
					wTgw[k] = 0;//wTgw = wt'*gw
					gwTgw[k] = 0; // gwTgw[k] = gw'gw;
					for(i=0; i<B; i++)
					{
						wTgw[k] += v[k*B+i]*gw[k*B+i]; 
						gwTgw[k] += gw[k*B+i]*gw[k*B+i]; 
					}
				}


				//calculate gw'x
				//Sg'x = s*G'x = v'x-s*gw'x
				calucate_gwx(gw,work_set);

				//tau = yita*tau_k;
				//linear_search for tau
				tau = line_search_logistic(v, gw, normsq, normsq_search, tau, b0, b, upd_t, fv,lambda,f_w,work_set);
				if(t==0)
				{
					//give a proper guess of f_w_temp
					f_w_temp = f_w + f_w + 100;
				}
				if (abs(f_w_temp-f_w)/abs(f_w_temp)<1e-5)
				{
					break;	
				}else
				{
					f_w_temp = f_w;
				}

				//update w, fv, normsq, omgega_v, and tau
				btot = 0;
				for (k = 0; k < p; k++)
				{
					//printf("%lf\n", normsq[k]);
					normsq[k] = b[k] * b[k];
					btot += normsq[k];

					//upd_t[k] = (b0[k] > 0)? b[k] / b0[k] : 1.0;
					for (j=0; j<B; j++)
					{
						w_temp[k*B+j]   = w[k*B+j];
						w[k*B+j] = G[k*B+j]*upd_t[k];
					}
					omgega_v = 0.5*btot*btot;
					tau_k = tau;
				}	

				//update t_zero
				t_minus = t_zero;
				t_zero = 0.5*(1+sqrt(1+4*t_zero*t_zero));
			}
		}

	}




	//calculate obj    
	btot = 0.0;
	for (k = 0; k < p; k++)
	{
		b0[k] = sqrt(normsq[k]);				
		/*	alpha = wtTxt[k];
		for (j = 0; j < m; j++)
		alpha[j] *= y[j];*/
		btot += b0[k];				
	}

	for (k = 0; k < p; k++)
	{
		betas[k] = b0[k] / btot;
	}
	//calculate_alpha()
	double loss=0;
	for (i=0; i<m; i++)
	{
		pred = 0.0;
		for (k = 0; k < p; k++)
		{
			preds[k] = 0.0;
			feature_node *xi = prob->xsp[k][i];
			while (xi->index!=-1)
			{
				preds[k] += w[k*B+xi->index-1]*xi->value;
				xi++;
			}
			pred += preds[k];
		}
		alpha_sum[i] = exp(-pred*y[i]);
		loss += log(1+alpha_sum[i]);
		alpha_sum[i] = alpha_sum[i]/(1+alpha_sum[i]);
	}

	ptr_fx = 0.5*btot*btot + C*loss;

	for (k = 0; k < p; k++)
	{
		betas[k] = b[k] / btot;
	}
	delete[] preds;
	delete[] normsq;
	delete[] normsq_search;
	delete[] b;
	delete[] b0;
	delete[] upd_t;
	return 0;
}




int StochProxMKL::mkl_batch_linear_l2svm()
{
	int l = prob->l;
	int n = svm_param->B;
    int B = n;
	int i,j,k, s, p,iter = 0;//,
	double C, d, G;
 

	int max_iter = 100;

	double temp = 0;
	int active_size = l;

	// PG: projected gradient, for shrinking and stopping
	double PG;
	double PGmax_old = INF;
	double PGmin_old = -INF;
	double PGmax_new, PGmin_new;

    p = num_kernel;
	double* K;
	double* preds = new double[p];
	double* normsq = new double[p];
	double* b0 = new double[p];
	double* b = new double[p];
	//double* w = new double[p*B];
	double pred;
	double upd;
	double btot;
	C = Cp;

	// default solver_type: L2LOSS_SVM_DUAL
	double diag_p = 1.0/Cp, diag_n = 1.0/Cn;
	double upper_bound_p = INF, upper_bound_n = INF;


	
    int m=l;
	double lambda = 1/(C*m);//*m
	for (k = p-1; k < p; k++)
	{
		//	wtTxt[k] = new double[m];
		for(i = 0; i < B; i++)
		{
			w[k*B+i] = 0;              
		}
		for (i = 0; i < m; i++)
			wtTxt[k][i] = 0.0;
	}

	for (k = 0; k < p; k++)
	{
		normsq[k] = 0.0;
	}
    double eta=1;
	double expcoef;
	double normsq_temp;
	while (iter < max_iter)
	{
		eta = eta0 / sqrt(float(iter+1));

		PGmax_new = -INF;
		PGmin_new = INF;

		for (i=0; i<active_size; i++)
		{
			int j = i+rand()%(active_size-i);
			swap(index[i], index[j]);
		}

		for (s=0;s<active_size;s++)
		{
			i = index[s];
			G = 0;
			int yi = y[i];
			pred = 0;
			for(k=0; k<p; k++)
			{
				feature_node *xi = prob->xsp[k][i];
				//double s_sigma = d_sigma[p];
				preds[k] = 0.0;
				while(xi->index!=-1)
				{
					preds[k] += w[k*n+xi->index-1]*(xi->value);
					xi++;
				}
				pred += preds[k];
			}
			G = pred*yi-1;

			

			PG = 0;
			if (iter>1)
			{
				if (G > PGmax_old)
				{
					active_size--;
					swap(index[s], index[active_size]);
					s--;
					continue;
				}
				else if (G < 0)
					PG = G;
			}
			if(iter>1)
			{
				if (G < PGmin_old)
				{
					active_size--;
					swap(index[s], index[active_size]);
					s--;
					continue;
				}
				else if (G > 0)
					PG = G;
			}
			else
				PG = G;

			PGmax_new = max(PGmax_new, PG);
			PGmin_new = min(PGmin_new, PG);

			double expywx = (1-y[i]*pred);
			expcoef = expywx;

			if(fabs(PG) > 1.0e-12)
			{
				for (k = 0; k < p; k++)
				{
					//printf("%lf\n", preds[k]);
					//alpha = wtTxt[k];
					//alpha[i] += y[i] *expcoef*eta;
					feature_node *xj = prob->xsp[k][i];
					while(xj->index!=-1)
					{
						w[k*B+xj->index-1] += y[i]*eta*expcoef*xj->value;
						xj++;
					}
					//K = KK[k]; K[ind] = K(xi,xi)=i*m+i
					//ind = i + m * i; 
					//||w+dw||^2 = ||w||^2 + ||dw||^2 + 2*w'dw
					// = ||w||^2 + ||dw||^2 + 2*y[i]*eta*\sum w_j*x_j 
					// = ||w||^2 + ||dw||^2 + 2*y[i]*eta*preds[k]
					normsq_temp = normsq[k];
					normsq[k] += expcoef*expcoef*eta * eta * QD[k][i] + 2*expcoef*eta*y[i]*preds[k];
					//printf("%lf\n", normsq[k]);
				}
				if(s%100==0)
				{
					for (k = 0; k < p; k++)
						b0[k] = sqrt(normsq[k]);	

					proximity_L1squared(p, b0, eta * lambda, b);
					//deal with w
					//proximal step
					for (k = 0; k < p; k++)
					{
						//printf("%lf\n", normsq[k]);
						normsq[k] = b[k] * b[k];
						upd = (b0[k] > 0)? b[k] / b0[k] : 1.0;
						//alpha = wtTxt[k];
						for (j=0;j<B;j++)
						{
							w[k*B+j] *= upd;
						}
						/*for (j = 0; j < m; j++)
						alpha[j] *= upd;	*/			
					}	
				}
			}
		}

		iter++;

		if(PGmax_new - PGmin_new <= eps)
		{
			if(active_size == l)
				break;
			else
			{
				active_size = l;
				//info("*"); info_flush();
				PGmax_old = INF;
				PGmin_old = -INF;
				continue;
			}
		}
		PGmax_old = PGmax_new;
		PGmin_old = PGmin_new;
		if (PGmax_old <= 0)
			PGmax_old = INF;
		if (PGmin_old >= 0)
			PGmin_old = -INF;
	}


	// calculate objective value
	btot = 0.0;
	for (k = 0; k < p; k++)
	{
		b0[k] = sqrt(normsq[k]);				
	/*	alpha = wtTxt[k];
		for (j = 0; j < m; j++)
			alpha[j] *= y[j];*/
		btot += b0[k];				
	}

	for (k = 0; k < p; k++)
	{
		betas[k] = b0[k] / btot;
	}
    //calculate_alpha()
	double loss=0;
	for (i=0; i<m; i++)
	{
		pred = 0.0;
		for (k = 0; k < p; k++)
		{
			preds[k] = 0.0;
			feature_node *xi = prob->xsp[k][i];
			while (xi->index!=-1)
			{
				preds[k] += w[k*B+xi->index-1]*xi->value;
				xi++;
			}
			pred += preds[k];
		}
		alpha_sum[i] = max(0.0,1-pred*y[i]);
		loss += alpha_sum[i];
	}

	ptr_fx = 0.5*btot*btot + C*loss;
	delete[] preds;
	delete[] normsq;
	delete[] b;
	delete[] b0;

	return 0;
}


//int StochProxMKL::mkl_batch_linear_l2svm()
//{
//	int i, j, t, k;
//	int B = svm_model->B;
//	int p = num_kernel;
//	int m = prob->l;
//	int iter;
//	int ind;
//	double eta;
//	double lambda = 1/(C*m);//*m
//	double* alpha;
//	double* K;
//	double* preds = new double[p];
//	double* normsq = new double[p];
//	double* b0 = new double[p];
//	double* b = new double[p];
//	//double* w = new double[p*B];
//	double pred;
//	double upd;
//	double btot;
//	double C = Cp;
//    float expcoef = 0.0;
//	srand ( time(NULL));
//	//wtTxt = new (double*)[p];	
//	for (k = 0; k < p; k++)
//	{
//		//	wtTxt[k] = new double[m];
//		for(i = 0; i < B; i++)
//		{
//			w[k*B+i] = 0;              
//		}
//		for (i = 0; i < m; i++)
//			wtTxt[k][i] = 0.0;
//	}
//
//	for (k = 0; k < p; k++)
//	{
//		normsq[k] = 0.0;
//	}
//
//	//for (k = 0; k < p; k++)
//	//{
//	//	//	wtTxt[k] = new double[m];
//	//	for(i = 0; i < B; i++)
//	//	{
//	//		w[k*B+i] = 0;              
//	//	}
//	//}
//    //numEpochs = 3;
//	double normsq_temp;
//	for (t = 0; t < m * numEpochs; t++)
//	{
//		iter = t / m;
//		//printf("%d... ", iter+1);
//
//		eta = eta0 / sqrt(float(t+1));
//
//		i = rand() % m; //t % m; 
//
//		//printf("%d\n", i);
//
//		pred = 0.0;
//	
//		for (k = 0; k < p; k++)
//		{
//			
//			//alpha = wtTxt[k];
//			
//			preds[k] = 0.0;
//			feature_node *xi = prob->xsp[k][i];
//
//			while (xi->index!=-1)
//			{
//				preds[k] += w[k*B+xi->index-1]*xi->value;
//				xi++;
//			}
//			pred += preds[k];
//		}
//
//		//if (y[i]*pred < 1.0) for hinge loss only y[i]*pred < 1.0 we have sub-gradient
//		//but for logistic regression, we have
//		//calculate exp(-y_i*w'*x)/(1+exp(-y_i*w'*x))*expcoef
//		double expywx = (1-y[i]*pred);
//        expcoef = expywx;
//		if (y[i]*pred < 1.0)
//		{
//			for (k = 0; k < p; k++)
//			{
//				//printf("%lf\n", preds[k]);
//				//alpha = wtTxt[k];
//				//alpha[i] += y[i] *expcoef*eta;
//				feature_node *xj = prob->xsp[k][i];
//				while(xj->index!=-1)
//				{
//					w[k*B+xj->index-1] += y[i]*eta*expcoef*xj->value;
//					xj++;
//				}
//				//K = KK[k]; K[ind] = K(xi,xi)=i*m+i
//				//ind = i + m * i; 
//				//||w+dw||^2 = ||w||^2 + ||dw||^2 + 2*w'dw
//				// = ||w||^2 + ||dw||^2 + 2*y[i]*eta*\sum w_j*x_j 
//				// = ||w||^2 + ||dw||^2 + 2*y[i]*eta*preds[k]
//				normsq_temp = normsq[k];
//				normsq[k] += expcoef*expcoef*eta * eta * QD[k][i] + 2*expcoef*eta*y[i]*preds[k];
//				//printf("%lf\n", normsq[k]);
//			}
//		}
//     
//        if(t%100==0)
//		{
//			for (k = 0; k < p; k++)
//				b0[k] = sqrt(normsq[k]);	
//
//			proximity_L1squared(p, b0, eta * lambda, b);
//			//deal with w
//			//proximal step
//			for (k = 0; k < p; k++)
//			{
//				//printf("%lf\n", normsq[k]);
//				normsq[k] = b[k] * b[k];
//				upd = (b0[k] > 0)? b[k] / b0[k] : 1.0;
//				//alpha = wtTxt[k];
//				for (j=0;j<B;j++)
//				{
//					w[k*B+j] *= upd;
//				}
//				/*for (j = 0; j < m; j++)
//				alpha[j] *= upd;	*/			
//			}	
//		}
//		//printf("\n");
//	}
//
//	//calculate obj
//    
//	btot = 0.0;
//	for (k = 0; k < p; k++)
//	{
//		b0[k] = sqrt(normsq[k]);				
//	/*	alpha = wtTxt[k];
//		for (j = 0; j < m; j++)
//			alpha[j] *= y[j];*/
//		btot += b0[k];				
//	}
//
//	for (k = 0; k < p; k++)
//	{
//		betas[k] = b0[k] / btot;
//	}
//    //calculate_alpha()
//	double loss=0;
//	for (i=0; i<m; i++)
//	{
//		pred = 0.0;
//		for (k = 0; k < p; k++)
//		{
//			preds[k] = 0.0;
//			feature_node *xi = prob->xsp[k][i];
//			while (xi->index!=-1)
//			{
//				preds[k] += w[k*B+xi->index-1]*xi->value;
//				xi++;
//			}
//			pred += preds[k];
//		}
//		alpha_sum[i] = max(0.0,1-pred*y[i]);
//		loss += alpha_sum[i];
//	}
//
//	ptr_fx = 0.5*btot*btot + C*loss;
//	delete[] preds;
//	delete[] normsq;
//	delete[] b;
//	delete[] b0;
//
//	return 0;
//}

//int StochProxMKL::mkl_online_linear()
//{
//	int i, j, t, k;
//	int B = svm_model->B;
//	int p = num_kernel;
//	int m = prob->l;
//	int iter;
//	int ind;
//	double eta;
//	double lambda = 1/(C*m);
//	double* alpha;
//	double* K;
//	double* preds = new double[p];
//	double* normsq = new double[p];
//	double* b0 = new double[p];
//	double* b = new double[p];
//	//double* w = new double[p*B];
//	double pred;
//	double upd;
//	double btot;
//	double C = Cp;
//	float expcoef = 0.0;
//	srand ( time(NULL));
//	//wtTxt = new (double*)[p];	
//	for (k = 0; k < p; k++)
//	{
//		//	wtTxt[k] = new double[m];
//		for(i = 0; i < B; i++)
//		{
//			w[k*B+i] = 0;              
//		}
//		for (i = 0; i < m; i++)
//			wtTxt[k][i] = 0.0;
//	}
//
//	for (k = 0; k < p; k++)
//	{
//		normsq[k] = 0.0;
//	}
//
//	for (k = 0; k < p; k++)
//	{
//		//	wtTxt[k] = new double[m];
//		for(i = 0; i < B; i++)
//		{
//			w[k*B+i] = 0;              
//		}
//	}
//	//numEpochs = 3;
//	double normsq_temp;
//	for (t = 0; t < m * numEpochs; t++)
//	{
//		iter = t / m;
//		if (t>m * 2)
//		{
//			t = t;
//		}
//		//printf("%d... ", iter+1);
//
//		eta = eta0 / sqrt(float(t+1));
//
//		i = rand() % m; //t % m; 
//
//		//printf("%d\n", i);
//
//		pred = 0.0;
//
//		for (k = 0; k < p; k++)
//		{
//
//			alpha = wtTxt[k];
//
//			preds[k] = 0.0;
//			feature_node *xi = prob->xsp[k][i];
//
//			while (xi->index!=-1)
//			{
//				preds[k] += w[k*B+xi->index-1]*xi->value;
//				xi++;
//			}
//			pred += preds[k];
//		}
//
//		//if (y[i]*pred < 1.0) for hinge loss only y[i]*pred < 1.0 we have sub-gradient
//		//but for logistic regression, we have
//		//calculate exp(-y_i*w'*x)/(1+exp(-y_i*w'*x))
//		//double expywx = exp(-y[i]*pred);*expcoef
//		//expcoef = expywx/(1+expywx);
//		if (y[i]*pred < 1.0)
//		{
//			for (k = 0; k < p; k++)
//			{
//				//printf("%lf\n", preds[k]);
//				alpha = wtTxt[k];
//				alpha[i] += y[i] * eta;
//				feature_node *xj = prob->xsp[k][i];
//				while(xj->index!=-1)
//				{
//					w[k*B+xj->index-1] += y[i]*eta*xj->value;
//					xj++;
//				}
//				//K = KK[k]; K[ind] = K(xi,xi)=i*m+i
//				//ind = i + m * i; 
//				//||w+dw||^2 = ||w||^2 + ||dw||^2 + 2*w'dw
//				// = ||w||^2 + ||dw||^2 + 2*y[i]*eta*\sum w_j*x_j 
//				// = ||w||^2 + ||dw||^2 + 2*y[i]*eta*preds[k]
//				normsq_temp = normsq[k];
//				normsq[k] += eta * eta * QD[k][i] + 2*eta*y[i]*preds[k];
//				if(_isnan(normsq[k]) )
//				{
//					t = t;
//				}
//				if(normsq[k]<0 )
//				{
//					t = t;
//				}
//				//printf("%lf\n", normsq[k]);
//			}
//		}
//
//
//		for (k = 0; k < p; k++)
//			b0[k] = sqrt(normsq[k]);	
//
//		proximity_L1squared(p, b0, eta * lambda, b);
//		//deal with w
//		//proximal step
//		for (k = 0; k < p; k++)
//		{
//			//printf("%lf\n", normsq[k]);
//			normsq[k] = b[k] * b[k];
//			upd = (b0[k] > 0)? b[k] / b0[k] : 1.0;
//			alpha = wtTxt[k];
//			for (j=0;j<B;j++)
//			{
//				w[k*B+j] *= upd;
//			}
//			for (j = 0; j < m; j++)
//				alpha[j] *= upd;				
//		}			
//		//printf("\n");
//	}
//
//	//calculate obj
//
//	btot = 0.0;
//	for (k = 0; k < p; k++)
//	{
//		b0[k] = sqrt(normsq[k]);				
//		alpha = wtTxt[k];
//		for (j = 0; j < m; j++)
//			alpha[j] *= y[j];
//		btot += b0[k];				
//	}
//
//	for (k = 0; k < p; k++)
//	{
//		betas[k] = b0[k] / btot;
//	}
//	//calculate_alpha()
//	for (i=0; i<m; i++)
//	{
//		alpha_sum[i] = 0;
//		for (k = 0; k < p; k++)
//		{
//			alpha_sum[i] += betas[k]* wtTxt[k][i];
//		}
//	}
//	ptr_fx = 0;
//	delete[] preds;
//	delete[] normsq;
//	delete[] b;
//	delete[] b0;
//
//	//for (k = 0; k < p; k++)
//	//{
//	//	delete[] wtTxt[k];
//	//}
//	//delete[] wtTxt;
//
//	return 0;
//}
void StochProxMKL::return_model()
{
	svm_model->mkl_obj = ptr_fx;
	int i = 0;
	for (i=0; i<num_kernel; i++ )
	{
		svm_model->sigma[i] = betas[i];
	}

	veccpy(&svm_model->w[0], &w[0], w_size);

	for (i=0;i<prob->l;i++)
	{
		svm_model->alpha[i] = alpha_sum[i];
	}
}
