#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <time.h>
#include "linear.h"

#if MX_API_VER < 0x07030000
typedef int mwIndex;
#endif

#define CMD_LEN 2048
#define Malloc(type,n) (type *)malloc((n)*sizeof(type))
#define INF HUGE_VAL

void print_null(const char *s){}

void (*liblinear_default_print_string) (const char *);

void exit_with_help()
{
	printf(
	"Usage: model = train(training_label_vector, training_instance_matrix, 'liblinear_options', 'col');\n"
	"liblinear_options:\n"
	"-s type : set type of solver (default 1)\n"
	"	0 -- L2-regularized logistic regression (not included)\n"
	"	1 -- L2-loss support vector machines (dual)(not included)\n"	
	"	2 -- L2-loss support vector machines (primal)(not included)\n"
	"	3 -- L1-loss support vector machines (dual)(not included)\n"
	"	4 -- multi-class support vector machines by Crammer and Singer(not included)\n"
	"	5 -- SVMFGM: Feature Generating Machine with Linear Kernel\n"
	"	6 -- LRFGM: Feature Generating Machine with logistic regression\n"
	"-c cost : set the parameter C (default 1)\n"
	"-e epsilon : set tolerance of termination criterion\n"
	"	-s 0 and 2\n" 
	"		|f'(w)|_2 <= eps*min(pos,neg)/l*|f'(w0)|_2,\n" 
	"		where f is the primal function, (default 0.01)\n"
	"	-s 1, 3, and 4\n"
	"		Dual maximal violation <= eps; similar to libsvm (default 0.1)\n"

	"Polynomial kernel (gamma*u'*v + coef0)^2\n"
	"-g gamma : set the parameter gamma (default 1)\n"
	"-r coef0 : set the parameter coef (default 1)\n"
	"-b bias : if bias >= 0, instance x becomes [x; bias]; if < 0, no bias term added (default -1)\n"
	"-wi weight: weights adjust the parameter C of different classes (see README for details)\n"
	"-v n: n-fold cross validation mode\n"
	"-q : quiet mode (no outputs)\n"

	"-p : whether or not use polynomial mappings (**cannot deal with very high dimensional problems)\n"
	"-t : retraining: whether or not do re-training with the selected features (default 1)\n"
	"-B : number of features: B should be greater than 1 (default 2)\n"
	"-m : number of iterations: (default 7)"
	"col:\n"
	"	if 'col' is set, training_instance_matrix is parsed in column format, otherwise is in row format\n"
	"version: 1.0\n"
	);
	exit(1);
}

// liblinear arguments
struct parameter param;		// set by parse_command_line
struct problem prob;		// set by read_problem
struct model *model_;
struct feature_node *x_space;
int cross_validation_flag;
int col_format_flag;
int nr_fold;
double bias;
static int max_line_len;
static char *line = NULL;

static char* readline(FILE *input)
{
	int len;
	
	if(fgets(line,max_line_len,input) == NULL)
		return NULL;

	while(strrchr(line,'\n') == NULL)
	{
		max_line_len *= 2;
		line = (char *) realloc(line,max_line_len);
		len = (int) strlen(line);
		if(fgets(line+len,max_line_len-len,input) == NULL)
			break;
	}
	return line;
}

double do_cross_validation()
{
	int i;
	int total_correct = 0;
	int *target = Malloc(int,prob.l);
	double retval = 0.0;

	cross_validation(&prob,&param,nr_fold,target);

	for(i=0;i<prob.l;i++)
		if(target[i] == prob.y[i])
			++total_correct;
	//mexPrintf("Cross Validation Accuracy = %g%%\n",100.0*total_correct/prob.l);
	retval = 100.0*total_correct/prob.l;

	free(target);
	return retval;
}



int parse_command_line(int argc, char **argv, char *input_file_name, char *model_file_name)
{
	int i;
	char cmd[CMD_LEN];
	// default values
	param.solver_type = L2LOSS_SVM_DUAL;
	param.C = 1;
	param.eps = INF; // see setting below
	param.nr_weight = 0;
	param.weight_label = NULL;
	param.weight = NULL;
	cross_validation_flag = 0;
	col_format_flag = 0;
	param.flag_poly = 0;
	param.random = 0; // not for random sampling.

	param.coef0 = 1.0;
	param.gamma = 1.0;
	bias = -1;
	param.t = 1;


	param.initial_type = 0;
	param.nB = 2;
	param.K = 2000;
	param.max_iteraion = 9;
	
	// parse options
	for(i=1;i<argc;i++)
	{
		if(argv[i][0] != '-') break;
		++i;
		if(i>=argc && argv[i-1][1] != 'z') // since option -z has no parameter
			return 1;
		switch(argv[i-1][1])
		{
			case 's':
				param.solver_type = atoi(argv[i]);
				break;
			case 'c':
				param.C = atof(argv[i]);
				break;
			case 'e':
				param.eps = atof(argv[i]);
				break;
			case 'p':
				param.flag_poly = atoi(argv[i]);
				break;
			case 'b':
				bias = atof(argv[i]);
				break;
			case 'B':
				param.nB = atoi(argv[i]);
				break;
			case 't':
				param.t = atoi(argv[i]);
				break;
			case 'I':
				param.initial_type = atoi(argv[i]);
				break;
			case 'v':
				cross_validation_flag = 1;
				nr_fold = atoi(argv[i]);
				if(nr_fold < 2)
				{
					//mexPrintf("n-fold cross validation: n must >= 2\n");
					return 1;
				}
				break;
			case 'w':
				++param.nr_weight;
				param.weight_label = (int *) realloc(param.weight_label,sizeof(int)*param.nr_weight);
				param.weight = (double *) realloc(param.weight,sizeof(double)*param.nr_weight);
				param.weight_label[param.nr_weight-1] = atoi(&argv[i-1][2]);
				param.weight[param.nr_weight-1] = atof(argv[i]);
				break;
			case 'r':
				param.coef0 = atof(argv[i]);
				break;

			case 'g':
				param.gamma = atof(argv[i]);
				break;
			case 'm':
				param.max_iteraion = atof(argv[i]);
				break;
			case 'k':
				param.K = atof(argv[i]);
				break;
			case 'q':
				//print_func = &print_null;
				i--;
			default:
				printf("unknown option\n");
				//mexPrintf("unknown option\n");
				return 1;
		}
	}

	if(param.eps == INF)
	{
		if(param.solver_type == L2_LR || param.solver_type == L2LOSS_SVM)
			param.eps = 0.01;
		else if(param.solver_type == L2LOSS_SVM_DUAL || param.solver_type == L1LOSS_SVM_DUAL || param.solver_type == MCSVM_CS)
		{
			param.eps = 0.1;
		}
		else
		{
			param.eps = 0.01;
		}
	}
	prob.bias = bias;

	prob.coef0 = param.coef0;
	prob.gamma = param.gamma;

	  if(i>=argc)
		exit_with_help();

	strcpy(input_file_name, argv[i]);

	if(i<argc-1)
		strcpy(model_file_name,argv[i+1]);
	else
	{
		char *p = strrchr(argv[i],'/');
		if(p==NULL)
			p = argv[i];
		else
			++p;
		sprintf(model_file_name,"%s.model",p);
	}

	if(param.eps == INF)
	{
		if(param.solver_type == L2_LR || param.solver_type == L2LOSS_SVM)
			param.eps = 0.01;
		else if(param.solver_type == L2LOSS_SVM_DUAL || param.solver_type == L1LOSS_SVM_DUAL || param.solver_type == MCSVM_CS)
		{
			param.eps = 0.1;
		}else
		{
			param.eps = 0.1;
		}
	}
param.eps = 0.1;
	prob.bias = bias;
	return 0;
}


void read_problem(const char *filename)
{
	int max_index, inst_max_index, i;
	long int elements, j;
	FILE *fp = fopen(filename,"r");
	char *endptr;
	char *idx, *val, *label;

	if(fp == NULL)
	{
		fprintf(stderr,"can't open input file %s\n",filename);
		exit(1);
	}

	prob.l = 0;
	elements = 0;
	max_line_len = 1024;
	line = Malloc(char,max_line_len);
	while(readline(fp)!=NULL)
	{
		char *p = strtok(line," \t"); // label

		// features
		while(1)
		{
			p = strtok(NULL," \t");
			if(p == NULL || *p == '\n') // check '\n' as ' ' may be after the last feature
				break;
			elements++;
		}
		elements++;
		prob.l++;
	}
	rewind(fp);

	prob.bias=bias;
	prob.elements = elements+prob.l;
	prob.y = Malloc(int,prob.l);
	prob.x = Malloc(struct feature_node *,prob.l);
	x_space = Malloc(struct feature_node,elements+prob.l);

	max_index = 0;
	j=0;
	for(i=0;i<prob.l;i++)
	{
		inst_max_index = 0; // strtol gives 0 if wrong format
		readline(fp);
		prob.x[i] = &x_space[j];
		label = strtok(line," \t");
		prob.y[i] = (int) strtol(label,&endptr,10);
		if(endptr == label)
			exit(1);//exit_input_error(i+1);

		while(1)
		{
			idx = strtok(NULL,":");
			val = strtok(NULL," \t");

			if(val == NULL)
				break;

			errno = 0;
			x_space[j].index = (int) strtol(idx,&endptr,10);
			if(endptr == idx || errno != 0 || *endptr != '\0' || x_space[j].index <= inst_max_index)
				exit(1);//exit_input_error(i+1);
			else
				inst_max_index = x_space[j].index;

			errno = 0;
			x_space[j].value = strtod(val,&endptr);
			if(endptr == val || errno != 0 || (*endptr != '\0' && !isspace(*endptr)))
				exit(1);//exit_input_error(i+1);

			++j;
		}

		if(inst_max_index > max_index)
			max_index = inst_max_index;

		if(prob.bias >= 0)
			x_space[j++].value = prob.bias;

		x_space[j++].index = -1;
	}

	if(prob.bias >= 0)
	{
		prob.n=max_index+1;
		for(i=1;i<prob.l;i++)
			(prob.x[i]-2)->index = prob.n; 
		x_space[j-2].index = prob.n;
	}
	else
		prob.n=max_index;
    prob.n_kernel = 1;
	fclose(fp);
}




// Interface function of matlab
// now assume prhs[0]: label prhs[1]: features
int main(int argc, char **argv)
{
	const char *error_msg;
	// fix random seed to have same results for each run
	// (for cross validation)
	srand(1);

	char input_file_name[1024];
	char model_file_name[1024];

   
	parse_command_line(argc, argv, input_file_name, model_file_name);
	read_problem(input_file_name);
	error_msg = check_parameter(&prob,&param);
	if(error_msg)
	{
		fprintf(stderr,"Error: %s\n",error_msg);
		destroy_param(&param);
		free(prob.y);
		free(prob.x);
		free(x_space);
		exit(1);
	}

	if(cross_validation_flag)
	{
		do_cross_validation();
	}
	else
	{
		model_=FGM_train(&prob, &param);
		printf("training is done!\n");
		save_model_poly(model_file_name, model_);
		printf("model is saved!\n");
		destroy_model(model_);
	}
        destroy_param(&param);
		free(prob.y);
		free(prob.x);
		free(x_space);
		
}
 