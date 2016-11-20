#include <stdio.h>
#include <ctype.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include "linear.h"


#if MX_API_VER < 0x07030000
typedef int mwIndex;
#endif

#define CMD_LEN 2048
#define Malloc(type,n) (type *)malloc((n)*sizeof(type))
#define INF HUGE_VAL

void print_null(const char *s){}

void (*liblinear_default_print_string) (const char *);


struct feature_node *x;
int max_nr_attr = 64;
struct problem prob;		// set by read_problem
struct feature_node *x_space;
int cross_validation_flag;
int col_format_flag;
int nr_fold;
double bias;


struct model* model_;
int flag_predict_probability=0;

void exit_input_error(int line_num)
{
	fprintf(stderr,"Wrong input format at line %d\n", line_num);
	exit(1);
}

static char *line = NULL;
static int max_line_len;

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

void do_predict(FILE *input, FILE *output, struct model* model_)
{
	int correct = 0;
	int total = 0;

	int nr_class=get_nr_class(model_);
	double *prob_estimates=NULL;
	int j, n;
	int nr_feature=get_nr_feature(model_);
	if(model_->bias>=0)
		n=nr_feature+1;
	else
		n=nr_feature;

	if(flag_predict_probability)
	{
		int *labels;

		if(model_->param.solver_type!=L2R_LR)
		{
			fprintf(stderr, "probability output is only supported for logistic regression\n");
			exit(1);
		}

		labels=(int *) malloc(nr_class*sizeof(int));
		get_labels(model_,labels);
		prob_estimates = (double *) malloc(nr_class*sizeof(double));
		fprintf(output,"labels");		
		for(j=0;j<nr_class;j++)
			fprintf(output," %d",labels[j]);
		fprintf(output,"\n");
		free(labels);
	}

	max_line_len = 1024;
	line = (char *)malloc(max_line_len*sizeof(char));
	while(readline(input) != NULL)
	{
		int i = 0;
		int target_label, predict_label;
		char *idx, *val, *label, *endptr;
		int inst_max_index = 0; // strtol gives 0 if wrong format

		label = strtok(line," \t");
		target_label = (int) strtol(label,&endptr,10);
		if(endptr == label)
			exit_input_error(total+1);

		while(1)
		{
			if(i>=max_nr_attr-2)	// need one more for index = -1
			{
				max_nr_attr *= 2;
				x = (struct feature_node *) realloc(x,max_nr_attr*sizeof(struct feature_node));
			}

			idx = strtok(NULL,":");
			val = strtok(NULL," \t");

			if(val == NULL)
				break;
			errno = 0;
			x[i].index = (int) strtol(idx,&endptr,10);
			if(endptr == idx || errno != 0 || *endptr != '\0' || x[i].index <= inst_max_index)
				exit_input_error(total+1);
			else
				inst_max_index = x[i].index;

			errno = 0;
			x[i].value = strtod(val,&endptr);
			if(endptr == val || errno != 0 || (*endptr != '\0' && !isspace(*endptr)))
				exit_input_error(total+1);

			// feature indices larger than those in training are not used
			if(x[i].index <= nr_feature)
				++i;
		}

		if(model_->bias>=0)
		{
			x[i].index = n;
			x[i].value = model_->bias;
			i++;
		}
		x[i].index = -1;

		if(flag_predict_probability)
		{
			int j;
			predict_label = predict_probability(model_,x,prob_estimates);
			fprintf(output,"%d",predict_label);
			for(j=0;j<model_->nr_class;j++)
				fprintf(output," %g",prob_estimates[j]);
			fprintf(output,"\n");
		}
		else
		{
			predict_label = predict(model_,x);
			fprintf(output,"%d\n",predict_label);
		}

		if(predict_label == target_label)
			++correct;
		++total;
	}
	printf("Accuracy = %g%% (%d/%d)\n",(double) correct/total*100,correct,total);
	if(flag_predict_probability)
		free(prob_estimates);
}
double do_predict_poly(FILE *input, FILE *output, struct model* model_, int flag = 0)
{
	int correct = 0;
	int total = 0;

	int nr_class=get_nr_class(model_);
	double *prob_estimates=NULL;
	int j, n;
	int nr_feature=get_nr_feature(model_);
	if(model_->bias>=0)
		n=nr_feature+1;
	else
		n=nr_feature;

	if(flag_predict_probability)
	{
		int *labels;

		if(model_->param.solver_type!=L2R_LR)
		{
			fprintf(stderr, "probability output is only supported for logistic regression\n");
			exit(1);
		}

		labels=(int *) malloc(nr_class*sizeof(int));
		get_labels(model_,labels);
		prob_estimates = (double *) malloc(nr_class*sizeof(double));
		fprintf(output,"labels");		
		for(j=0;j<nr_class;j++)
			fprintf(output," %d",labels[j]);
		fprintf(output,"\n");
		free(labels);
	}

	max_line_len = 1024;
	line = (char *)malloc(max_line_len*sizeof(char));
	while(readline(input) != NULL)
	{
		int i = 0;
		int target_label, predict_label;
		char *idx, *val, *label, *endptr;
		int inst_max_index = 0; // strtol gives 0 if wrong format

		label = strtok(line," \t");
		target_label = (int) strtol(label,&endptr,10);
		if (target_label<=0)
		{
			target_label = -1;
		}else
		{
			target_label = 1;
		}
		if(endptr == label)
			exit_input_error(total+1);

		while(1)
		{
			if(i>=max_nr_attr-2)	// need one more for index = -1
			{
				max_nr_attr *= 2;
				x = (struct feature_node *) realloc(x,max_nr_attr*sizeof(struct feature_node));
			}

			idx = strtok(NULL,":");
			val = strtok(NULL," \t");

			if(val == NULL)
				break;
			errno = 0;
			x[i].index = (int) strtol(idx,&endptr,10);
			if(endptr == idx || errno != 0 || *endptr != '\0' || x[i].index <= inst_max_index)
				exit_input_error(total+1);
			else
				inst_max_index = x[i].index;

			errno = 0;
			x[i].value = strtod(val,&endptr);
			if(endptr == val || errno != 0 || (*endptr != '\0' && !isspace(*endptr)))
				exit_input_error(total+1);

			// feature indices larger than those in training are not used
			if(x[i].index <= nr_feature)
				++i;
		}

		if(model_->bias>=0)
		{
			x[i].index = n;
			x[i].value = model_->bias;
			i++;
		}
		x[i].index = -1;

		if(flag_predict_probability)
		{
			int j;
			predict_label = predict_probability(model_,x,prob_estimates);
			fprintf(output,"%d",predict_label);
			for(j=0;j<model_->nr_class;j++)
				fprintf(output," %g",prob_estimates[j]);
			fprintf(output,"\n");
		}
		else
		{
			//predict_label = predict_poly(model_,x,flag);
			double value = 0.0;
			predict_label = predict_poly_value(model_,x,value,flag);
			fprintf(output,"%f\n",value);
		}

		if(predict_label == target_label)
			++correct;
		++total;
	}

	printf("Accuracy = %g  %% (%d/%d)\n",(double) correct/total*100,correct,total);
	if(flag_predict_probability)
		free(prob_estimates);
	return (double) correct/total*100;
}

double batch_do_predict_poly(FILE *output, struct model* model_, int flag = 0)
{
	int correct = 0;
	int total = 0;

	int nr_class=get_nr_class(model_);
	double *prob_estimates=NULL;
	int j, n;
	int nr_feature=get_nr_feature(model_);
	if(model_->bias>=0)
		n=nr_feature+1;
	else
		n=nr_feature;

	if(flag_predict_probability)
	{
		int *labels;

		if(model_->param.solver_type!=L2R_LR)
		{
			fprintf(stderr, "probability output is only supported for logistic regression\n");
			exit(1);
		}

		labels=(int *) malloc(nr_class*sizeof(int));
		get_labels(model_,labels);
		prob_estimates = (double *) malloc(nr_class*sizeof(double));
		fprintf(output,"labels");		
		for(j=0;j<nr_class;j++)
			fprintf(output," %d",labels[j]);
		fprintf(output,"\n");
		free(labels);
	}

    for(int k = 0; k< prob.l; k++)
	{
		int target_label, predict_label;
		target_label = prob.y[k];
		if (target_label<=0)
		{
			target_label = -1;
		}else
		{
			target_label = 1;
		}
		if(flag_predict_probability)
		{
			int j;
			predict_label = predict_probability(model_,x,prob_estimates);
			fprintf(output,"%d",predict_label);
			for(j=0;j<model_->nr_class;j++)
				fprintf(output," %g",prob_estimates[j]);
			fprintf(output,"\n");
		}
		else
		{
			predict_label = predict_poly(model_,prob.x[k],flag);
			fprintf(output,"%d\n",predict_label);
		}

		if(predict_label == target_label)
			++correct;
		++total;
	}

	printf("Accuracy = %g  %% (%d/%d)\n",(double) correct/total*100,correct,total);
	if(flag_predict_probability)
		free(prob_estimates);
	return (double) correct/total*100;
}

void exit_with_help()
{
	printf(
		"Usage: predict [options] test_file model_file output_file\n"
		"options:\n"
		"-b probability_estimates: whether to output probability estimates, 0 or 1 (default 0)\n"
		);
	exit(1);
}


void read_problem(const char *filename)
{
	int max_index, inst_max_index, i;
		int  n;
	int nr_feature=get_nr_feature(model_);
	if(model_->bias>=0)
		n=nr_feature+1;
	else
		n=nr_feature;

		//if(model_->bias>=0)
		//{
		//	x[i].index = n;
		//	x[i].value = model_->bias;
		//	i++;
		//}
		//x[i].index = -1;

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

	prob.bias=model_->bias;
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
		if(prob.y[i]<1)
		{
			prob.y[i] = -1;
		}
		else
		{
			prob.y[i] = 1;
		}
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

			if(x_space[j].index <= nr_feature)
				++j;
			
		}

		//if(inst_max_index > max_index)
		//	max_index = inst_max_index;
  	
		if(prob.bias >= 0)
		{
			x_space[j].index = n;
			x_space[j].value = prob.bias;
			j++;
		}
		x_space[j++].index = -1;
	}
	fclose(fp);
}

void read_parameter(const char *filename, double *&param_enty, char &char_para, int &length_param);


void read_parameter(FILE *fp, double *&param_enty, char &char_para, int &length_param)
{
	double fVal;
	char_para  = getc(fp);
	int i = 0;
	//if (char_para=='C')
	{
		while(!feof(fp))		
		{			
			fVal = 0;		
			fscanf(fp," %lf",&fVal);	
			if(fVal != 0)				
				printf(" %lf\n",fVal);	
			i++;
		}	
	}
	length_param = i;
    param_enty = new double [length_param];
	i = 0;
	rewind(fp);
	getc(fp);
	//if (char_para=='C')
	{
		while(!feof(fp))		
		{			
			fVal = 0;		
			fscanf(fp," %lf",&fVal);	
			param_enty[i] = fVal;
			i++;
		}	
	}
	fclose(fp);
}


int main(int argc, char **argv)
{
	FILE *input, *output, *param_file, *out_result;
	int i;
	int k;
    int flag =0;
	double *para_entry;
	char char_para;
	int length_param;

	// parse options
	for(i=1;i<argc;i++)
	{
		if(argv[i][0] != '-') break;
		++i;
		switch(argv[i-1][1])
		{
		case 'b':
			flag_predict_probability = atoi(argv[i]);
			break;
		case 'f':
			flag = atoi(argv[i]);
			break;

		default:
			fprintf(stderr,"unknown option: -%c\n", argv[i-1][1]);
			exit_with_help();
			break;
		}
	}
	if(i>=argc)
		exit_with_help();

	input = fopen(argv[i],"r");
	if(input == NULL)
	{
		fprintf(stderr,"can't open input file %s\n",argv[i]);
		exit(1);
	}


    if (flag==1)
	{
		//for experiments, we need test many parameters
		int para_B[40] = {2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18, 20, 24, 25, 26, 30,32, 35, 38, 40, 42, 45, 48, 50, 55, 60, 65,70,75,80, 85, 90, 95, 100, 105, 110, 115, 120};
		//int para_B[26] = {20,  40,  60, 80, 100, 120, 140, 160, 180, 200, 220, 240, 260, 280, 300, 320, 340, 360, 380, 400};
		param_file = fopen(argv[i+3],"r");
		if(param_file == NULL)
		{
			fprintf(stderr,"can't open parameter file %s\n",argv[i+3]);
			exit(1);
		}
		read_parameter(param_file, para_entry, char_para, length_param);
		if (char_para=='B')
		{
			length_param = 40;
		}

		char out_put_result[1000];
        strcpy(out_put_result,argv[i+4]);
        //strcpy(out_put_result,argv[i+4]);
		//strcat(out_put_result,argv[i+1]);
		//strcpy(out_put_result,"_result");
		out_result = fopen(out_put_result,"w");

		double acc = 0.0;
		//strcat(out_put_result,"_result\n");
		//fprintf(out_result, out_put_result); 
		for(k=0;k<length_param;k++)
		{
		
			char model_file[1024];
			char param_char[1000];
			strcpy(model_file,argv[i+1]);
			if (char_para=='C')
			{
				sprintf(param_char, "%.10lf ", para_entry[k]); 
				printf("C = %.4lf\n ", para_entry[k]); 
				fprintf(out_result, "%.10lf\n", para_entry[k]); 
				strcat(model_file,".c.");
				strcat(model_file,param_char);
			}
			else
			{
				int B = para_B[k];
				sprintf(param_char, "%d ", B); 
				printf("B = %d\n", B); 
				fprintf(out_result, "%d\n", B); 
				strcat(model_file,".B.");
				strcat(model_file,param_char);
			}

			output = fopen(argv[i+2],"w");
			if(output == NULL)
			{
				fprintf(stderr,"can't open output file %s\n",argv[i+2]);
				exit(1);
			}

			if((model_=load_model_poly(model_file))==0)
			{
				fprintf(stderr,"can't open model file %s\n",model_file);
				exit(1);
			}
		
			printf("Training time = %lf\n", model_->run_time);
			printf("Feature number = %d\n", model_->feature_pair);
			fprintf(out_result,"%lf\n", model_->run_time);
			fprintf(out_result,"%d\n", model_->feature_pair);
			//load problem at the first time
			if (k==0)
			{
				read_problem(argv[i]);
			}
			acc = batch_do_predict_poly(output, model_);
			fprintf(out_result,"%g\n", acc);
			if (model_->param.t==1)
			{
				//rewind(input);
				acc = batch_do_predict_poly(output, model_,1);///
				fprintf(out_result,"%g\n", acc);
				//rewind(input);
				//do_predict_poly(input, output, model_,2);
			}
			
			printf("\n");
			fprintf(out_result,"\n", acc);
			//rewind(input);
			destroy_model(model_);
			//free(line);
			//free(x);
			//fclose(input);
			fclose(output);
		}
			fclose(out_result);
	}
	else
	{
			output = fopen(argv[i+2],"w");
			if(output == NULL)
			{
				fprintf(stderr,"can't open output file %s\n",argv[i+2]);
				exit(1);
			}

			if((model_=load_model_poly(argv[i+1]))==0)
			{
				fprintf(stderr,"can't open model file %s\n",argv[i+1]);
				exit(1);
			}

			x = (struct feature_node *) malloc(max_nr_attr*sizeof(struct feature_node));
			printf("training time = %lf\n", model_->run_time);
			//printf("feature number = %d\n", model_->feature_pair);
			printf("feature number = %d\n", model_->B);
			do_predict_poly(input, output, model_,2);
			printf("\n");
			if (model_->param.t==1)
			{
				rewind(input);
				do_predict_poly(input, output, model_,1);///
				//rewind(input);
				//do_predict_poly(input, output, model_,2);
			}
			destroy_model(model_);
			free(line);
			free(x);
			fclose(output);
	}

  
	free(prob.y);
	free(prob.x);
	free(x_space);
	fclose(input);
	return 0;
}

