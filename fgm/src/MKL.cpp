#include "MKL.h"
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "float.h"


#define INF HUGE_VAL

#undef GETI
#define GETI(i) (y[i]+1)



#if 1
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
#else
static void info(char *fmt,...) {}
static void info_flush() {}
#endif

#ifndef min
template <class T> inline T min(T x,T y) { return (x<y)?x:y; }
#endif
#ifndef max
template <class T> inline T max(T x,T y) { return (x>y)?x:y; }
#endif

template <class T> static inline void swap(T& x, T& y) { T t=x; x=y; y=t; }

void MKL::MKL_allocate()
{

	index		= new int[l];		
	alpha		= new double[l_alpha]; // store alpha
	y			= new schar[l];	
	w			= new double[max_w_size]; 
	QD			= new double[l];       //Q_ii for each subkernel
}

MKL::~MKL()
{   
	delete [] index;
	delete [] alpha;
	delete [] y;
	delete [] w;

	delete [] QD;
}

void MKL::warm_set_model(int num_kernel_)
{
	num_kernel	= num_kernel_;
	w_size		= num_kernel*param->B;
	for(int i=0; i<w_size; i++)
		w[i] = 0;

	if (num_kernel == 2)
	{
		model_->sigma[0] = 0.5;
		model_->sigma[1] = 0.5;
	}
	else
	{
		model_->sigma[num_kernel-1] = 0.0;
	}
	//for (int i = 0; i < num_kernel_; i++)
	//{
	//	model_->sigma[i] = 1.0/num_kernel_;
	//}
}


void MKL::MKL_init()
{
	Cn = Cp = param->C;

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
		index[i] = i;
	}
}




void MKL::reset_model()
{
	model_->mkl_obj = mkl_obj;
	int i = 0;
	if (param->solver_type == LRFGM)
	{
		for (i=0;i<prob->l;i++)
		{
			model_->alpha[i] = alpha[2*i];
		}
	}else
	{
		for (i=0;i<prob->l;i++)
		{
			model_->alpha[i] = alpha[i];
		}
	}
	//for (i=0; i<num_kernel; i++ )
	//{
	//	svm_model->sigma[i] = lambda[i];
	//}
	//for (i = 0; i<w_size; i++)
	//{
	//	model_->w[i] = w[i];
	//}
}

void MKL::reset_model(int w_size_temp)
{
	model_->mkl_obj = mkl_obj;
	int i = 0;
	if (param->solver_type == LRFGM)
	{
		for (i=0;i<prob->l;i++)
		{
			model_->alpha[i] = alpha[2*i];
		}
	}else
	{
		for (i=0;i<prob->l;i++)
		{
			model_->alpha[i] = alpha[i];
		}
	}
	for (i = 0; i<w_size_temp; i++)
	{
		model_->w[i] = w[i];
	}
}

void MKL::pure_train_one()
{
	switch(param->solver_type)
	{
	//case L2LOSS_SVM_DUAL:
	//	//missing here
	//	break;
	//case L1LOSS_SVM_DUAL:
	//	//missing here
	//	break;
	case SVMFGM:
		linear_solver_svc();
		break;
	case PSVMFGM:
		//coming soon;
		break;
	case MINLR:
		//coming soon;
		break;
	case LRFGM:
        linear_solver_lr();
		break;
	default:
		fprintf(stderr, "Error: unknown solver_type\n");
		break;
	}
}


void MKL::train_one(double *w, double* alpha_, double *sigma_)
{
	switch(param->solver_type)
	{
	//case L2LOSS_SVM_DUAL:
	//	//missing here
	//	//solve_linear_c_svc(prob, w, alpha, eps, Cp, Cn, L2LOSS_SVM_DUAL,t_d_set, d_sigma, iter);
	//	break;
	//case L1LOSS_SVM_DUAL:
	//	//missing here
	//	//solve_linear_c_svc(prob, w, alpha, eps, Cp, Cn, L2LOSS_SVM_DUAL,t_d_set, d_sigma, iter);
	//	break;
	case SVMFGM:
		mkl_svm_train(w, alpha_, sigma_);
		break;
	case LRFGM:
        mkl_lr_train(w, alpha_, sigma_);
		break;
	default:
		fprintf(stderr, "Error: unknown solver_type\n");
		break;
	}
}

//when there is only one kernel
void MKL::linear_solver_svc()
{
	int l = prob->l;
	int n = param->B;


	int i, s, p,iter = 0;//,
	double C, d, G;

	int max_iter = 1000;
	double temp = 0;
	int active_size = l;

	// PG: projected gradient, for shrinking and stopping
	double PG;
	double PGmax_old = INF;
	double PGmin_old = -INF;
	double PGmax_new, PGmin_new;

	// default solver_type: L2LOSS_SVM_DUAL
	double diag_p = 1.0/Cp, diag_n = 1.0/Cn;
	double upper_bound_p = INF, upper_bound_n = INF;

	// w: 1: bias, n:linear

	if(param->solver_type == L2R_L1LOSS_SVC_DUAL)
	{
		diag_p = 0; diag_n = 0;
		upper_bound_p = Cp; upper_bound_n = Cn;
	}

	for(i=0; i<l; i++)
	{

		alpha[i] = 0;
		if(prob->y[i] > 0)
		{
			QD[i] = diag_p;
		}
		else
		{
			QD[i] = diag_n;
		}

		feature_node *xi = prob->xsp[0][i];
		while(xi->index!=-1)
		{
			{
				QD[i] += (xi->value)*(xi->value);
			}
			xi++;
		}

		index[i] = i;
	}

	for(i=0; i<w_size; i++)
	{
		w[i] = 0;
	}

	while (iter < max_iter)
	{
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
			schar yi = y[i];

			for(p=0; p<num_kernel; p++)
			{
				feature_node *xi = prob->xsp[p][i];
				while(xi->index!=-1)
				{
					G += w[p*n+xi->index-1]*(xi->value);
					xi++;
				}
			}
			G = G*yi-1;

			if(y[i] == 1)
			{
				C = upper_bound_p; 
				G += alpha[i]*diag_p; 
			}
			else 
			{
				C = upper_bound_n;
				G += alpha[i]*diag_n; 
			}

			PG = 0;
			if (alpha[i] == 0)
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
			else if (alpha[i] == C)
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

			if(fabs(PG) > 1.0e-12)
			{
				double alpha_old = alpha[i];
				alpha[i] = min(max(alpha[i] - G/QD[i], 0.0), C);
				d = (alpha[i] - alpha_old)*yi;

				for(p=0; p<num_kernel; p++)
				{
					feature_node *xi = prob->xsp[p][i];
					while(xi->index!=-1)
					{
						w[p*n+xi->index-1] += d*(xi->value);
						xi++;
					}
				}
			}
		}

		iter++;
		if(iter % 10 == 0)
		{
			//info("."); 
			info_flush();
		}

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

	double v = 0;
	double tmp = 0;
	int nSV = 0;


	tmp = 0;
	for(i=0; i<w_size; i++)
	{	
		tmp += w[i]*w[i];					
	}
	v += tmp/2;


	for(i=0; i<l; i++)
	{
		if (y[i] == 1)
			v += alpha[i]*(alpha[i]*diag_p - 2); 
		else
			v += alpha[i]*(alpha[i]*diag_n - 2);
		if(alpha[i] > 0)
			++nSV;
	}
    mkl_obj = -v/2;
	//info("Objective value = %lf\n",v/2);
	//info("nSV = %d\n",nSV);
}


//when there is only one kernel
void MKL::linear_solver_svc_match_pursuit(int w_size_temp)
{
	int l = prob->l;
	int n = param->B;
    int w_size = w_size_temp;

	int i, s, p,iter = 0;//,
	double C, d, G;

	int max_iter = 1000;
	double temp = 0;
	int active_size = l;

	// PG: projected gradient, for shrinking and stopping
	double PG;
	double PGmax_old = INF;
	double PGmin_old = -INF;
	double PGmax_new, PGmin_new;

	// default solver_type: L2LOSS_SVM_DUAL
	double diag_p = 1.0/Cp, diag_n = 1.0/Cn;
	double upper_bound_p = INF, upper_bound_n = INF;

	// w: 1: bias, n:linear

	if(param->solver_type == L2R_L1LOSS_SVC_DUAL)
	{
		diag_p = 0; diag_n = 0;
		upper_bound_p = Cp; upper_bound_n = Cn;
	}

	for(i=0; i<l; i++)
	{

		alpha[i] = 0;
		if(prob->y[i] > 0)
		{
			QD[i] = diag_p;
		}
		else
		{
			QD[i] = diag_n;
		}

		feature_node *xi = prob->xsp[0][i];
		while(xi->index!=-1)
		{
			{
				QD[i] += (xi->value)*(xi->value);
			}
			xi++;
		}

		index[i] = i;
	}

	for(i=0; i<w_size; i++)
	{
		w[i] = 0;
	}

	while (iter < max_iter)
	{
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
			schar yi = y[i];
            
			num_kernel = 1;
			for(p=0; p<num_kernel; p++)
			{
				feature_node *xi = prob->xsp[p][i];
				while(xi->index!=-1)
				{
					G += w[p*n+xi->index-1]*(xi->value);
					xi++;
				}
			}
			G = G*yi-1;

			if(y[i] == 1)
			{
				C = upper_bound_p; 
				G += alpha[i]*diag_p; 
			}
			else 
			{
				C = upper_bound_n;
				G += alpha[i]*diag_n; 
			}

			PG = 0;
			if (alpha[i] == 0)
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
			else if (alpha[i] == C)
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

			if(fabs(PG) > 1.0e-12)
			{
				double alpha_old = alpha[i];
				alpha[i] = min(max(alpha[i] - G/QD[i], 0.0), C);
				d = (alpha[i] - alpha_old)*yi;

				for(p=0; p<num_kernel; p++)
				{
					feature_node *xi = prob->xsp[p][i];
					while(xi->index!=-1)
					{
						w[p*n+xi->index-1] += d*(xi->value);
						xi++;
					}
				}
			}
		}

		iter++;
		if(iter % 10 == 0)
		{
			//info("."); 
			info_flush();
		}

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

	double v = 0;
	double tmp = 0;
	int nSV = 0;


	tmp = 0;
	for(i=0; i<w_size; i++)
	{	
		tmp += w[i]*w[i];					
	}
	v += tmp/2;

    
	for(i=0; i<l; i++)
	{
		if (y[i] == 1)
			v += alpha[i]*(alpha[i]*diag_p - 2); 
		else
			v += alpha[i]*(alpha[i]*diag_n - 2);
		if(alpha[i] > 0)
			++nSV;
	}
	mkl_obj = -v/2;

	//info("Objective value = %lf\n",v/2);
	//info("nSV = %d\n",nSV);
}


void MKL::mkl_svm_train(double *w, double* alpha_, double *d_sigma)
{
	int l = prob->l;
	int n = param->B;

	int i, s, p,iter = 0;//,
	double C, d, G;

	int max_iter = 1000;

	double temp = 0;
	int active_size = l;

	// PG: projected gradient, for shrinking and stopping
	double PG;
	double PGmax_old = INF;
	double PGmin_old = -INF;
	double PGmax_new, PGmin_new;



	// default solver_type: L2LOSS_SVM_DUAL
	double diag_p = 1.0/Cp, diag_n = 1.0/Cn;
	double upper_bound_p = INF, upper_bound_n = INF;

	if(param->solver_type == L2R_L1LOSS_SVC_DUAL)
	{
		diag_p = 0; diag_n = 0;
		upper_bound_p = Cp; upper_bound_n = Cn;
	}

	for(i=0; i<l; i++)
	{

		alpha[i] = 0;
		if(prob->y[i] > 0)
		{
			QD[i] = diag_p;
		}
		else
		{
			QD[i] = diag_n;
		}
		for(p=0; p<num_kernel; p++)
		{
			feature_node *xi = prob->xsp[p][i];

			while(xi->index!=-1)
			{
				{
					QD[i] += (xi->value)*(xi->value)*d_sigma[p];
				}
				xi++;
			}
		}
		index[i] = i;
	}


	for(i=0; i<w_size; i++)
	{
		w[i] = 0;
	}

	while (iter < max_iter)
	{
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
			schar yi = y[i];

			for(p=0; p<num_kernel; p++)
			{
				feature_node *xi = prob->xsp[p][i];
				double s_sigma = (d_sigma[p]);
				while(xi->index!=-1)
				{
					G += s_sigma*w[p*n+xi->index-1]*(xi->value);
					xi++;
				}
			}
			G = G*yi-1;

			if(y[i] == 1)
			{
				C = upper_bound_p; 
				G += alpha[i]*diag_p; 
			}
			else 
			{
				C = upper_bound_n;
				G += alpha[i]*diag_n; 
			}

			PG = 0;
			if (alpha[i] == 0)
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
			else if (alpha[i] == C)
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

			if(fabs(PG) > 1.0e-12)
			{
				double alpha_old = alpha[i];
				alpha[i] = min(max(alpha[i] - G/QD[i], 0.0), C);
				d = (alpha[i] - alpha_old)*yi;

				for(p=0; p<num_kernel; p++)
				{
					feature_node *xi = prob->xsp[p][i];
					while(xi->index!=-1)
					{
						w[p*n+xi->index-1] += d*(xi->value);
						xi++;
					}
				}
			}
		}

		iter++;
		if(iter % 10 == 0)
		{
			//info("."); 
			info_flush();
		}

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

	double v = 0;
	double tmp = 0;
	int nSV = 0;


	for(p=0; p< num_kernel; p++)
	{
		if(d_sigma[p] != 0)
		{
			tmp = 0;
			for(i=0; i<param->B; i++)
			{	
				tmp += w[param->B*p+i]*w[param->B*p+i];					
			}
			v += tmp*d_sigma[p];
		}
	}
	for(i=0; i<l; i++)
	{
		if (y[i] == 1)
			v += alpha[i]*(alpha[i]*diag_p - 2); 
		else
			v += alpha[i]*(alpha[i]*diag_n - 2);
		if(alpha[i] > 0)
			++nSV;
	}

	//info("Objective value = %lf\n",v/2);
	//info("nSV = %d\n",nSV);
	for(i=0; i<l; i++)
		alpha_[i] = alpha[i];


	alpha_[l] = -v/2;
	mkl_obj = alpha_[l];
}


// To support weights for instances, use GETI(i) (i)
void MKL::linear_solver_lr()
{
	int l = prob->l;
	int n = param->B;
	int i, s, iter = 0;
	int max_iter = 1000;
	int max_inner_iter = 100; // for inner Newton
	double innereps = 1e-2; 
	double innereps_min = min(1e-8, eps);
	double upper_bound[3] = {Cn, 0, Cp};

	for(i=0; i<w_size; i++)
	{
		w[i] = 0;
	}

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
		alpha[2*i] = min(0.001*upper_bound[GETI(i)], 1e-8);
		alpha[2*i+1] = upper_bound[GETI(i)] - alpha[2*i];

		QD[i] = 0;
		feature_node *xi = prob->xsp[0][i];
		while (xi->index != -1)
		{
			QD[i] += (xi->value)*(xi->value);
			w[xi->index-1] += y[i]*alpha[2*i]*xi->value;
			xi++;
		}
		index[i] = i;
	}

	while (iter < max_iter)
	{
		for (i=0; i<l; i++)
		{
			int j = i+rand()%(l-i);
			swap(index[i], index[j]);
		}
		int newton_iter = 0;
		double Gmax = 0;
		for (s=0; s<l; s++)
		{
			i = index[s];
			schar yi = y[i];
			double C = upper_bound[GETI(i)];
			double ywTx = 0, xisq = QD[i];
			feature_node *xi = prob->xsp[0][i];
			while (xi->index != -1)
			{
				ywTx += w[xi->index-1]*xi->value;
				xi++;
			}
			ywTx *= y[i];
			double a = xisq, b = ywTx;

			// Decide to minimize g_1(z) or g_2(z)
			int ind1 = 2*i, ind2 = 2*i+1, sign = 1;
			if(0.5*a*(alpha[ind2]-alpha[ind1])+b < 0) 
			{
				ind1 = 2*i+1;
				ind2 = 2*i;
				sign = -1;
			}

			//  g_t(z) = z*log(z) + (C-z)*log(C-z) + 0.5a(z-alpha_old)^2 + sign*b(z-alpha_old)
			double alpha_old = alpha[ind1];
			double z = alpha_old;
			if(C - z < 0.5 * C) 
				z = 0.1*z;
			double gp = a*(z-alpha_old)+sign*b+log(z/(C-z));
			Gmax = max(Gmax, fabs(gp));

			// Newton method on the sub-problem
			const double eta = 0.1; // xi in the paper
			int inner_iter = 0;
			while (inner_iter <= max_inner_iter) 
			{
				if(fabs(gp) < innereps)
					break;
				double gpp = a + C/(C-z)/z;
				double tmpz = z - gp/gpp;
				if(tmpz <= 0) 
					z *= eta;
				else // tmpz in (0, C)
					z = tmpz;
				gp = a*(z-alpha_old)+sign*b+log(z/(C-z));
				newton_iter++;
				inner_iter++;
			}

			if(inner_iter > 0) // update w
			{
				alpha[ind1] = z;
				alpha[ind2] = C-z;
				xi =  prob->xsp[0][i];
				while (xi->index != -1)
				{
					w[xi->index-1] += sign*(z-alpha_old)*yi*xi->value;
					xi++;
				}  
			}
		}

		iter++;
		if(iter % 10 == 0)
			info(".");

		if(Gmax < eps) 
			break;

		if(newton_iter < l/10) 
			innereps = max(innereps_min, 0.1*innereps);

	}

	//printf("\noptimization finished, #iter = %d\n",iter);
	if (iter >= max_iter)
		info("\nWARNING: reaching max number of iterations\nUsing -s 0 may be faster (also see FAQ)\n\n");
    info("FGM-LR\n");
	// calculate objective value
	//for (i=0;i<prob->l;i++)
	//{
	//	mexPrintf("%f\n",alpha[i]);
	//}
	double v = 0;
	for(i=0; i<w_size; i++)
		v += w[i] * w[i];
	v *= 0.5;
	for(i=0; i<l; i++)
		v += alpha[2*i] * log(alpha[2*i]) + alpha[2*i+1] * log(alpha[2*i+1]);

	double C = upper_bound[GETI(0)];
	v -= C * prob->l * log(C);
    mkl_obj = v;
}


void MKL::mkl_lr_train(double *w, double* alpha_, double *d_sigma)
{
	// initial vaule
	int i, s, iter = 0;
	int n = param->B;
	double *QD = new double[l];
	int max_iter = 1000;
	int max_inner_iter = 100; // for inner Newton
	double innereps = 1e-2; 
	double innereps_min = min(1e-8, eps);
	double upper_bound[3] = {Cn, 0, Cp};
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
		alpha[2*i] = min(0.001*upper_bound[GETI(i)], 1e-8);
		alpha[2*i+1] = upper_bound[GETI(i)] - alpha[2*i];

		QD[i] = 0;

		for(p=0; p<num_kernel; p++)
		{
			feature_node *xi = prob->xsp[p][i];
			while(xi->index!=-1)
			{

				QD[i] += (xi->value)*(xi->value)*d_sigma[p];
				w[p*n+xi->index-1] += y[i] * alpha[2*i] * xi->value;
				xi++;
			}
		}
		index[i] = i;
	}

	while (iter < max_iter)
	{
		for (i=0; i<l; i++)
		{
			int j = i+rand()%(l-i);
			swap(index[i], index[j]);
		}
		int newton_iter = 0;
		double Gmax = 0;
		for (s=0; s<l; s++)
		{
			i = index[s];
			schar yi = y[i];
			double C = upper_bound[GETI(i)];
			double ywTx = 0, xisq = QD[i];

			for(p=0; p<num_kernel; p++)
			{
				feature_node *xi = prob->xsp[p][i];
				while (xi->index != -1)
				{
					ywTx += d_sigma[p]*w[p*n+xi->index-1] * xi->value;
					xi++;
				}
			}

			/*feature_node *xi = prob->x[i];
			while (xi->index != -1)
			{
				ywTx += w[xi->index-1]*xi->value;
				xi++;
			}*/
			ywTx *= y[i];
			double a = xisq, b = ywTx;

			// Decide to minimize g_1(z) or g_2(z)
			int ind1 = 2*i, ind2 = 2*i+1, sign = 1;
			if(0.5*a*(alpha[ind2]-alpha[ind1])+b < 0) 
			{
				ind1 = 2*i+1;
				ind2 = 2*i;
				sign = -1;
			}

			//  g_t(z) = z*log(z) + (C-z)*log(C-z) + 0.5a(z-alpha_old)^2 + sign*b(z-alpha_old)
			double alpha_old = alpha[ind1];
			double z = alpha_old;
			if(C - z < 0.5 * C) 
				z = 0.1*z;
			double gp = a*(z-alpha_old)+sign*b+log(z/(C-z));
			Gmax = max(Gmax, fabs(gp));
			//printf("gradient is %f\n",Gmax);
			// Newton method on the sub-problem
			const double eta = 0.1; // xi in the paper
			int inner_iter = 0;
			while (inner_iter <= max_inner_iter) 
			{
				if(fabs(gp) < innereps)
					break;
				double gpp = a + C/(C-z)/z;
				double tmpz = z - gp/gpp;
				if(tmpz <= 0) 
					z *= eta;
				else // tmpz in (0, C)
					z = tmpz;
				gp = a*(z-alpha_old)+sign*b+log(z/(C-z));
				newton_iter++;
				inner_iter++;
			}

			if(inner_iter > 0) // update w
			{
				alpha[ind1] = z;
				alpha[ind2] = C-z;
                double d = sign*(z-alpha_old)*yi;
				for(p=0; p<num_kernel; p++)
				{
					feature_node *xi = prob->xsp[p][i];
					while (xi->index != -1)
					{
						w[p*n+xi->index-1] += d*xi->value;
						xi++;
					}  
				}
			}
		}

		iter++;
		//if(iter % 10 == 0)
			//info(".");

		if(Gmax < eps) 
			break;

		if(newton_iter < l/10) 
			innereps = max(innereps_min, 0.1*innereps);

	}

	//info("\noptimization finished, #iter = %d\n",iter);
	if (iter >= max_iter)
		info("\nWARNING: reaching max number of iterations\nUsing -s 0 may be faster (also see FAQ)\n\n");

	// calculate objective value
	
	double v = 0;
	for(i=0; i<w_size; i++)
		v += w[i] * w[i];
	v *= 0.5;
	for(i=0; i<l; i++)
	{
		alpha_[i] = alpha[2*i];
		v += alpha[2*i] * log(alpha[2*i]) + alpha[2*i+1] * log(alpha[2*i+1]); 
	}		
	double C = upper_bound[GETI(0)];
	v -= C * prob->l * log(C);
	alpha_[l] = v;
	mkl_obj = v;


}





void MKL::SimpleMKL()
{

	int i,j;
	int nloop, loop, maxloop;
	nloop = 1;
	loop = 1;
	maxloop = 12;

	int l = prob->l;   
	int n = prob->n;
	int n_kernel = num_kernel;
	//  
	// calculate weighted C here only for two-class problem
	double *weighted_C = Malloc(double, 2);
	for(i=0; i<2; i++)
		weighted_C[i] = param->C;
	if (param->nr_weight>0)
	{
		for(i=0;i<2;i++)
			weighted_C[i] = param->weight[i];
	}

	//train svm with w alpha and sigma as input
	train_one(&model_->w[0], &model_->alpha[0], &model_->sigma[0]);

	double obj = model_->alpha[l];
	//grad
	double *grad = Malloc(double,n_kernel);
	for(i=0; i<n_kernel; i++)
	{
		grad[i] = 0;
		for(j=0; j<param->B; j++)
		{
			grad[i] += model_->w[i*param->B+j]*model_->w[i*param->B+j];
		}
		grad[i] *= -0.5;
	}

	while(loop==1 && maxloop > 0 && n_kernel > 1)
	{

		nloop++;
		// update

		//mexPrintf("Iter=%d\n",nloop);
		double old_obj = obj;
		double gold = (sqrt(double(5))+1)/2;

		// copy sigma_new
		double *sigma_new = Malloc(double,n_kernel);
		for(i=0; i<n_kernel; i++)
			sigma_new[i] = model_->sigma[i];

		// normalize gradient
		double sum_grad = 0;
		for(i=0; i<n_kernel; i++)
			sum_grad += grad[i]*grad[i];
		double sqrt_grad = sqrt(sum_grad);
		for(i=0; i<n_kernel; i++)
			grad[i] /= sqrt_grad;

		// my_max
		double max_sigma = 0;
		int max_index = 0;

		my_max(&model_->sigma[0],n_kernel,&max_sigma,&max_index);
		double grad_temp=grad[max_index];
		for(i=0; i<n_kernel; i++)
			grad[i] -= grad_temp;

		double *desc = Malloc(double, n_kernel);
		double sum_desc = 0;

		for(i=0; i<n_kernel; i++)
		{
			if( model_->sigma[i] > 0 || grad[i] < 0)
			{
				desc[i] = -grad[i];
			}
			else
			{
				desc[i] = 0;
			}

			sum_desc += desc[i];
		}

		desc[max_index] = -sum_desc;

		double stepmin = 0;
		double costmin = old_obj;
		double costmax = 0;
		double stepmax = 0;

		my_soft_min(sigma_new, desc, n_kernel, &stepmax);

		double deltamax = stepmax;

		int flag = 1;
		if(stepmax == 0)
		{
			flag = 0;
		}

		if(flag == 1)
		{
			if (stepmax > 0.1)//update here
			{
				stepmax = 0.1;
				deltamax = stepmax;	
			}

			double *tmp_sigma = Malloc(double, n_kernel);
			double *tmp_w = Malloc(double, (param->B+1)*n_kernel);
			double *tmp_alpha = Malloc(double, l+1);

			while(costmax < costmin)
			{
				//cost_svm_class

				for(i=0; i<n_kernel; i++)
			 {
				 tmp_sigma[i] = sigma_new[i] + stepmax*desc[i];
			 }

				for(i=0; i< param->B*n_kernel; i++)
			 {
				 tmp_w[i] = model_->w[i];
			 }

				for(i=0; i<= l; i++)
			 {
				 tmp_alpha[i] = model_->alpha[i];
			 }


				train_one(&tmp_w[0], &tmp_alpha[0], &tmp_sigma[0]);

				costmax = tmp_alpha[l];

				if(costmax < costmin)
			 {
				 costmin = costmax;

				 for(i=0; i<n_kernel; i++)
				 {
					 sigma_new[i] = tmp_sigma[i];
					 model_->sigma[i] = tmp_sigma[i];
				 }

				 sum_desc = 0;
				 int fflag = 1;

				 for(i=0; i<n_kernel; i++)
				 {
					 if(sigma_new[i] > 1e-12 || desc[i] > 0)
						 ;
					 else
						 desc[i] = 0;

					 if( i != max_index)
						 sum_desc += desc[i];

					 if(desc[i] < 0)
						 fflag = 0;
				 }//for(i=0; i<n_kernel; i++)

				 desc[max_index] = -sum_desc;

				 for(i=0; i< param->B*n_kernel; i++)
					 model_->w[i] = tmp_w[i];

				 for(i=0; i<= l; i++)
					 model_->alpha[i] = tmp_alpha[i];

				 if(fflag)
				 {
					 stepmax = 0;
					 deltamax = 0;
				 }
				 else{
					 my_soft_min(sigma_new, desc, n_kernel, &stepmax);
					 deltamax = stepmax;
					 costmax = 0;
				 }//if(fflag)

				}//end of if(costmax < costmin)
			}//while(costmax < costmin)


			double *step = Malloc(double,4);
			step[0] = stepmin;
			step[1] = 0;
			step[2] = 0;
			step[3] = stepmax;

			double *cost = Malloc(double,4);
			cost[0] = costmin;
			cost[1] = 0;
			cost[2] = 0;
			cost[3] = costmax;

			double min_val;
			int min_inx;

			if(costmax < costmin){
				min_val = costmax;
				min_inx = 3;
		 }
			else{
				min_val = costmin;
				min_inx = 0;
		 }



			double *tmp_sigma_1 = Malloc(double, n_kernel);
			double *tmp_w_1 = Malloc(double, (param->B+1)*n_kernel);
			double *tmp_alpha_1 = Malloc(double, l+1);

			double *tmp_sigma_2 = Malloc(double, n_kernel);
			double *tmp_w_2 = Malloc(double, (param->B+1)*n_kernel);
			double *tmp_alpha_2 = Malloc(double, l+1);
			int stepLoop=0;
			while ((stepmax - stepmin) > 1e-1*fabs(deltamax) && stepmax > 1e-12 )
			{
				stepLoop = stepLoop+1;
				if (stepLoop>8)
				{
					break;
				}
				double stepmedr  = stepmin+(stepmax-stepmin)/gold;
				double stepmedl =  stepmin+(stepmedr-stepmin)/gold;  


				//cost_svm_class_1
				for(i=0; i<n_kernel; i++)
					tmp_sigma_1[i] = sigma_new[i] + stepmedr*desc[i];

				for(i=0; i< param->B*n_kernel; i++)
					tmp_w_1[i] = model_->w[i];

				for(i=0; i<= l; i++)
					tmp_alpha_1[i] = model_->alpha[i];

				// regroup_dt(prob->d_set, prob->n_kernel, tmp_sigma_1, model_->t_d_set);

				train_one(&tmp_w_1[0], &tmp_alpha_1[0], &tmp_sigma_1[0]);


				//cost_svm_class_2
				for(i=0; i<n_kernel; i++)
					tmp_sigma_2[i] = sigma_new[i] + stepmedl*desc[i];

				for(i=0; i< param->B*n_kernel; i++)
					tmp_w_2[i] = model_->w[i];

				for(i=0; i<= l; i++)
					tmp_alpha_2[i] = model_->alpha[i];


				train_one(&tmp_w_2[0], &tmp_alpha_2[0], &tmp_sigma_2[0]);
				//train_one(prob, param, tmp_w_2, tmp_alpha_2, 1, 1,&model_->y[0], tmp_sigma_2, n_kernel);

				step[0] = stepmin;
				step[1] = stepmedl;
				step[2] = stepmedr;
				step[3] = stepmax;

				cost[0] = costmin;
				cost[1] = tmp_alpha_2[l];
				cost[2] = tmp_alpha_1[l];
				cost[3] = costmax;


				my_min(cost, 4,&min_val,&min_inx);


				switch(min_inx)
				{
				case 0:
					stepmax = stepmedl;
					costmax = cost[1];
					for(i=0; i< param->B*n_kernel; i++)
						model_->w[i] = tmp_w_2[i];

					for(i=0; i<= l; i++)
						model_->alpha[i] = tmp_alpha_2[i];
					break;

				case 1:
					stepmax = stepmedr;
					costmax = cost[2];
					for(i=0; i< param->B*n_kernel; i++)
						model_->w[i] = tmp_w_1[i];

					for(i=0; i<= l; i++)
						model_->alpha[i] = tmp_alpha_1[i];
					break;

				case 2:
					stepmin = stepmedl;
					costmin = cost[1];
					for(i=0; i< param->B*n_kernel; i++)
						model_->w[i] = tmp_w_2[i];

					for(i=0; i<= l; i++)
						model_->alpha[i] = tmp_alpha_2[i];

					break;
				case 3:
					stepmin = stepmedr;
					costmin = cost[2];
					for(i=0; i< param->B*n_kernel; i++)
						model_->w[i] = tmp_w_1[i];

					for(i=0; i<= l; i++)
						model_->alpha[i] = tmp_alpha_1[i];
					break;
				}//switch(min_inx)
			}//while (stepmax - stepmin) > 1e-1*fabs(deltmax) && stepmax > 1e-12

			// assignment
			double step_size = step[min_inx];
			if(model_->alpha[l] < old_obj)
			{
				for(i=0; i<n_kernel; i++)
				{
					sigma_new[i] += step_size*desc[i];
					model_->sigma[i] = sigma_new[i];
				}
			}//if(model_->alpha[l] < old_obj)


			free(tmp_alpha_2);
			free(tmp_w_2);
			free(tmp_sigma_2);

			free(tmp_alpha_1);
			free(tmp_w_1);
			free(tmp_sigma_1);

			free(cost);
			free(step);

			free(tmp_alpha);
			free(tmp_w);
			free(tmp_sigma);
		}//if(flag)

		// condition
		double sigma_max;
		int sigma_max_inx;

		my_max(&model_->sigma[0],n_kernel,&sigma_max, &sigma_max_inx);
		if (sigma_max > 1e-12)
		{

			double sigma_sum = 0;
			for(i=0; i<n_kernel; i++)
			{
				if(model_->sigma[i] < 1e-12)
					model_->sigma[i] = 0;

				sigma_sum += model_->sigma[i];
			}

			for(i=0; i<n_kernel; i++)
				model_->sigma[i] /= sigma_sum;		
		}


		for(i=0; i<n_kernel; i++)
		{
			grad[i] = 0;
			for(j=0; j<param->B; j++)
				grad[i] += model_->w[i*param->B+j]*model_->w[i*param->B+j];
			grad[i] *= -0.5;
		}

		double min_grad = 0;
		double max_grad = 0;
		int ffflag = 1;

		for(i=0; i<n_kernel; i++)
		{
			if(model_->sigma[i] > 1e-8)
		 {
			 if(ffflag){
				 min_grad = grad[i];
				 max_grad = grad[i];
				 ffflag = 0;
			 }
			 else{
				 if(grad[i] < min_grad)
					 min_grad = grad[i];
				 if(grad[i] > max_grad)
					 max_grad = grad[i];
			 }
		 }
		}

		double KKTconstraint = fabs(min_grad - max_grad)/fabs(min_grad);

		double *tmp_grad = Malloc(double,n_kernel);
		for(i=0; i<n_kernel; i++)
			tmp_grad[i] = -grad[i];

		double max_tmp;
		int max_tmp_inx;
		my_max(tmp_grad, n_kernel, &max_tmp, &max_tmp_inx);

		double tmp_sum = 0;
		for(i=0; i<l; i++)
			tmp_sum += model_->alpha[i];

		double dual_gap = (model_->alpha[l]+max_tmp -  tmp_sum)/model_->alpha[l];

		if(KKTconstraint < 0.05 || fabs(dual_gap) < 0.01)
			loop = 0;


		free(tmp_grad); 
		free(desc);
		free(sigma_new);
		///////////////////////////////////////////
		if(nloop > maxloop)
		{
			loop = 0;
			break;
		}

	}//while loop && maxloop > 0
	free(weighted_C);
	free(grad);
}


