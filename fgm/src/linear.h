#ifndef _LIBLINEAR_H
#define _LIBLINEAR_H

#include <math.h>
#include <cmath>

using std::abs;

#ifdef __cplusplus
extern "C" {
#endif

	struct feature_node
	{
		int index;
		float value;
	};

	//To store the weight of features: used in polynomial case and other nonlinear case
	//struct weight
	//{
	//	int index1;//index 0 for single features
	//	int index2;//index2 = -1 indicate the linear features
	//	int indexi;// denote whether this component has been used, only effective for retraining
	//	int indexj;
	//	double value;// weight value
	//};

struct weight
{

    int index1;//index 0 for single features
	int index2;
	int indexi;// denote whether this componet has been used, only effictive for retraining
	int indexj;
    double value;

	bool operator<( weight & b)
	{
		return abs(value) < abs(b.value);
	}


	bool operator<=(weight & b)
	{
		return abs(value) <= abs(b.value);
	}
	/*bool operator<(weight &a,  weight & b)
	{
		return abs(a.value) < abs(b.value);
	}*/

	bool operator>(  weight & b)
	{
		return abs(value) > abs(b.value);
	}
	bool operator==(  weight & b)
	{
		return abs(value) == abs(b.value);
	}

	weight &operator = (  weight & b)
	{
		//weight a;
		index1 = b.index1;
		index2 = b.index2;
		value = b.value;
		return *this;
	}
	/*ostream & operator<<( weight & b ) 
	{
		cout << b.value;
	}*/
};

	struct solution_struct
	{
		weight *w_FGM; 
		weight *w_FGM_retrain;
		weight *w_FGM_B; //The best B features
	};
	struct problem
	{
		int l, n, n_kernel;
		int *y;
		struct feature_node **x;
		struct feature_node ***xsp; //to store sub_features 
		struct feature_node ***xsp_temp; //to store sub_features 
		double bias;            /* < 0 if no bias term */  
		int w_size;
		double coef0;
		double gamma;
		long int elements;
		int B;
	};

typedef struct 
{
	double val;
	int ind;
}
doubleIntPair;


	//5 7 9
	enum {L2R_LR, L2R_L2LOSS_SVC_DUAL, L2R_L2LOSS_SVC, L2R_L1LOSS_SVC_DUAL, MCSVM_CS, L1R_L2LOSS_SVC, L1R_LR, L2R_LR_DUAL, SVMFGM, LRFGM, MINLR, PSVMFGM, PROXFGM, PROXFGMLR, STOCHFGM,STOCHLR, OMPLR, OMPL2}; /* solver_type */

	struct parameter
	{
		int solver_type;
		/* these are for training only */
		double eps;	        /* stopping criteria */
		double C;
		int nr_weight;
		int *weight_label;
		double* weight;
		int initial_type;//0 for average initialization; 1 for training initialization;
		int flag_poly; //denote whether do poly or not
		int random;
		double coef0;
		double gamma;
		int max_iteraion;
		int t;
		int K; //sampling size
		int B; //number of features;
		int fCRS;
		int z;
		int Ks;
	};

	struct model
	{
		struct parameter param;
		int nr_class;		/* number of classes */
		int nr_feature;
		double *w;
		int *label;		/* label of each class (label[n]) */
		double bias;
		double *alpha;  /* liyf 08.10.28*/
		int l;                   /* number of instance liyf 08.10.28*/

		double *sigma;
		int n_kernel;
		weight *w2s; 
		solution_struct *solution_;
		int *count; //for uBalanced problem
		int w_size;
		int feature_pair;
		int B;
		double mkl_obj;
		double run_time;
	};

	typedef struct node
	{
		weight w;
		struct node *lLink;
		struct node *rLink;
	}Wt;


	struct model* train(const struct problem *prob, const struct parameter *param);
	struct model* FGM_train(struct problem *prob, const struct parameter *param);
	struct model* LSSVM_train(struct problem *prob, const struct parameter *param);
	void cross_validation(const struct problem *prob, const struct parameter *param, int nr_fold, int *target);

	void free_and_destroy_model(struct model **model_ptr_ptr);
	int check_probability_model(const struct model *model_);
	int predict_values(const struct model *model_, const struct feature_node *x, double* dec_values);
	int predict_values_poly(const struct model *model_, const struct feature_node *x, double* dec_values);
	int predict(const struct model *model_, const struct feature_node *x);
	int predict_poly(const struct model *model_, const struct feature_node *x, int flag);
	int predict_poly_value(const struct  model *model_, const feature_node *x, double &value,int flag);
	int predict_probability(const struct model *model_, const struct feature_node *x, double* prob_estimates);

	int save_model(const char *model_file_name, const struct model *model_);
	int save_model_poly(const char *model_file_name, const struct model *model_);
	struct model *load_model(const char *model_file_name);
	struct model *load_model_poly(const char *model_file_name);

	int get_nr_feature(const struct model *model_);
	int get_nr_class(const struct model *model_);
	void get_labels(const struct model *model_, int* label);

	void destroy_model(struct model *model_);
	void destroy_param(struct parameter *param);
	void destroy_predict_model(struct model *model_);

	const char *check_parameter(const struct problem *prob, const struct parameter *param);

#ifdef __cplusplus
}
#endif

#endif /* _LIBLINEAR_H */

