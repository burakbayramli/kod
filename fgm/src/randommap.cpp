//To facilitate the calculation, we use matlab 
//to generate the normal random vectors.
//First load the random vector, and do transform.
//This is only for group features;
//Here we do the transformation instance by instance and group by by group;
#include "linear.h"
#include <math.h>
void random_feature_d(float * destination, int &flag_zero, int group_start, int group_end,
					  float **normal_vector,int num_map,feature_node *x_)
{
	int i = 0;
	int k = 0;
	int group_size = group_end-group_start+1;
	int n_nonzeros = 0;

	feature_node *x = x_;
	if (x->index==-1||x->index>group_end)
	{
		for(i=0;i<num_map;i++)
		{
			destination[i] = 1;
			destination[i + num_map] = 0;
		}
		flag_zero = 0;
		return;
	}
	flag_zero = 1;
	float *new_feature = new float[num_map];
	for (k=0;k<num_map; k++)
	{
		new_feature[k] = 0;
	}
	while (x->index!=-1&&x->index<group_end+1)
	{
		for (k=0; k<num_map; k++)
		{
			new_feature[k] = x->value * normal_vector[k][x->index-group_start];
		}
		x++;
	}
	for (k=0; k<num_map; k++)
	{
		destination[k] = cos(new_feature[k]);
		destination[k+num_map] = sin(new_feature[k]);
	}
}


void HIK_feature_d(float *destination, int &flag_zero, 
					  float V_min,float V_max, int V_bar, float x)
{
	int i = 0;
	int k = 0;
	//do transformation
	int v = int (V_bar*(x-V_min)/(V_max-V_min));
	int v_value = max(v,V_bar);
	for (i = 0;i<v_value;i++)
	{
		destination[i] = 1;
	}
	
}