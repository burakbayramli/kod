#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "ProxMKL.h"
#include "stocha_prox_mkl.h"
#include "MKL.h"

#include "FGM.h"

#include "random.h"
#include <vector>
#include <malloc.h>
//#include "maxiheap.h"
//#include <algorithm>
//#include "mex.h"
//#include "shash.h"
//#include "miniheap.h"


#ifndef min
template <class T> inline T min(T x,T y) { return (x<y)?x:y; }
#endif
#ifndef max
template <class T> inline T max(T x,T y) { return (x>y)?x:y; }
#endif

#define Malloc(type,n) (type *)malloc((n)*sizeof(type))
#define INF HUGE_VAL
#define _CRTDBG_MAP_ALLOC

#if _MSC_VER
#include <crtdbg.h>
#endif

#if 1
static void info(const char *fmt, ...)
{
	va_list ap;
	va_start(ap, fmt);
	vprintf(fmt, ap);
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

void FGM::FGM_allocate()
{
	sub_x_space = Malloc(feature_node *, max_iteration);
	prob->xsp = Malloc(feature_node **, max_iteration);
	w_lin = Malloc(double, prob->n);
	QD = Malloc(float, prob->n);
	QD_count = Malloc(int, prob->n);
	w2b_B = Malloc(weight, 1 * param->B);
	w2b_temp = Malloc(weight, 3 * param->B);
	int i = 0;
	for (i = 0; i < max_iteration; i++)
	{

		prob->xsp[i] = Malloc(struct feature_node *, prob->l);
	}
}

FGM::~FGM()
{

	for (int i = 0; i < n_ITER - 1; i++)
	{
		free(sub_x_space[i]);
		free(prob->xsp[i]);
	}

	free(sub_x_space);
	free(prob->xsp);
	free(w2b_temp);
	free(w2b_B);
	free(w_lin);
	free(QD);
	free(QD_count);
}
void FGM::normalize()
{
	FILE *fp = fopen("D://tmingkui//new_train_normalized", "w");
	for (int i = 0; i < prob->l; i++)
	{
		fprintf(fp, "%d", prob->y[i]);
		feature_node *xi = prob->x[i];
		{
			while (xi->index != -1)
			{
				//QD_count[xi->index-1] += 1;
				xi->value = xi->value / QD[xi->index - 1];
				fprintf(fp, " %ld:%g", xi->index, xi->value);
				xi++;
			}
		}
		fprintf(fp, "\n");
	}
}
void FGM::FGM_init()
{
	int i = 0;
	for (i = 0; i < prob->l; i++)
	{
		alpha[i] = 1.0;
	}


	for (i = 0; i < param->B; i++)
	{
		w2b_temp[i].index1 = i;
		w2b_temp[i].index2 = -1;
		w2b_temp[i].value = 0;
	}

	//for(i=0; i<prob->n;i++)
	//{
	//	QD_count[i] = 1;
	//}
	//for(i=0;i<prob->l;i++)
	//{
	//	feature_node *xi = prob->x[i];
	//	{
	//		while (xi->index != -1)
	//		{
	//			QD_count[xi->index-1] += 1;
	//			///QD[xi->index-1] += xi->value*xi->value;
	//			xi++;
	//		}
	//	}
	//}


	if (param->fCRS == 0)
	{
		for (i = 0; i < prob->n; i++)
		{
			QD[i] = 0;
			QD_count[i] = 1;
			//QD_count[i]=0;
		}
		for (i = 0; i < prob->l; i++)
		{
			feature_node *xi = prob->x[i];
			{
				while (xi->index != -1)
				{
					//QD_count[xi->index-1] += 1;
					QD[xi->index - 1] += xi->value*xi->value;
					xi++;
				}
			}
		}
		for (i = 0; i < prob->n; i++)
		{
			//if (QD_count[i]>0)
			{
				//double r = double(QD_count[i])/prob->n;/sqrt(r);/QD_count[i]
				QD[i] = sqrt(QD[i]);
			}//else
		}
	}
	else //if -f 1
	{
		for (i = 0; i < prob->n; i++)
		{
			QD_count[i] = 1;
			QD[i] = 1.0;
		}
	}
	//normalize();
}


//calculate the final w by wighting 
void FGM::calculate_w2_poly()
{
	int i = 0;
	int j = 0;
	int k = 0; //for specified features
	int p = 0;
	int flag = 0;
	//initialize
	for (p = 0; p < prob->n_kernel; p++)
	{
		for (i = 0; i < param->B; i++)
		{
			svm_model->w2s[p*param->B + i].value = svm_model->sigma[p] * svm_model->w[p*param->B + i];
		}
	}

	p = 0;
	while (abs(svm_model->sigma[p]) <= 1e-4)// record when svm_model->sigma[p]!=0
	{
		p++;
	}
	int temp_p;
	temp_p = p;
	for (i = 0; i < param->B; i++)
	{
		svm_model->solution_->w_FGM[i].index1 = svm_model->w2s[p*param->B + i].index1;
		svm_model->solution_->w_FGM[i].index2 = svm_model->w2s[p*param->B + i].index2;
		svm_model->solution_->w_FGM[i].value = svm_model->w2s[p*param->B + i].value;
	}
	k = param->B;
	svm_model->solution_->w_FGM[k].index1 = -1;// ****
	j = 0;

	for (p = p + 1; p < prob->n_kernel - 1; p++) // match from the second to the last
	{
		if (abs(svm_model->sigma[p]) <= 1e-4)
		{
			continue;
		}
		else
		{
			for (i = 0; i < param->B; i++)// for each elements, the new elements should be matched.
			{
				j = 0;
				flag = 0;
				while (svm_model->solution_->w_FGM[j].index1 != -1)
				{
					if ((svm_model->w2s[p*param->B + i].index1 == svm_model->solution_->w_FGM[j].index1)
						&& (svm_model->w2s[p*param->B + i].index2 == svm_model->solution_->w_FGM[j].index2))
					{
						svm_model->solution_->w_FGM[j].value =
							svm_model->solution_->w_FGM[j].value + svm_model->w2s[p*param->B + i].value;
						flag++;
						break;
					}
					j++;
				}
				if (flag == 0)// not match
				{
					svm_model->solution_->w_FGM[j].index1 = svm_model->w2s[p*param->B + i].index1;
					svm_model->solution_->w_FGM[j].index2 = svm_model->w2s[p*param->B + i].index2;
					svm_model->solution_->w_FGM[j].value = svm_model->w2s[p*param->B + i].value;
					j++;
					svm_model->solution_->w_FGM[j].index1 = -1;
				}
			}
		}
	}
	j = 0;
	while (svm_model->solution_->w_FGM[j].index1 != -1)
	{
		j++;
	}
	svm_model->feature_pair = j;
	sort_w2b(svm_model->solution_->w_FGM, svm_model->feature_pair);
}
#undef GETI
#define GETI(i) (y[i]+1)
void FGM::solve_l2r_lr_dual_retrain(double eps,
	double Cp, double Cn, int solver_type, weight *w2, int feature_num, int bias)
{
	int l = prob->l;
	int n = feature_num;
	int w_size = feature_num;
	int i, s, iter = 0;
	double *xTx = new double[l];
	int max_iter = 1000;
	int *index = new int[l];
	double *alpha = new double[2 * l]; // store alpha and C - alpha
	schar *y = new schar[l];
	int max_inner_iter = 40; // for inner Newton
	double innereps = 1e-2;
	double innereps_min = min(1e-8, eps);
	double upper_bound[3] = { Cn, 0, Cp };
	double tmp_value;
	feature_node *xi;

	int j;
	for (i = 0; i < w_size; i++)
		w2[i].value = 0;
	for (i = 0; i < l; i++)
	{
		if (prob->y[i] > 0)
		{
			y[i] = +1;
		}
		else
		{
			y[i] = -1;
		}
		alpha[2 * i] = min(0.001*upper_bound[GETI(i)], 1e-8);
		alpha[2 * i + 1] = upper_bound[GETI(i)] - alpha[2 * i];

		xTx[i] = 0;
		j = 0;
		//xi = &prob->x[i][0];
		{
			while (w2[j].index1 != -1)
			{
				tmp_value = SearchSparseElement(&prob->xsp[w2[j].indexi][i][0], w2[j].indexj);
				w2[j].value += y[i] * alpha[2 * i] * tmp_value;
				xTx[i] += tmp_value *tmp_value;
				j++;
			}
		}
		index[i] = i;
	}

	while (iter < max_iter)
	{
		for (i = 0; i < l; i++)
		{
			int j = i + rand() % (l - i);
			swap(index[i], index[j]);
		}
		int newton_iter = 0;
		double Gmax = 0;
		for (s = 0; s < l; s++)
		{
			i = index[s];
			schar yi = y[i];
			double C = upper_bound[GETI(i)];
			double ywTx = 0, xisq = xTx[i];


			j = 0;
			xi = &prob->x[i][0];
			if (bias)
			{
				while (w2[j].index1 != -2)
				{
					tmp_value = SearchSparseElement(&prob->xsp[w2[j].indexi][i][0], w2[j].indexj);
					ywTx += w2[j].value*tmp_value;
					j++;
				}
				ywTx += w2[j].value*y[i];
			}
			else
			{
				while (w2[j].index1 != -1)
				{
					tmp_value = SearchSparseElement(&prob->xsp[w2[j].indexi][i][0], w2[j].indexj);
					ywTx += w2[j].value*tmp_value;
					j++;
				}
			}
			ywTx *= y[i];
			double a = xisq, b = ywTx;

			// Decide to minimize g_1(z) or g_2(z)
			int ind1 = 2 * i, ind2 = 2 * i + 1, sign = 1;
			if (0.5*a*(alpha[ind2] - alpha[ind1]) + b < 0)
			{
				ind1 = 2 * i + 1;
				ind2 = 2 * i;
				sign = -1;
			}

			//  g_t(z) = z*log(z) + (C-z)*log(C-z) + 0.5a(z-alpha_old)^2 + sign*b(z-alpha_old)
			double alpha_old = alpha[ind1];
			double z = alpha_old;
			if (C - z < 0.5 * C)
				z = 0.1*z;
			double gp = a*(z - alpha_old) + sign*b + log(z / (C - z));
			Gmax = max(Gmax, fabs(gp));

			// Newton method on the sub-problem
			const double eta = 0.1; // xi in the paper
			int inner_iter = 0;
			while (inner_iter <= max_inner_iter)
			{
				if (fabs(gp) < innereps)
					break;
				double gpp = a + C / (C - z) / z;
				double tmpz = z - gp / gpp;
				if (tmpz <= 0)
					z *= eta;
				else // tmpz in (0, C)
					z = tmpz;
				gp = a*(z - alpha_old) + sign*b + log(z / (C - z));
				newton_iter++;
				inner_iter++;
			}

			if (inner_iter > 20)
			{
				printf("inner_iter = %d\n", inner_iter);
			}

			if (inner_iter > 0) // update w
			{
				alpha[ind1] = z;
				alpha[ind2] = C - z;

				j = 0;
				xi = &prob->x[i][0];
				if (bias)
				{

				}
				else
				{
					while (w2[j].index1 != -1)
					{
						tmp_value = SearchSparseElement(&prob->xsp[w2[j].indexi][i][0], w2[j].indexj);
						w2[j].value += sign*(z - alpha_old)*yi*tmp_value;
						j++;
					}
				}

				//xi = prob->x[i];
				//while (xi->index != -1)
				//{
				//	w[xi->index-1] += sign*(z-alpha_old)*yi*xi->value;
				//	xi++;
				//}  
			}
			if (inner_iter > 20)
			{
				info("inner_iter = %d\n \n", inner_iter);
			}
		}

		iter++;
		if (iter % 10 == 0)
			info(".");

		if (Gmax < eps)
			break;

		if (newton_iter < l / 10)
			innereps = max(innereps_min, 0.1*innereps);

	}

	info("\noptimization finished, #iter = %d\n", iter);
	if (iter >= max_iter)
		info("\nWARNING: reaching max number of iterations\nUsing -s 0 may be faster (also see FAQ)\n\n");

	// calculate objective value

	double v = 0;
	for (i = 0; i < w_size; i++)
		v += w2[i].value * w2[i].value;
	v *= 0.5;
	for (i = 0; i < l; i++)
		v += alpha[2 * i] * log(alpha[2 * i]) + alpha[2 * i + 1] * log(alpha[2 * i + 1])
		- upper_bound[GETI(i)] * log(upper_bound[GETI(i)]);
	info("Objective value = %lf\n", v);

	delete[] xTx;
	delete[] alpha;
	delete[] y;
	delete[] index;
}



void FGM::svm_retrain(double eps,
	double Cp, double Cn, int solver_type, weight *w2, int feature_num, int bias)
{

	int l = prob->l;
	int n = feature_num;
	int w_size = feature_num;

	int i, s, iter = 0;//,
	double C, d, G;
	double *QD = new double[l];
	int max_iter = 1000;
	int *index = new int[l];
	double temp = 0;
	double *alpha = new double[l];
	schar *y = new schar[l];
	int active_size = l;

	// PG: projected gradient, for shrinking and stopping
	double PG;
	double PGmax_old = INF;
	double PGmin_old = -INF;
	double PGmax_new, PGmin_new;

	// default solver_type: L2LOSS_SVM_DUAL
	double diag_p = 0.5 / Cp, diag_n = 0.5 / Cn;
	double upper_bound_p = INF, upper_bound_n = INF;

	// w: 1: bias, n:linear, (n+1)n/2: quadratic
	double tmp_value;
	int j = 0;

	if (solver_type == L2R_L1LOSS_SVC_DUAL)
	{
		diag_p = 0; diag_n = 0;
		upper_bound_p = Cp; upper_bound_n = Cn;
	}

	for (i = 0; i < l; i++)
	{
		{
			alpha[i] = 0;
		}

		if (prob->y[i] > 0)
		{
			y[i] = +1;
			QD[i] = diag_p;
		}
		else
		{
			y[i] = -1;
			QD[i] = diag_n;
		}
		if (bias)// add bias here
		{
			QD[i] = QD[i] + 1;
		}
		j = 0;
		if (bias)
		{
			while (w2[j].index1 != -2)
			{
				tmp_value = SearchSparseElement(&prob->xsp[w2[j].indexi][i][0], w2[j].indexj);
				QD[i] += tmp_value *tmp_value;
				j++;
			}
		}
		else
		{
			while (w2[j].index1 != -1)
			{
				tmp_value = SearchSparseElement(&prob->xsp[w2[j].indexi][i][0], w2[j].indexj);
				QD[i] += tmp_value *tmp_value;
				j++;
			}
		}
		index[i] = i;
	}


	for (i = 0; i < w_size; i++)
	{
		w2[i].value = 0;
	}
	// int l_new = min(active_size,50000);
	while (iter < max_iter)
	{
		PGmax_new = -INF;
		PGmin_new = INF;

		for (i = 0; i < active_size; i++)
		{
			int j = i + rand() % (active_size - i);
			swap(index[i], index[j]);
		}
		//l_new = min(active_size,l);
		for (s = 0; s < active_size; s++)
		{
			i = index[s];
			G = 0;
			schar yi = y[i];

			j = 0;
			if (bias)
			{
				while (w2[j].index1 != -2)
				{
					tmp_value = SearchSparseElement(&prob->xsp[w2[j].indexi][i][0], w2[j].indexj);
					G += w2[j].value*tmp_value;
					j++;
				}
				G += w2[j].value*y[i];
			}
			else
			{
				while (w2[j].index1 != -1)
				{
					tmp_value = SearchSparseElement(&prob->xsp[w2[j].indexi][i][0], w2[j].indexj);
					G += w2[j].value*tmp_value;
					j++;
				}
			}
			G = G*yi - 1;

			if (y[i] == 1)
			{
				C = upper_bound_p;
				G += alpha[i] * diag_p;
			}
			else
			{
				C = upper_bound_n;
				G += alpha[i] * diag_n;
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

			if (fabs(PG) > 1.0e-12)
			{
				double alpha_old = alpha[i];
				alpha[i] = min(max(alpha[i] - G / QD[i], 0.0), C);
				d = (alpha[i] - alpha_old)*yi;

				j = 0;
				if (bias)
				{
					while (w2[j].index1 != -2)
					{
						tmp_value = SearchSparseElement(&prob->xsp[w2[j].indexi][i][0], w2[j].indexj);
						w2[j].value += d*tmp_value;
						j++;
					}
					w2[j].value += d * 1 * y[i];
				}
				else
				{
					while (w2[j].index1 != -1)
					{
						tmp_value = SearchSparseElement(&prob->xsp[w2[j].indexi][i][0], w2[j].indexj);
						w2[j].value += d*tmp_value;
						j++;
					}
				}

			}
		}

		iter++;
		if (iter % 10 == 0)
		{
			info(".");
			info_flush();
		}

		if (PGmax_new - PGmin_new <= eps)
		{
			if (active_size == l)
				break;
			else
			{
				active_size = l;
				printf("*");
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
	for (i = 0; i < w_size; i++)
	{
		tmp += w2[i].value*w2[i].value;
	}
	v += tmp;

	for (i = 0; i < l; i++)
	{
		if (y[i] == 1)
			v += alpha[i] * (alpha[i] * diag_p - 2);
		else
			v += alpha[i] * (alpha[i] * diag_n - 2);
		if (alpha[i] > 0)
			++nSV;
	}

	printf("Objective value = %lf\n", v / 2);
	printf("nSV = %d\n", nSV);


	delete[] QD;
	delete[] alpha;
	delete[] y;
	delete[] index;
}


double SearchSparseElement_r(feature_node *xi, int index)
{
	double node_value = 0;
	while (xi->index != -1)
	{
		if (xi->index == index + 1)
		{
			node_value = xi->value;
			break;
		}
		xi++;
	}
	return node_value;
}

//
// Interface functions
//
void svm_retrain_r(problem *prob, double eps,
	double Cp, double Cn, int solver_type, weight *w2, int feature_num, int bias)
{

	int l = prob->l;
	int n = feature_num;
	int w_size = feature_num;

	int i, s, iter = 0;//,
	double C, d, G;
	double *QD = new double[l];
	int max_iter = 100;
	int *index = new int[l];
	double temp = 0;
	double *alpha = new double[l];
	schar *y = new schar[l];
	int active_size = l;

	// PG: projected gradient, for shrinking and stopping
	double PG;
	double PGmax_old = INF;
	double PGmin_old = -INF;
	double PGmax_new, PGmin_new;

	// default solver_type: L2LOSS_SVM_DUAL
	double diag_p = 0.5 / Cp, diag_n = 0.5 / Cn;
	double upper_bound_p = INF, upper_bound_n = INF;

	// w: 1: bias, n:linear, (n+1)n/2: quadratic
	double tmp_value;
	int j = 0;
	feature_node *xi;
	if (solver_type == L2R_L1LOSS_SVC_DUAL)
	{
		diag_p = 0; diag_n = 0;
		upper_bound_p = Cp; upper_bound_n = Cn;
	}

	for (i = 0; i < l; i++)
	{
		{
			alpha[i] = 0;
		}

		if (prob->y[i] > 0)
		{
			y[i] = +1;
			QD[i] = diag_p;
		}
		else
		{
			y[i] = -1;
			QD[i] = diag_n;
		}
		if (bias)// add bias here
		{
			QD[i] = QD[i] + 1;
		}
		j = 0;
		xi = &prob->x[i][0];
		if (bias)
		{
			while (w2[j].index1 != -2)
			{
				tmp_value = SearchSparseElement_r(xi, w2[j].index1);
				QD[i] += tmp_value *tmp_value;
				j++;
			}
		}
		else
		{
			while (w2[j].index1 != -1)
			{
				tmp_value = SearchSparseElement_r(xi, w2[j].index1);
				QD[i] += tmp_value *tmp_value;
				j++;
			}
		}
		index[i] = i;
	}


	for (i = 0; i < w_size; i++)
	{
		w2[i].value = 0;
	}

	int l_new = min(prob->l, 20000);
	while (iter < max_iter)
	{
		PGmax_new = -INF;
		PGmin_new = INF;

		for (i = 0; i < active_size; i++)
		{
			int j = i + rand() % (active_size - i);
			swap(index[i], index[j]);
		}

		for (s = 0; s < l_new; s++)
		{
			i = index[s];
			G = 0;
			schar yi = y[i];

			j = 0;
			xi = &prob->x[i][0];
			if (bias)
			{
				while (w2[j].index1 != -2)
				{
					tmp_value = SearchSparseElement_r(xi, w2[j].index1);
					G += w2[j].value*tmp_value;
					j++;
				}
				G += w2[j].value*y[i];
			}
			else
			{
				while (w2[j].index1 != -1)
				{
					tmp_value = SearchSparseElement_r(xi, w2[j].index1);
					G += w2[j].value*tmp_value;
					j++;
				}
			}
			G = G*yi - 1;

			if (y[i] == 1)
			{
				C = upper_bound_p;
				G += alpha[i] * diag_p;
			}
			else
			{
				C = upper_bound_n;
				G += alpha[i] * diag_n;
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

			if (fabs(PG) > 1.0e-12)
			{
				double alpha_old = alpha[i];
				alpha[i] = min(max(alpha[i] - G / QD[i], 0.0), C);
				d = (alpha[i] - alpha_old)*yi;

				j = 0;
				xi = &prob->x[i][0];
				if (bias)
				{
					while (w2[j].index1 != -2)
					{
						tmp_value = SearchSparseElement_r(xi, w2[j].index1);
						w2[j].value += d*tmp_value;//dt[xi->index-1].value*dt[xi->index-1].value;
						j++;
					}
					w2[j].value += d * 1 * y[i];
				}
				else
				{
					while (w2[j].index1 != -1)
					{
						tmp_value = SearchSparseElement_r(xi, w2[j].index1);
						w2[j].value += d*tmp_value;//dt[xi->index-1].value*dt[xi->index-1].value;
						j++;
					}
				}

			}
		}

		iter++;
		if (iter % 10 == 0)
		{
			info(".");
		}

		if (PGmax_new - PGmin_new <= eps)
		{
			if (active_size == l)
				break;
			else
			{
				active_size = l;
				printf("*");
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
	for (i = 0; i < w_size; i++)
	{
		tmp += w2[i].value*w2[i].value;
	}
	v += tmp;

	for (i = 0; i < l; i++)
	{
		if (y[i] == 1)
			v += alpha[i] * (alpha[i] * diag_p - 2);
		else
			v += alpha[i] * (alpha[i] * diag_n - 2);
		if (alpha[i] > 0)
			++nSV;
	}

	printf("Objective value = %lf\n", v / 2);
	printf("nSV = %d\n", nSV);


	delete[] QD;
	delete[] alpha;
	delete[] y;
	delete[] index;
}



void FGM::set_model()
{
	if (param->t == 1)
	{
		calculate_w2_poly_r();
		//Do retraining with all the selected features using SVM
		//printf("do retraining with all selected features using SVM\n");
		//svm_retrain(0.1,5, 5, L2R_L2LOSS_SVC_DUAL,
		//svm_model->solution_->w_FGM_retrain,svm_model->feature_pair,0);

		//solve_l2r_lr_dual_retrain(0.01, 8, 8, L2R_L2LOSS_SVC_DUAL,
		//svm_model->solution_->w_FGM_retrain,svm_model->feature_pair,0);
		//Do retraining on the param->B data sets.
		//printf("do retraining on the best B features using SVM\n");
		//svm_retrain(param->eps, param->C, param->C, L2R_L2LOSS_SVC_DUAL,
		//svm_model->solution_->w_FGM_B, param->B+1,0);

	}
	else
	{
		//obtain model_->feature_pair
		calculate_w2_poly();
	}
}





void FGM::sort_w2b_wf(weight *w2b, int K)
{
	int i;
	int j;
	int tindex1;
	int tindex2;

	int tindexi;
	int tindexj;

	double tvalue;
	for (i = 0; i < K - 1; i++)//sort by i no need to sort w.value
	{
		for (j = i + 1; j<K; j++)
		{
			if (w2b[i].index1>w2b[j].index1)
			{
				tindex1 = w2b[i].index1;
				tindex2 = w2b[i].index2;
				tindexi = w2b[i].indexi;
				tindexj = w2b[i].indexj;
				tvalue = w2b[i].value;

				w2b[i].index1 = w2b[j].index1;
				w2b[i].index2 = w2b[j].index2;
				w2b[i].indexi = w2b[j].indexi;
				w2b[i].indexj = w2b[j].indexj;
				w2b[i].value = w2b[j].value;

				w2b[j].index1 = tindex1;
				w2b[j].index2 = tindex2;
				w2b[j].indexi = tindexi;
				w2b[j].indexj = tindexj;
				w2b[j].value = tvalue;
			}
		}
	}
	for (i = 0; i < K; i++)//sort by j 
	{
		for (j = i + 1; j < K; j++)
		{
			if ((w2b[i].index1 == w2b[j].index1) && (w2b[i].index2 > w2b[j].index2))
			{
				tindex1 = w2b[i].index1;
				tindex2 = w2b[i].index2;
				tindexi = w2b[i].indexi;
				tindexj = w2b[i].indexj;
				tvalue = w2b[i].value;

				w2b[i].index1 = w2b[j].index1;
				w2b[i].index2 = w2b[j].index2;
				w2b[i].indexi = w2b[j].indexi;
				w2b[i].indexj = w2b[j].indexj;
				w2b[i].value = w2b[j].value;

				w2b[j].index1 = tindex1;
				w2b[j].index2 = tindex2;
				w2b[j].indexi = tindexi;
				w2b[j].indexj = tindexj;
				w2b[j].value = tvalue;
			}
		}
	}

}

//sort by i no need to sort w.value
void FGM::sort_w2b_w(int K)
{
	int i;
	int j;
	int tindex1;
	int tindex2;
	int tindexi;
	int tindexj;
	double tvalue;
	for (i = 0; i < K - 1; i++)
	{
		for (j = i + 1; j < K; j++)
		{
			if (abs(svm_model->solution_->w_FGM_retrain[i].value) <
				abs(svm_model->solution_->w_FGM_retrain[j].value))
			{
				tindex1 = svm_model->solution_->w_FGM_retrain[i].index1;
				tindex2 = svm_model->solution_->w_FGM_retrain[i].index2;
				tindexi = svm_model->solution_->w_FGM_retrain[i].indexi;
				tindexj = svm_model->solution_->w_FGM_retrain[i].indexj;
				tvalue = svm_model->solution_->w_FGM_retrain[i].value;

				svm_model->solution_->w_FGM_retrain[i].index1 = svm_model->solution_->w_FGM_retrain[j].index1;
				svm_model->solution_->w_FGM_retrain[i].index2 = svm_model->solution_->w_FGM_retrain[j].index2;
				svm_model->solution_->w_FGM_retrain[i].indexi = svm_model->solution_->w_FGM_retrain[j].indexi;
				svm_model->solution_->w_FGM_retrain[i].indexj = svm_model->solution_->w_FGM_retrain[j].indexj;
				svm_model->solution_->w_FGM_retrain[i].value = svm_model->solution_->w_FGM_retrain[j].value;

				svm_model->solution_->w_FGM_retrain[j].index1 = tindex1;
				svm_model->solution_->w_FGM_retrain[j].index2 = tindex2;
				svm_model->solution_->w_FGM_retrain[j].indexi = tindexi;
				svm_model->solution_->w_FGM_retrain[j].indexj = tindexj;
				svm_model->solution_->w_FGM_retrain[j].value = tvalue;
			}
		}
	}

}


//calculate the final w by wighting 
void FGM::calculate_w2_poly_r()
{
	int i = 0;
	int j = 0;
	int k = 0; //for specified features
	int p = 0;
	int flag = 0;
	//initialize
	for (p = 0; p < prob->n_kernel; p++)
	{
		for (i = 0; i < param->B; i++)
		{
			svm_model->w2s[p*param->B + i].value = svm_model->w[p*param->B + i];
		}
	}

	p = 0;
	// record when svm_model->sigma[p]!=0svm_model->sigma[p]*
	while (abs(svm_model->sigma[p]) <= 1e-10)
	{
		p++;
	}
	int temp_p;
	temp_p = p;
	for (i = 0; i < param->B; i++)
	{
		svm_model->solution_->w_FGM[i].index1 = svm_model->w2s[p*param->B + i].index1;
		svm_model->solution_->w_FGM[i].index2 = svm_model->w2s[p*param->B + i].index2;
		svm_model->solution_->w_FGM[i].value = svm_model->w2s[p*param->B + i].value;
		svm_model->solution_->w_FGM_retrain[i].index1 = svm_model->w2s[p*param->B + i].index1;
		svm_model->solution_->w_FGM_retrain[i].index2 = svm_model->w2s[p*param->B + i].index2;
		svm_model->solution_->w_FGM_retrain[i].indexi = p;
		svm_model->solution_->w_FGM_retrain[i].indexj = i;
		svm_model->solution_->w_FGM_retrain[i].value = svm_model->w2s[p*param->B + i].value;
	}
	k = param->B;
	svm_model->solution_->w_FGM[k].index1 = -1;// should notice
	svm_model->solution_->w_FGM_retrain[k].index1 = -1;
	j = 0;

	for (p = p + 1; p < prob->n_kernel; p++) // match from the second to the last
	{
		if (abs(svm_model->sigma[p]) <= 1e-4)
		{
			continue;
		}
		else
		{
			for (i = 0; i < param->B; i++)// for each elements, the new element should be matched.
			{
				j = 0;
				flag = 0;
				while (svm_model->solution_->w_FGM[j].index1 != -1)
				{
					if ((svm_model->w2s[p*param->B + i].index1 ==
						svm_model->solution_->w_FGM[j].index1) &&
						(svm_model->w2s[p*param->B + i].index2 ==
						svm_model->solution_->w_FGM[j].index2))
					{
						svm_model->solution_->w_FGM[j].value =
							svm_model->solution_->w_FGM[j].value + svm_model->w2s[p*param->B + i].value;
						svm_model->solution_->w_FGM_retrain[j].value =
							svm_model->solution_->w_FGM_retrain[j].value + svm_model->w2s[p*param->B + i].value;
						flag++;
						break;
					}
					j++;
				}
				if (flag == 0)// no match
				{
					svm_model->solution_->w_FGM[j].index1 = svm_model->w2s[p*param->B + i].index1;
					svm_model->solution_->w_FGM[j].index2 = svm_model->w2s[p*param->B + i].index2;
					svm_model->solution_->w_FGM[j].value = svm_model->w2s[p*param->B + i].value;
					svm_model->solution_->w_FGM_retrain[j].indexi = p;
					svm_model->solution_->w_FGM_retrain[j].indexj = i;
					svm_model->solution_->w_FGM_retrain[j].index1 = svm_model->w2s[p*param->B + i].index1;
					svm_model->solution_->w_FGM_retrain[j].index2 = svm_model->w2s[p*param->B + i].index2;
					svm_model->solution_->w_FGM_retrain[j].value = svm_model->w2s[p*param->B + i].value;
					j++;
					svm_model->solution_->w_FGM[j].index1 = -1;
					svm_model->solution_->w_FGM_retrain[j].index1 = -1;
				}
			}
		}
	}

	j = 0;
	while (svm_model->solution_->w_FGM[j].index1 != -1)
	{
		j++;
	}
	svm_model->feature_pair = j;
	// sort by w value of svm_model->solution_->w_FGM_retrain
	sort_w2b_w(svm_model->feature_pair);
	//assign the index to svm_model->solution_->w_FGM_B
	for (i = 0; i < param->B; i++)
	{
		svm_model->solution_->w_FGM_B[i].index1 = svm_model->solution_->w_FGM_retrain[i].index1;
		svm_model->solution_->w_FGM_B[i].index2 = svm_model->solution_->w_FGM_retrain[i].index2;
		svm_model->solution_->w_FGM_B[i].indexi = svm_model->solution_->w_FGM_retrain[i].indexi;
		svm_model->solution_->w_FGM_B[i].indexj = svm_model->solution_->w_FGM_retrain[i].indexj;
		svm_model->solution_->w_FGM_B[i].value = svm_model->solution_->w_FGM_retrain[i].value;
	}

	svm_model->solution_->w_FGM_B[i].index1 = -1;

	// sort by index 
	sort_w2b(svm_model->solution_->w_FGM, svm_model->feature_pair);
	// sort by index: the sorting of svm_model->solution_->w_FGM is the same to svm_model->solution_->w_FGM_retrain
	sort_w2b_wf(svm_model->solution_->w_FGM_retrain, svm_model->feature_pair);
	sort_w2b_wf(svm_model->solution_->w_FGM_B, param->B);//sort by index

}



void FGM::sort_rs(int *w, int n_rsi)
{
	int i;
	int j;
	int w_temp;
	for (i = 0; i < n_rsi - 1; i++)
	{
		for (j = i + 1; j< n_rsi; j++)
		{
			if (w[i]>w[j])
			{
				w_temp = w[i];
				w[i] = w[j];
				w[j] = w_temp;
			}
		}
	}
}
#define INF HUGE_VAL
void simple_sort(weight *h, double v, int B, int indx)
{
	//find the minmum
	int i;
	double min_value = INF;
	int min_indx;
	for (i = 0; i<B; i++)
	{
		if (min_value>h[i].value)
		{
			min_value = h[i].value;
			min_indx = i;
		}
	}
	if (v > min_value)
	{
		h[min_indx].value = v;
		h[min_indx].index1 = indx;
	}
}

void sort_w2b_value(weight *w2b, int K)
{
	int i;
	int j;
	int tindex1;
	int tindex2;
	double tvalue;
	for (i = 0; i < K - 1; i++)//sort by i no need to sort w.value
	{
		for (j = i + 1; j < K; j++)
		{
			if (w2b[i].value < w2b[j].value)
			{
				tindex1 = w2b[i].index1;
				tindex2 = w2b[i].index2;
				tvalue = w2b[i].value;
				w2b[i].index1 = w2b[j].index1;
				w2b[i].index2 = w2b[j].index2;
				w2b[i].value = w2b[j].value;
				w2b[j].index1 = tindex1;
				w2b[j].index2 = tindex2;
				w2b[j].value = tvalue;
			}
		}
	}
}
void FGM::most_violated_research(int iteration)
{
	int i = 0;

	w_size_temp = 500;
	//weight *w2b_temp = Malloc(weight, w_size_temp);


	for (i = 0; i < w_size_temp; i++)
	{
		w2b_temp[i].value = 0.0;
		w2b_temp[i].index1 = i;
		w2b_temp[i].index2 = -1;
	}

	for (i = 0; i < prob->n; i++)
	{
		w_lin[i] = 0.0;
	}

	for (i = 0; i < prob->l; i++)
	{
		feature_node *xi = prob->x[i];
		if (alpha[i] != 0)
		{
			while (xi->index != -1)
			{
				w_lin[xi->index - 1] += alpha[i] * xi->value*prob->y[i];
				xi++;
			}
		}
	}

	for (i = 0; i < prob->n; i++)
	{
		if (fabs(w_lin[i]) > 0)
		{
			heap_sort(w2b_temp, fabs(w_lin[i]), w_size_temp, i, -1);
		}
	}

	sort_w2b(w2b_temp, w_size_temp);
	//for (i=0;i<w_size_temp;i++)
	//{
	//	printf("ID:SCORE, %d:%f\n",w2b_temp[i].index1,w2b_temp[i].value);
	//}

	for (i = 0; i < w_size_temp; i++)
	{
		w2b_temp[i].value = 0.0;
		w2b_temp[i].index1 = i;
		w2b_temp[i].index2 = -1;
	}
	for (i = 0; i < prob->n; i++)
	{
		if (fabs(w_lin[i]) > 0)
		{
			heap_sort(w2b_temp, fabs(w_lin[i]) / QD[i], w_size_temp, i, -1);
		}
	}
	printf("after considering normalization!\n");
	sort_w2b(w2b_temp, w_size_temp);
	for (i = 0; i < w_size_temp; i++)
	{
		printf("ID:SCORE:Diag:num, %d:%f:%f:%d\n", w2b_temp[i].index1, w2b_temp[i].value, QD[w2b_temp[i].index1], QD_count[w2b_temp[i].index1]);
	}
	//record_subfeature_sparse(w2b_temp,iteration);
	//record_subfeature_sparse(w2b_temp,w_size_temp,0);
	//free(w2b);
}
void FGM::most_violated(int iteration)
{
	int i = 0;

	long int t_start = clock();
	long int t_finish;
	double runtime;
	double alphay;

	w_size_temp = param->B;
	//weight *w2b_temp = Malloc(weight, w_size_temp);


	for (i = 0; i < w_size_temp; i++)
	{
		w2b_temp[i].value = 0.0;
		w2b_temp[i].index1 = i;
		w2b_temp[i].index2 = -1;
	}

	for (i = 0; i < prob->n; i++)
	{
		w_lin[i] = 0.0;
	}

	for (i = 0; i < prob->l; i++)
	{
		feature_node *xi = prob->x[i];
		if (alpha[i] != 0)
		{
			alphay = alpha[i] * prob->y[i];
			while (xi->index != -1)
			{
				w_lin[xi->index - 1] += alphay*xi->value;
				xi++;
			}
		}
	}
	//if(iteration==0)
	//{
	//	int flag = 0;
	//	for(i=0;i<prob->n;i++)
	//	{
	//		if (fabs(QD[i])>0)
	//		{
	//			//remove redunt features
	//			flag = remove_redunt(w2b_temp,fabs(w_lin[i])/QD[i],w_size_temp,i);
	//			if (flag==0)
	//			{
	//				heap_sort(w2b_temp,fabs(w_lin[i])/QD[i],w_size_temp,i,-1);
	//			}
	//		}
	//	}
	//}else
	//{
	//	int flag = 0;
	//	for(i=0;i<prob->n;i++)
	//	{
	//		if (fabs(QD[i])>0&&QD_count[i]>0)
	//		{
	//			//remove redunt features
	//			//remove_redunt(w2b_temp,fabs(w_lin[i])/QD[i],w_size_temp,i);
	//			flag = remove_redunt(w2b_temp,fabs(w_lin[i])/QD[i],w_size_temp,i);
	//			if (flag==0)
	//			{
	//				heap_sort(w2b_temp,fabs(w_lin[i])/QD[i],w_size_temp,i,-1);
	//			}
	//			//heap_sort(w2b_temp,fabs(w_lin[i])/QD[i],w_size_temp,i,-1);
	//		}
	//	}

	//}

	for (i = 0; i < prob->n; i++)
	{
		if (fabs(QD[i]) > 0 && QD_count[i] > 0)
		{
			//remove redunt features
			//remove_redunt(w2b_temp,fabs(w_lin[i])/QD[i],w_size_temp,i);
			heap_sort(w2b_temp, fabs(w_lin[i]) / QD[i], w_size_temp, i, -1);
			//heap_sort(w2b_temp,fabs(w_lin[i])/QD[i],w_size_temp,i,-1);
		}
	}

	//printf("QD[i] = %f", QD[0]);
	sort_w2b(w2b_temp, w_size_temp);
	for (i = 0; i < B; i++)
	{
		printf("**%d::%d::%f **", w2b_temp[i].index1, QD_count[w2b_temp[i].index1], w2b_temp[i].value);
	}
	printf("\n");

	t_start = clock();
	record_subfeature_sparse(w2b_temp, iteration);
	t_finish = clock();
	runtime = (double(t_finish - t_start) / CLOCKS_PER_SEC);
	//printf("record_subfeature_sparse time is %f\n",runtime);
	//record_subfeature_sparse(w2b_temp,w_size_temp,0);
	///free(w2b);
}

int FGM::remove_redunt(weight *w2b_temp, double s, int w_size_temp, int i_feat)
{
	int i = 0;
	int flag = 0;
	for (i = 0; i < w_size_temp; i++)
	{
		//remove the feature
		if (s < 1e-10)
		{
			QD_count[i_feat] = 0;
			//flag = 1; //redunt
			break;
		}
		if (fabs(s - w2b_temp[i].value) < abs(s)*1e-6)
		{
			if (abs(s - w2b_temp[i].value) < abs(s)*1e-10)
			{
				QD_count[i_feat] = 0;
				flag = 1; //redunt
				//printf("=============Redunt features found = %d\n", i_feat);
			}
			flag = 1;
			//printf("=============Redunt features found = %d\n", i_feat);
			break;
		}
	}
	return flag;
}
void FGM::most_violated(int iteration, double &runtime)
{
	int i = 0;

	long int t_start = clock();
	long int t_finish;

	double alphay;

	w_size_temp = param->B;
	//weight *w2b_temp = Malloc(weight, w_size_temp);


	for (i = 0; i < w_size_temp; i++)
	{
		w2b_temp[i].value = 0.0;
		w2b_temp[i].index1 = i;
		w2b_temp[i].index2 = -1;
	}

	for (i = 0; i < prob->n; i++)
	{
		w_lin[i] = 0.0;
	}

	for (i = 0; i < prob->l; i++)
	{
		feature_node *xi = prob->x[i];
		if (alpha[i] != 0)
		{
			alphay = alpha[i] * prob->y[i];
			while (xi->index != -1)
			{
				w_lin[xi->index - 1] += alphay*xi->value;
				xi++;
			}
		}
	}
	//   if(iteration==0)
	//{
	//	int flag = 0;
	//	for(i=0;i<prob->n;i++)
	//	{
	//		if (fabs(QD[i])>0)
	//		{
	//			//remove redunt features
	//			flag = remove_redunt(w2b_temp,fabs(w_lin[i])/QD[i],w_size_temp,i);
	//			if (flag==0)
	//			{
	//				heap_sort(w2b_temp,fabs(w_lin[i])/QD[i],w_size_temp,i,-1);
	//			}
	//		}
	//	}
	//}else
	//{
	for (i = 0; i < prob->n; i++)
	{
		if (fabs(QD[i]) > 0 && QD_count[i] > 0)
		{
			//remove redunt features
			//remove_redunt(w2b_temp,fabs(w_lin[i])/QD[i],w_size_temp,i);
			heap_sort(w2b_temp, fabs(w_lin[i]), w_size_temp, i, -1);
			//heap_sort(w2b_temp,fabs(w_lin[i])/QD[i],w_size_temp,i,-1);
		}
	}

	//}

	//printf("QD[i] = %f", QD[0]);
	sort_w2b(w2b_temp, w_size_temp);
	/*for (i=0;i<B;i++)
	{
	printf("**%d::%d::%f **",w2b_temp[i].index1,QD_count[w2b_temp[i].index1],w2b_temp[i].value);
	}*/
	//printf("\n");

	t_start = clock();
	record_subfeature_sparse(w2b_temp, iteration);
	t_finish = clock();
	runtime = (double(t_finish - t_start) / CLOCKS_PER_SEC);
	//printf("record_subfeature_sparse time is %f\n",runtime);
	//record_subfeature_sparse(w2b_temp,w_size_temp,0);
	///free(w2b);
}

void sort_w2b(weight *w2b, int K)
{
	int i;
	int j;
	int tindex1;
	int tindex2;
	double tvalue;
	for (i = 0; i < K - 1; i++)//sort by i no need to sort w.value
	{
		for (j = i + 1; j<K; j++)
		{
			if (w2b[i].index1>w2b[j].index1)
			{
				tindex1 = w2b[i].index1;
				tindex2 = w2b[i].index2;
				tvalue = w2b[i].value;
				w2b[i].index1 = w2b[j].index1;
				w2b[i].index2 = w2b[j].index2;
				w2b[i].value = w2b[j].value;
				w2b[j].index1 = tindex1;
				w2b[j].index2 = tindex2;
				w2b[j].value = tvalue;
			}
		}
	}
	for (i = 0; i < K; i++)//sort by j 
	{
		for (j = i + 1; j < K; j++)
		{
			if ((w2b[i].index1 == w2b[j].index1) && (w2b[i].index2 > w2b[j].index2))
			{
				tindex1 = w2b[i].index1;
				tindex2 = w2b[i].index2;
				tvalue = w2b[i].value;
				w2b[i].index1 = w2b[j].index1;
				w2b[i].index2 = w2b[j].index2;
				w2b[i].value = w2b[j].value;
				w2b[j].index1 = tindex1;
				w2b[j].index2 = tindex2;
				w2b[j].value = tvalue;
			}
		}
	}

}



void sort_and_compare(weight *w2b_temp, weight *w2b, int w_size_temp)
{
	//first sort w2b
	int i;
	sort_w2b_value(w2b, 2 * w_size_temp);
	weight *w2b_temp2 = Malloc(weight, w_size_temp);

	for (i = 0; i < w_size_temp; i++)
	{
		w2b_temp2[i].value = w2b[i].value;
		w2b_temp2[i].index1 = w2b[i].index1;
		w2b_temp2[i].index2 = w2b[i].index2;
	}
	sort_w2b(w2b_temp2, w_size_temp);
	int flag = 0;
	for (i = 0; i < w_size_temp; i++)
	{
		if (w2b_temp2[i].index1 == w2b_temp[i].index1)
		{
			continue;
		}
		else
		{
			flag = 1;
			break;
		}
	}
	if (flag == 0)
	{
		for (i = w_size_temp - 1; i < 2 * w_size_temp - 1; i++)
		{
			w2b_temp[i - w_size_temp].index1 = w2b[i].index1;
			w2b_temp[i - w_size_temp].index2 = w2b[i].index2;
			w2b_temp[i - w_size_temp].value = w2b[i].value;
		}
	}
	else
	{
		for (i = 0; i < w_size_temp; i++)
		{
			w2b_temp[i].index1 = w2b[i].index1;
			w2b_temp[i].index2 = w2b[i].index2;
			w2b_temp[i].value = w2b[i].value;
		}
	}
	free(w2b_temp2);
}



void sort_w2b_w(weight *wfs, int K)
{
	int i;
	int j;
	int tindex1;
	int tindex2;
	int tindexi;
	int tindexj;
	double tvalue;
	for (i = 0; i < K - 1; i++)
	{
		for (j = i + 1; j < K; j++)
		{
			if (abs(wfs[i].value) < abs(wfs[j].value))
			{
				tindex1 = wfs[i].index1;
				tindex2 = wfs[i].index2;
				tindexi = wfs[i].indexi;
				tindexj = wfs[i].indexj;
				tvalue = wfs[i].value;

				wfs[i].index1 = wfs[j].index1;
				wfs[i].index2 = wfs[j].index2;
				wfs[i].indexi = wfs[j].indexi;
				wfs[i].indexj = wfs[j].indexj;
				wfs[i].value = wfs[j].value;

				wfs[j].index1 = tindex1;
				wfs[j].index2 = tindex2;
				wfs[j].indexi = tindexi;
				wfs[j].indexj = tindexj;
				wfs[j].value = tvalue;
			}
		}
	}

}



void FGM::most_violated_(int iteration)
{
	int i = 0;

	w_size_temp = param->B;
	weight *w2b = Malloc(weight, 2 * w_size_temp);


	for (i = 0; i < 2 * w_size_temp; i++)
	{
		w2b[i].value = 0.0;
		w2b[i].index1 = i;
		w2b[i].index2 = -1;
	}

	for (i = 0; i < prob->n; i++)
	{
		w_lin[i] = 0.0;
	}

	for (i = 0; i < prob->l; i++)
	{
		feature_node *xi = prob->x[i];
		if (alpha[i] != 0)
		{
			while (xi->index != -1)
			{
				w_lin[xi->index - 1] += alpha[i] * xi->value*prob->y[i];
				xi++;
			}
		}
	}

	for (i = 0; i < prob->n; i++)
	{
		if (fabs(w_lin[i]) > 0)
		{
			simple_sort(w2b, fabs(w_lin[i] / sqrt(QD[i])), 2 * w_size_temp, i);
		}
	}

	sort_and_compare(w2b_temp, w2b, w_size_temp);
	sort_w2b(w2b_temp, w_size_temp);
	//for (i=0;i<B;i++)
	//{
	//	printf("ID:SCORE, %d:%f\n",w2b_temp[i].index1,w2b_temp[i].value);
	//}
	record_subfeature_sparse(w2b_temp, iteration);
	free(w2b);
}


bool search_wb(weight *w2b_temp, int B, weight w2b_elem)
{
	for (int i = 0; i < B; i++)
	{
		if (w2b_elem.index1 == w2b_temp[i].index1 && w2b_elem.index2 == w2b_temp[i].index2)
		{
			return true;
		}
	}
	return false;

}
int  FGM::merge_wb(weight *w2b_temp, weight *w2b_B, weight *w2b)
{
	int temp_B = 0;

	for (int i = 0; i < param->B; i++)
	{
		w2b_temp[i].index1 = w2b_B[i].index1;
		w2b_temp[i].index2 = w2b_B[i].index2;
		w2b_temp[i].value = w2b_B[i].value;
	}
	temp_B = param->B;
	for (int i = 0; i < param->B; i++)
	{
		if (!search_wb(w2b_temp, param->B, w2b[i]))
		{
			w2b_temp[temp_B].index1 = w2b[i].index1;
			w2b_temp[temp_B].index2 = w2b[i].index2;
			w2b_temp[temp_B].value = w2b[i].value;
			temp_B++;
		}

	}
	return temp_B;
}

void FGM::most_violated_pursuit_init()
{
	int i = 0;

	w_size_temp = 2 * param->B;
	//weight *w2b_temp = Malloc(weight, w_size_temp);


	for (i = 0; i < w_size_temp; i++)
	{
		w2b_temp[i].value = 0.0;
		w2b_temp[i].index1 = 0;
		w2b_temp[i].index2 = 0;
	}

	for (i = 0; i < prob->n; i++)
	{
		w_lin[i] = 0.0;
	}

	for (i = 0; i < prob->l; i++)
	{
		feature_node *xi = prob->x[i];
		if (alpha[i] != 0)
		{
			while (xi->index != -1)
			{
				w_lin[xi->index - 1] += alpha[i] * xi->value*prob->y[i];
				xi++;
			}
		}
	}

	for (i = 0; i < prob->n; i++)
	{
		if (abs(w_lin[i]) > 0)
		{
			heap_sort(w2b_temp, abs(w_lin[i]), w_size_temp, i, -1);
		}
	}

	sort_w2b(w2b_temp, w_size_temp);

	//for (i=0;i<2*param->B;i++)
	//{
	//	printf("Feature %d is %d\n",i+1,w2b_temp[i].index1);
	//}
	//merge 
	//int temp_B = merge_wb(w2b_temp,w2b_B,w2b);
	//merge_wb(w2b_temp,w2b_B,w2b);
	//sort_w2b(w2b_temp,2*param->B);

	//release the memory
	//free(sub_x_space[0]);
	record_subfeature_sparse(w2b_temp, w_size_temp, 0);
	//free(w2b);
}


//i: find those 2B features
//ii: merge the features
//iii: store the features
void FGM::most_violated_pursuit(int iteration)
{
	int i = 0;

	weight *w2b = Malloc(weight, param->B);

	for (i = 0; i < param->B; i++)
	{
		w2b[i].value = 0.0;
		w2b[i].index1 = 0;
		w2b[i].index2 = 0;
	}

	for (i = 0; i < prob->n; i++)
	{
		w_lin[i] = 0.0;
	}

	for (i = 0; i < prob->l; i++)
	{
		feature_node *xi = prob->x[i];
		if (alpha[i] != 0)
		{
			while (xi->index != -1)
			{
				w_lin[xi->index - 1] += alpha[i] * xi->value*prob->y[i];
				xi++;
			}
		}
	}

	for (i = 0; i < prob->n; i++)
	{
		if (abs(w_lin[i]) > 0)
		{
			heap_sort(w2b, abs(w_lin[i]), param->B, i, -1);
		}
	}
	sort_w2b(w2b, param->B);

	//merge 
	w_size_temp = merge_wb(w2b_temp, w2b_B, w2b);
	printf("w_size_temp is %d\n", w_size_temp);
	sort_w2b(w2b_temp, w_size_temp);

	//release the memory
	free(sub_x_space[0]);
	record_subfeature_sparse(w2b_temp, w_size_temp, 0);
	free(w2b);
}

void FGM::most_violated_pursuit_B(int iteration)
{
	//release the memory
	sort_w2b(w2b_B, B);
	//for (int i=0;i<param->B;i++)
	//{
	//	printf("Feature %d is %d\n",i+1,w2b_B[i].index1);
	//}
	free(sub_x_space[0]);
	record_subfeature_sparse(w2b_B, B, 0);

}

//drop those features with smaller scores
void FGM::prune_feature()
{
	for (int i = 0; i < svm_model->B; i++)
	{
		w2b_B[i].index1 = 0;
		w2b_B[i].index2 = 0;
		w2b_B[i].value = 0;
	}
	for (int i = 0; i < w_size_temp; i++)
	{
		heap_sort(w2b_B, abs(svm_model->w[i]), svm_model->B, w2b_temp[i].index1, w2b_temp[i].index2);
	}
}

void FGM::most_violated_w_poly(int iteration)
{
	int i = 0;
	int j = 0;
	int num = 0;
	int k = 0;
	weight *w2b = Malloc(weight, param->B);


	num = svm_model->feature_pair;
	int tindex1;
	int tindex2;
	double tvalue;

	// sort by svm_model->solution_->w_FGM value^2
	for (i = 0; i < param->B; i++)
		for (j = num - 1; j >= 1; j--)
		{
		if (abs(svm_model->solution_->w_FGM[j].value) > abs(svm_model->solution_->w_FGM[j - 1].value))
		{

			tindex1 = svm_model->solution_->w_FGM[j].index1;
			tindex2 = svm_model->solution_->w_FGM[j].index2;
			tvalue = svm_model->solution_->w_FGM[j].value;
			svm_model->solution_->w_FGM[j].index1 = svm_model->solution_->w_FGM[j - 1].index1;
			svm_model->solution_->w_FGM[j].index2 = svm_model->solution_->w_FGM[j - 1].index2;
			svm_model->solution_->w_FGM[j].value = svm_model->solution_->w_FGM[j - 1].value;
			svm_model->solution_->w_FGM[j - 1].index1 = tindex1;
			svm_model->solution_->w_FGM[j - 1].index2 = tindex2;
			svm_model->solution_->w_FGM[j - 1].value = tvalue;
		}
		}
	//obtain w2b
	for (i = 0; i < param->B; i++)
	{
		w2b[i].index1 = svm_model->solution_->w_FGM[i].index1;
		w2b[i].index2 = svm_model->solution_->w_FGM[i].index2;
		w2b[i].value = svm_model->solution_->w_FGM[i].value;
	}

	// sort by i first and then sort by j i1,i1,...,in....
	sort_w2b(w2b, param->B);


	record_subfeature_sparse(w2b, iteration);

	free(w2b);

}
void FGM::sort_w2b(weight *w2b, int K)
{

	int i;
	int j;
	int tindex1;
	int tindex2;
	double tvalue;
	for (i = 0; i < K - 1; i++)//sort by i no need to sort w.value
	{
		for (j = i + 1; j<K; j++)
		{
			if (w2b[i].index1>w2b[j].index1)
			{
				tindex1 = w2b[i].index1;
				tindex2 = w2b[i].index2;
				tvalue = w2b[i].value;
				w2b[i].index1 = w2b[j].index1;
				w2b[i].index2 = w2b[j].index2;
				w2b[i].value = w2b[j].value;
				w2b[j].index1 = tindex1;
				w2b[j].index2 = tindex2;
				w2b[j].value = tvalue;
			}
		}
	}
	for (i = 0; i < K; i++)//sort by j 
	{
		for (j = i + 1; j < K; j++)
		{
			if ((w2b[i].index1 == w2b[j].index1) && (w2b[i].index2 > w2b[j].index2))
			{
				tindex1 = w2b[i].index1;
				tindex2 = w2b[i].index2;
				tvalue = w2b[i].value;
				w2b[i].index1 = w2b[j].index1;
				w2b[i].index2 = w2b[j].index2;
				w2b[i].value = w2b[j].value;
				w2b[j].index1 = tindex1;
				w2b[j].index2 = tindex2;
				w2b[j].value = tvalue;
			}
		}
	}

}


void FGM::heap_sort(weight *h, double X, int K, int i, int j)
{
	int p = 0;
	int q = 0;
	double tv;
	int tindex1;
	int tindex2;
	int flag = 0;
	X = fabs(X);
	if (X > h[0].value)
	{
		flag = remove_redunt(h, X, K, i);
		if (flag)//redunt features
		{
			return;
		}
		h[0].value = X;
		h[0].index1 = i;
		h[0].index2 = j;
		int p = 0;
		while (p < K)
		{
			q = 2 * p + 1;
			if (q >= K)
				break;
			if ((q < K - 1) && (h[q + 1].value < h[q].value))
				q = q + 1;
			if (h[q].value < h[p].value)
			{
				tv = h[p].value;
				tindex1 = h[p].index1;
				tindex2 = h[p].index2;
				h[p].index1 = h[q].index1;
				h[p].index2 = h[q].index2;
				h[p].value = h[q].value;
				h[q].value = tv;
				h[q].index1 = tindex1;
				h[q].index2 = tindex2;
				p = q;
			}
			else
				break;
		}
	}


}

int count_element(problem *&prob, const parameter *param, weight *w2b, int B, int iteration)
{
	int i = 0;
	int j = 0;
	int k = 0;
	int iw = 0;
	int jw = 0;

	int i_start = 0;
	int j_start = 0;
	int iw_temp = 0;
	feature_node *xi;
	feature_node *xj;

	double sqrt2_coef0_g = sqrt(2.0)*sqrt(prob->coef0*prob->gamma);
	double sqrt2_g = sqrt(2.0)*prob->gamma;

	long int element = 0;
	if (param->flag_poly == 0)
	{

		for (k = 0; k < prob->l; k++)
		{
			i_start = 0;
			xi = prob->x[k];
			j = 0;
			for (i = 0; i < B; i++)
			{
				iw = w2b[i].index1;
				if (xi[i_start].index == -1)
				{
					break;
				}
				while (xi[i_start].index != -1 && xi[i_start].index - 1 < iw)
				{
					i_start++;
				}
				if (xi[i_start].index - 1 == iw)
				{
					element++;
					j++;
				}
			}

		}
		printf("element number is %d\n", element);
		return element;
	}


	// if polynomial kernel is used
	for (k = 0; k < prob->l; k++)
	{
		xi = prob->x[k];
		xj = prob->x[k];
		i_start = 0;
		j_start = 0;

		j = 0;
		for (i = 0; i < B; i++)
		{
			iw = w2b[i].index1;
			jw = w2b[i].index2;

			// for the linear features where w2b[iw].index2==-1
			if (jw == -1)
			{
				//record the linear feature here
				//find the corresponding xi and xj
				while (xi[i_start].index - 1 < iw&&xi[i_start].index != -1)
				{
					i_start++;
				}
				if (xi[i_start].index == -1)
				{
					break;
				}

				if (xi[i_start].index - 1 == iw)
				{
					element++;
					j++;
				}
			}
			else
			{

				//quadratic term
				//find the corresponding xi and xj
				while (xi[i_start].index - 1 < iw && xi[i_start].index != -1)
				{
					i_start++;
				}
				if (xi[i_start].index == -1)
				{
					break;
				}
				//find the corresponding xi and xj
				while (xj[j_start].index - 1 < jw && xj[j_start].index != -1)
				{
					j_start++;
				}
				if (xj[j_start].index == -1)
				{
					continue;
				}

				if (xi[i_start].index - 1 == iw && xj[j_start].index - 1 == jw)
				{
					//prob->xsp[iteration][k][j].index = i+1;
					j++;
					element++;
					j_start++;
					//iw++;
				}

			} //end for else
		}
	}

	return element;
}


void FGM::record_subfeature_sparse(weight *w2b, int iteration)
{
	int i = 0;
	int j = 0;
	int k = 0;
	int iw = 0;
	int jw = 0;

	int i_start = 0;
	int j_start = 0;
	int iw_temp = 0;
	feature_node *xi;
	feature_node *xj;

	float sqrt2_coef0_g = sqrt(2.0)*sqrt(prob->coef0*prob->gamma);
	float sqrt2_g = sqrt(2.0)*prob->gamma;

	long int element = 0;

	element = count_element(prob, param, w2b, B, iteration);
	sub_x_space[iteration] = Malloc(feature_node, element + prob->l);
	element = 0;
	if (param->flag_poly == 0)
	{

		for (k = 0; k < prob->l; k++)
		{
			prob->xsp[iteration][k] = &sub_x_space[iteration][element];
			i_start = 0;
			xi = prob->x[k];
			j = 0;
			for (i = 0; i < B; i++)
			{
				iw = w2b[i].index1;
				if (xi[i_start].index == -1)
				{
					break;
				}
				while (xi[i_start].index != -1 && xi[i_start].index - 1 < iw)
				{
					i_start++;
				}
				if (xi[i_start].index - 1 == iw)
				{
					prob->xsp[iteration][k][j].index = i + 1;
					prob->xsp[iteration][k][j].value = xi[i_start].value;
					j++;
					element++;
				}
			}
			prob->xsp[iteration][k][j].index = -1;
			element++;

		}
		for (i = 0; i < param->B; i++)
		{
			svm_model->w2s[iteration*param->B + i].index1 = w2b[i].index1;
			svm_model->w2s[iteration*param->B + i].index2 = w2b[i].index2;
			svm_model->w2s[iteration*param->B + i].value = w2b[i].value;
		}
		return;
	}


	element = 0;

	// if polynomial kernel is used
	for (k = 0; k < prob->l; k++)
	{
		xi = prob->x[k];
		xj = prob->x[k];
		i_start = 0;
		j_start = 0;

		j = 0;
		prob->xsp[iteration][k] = &sub_x_space[iteration][element];
		for (i = 0; i < B; i++)
		{
			iw = w2b[i].index1;
			jw = w2b[i].index2;

			// for the linear features where w2b[iw].index2==-1
			if (jw == -1)
			{
				//record the linear feature here
				//find the corresponding xi and xj
				while (xi[i_start].index - 1 < iw&&xi[i_start].index != -1)
				{
					i_start++;
				}
				if (xi[i_start].index == -1)
				{
					break;
				}

				if (xi[i_start].index - 1 == iw)
				{
					prob->xsp[iteration][k][j].index = i + 1;
					prob->xsp[iteration][k][j].value = xi[i_start].value*sqrt2_coef0_g;
					element++;
					j++;
				}
			}
			else
			{

				//quadratic term
				//find the corresponding xi and xj
				while (xi[i_start].index - 1 < iw && xi[i_start].index != -1)
				{
					i_start++;
				}
				if (xi[i_start].index == -1)
				{
					break;
				}
				//find the corresponding xi and xj
				while (xj[j_start].index - 1 < jw && xj[j_start].index != -1)
				{
					j_start++;
				}
				if (xj[j_start].index == -1)
				{
					continue;
				}

				if (xi[i_start].index - 1 == iw && xj[j_start].index - 1 == jw)
				{
					prob->xsp[iteration][k][j].index = i + 1;
					if (iw == jw)
					{
						prob->xsp[iteration][k][j].value =
							xj[j_start].value *xi[i_start].value *prob->gamma;
					}
					else
					{
						prob->xsp[iteration][k][j].value =
							xj[j_start].value *xi[i_start].value*sqrt2_g;
					}
					j++;
					element++;
					j_start++;
					//iw++;
				}

			} //end for else
		}
		prob->xsp[iteration][k][j].index = -1;
		element++;

	}

	for (i = 0; i < param->B; i++)
	{
		svm_model->w2s[iteration*param->B + i].index1 = w2b[i].index1;
		svm_model->w2s[iteration*param->B + i].index2 = w2b[i].index2;
		svm_model->w2s[iteration*param->B + i].value = w2b[i].value;
	}
}



void FGM::record_subfeature_sparse(weight *w2b, int B, int iteration)
{
	int i = 0;
	int j = 0;
	int k = 0;
	int iw = 0;
	int jw = 0;

	int i_start = 0;
	int j_start = 0;
	int iw_temp = 0;
	feature_node *xi;
	feature_node *xj;

	float sqrt2_coef0_g = sqrt(2.0)*sqrt(prob->coef0*prob->gamma);
	float sqrt2_g = sqrt(2.0)*prob->gamma;

	long int element = 0;

	element = count_element(prob, param, w2b, B, iteration);
	sub_x_space[iteration] = Malloc(feature_node, element + prob->l);
	element = 0;
	if (param->solver_type == SVMFGM && param->flag_poly == 0)
	{

		for (k = 0; k < prob->l; k++)
		{
			prob->xsp[iteration][k] = &sub_x_space[iteration][element];
			i_start = 0;
			xi = prob->x[k];
			j = 0;
			for (i = 0; i < B; i++)
			{
				iw = w2b[i].index1;
				if (xi[i_start].index == -1)
				{
					break;
				}
				while (xi[i_start].index != -1 && xi[i_start].index - 1 < iw)
				{
					i_start++;
				}
				if (xi[i_start].index - 1 == iw)
				{
					prob->xsp[iteration][k][j].index = i + 1;
					prob->xsp[iteration][k][j].value = xi[i_start].value;
					j++;
					element++;
				}
			}
			prob->xsp[iteration][k][j].index = -1;
			element++;

		}
		//for (i = 0; i <param->B; i++)
		//{
		//	svm_model->w2s[iteration*param->B+i].index1 = w2b[i].index1;
		//	svm_model->w2s[iteration*param->B+i].index2 = w2b[i].index2;
		//	svm_model->w2s[iteration*param->B+i].value = w2b[i].value;
		//}
		return;
	}


	element = 0;

	// if polynomial kernel is used
	for (k = 0; k < prob->l; k++)
	{
		xi = prob->x[k];
		xj = prob->x[k];
		i_start = 0;
		j_start = 0;

		j = 0;
		prob->xsp[iteration][k] = &sub_x_space[iteration][element];
		for (i = 0; i < B; i++)
		{
			iw = w2b[i].index1;
			jw = w2b[i].index2;

			// for the linear features where w2b[iw].index2==-1
			if (jw == -1)
			{
				//record the linear feature here
				//find the corresponding xi and xj
				while (xi[i_start].index - 1 < iw&&xi[i_start].index != -1)
				{
					i_start++;
				}
				if (xi[i_start].index == -1)
				{
					break;
				}

				if (xi[i_start].index - 1 == iw)
				{
					prob->xsp[iteration][k][j].index = i + 1;
					prob->xsp[iteration][k][j].value = xi[i_start].value*sqrt2_coef0_g;
					element++;
					j++;
				}
			}
			else
			{

				//quadratic term
				//find the corresponding xi and xj
				while (xi[i_start].index - 1 < iw && xi[i_start].index != -1)
				{
					i_start++;
				}
				if (xi[i_start].index == -1)
				{
					break;
				}
				//find the corresponding xi and xj
				while (xj[j_start].index - 1 < jw && xj[j_start].index != -1)
				{
					j_start++;
				}
				if (xj[j_start].index == -1)
				{
					continue;
				}

				if (xi[i_start].index - 1 == iw && xj[j_start].index - 1 == jw)
				{
					prob->xsp[iteration][k][j].index = i + 1;
					if (iw == jw)
					{
						prob->xsp[iteration][k][j].value =
							xj[j_start].value *xi[i_start].value *float(prob->gamma);
					}
					else
					{
						prob->xsp[iteration][k][j].value =
							xj[j_start].value *xi[i_start].value*sqrt2_g;
					}
					j++;
					element++;
					j_start++;
					//iw++;
				}

			} //end for else
		}
		prob->xsp[iteration][k][j].index = -1;
		element++;

	}

	//for (i = 0; i <param->B; i++)
	//{
	//	svm_model->w2s[iteration*param->B+i].index1 = w2b[i].index1;
	//	svm_model->w2s[iteration*param->B+i].index2 = w2b[i].index2;
	//	svm_model->w2s[iteration*param->B+i].value = w2b[i].value;
	//}
}

int FGM::cutting_set_evolve()
{
	//FGM_init();
	most_violated(0);
	double mkl_obj = 0;

	MKL FGM_MKL(prob, param, 1, svm_model);
	FGM_MKL.MKL_init();

	//train SVM with one kernel
	FGM_MKL.warm_set_model(1);
	FGM_MKL.pure_train_one();
	FGM_MKL.reset_model();
	mkl_obj = svm_model->mkl_obj;
	printf("Iteration=%d\n", 0);
	printf("Objective is %f\n", mkl_obj);


	int iter = 1;
	double bestobj = INF;
	double ObjDif = 0.0;
	most_violated(1);
	while (iter < max_iteration - 1)
	{
		printf("******************************\n");
		printf("Iteration=%d\n", iter);
		svm_model->n_kernel = iter + 1;
		prob->n_kernel = iter + 1;
		FGM_MKL.warm_set_model(iter + 1);
		FGM_MKL.SimpleMKL();
		FGM_MKL.reset_model();
		mkl_obj = svm_model->mkl_obj;
		ObjDif = fabs(mkl_obj - bestobj);
		bestobj = mkl_obj;
		printf("Objective is %f\n", mkl_obj);
		printf("Objective difference is %f\n", ObjDif);

		if (ObjDif < 0.001*abs(bestobj))
		{
			break;
		}
		if (iter >= max_iteration - 1 || svm_model->sigma[iter] < 0.0001)
		{
			break;
		}
		most_violated(iter + 1);

		iter++;
	}
	n_ITER = iter;
	return iter;
}




int FGM::cutting_set_prox_logistic_evolve()
{
	most_violated(0);
	double mkl_obj = 0;
	double mkl_obj_initial = 0;
	//train SVM with one kernel
	ProxMKL smoothmkl(prob, param, 1, svm_model);
	smoothmkl.smoothmkl_init();
	smoothmkl.mkl_prox_linear_logistic();
	smoothmkl.return_model();

	mkl_obj = svm_model->mkl_obj;
	printf("ProxMKL LR Objectiveis %f\n", mkl_obj);
	mkl_obj_initial = mkl_obj;
	//reset_status(1);
	most_violated(1);

	int iter = 1;
	int iter_flag = 1;
	double bestobj = mkl_obj;
	double ObjDif = 0.0;
	double initial_obj = mkl_obj;
	while (iter_flag > 0 && iter < max_iteration)
	{
		//create MKL object
		smoothmkl.warm_start_model(iter + 1);
		smoothmkl.mkl_prox_linear_logistic();
		smoothmkl.return_model();
		mkl_obj = svm_model->mkl_obj;

		printf("******************************\n");
		printf("iter=%d\n", iter);
		ObjDif = fabs(mkl_obj - bestobj);
		//if(iter == 1)
		//{
		//	mkl_obj_initial = ObjDif;
		//}
		printf("ProxMKL LR Objectiveis %f\n", mkl_obj);
		printf("ProxMKL LR Objective difference is %f\n", ObjDif);
		if (ObjDif < 0.0001*abs(initial_obj))
		{
			break;
		}
		if (iter >= max_iteration - 1)
		{
			break;
		}
		if (iter_flag)
		{
			//re-initialize the parameters
			bestobj = mkl_obj;
			svm_model->n_kernel = iter + 1;
			prob->n_kernel = iter + 1;
			{
				//reset_status(iter+1);
				most_violated(iter + 1);
			}
			iter = iter + 1;
		}
	}
	n_ITER = iter;
	return iter;
}

int FGM::cutting_set_prox_logistic_evolve(double &run_time)
{
	double run_time_ = 0;
	most_violated(0, run_time_);
	run_time = run_time_;
	double mkl_obj = 0;
	double mkl_obj_initial = 0;
	//train SVM with one kernel
	ProxMKL smoothmkl(prob, param, 1, svm_model);
	smoothmkl.smoothmkl_init();
	smoothmkl.mkl_prox_linear_logistic();
	smoothmkl.return_model();

	mkl_obj = svm_model->mkl_obj;
	printf("ProxMKL LR Objectiveis %f\n", mkl_obj);
	mkl_obj_initial = mkl_obj;
	//reset_status(1);
	most_violated(1, run_time_);
	run_time += run_time_;
	int iter = 1;
	int iter_flag = 1;
	double bestobj = mkl_obj;
	double ObjDif = 0.0;
	double initial_obj = mkl_obj;
	while (iter_flag > 0 && iter < max_iteration)
	{
		//create MKL object
		smoothmkl.warm_start_model(iter + 1);
		smoothmkl.mkl_prox_linear_logistic();
		smoothmkl.return_model();
		mkl_obj = svm_model->mkl_obj;
		printf("******************************\n");
		printf("iter=%d\n", iter);
		ObjDif = fabs(mkl_obj - bestobj);
		//if(iter == 1)
		//{
		//	mkl_obj_initial = ObjDif;
		//}
		printf("ProxMKL LR Objectiveis %f\n", mkl_obj);
		printf("ProxMKL LR Objective difference is %f\n", ObjDif);
		if (ObjDif < 0.0001*abs(initial_obj))
		{
			break;
		}
		if (iter >= max_iteration - 1)
		{
			break;
		}
		if (iter_flag)
		{
			//re-initialize the parameters
			bestobj = mkl_obj;
			svm_model->n_kernel = iter + 1;
			prob->n_kernel = iter + 1;
			{
				//reset_status(iter+1);
				most_violated(iter + 1, run_time_);
				run_time += run_time_;
			}
			iter = iter + 1;
		}
	}
	n_ITER = iter;
	return iter;
}

int FGM::cutting_set_prox_svm_evolve(double &run_time)
{
	double run_time_ = 0;
	most_violated(0, run_time_);
	run_time = run_time_;
	double mkl_obj = 0;
	double mkl_obj_initial = 0;
	//train SVM with one kernel
	ProxMKL smoothmkl(prob, param, 1, svm_model);
	smoothmkl.smoothmkl_init();
	smoothmkl.mkl_prox_linear_l2svm();
	smoothmkl.return_model();

	mkl_obj = svm_model->mkl_obj;
	printf("ProxMKL SVM Objectiveis %f\n", mkl_obj);
	mkl_obj_initial = mkl_obj;
	//reset_status(1);
	most_violated(1, run_time_);
	run_time += run_time_;
	int iter = 1;
	int iter_flag = 1;
	double bestobj = mkl_obj;
	double ObjDif = 0.0;
	double initial_obj = mkl_obj;
	while (iter_flag > 0 && iter < max_iteration)
	{
		//create MKL object
		smoothmkl.warm_start_model(iter + 1);
		smoothmkl.mkl_prox_linear_l2svm();
		smoothmkl.return_model();
		mkl_obj = svm_model->mkl_obj;
		printf("******************************\n");
		printf("iter=%d\n", iter);
		ObjDif = fabs(mkl_obj - bestobj);
		//if(iter == 1)
		//{
		//	mkl_obj_initial = ObjDif;
		//}
		printf("ProxMKL SVM Objectiveis %f\n", mkl_obj);
		printf("ProxMKL SVM Objective difference is %f\n", ObjDif);
		if (ObjDif < 0.0001*abs(initial_obj))
		{
			break;
		}
		if (iter >= max_iteration - 1)
		{
			break;
		}
		if (iter_flag)
		{
			//re-initialize the parameters
			bestobj = mkl_obj;
			svm_model->n_kernel = iter + 1;
			prob->n_kernel = iter + 1;
			{
				//reset_status(iter+1);
				most_violated(iter + 1, run_time_);
				run_time += run_time_;
			}
			iter = iter + 1;
		}
	}
	n_ITER = iter;
	return iter;
}

int FGM::cutting_set_prox_svm_evolve()
{
	double run_time = 0;
	double run_time_ = 0;
	most_violated(0, run_time_);
	run_time = run_time_;
	double mkl_obj = 0;
	double mkl_obj_initial = 0;
	//train SVM with one kernel
	ProxMKL smoothmkl(prob, param, 1, svm_model);
	smoothmkl.smoothmkl_init();
	smoothmkl.mkl_prox_linear_l2svm();
	smoothmkl.return_model();

	mkl_obj = svm_model->mkl_obj;
	printf("Start\n");
	printf("ProxMKL SVM Objectiveis %f\n", mkl_obj);
	mkl_obj_initial = mkl_obj;
	//reset_status(1);
	most_violated(1, run_time_);
	run_time += run_time_;
	int iter = 1;
	int iter_flag = 1;
	double bestobj = mkl_obj;
	double ObjDif = 0.0;
	double initial_obj = mkl_obj;
	while (iter_flag > 0 && iter < max_iteration)
	{
		//create MKL object
		smoothmkl.warm_start_model(iter + 1);
		smoothmkl.mkl_prox_linear_l2svm();
		smoothmkl.return_model();
		mkl_obj = svm_model->mkl_obj;

		printf("******************************\n");
		printf("iter=%d\n", iter);
		ObjDif = fabs(mkl_obj - bestobj);
		//if(iter == 1)
		//{
		//	mkl_obj_initial = ObjDif;
		//}
		printf("ProxMKL SVM Objectiveis %f\n", mkl_obj);
		printf("ProxMKL SVM Objective difference is %f\n", ObjDif);
		if (ObjDif < 0.0001*abs(initial_obj))
		{
			break;
		}
		if (iter >= max_iteration - 1)
		{
			break;
		}
		if (iter_flag)
		{
			//re-initialize the parameters
			bestobj = mkl_obj;
			svm_model->n_kernel = iter + 1;
			prob->n_kernel = iter + 1;
			{
				//reset_status(iter+1);
				most_violated(iter + 1, run_time_);
				run_time += run_time_;
			}
			iter = iter + 1;
		}
	}
	n_ITER = iter;
	return iter;
}

int FGM::cutting_set_stoch_prox_svm_evolve()
{
	most_violated(0);
	double mkl_obj = 0;
	//train SVM with one kernel
	StochProxMKL smoothmkl(prob, param, 1, svm_model);
	smoothmkl.smoothmkl_init();
	smoothmkl.stoch_mkl_prox_linear_lr();
	smoothmkl.return_model();

	mkl_obj = svm_model->mkl_obj;
	printf("ProxMKL Objectiveis %f\n", mkl_obj);
	//reset_status(1);
	most_violated(1);

	int iter = 1;
	int iter_flag = 1;
	double bestobj = mkl_obj;
	double ObjDif = 0.0;

	while (iter_flag > 0 && iter < max_iteration)
	{
		//create MKL object
		smoothmkl.warm_start_model(iter + 1);
		smoothmkl.stoch_mkl_prox_linear_lr();
		smoothmkl.return_model();
		mkl_obj = svm_model->mkl_obj;
		printf("******************************\n");
		printf("iter=%d\n", iter);
		ObjDif = fabs(mkl_obj - bestobj);
		printf("ProxMKL Objectiveis %f\n", mkl_obj);
		printf("ProxMKL Objective difference is %f\n", ObjDif);
		if (abs(mkl_obj - bestobj) < 0.01*abs(bestobj) && iter > 4)
		{
			break;
		}
		if (iter >= max_iteration - 1)
		{
			break;
		}
		if (iter_flag)
		{
			//re-initialize the parameters
			bestobj = mkl_obj;
			svm_model->n_kernel = iter + 1;
			prob->n_kernel = iter + 1;
			{
				//reset_status(iter+1);
				most_violated(iter + 1);
			}
			iter = iter + 1;
		}
	}
	n_ITER = iter;
	return iter;
}


int FGM::FGM_train_one()
{
	int n_ker;

	if (param->solver_type == LRFGM || param->solver_type == SVMFGM)
	{
		n_ker = cutting_set_evolve();
	}
	if (param->solver_type == PROXFGM)
	{
		n_ker = cutting_set_prox_svm_evolve();
	}
	if (param->solver_type == PROXFGMLR)
	{
		n_ker = cutting_set_prox_logistic_evolve();
	}
	if (param->solver_type == STOCHFGM)
	{
		n_ker = cutting_set_stoch_prox_svm_evolve();
	}
	return n_ker;
}


int FGM::FGM_train_one(double &run_time)
{
	int n_ker = 0;

	if (param->solver_type == LRFGM || param->solver_type == SVMFGM)
	{
		n_ker = cutting_set_evolve();
	}
	if (param->solver_type == PROXFGM)
	{
		n_ker = cutting_set_prox_svm_evolve(run_time);
	}
	if (param->solver_type == PROXFGMLR)
	{
		n_ker = cutting_set_prox_logistic_evolve(run_time);
	}
	if (param->solver_type == STOCHFGM)
	{
		n_ker = cutting_set_stoch_prox_svm_evolve();
	}
	return n_ker;
}