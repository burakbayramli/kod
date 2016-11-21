/****************************************************************/
A linear-time SVM-type feature selection algorithm is proposed 
for large-scale and extremely high dimensional datasets, a very 
small subset of non-monotonic features can be identified from 3 
Million features for suspicious URLs prediction: 
Mingkui Tan, Li Wang, Ivor W. Tsang. Learning Sparse SVM for Feature 
Selection on Very High Dimensional Datasets. Proceedings
of the 27th International Conference on Machine Learning 
(ICML 2010), Haifa, Israel, June 2010.  


/******************************************************************/
Usage Options:

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
	"-m : number of iterations: (default 7)\n"

/******************************************************************/

Examples:

/***train

/**linear SVM ***/
train -s 5 -B 10 -c 5 -m 8 -t 1 - p 0 your_data_route\dataset  your_model_route\model

/**linear logistic regression (LR) ***/
train -s 6 -B 10 -c 5 -m 8 -t 1 - p 0 your_data_route\dataset  your_model_route\model

/**polynomial SVM ***/
train -s 5 -B 10 -c 5 -m 8 -t 1 - p 1 your_data_route\dataset  your_model_route\model

/**polynomial logistic regression (LR) ***/
train -s 6 -B 10 -c 5 -m 8 -t 1 - p 1 your_data_route\dataset  your_model_route\model

/***predict

predict your_test_data_route\test_data your_model_route\model your_model_route\output



/****************************************************************/

Acknowledgement:

We use the liblinear(http://www.csie.ntu.edu.tw/~cjlin/liblinear/)
as our svm solver.
We adapt the simpleMKL solver by Yufeng Li (http://lamda.nju.edu.cn/liyf/).
The auther are very grateful for their contributions!



