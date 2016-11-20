#ifndef FGM_H_
#define FGM_H_
#include <math.h>
#include "linear.h"


template <class T> static inline void swap(T& x, T& y) { T t=x; x=y; y=t; }
#define min2(a, b)      ((a) <= (b) ? (a) : (b))

class FGM
{
public:
	FGM();
	FGM(problem *&prob_, model *&model_,const parameter *svm_param_,int max_iteration_)
	{
		max_iteration	= max_iteration_;
		param 			= svm_param_; 
		prob			= prob_;
		alpha			= model_->alpha;
		B				= svm_param_->B;
		//we allocate the memory for the sub-features in advance
		elements		= prob->l*(svm_param_->B+1);
		svm_model       = model_;
		
		FGM_allocate();
	}

	double SearchSparseElement(feature_node *xi,int index)
	{
		double node_value = 0;
		while(xi->index!=-1)
		{
			if (xi->index == index+1)
			{
				node_value = xi->value;
				break;
			}
			xi++;
		}
		return node_value;
	}

	~FGM();
	void FGM_init();
	void most_violated_(int iteration);
	void most_violated(int iteration);//worst-case analysis
	void most_violated_w_poly(int iteration);
	int cutting_set_evolve();
	int FGM_train_one();

	void reset_model();
	void reset_sub_problem();
	void FGM_allocate();
	void heap_sort(weight *h,double X,int K, int i, int j);
	void sort_w2b(weight *w2b, int B);
	void sort_rs(int *w,int n_rsi);
	void record_subfeature_sparse(weight *w2b, int iteration);

	void calculate_w2_poly();
	void calculate_w2_poly_r();
	void sort_w2b_wf(weight *w2b, int K);
	void sort_w2b_w(int K);
	void set_model();
	void svm_retrain(double eps, double Cp, double Cn, 
		int solver_type, weight *w2, int feature_num, int bias=0);
	void solve_l2r_lr_dual_retrain(double eps, 
					  double Cp, double Cn, int solver_type, weight *w2, int feature_num, int bias);


	void most_violated_pursuit_init();
	void most_violated_research(int iteration);
	void record_subfeature_sparse(weight *w2b, int B, int iteration);
	void most_violated_pursuit(int iteration);
	int  merge_wb(weight *w2b_temp,weight *w2b_B,weight *w2b);
    void prune_feature();
	void most_violated_pursuit_B(int iteration);
	//void prune_feature_init();
	int  matching_puit_evolve();
	int FGM_train_one(double &run_time);
	int cutting_set_prox_logistic_evolve(double &run_time);
	int cutting_set_prox_logistic_evolve();
	int cutting_set_prox_svm_evolve();
	int cutting_set_prox_svm_evolve(double &run_time);
	int cutting_set_stoch_prox_svm_evolve();
    void normalize();
	void most_violated(int iteration, double &run_time);
    int remove_redunt(weight *w2b_temp,double s,int w_size_temp,int i_feat);

	//for poly
	void markov_sample_hash(weight *w2s, double *wlinear, int B,int iteration);
	
private:
	const parameter		*param;
	problem				*prob;
    double				*w_lin;// for linear features
	float               *QD;  //for feature
	int                 *QD_count;
	int					max_iteration;
	int                 n_ITER;
	int					elements;
	feature_node		**sub_x_space;//
	solution_struct			solution;
	double              *alpha;
	int					B;
	model				*svm_model;
	weight              *w2b_B;
	weight              *w2b_temp;
	int                 w_size_temp;
};



#endif  // LBFGSMKL_H_