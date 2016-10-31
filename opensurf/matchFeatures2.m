function [index_pairs, match_metric] = matchFeatures2(varargin)
%matchFeatures Find matching features
%   INDEX_PAIRS = matchFeatures(FEATURES1, FEATURES2) returns a P-by-2
%   matrix, INDEX_PAIRS, containing indices to the features most likely to
%   correspond between the two input feature matrices. The function takes
%   two inputs, FEATURES1, an M1-by-N matrix, and FEATURES2, an M2-by-N
%   matrix. FEATURES1 and FEATURES2 can also be binaryFeatures objects in 
%   the case of binary descriptors produced by the FREAK descriptor.
%
%   [INDEX_PAIRS, MATCH_METRIC] = matchFeatures(FEATURES1, FEATURES2, ...)
%   also returns the metric values that correspond to the associated
%   features indexed by INDEX_PAIRS in a P-by-1 matrix MATCH_METRIC.
%
%   [INDEX_PAIRS, MATCH_METRIC] = matchFeatures(...,Name, Value) specifies
%   additional name-value pairs described below:
%
%   'Method'           A string used to specify the matching strategy. All
%                      three methods use the match threshold. Two feature 
%                      vectors match when the distance between them is less 
%                      than the threshold set by the MatchThreshold 
%                      parameter. Set the method to one of the following: 
%
%                      'Threshold': Only uses the match threshold. This 
%                         method can return more than one match for each 
%                         feature. 
%      
%                      'NearestNeighborSymmetric': Only returns unique 
%                         matches in addition to using the match threshold. 
%                         A feature vector only matches to its nearest 
%                         neighbor in the other feature set.
%
%                      'NearestNeighborRatio': Eliminates ambiguous matches 
%                         in addition to using the match threshold. A 
%                         feature vector is matched to its nearest neighbor 
%                         in the other feature set, when the nearest 
%                         neighbor satisfies a ratio test. The ratio test 
%                         compares the distances from the feature vector to
%                         its first and second nearest neighbors in the 
%                         other feature set.
%
%                      Default: 'NearestNeighborRatio'
%
%   'MatchThreshold'   A scalar T, 0 < T <= 100, specifying a threshold
%                      for selecting the strongest matches. Matches having
%                      metrics more than T percent from a perfect match 
%                      are rejected. Increase T to return more matches.
% 
%                      Default: 10.0 for binary feature vectors 
%                                1.0 otherwise
%
%   'MaxRatio'         A scalar R, 0 < R <= 1, specifying a ratio threshold 
%                      for rejecting ambiguous matches. Increase R to return
%                      more matches. This parameter is used only with
%                      'NearestNeighborRatio' method.
%
%                      Default: 0.6
%
%   'Metric'           A string used to specify the feature matching
%                      metric. This parameter is not applicable when 
%                      FEATURES1 and FEATURES2 are binaryFeatures objects.
%                      Possible values are:
%                        'SAD'         : Sum of absolute differences
%                        'SSD'         : Sum of squared differences 
%                        'normxcorr'   : Normalized cross-correlation
%
%                      Default: 'SSD'
%
%                      Note: When FEATURES1 and FEATURES2 are binaryFeatures 
%                            objects, Hamming distance is used to compute
%                            the similarity metric.
%
%   'Prenormalized'    A logical scalar. Use true to indicate that FEATURES1
%                      and FEATURES2 are already normalized to unit vectors 
%                      prior to matching. Setting this flag to false will 
%                      result in normalizing FEATURES1 and FEATURES2. Note 
%                      that setting this flag to true when features are not 
%                      normalized in advance will produce wrong results. 
%                      This parameter is not applicable when FEATURES1 and 
%                      FEATURES2 are binaryFeatures objects.
%
%                      Default: false
%
%   Notes
%   ----- 
%   The range of values of MATCH_METRIC varies as a function of the feature
%   matching metric being used. Prior to computation of SAD and SSD
%   metrics, the feature vectors are normalized to unit vectors. The table
%   below summarizes the metric ranges and perfect match values:
%
%      Metric      Range                            Perfect Match Value
%      ----------  -------------------------------  ------------------- 
%      SAD         [0, 2*sqrt(size(FEATURES1, 1))]          0
%      SSD         [0, 4]                                   0
%      normxcorr   [-1, 1]                                  1
%
%      Hamming     [0, FEATURES1.NumBits]                   0
%
%   This function changed in the release R2012b. Previous versions
%   used a different matching strategy. If you need the same results 
%   produced by the previous implementation, use 
%   matchFeatures(FEATURES1, FEATURES2, 'Method', 'NearestNeighbor_old',...).
%   
%   Class Support
%   -------------
%   FEATURES1 and FEATURES2 can be logical, int8, uint8, int16, uint16,
%   int32, uint32, single, double, or binaryFeatures object.
%
%   The output class of INDEX_PAIRS is uint32. MATCH_METRIC is double when
%   FEATURES1 and FEATURES2 are double. Otherwise, it is single.
%
%   Example 1
%   ---------
%   % Find corresponding interest points between a pair of images using
%   % local neighborhoods.
%   I1 = rgb2gray(imread('viprectification_deskLeft.png'));
%   I2 = rgb2gray(imread('viprectification_deskRight.png'));
% 
%   % Find corners  
%   points1 = detectHarrisFeatures(I1);
%   points2 = detectHarrisFeatures(I2);
% 
%   % Extract neighborhood features
%   [features1, valid_points1] = extractFeatures(I1, points1);
%   [features2, valid_points2] = extractFeatures(I2, points2);
% 
%   % Match features
%   index_pairs = matchFeatures(features1, features2);
% 
%   % Retrieve locations of corresponding points for each image
%   matched_points1 = valid_points1(index_pairs(:, 1), :);
%   matched_points2 = valid_points2(index_pairs(:, 2), :);
% 
%   % Note that you can clearly see the effect of translation
%   % between the two images despite several erroneous matches.
%   figure; showMatchedFeatures(I1, I2, matched_points1, matched_points2);
%
%   Example 2
%   ---------
%   % Use SURF features to find corresponding points between two images
%   % rotated and scaled with respect to each other
%   I1 = imread('cameraman.tif');
%   I2 = imresize(imrotate(I1,-20), 1.2);
% 
%   points1 = detectSURFFeatures(I1);
%   points2 = detectSURFFeatures(I2);
% 
%   [f1, vpts1] = extractFeatures(I1, points1);
%   [f2, vpts2] = extractFeatures(I2, points2);
%         
%   % SURF feature vectors are already normalized.
%   index_pairs = matchFeatures(f1, f2, 'Prenormalized', true);
%   matched_pts1 = vpts1(index_pairs(:, 1));
%   matched_pts2 = vpts2(index_pairs(:, 2));
%   
%   % Note that there are still several outliers present in the data, but
%   % otherwise you can clearly see the effects of rotation and scaling on 
%   % the display of matched features.
%   figure; showMatchedFeatures(I1,I2,matched_pts1,matched_pts2);
%   legend('matched points 1','matched points 2');
%
% See also showMatchedFeatures, vision.CornerDetector, detectSURFFeatures,
%          detectMSERFeatures, extractFeatures, estimateFundamentalMatrix, 
%          estimateGeometricTransform, binaryFeatures.
 
%  Copyright 2011 The MathWorks, Inc.
%
%   We use the normalized cross-correlation as defined in this paper:
%   
%      "Fast Normalized Cross-Correlation", by J. P. Lewis, Industrial
%      Light & Magic.
%
%   We implement the nearest-neighbor ratio method as defined in
%   this paper:
%
%      David Lowe, "Distinctive image features from scale-invariant 
%      keypoints," International Journal of Computer Vision, 60, 2 (2004)
%
%   The three matching strategies are described in this paper:
%   
%      K. Mikolajczyk and C. Shmid, "A Performance Evaluation of Local 
%      Descriptors," IEEE PAMI, 27, 10 (2005)

%#codegen
%#ok<*EMCLS>
%#ok<*EMCA>

isUsingCodeGeneration = ~isempty(eml.target);

% Parse and check inputs
if isUsingCodeGeneration
    [features1, features2, metric, match_thresh, method, maxRatioThreshold, ...
        isPrenormalized] = parseInputsCodegen(varargin{:});
else
    [features1, features2, metric, match_thresh, method, maxRatioThreshold, ...
        isPrenormalized] = parseInputs(varargin{:});
end

checkFeatureConsistency(features1, features2);

% Match features
features1 = features1';
features2 = features2';
[index_pairs_internal, match_metric_internal] = ...
    cvalgMatchFeatures2(features1, features2, ...
                       metric, match_thresh, method, maxRatioThreshold, isPrenormalized);
index_pairs = index_pairs_internal';
match_metric = match_metric_internal';

%==========================================================================
% Make sure the features are of compatible classes and sizes
%==========================================================================
function checkFeatureConsistency(features1, features2)

coder.internal.errorIf(size(features1, 2) ~= size(features2, 2), ...
                       'vision:matchFeatures:featuresNotSameDimension');

%==========================================================================
% Parse and check inputs for code generation
%==========================================================================
function [features1, features2, metric, match_thresh, method, ...
    maxRatioThreshold, isPrenormalized] = parseInputsCodegen(varargin)

eml_lib_assert(nargin == 2 || nargin > 3, ...
    'vision:matchFeatures:NotEnoughArgs', ...
    'Not enough input arguments.');

f1 = varargin{1};
f2 = varargin{2};

checkFeatures(f1);
checkFeatures(f2);

isBinaryFeature = isa(f1, 'binaryFeatures');
defaults = getDefaultParameters(isBinaryFeature);

% Set parser inputs
params = struct( ...
    'Metric',                uint32(0), ...
    'MatchThreshold',        uint32(0), ...
    'Method',                uint32(0), ...
    'MaxRatio',              uint32(0), ...
    'Prenormalized',         uint32(0));

popt = struct( ...
    'CaseSensitivity', true, ...
    'StructExpand',    true, ...
    'PartialMatching', false);

if (nargin > 3)
    % Parse parameter/value pairs
    optarg = eml_parse_parameter_inputs(params, popt, varargin{3:end});
    
    metric = tolower(eml_get_parameter_value(optarg.Metric, ...
        defaults.Metric, varargin{3:end}));    
    match_thresh  = eml_get_parameter_value(optarg.MatchThreshold, ...
        defaults.MatchThreshold, varargin{3:end});
    method = tolower(eml_get_parameter_value(optarg.Method, ...
        defaults.Method, varargin{3:end}));
    maxRatioThreshold = eml_get_parameter_value(optarg.MaxRatio, ...
        defaults.MaxRatio, varargin{3:end});
    isPrenormalizedFromUser = eml_get_parameter_value(optarg.Prenormalized, ...
        defaults.Prenormalized, varargin{3:end});
    
    % Check parameters
    checkMetric(metric);
    checkMatchThreshold(match_thresh);
    checkMatchMethod(method);
    checkMaxRatioThreshold(maxRatioThreshold);   
    checkPrenormalized(isPrenormalizedFromUser);
    
    isPrenormalized = logical(isPrenormalizedFromUser);
else
    metric = defaults.Metric;
    match_thresh = defaults.MatchThreshold;
    method = defaults.Method;
    maxRatioThreshold = defaults.MaxRatio;
    isPrenormalized = logical(defaults.Prenormalized);
end

[features1, features2, metric] = assignFeaturesAndMetric(f1, f2, metric);

%==========================================================================
% Parse and check inputs
%==========================================================================
function [features1, features2, metric, match_thresh, method, ...
    maxRatioThreshold, isPrenormalized] = parseInputs(varargin)

if nargin >= 1
    isBinaryFeature = isa(varargin{1}, 'binaryFeatures');
else
    isBinaryFeature = false;
end

defaults = getDefaultParameters(isBinaryFeature);

% Setup parser
parser = inputParser;
parser.CaseSensitive = true;
parser.addRequired('features1', @checkFeatures);
parser.addRequired('features2', @checkFeatures);
parser.addParamValue('MatchThreshold', defaults.MatchThreshold, ...
    @checkMatchThreshold);
parser.addParamValue('Method', defaults.Method, @checkMatchMethod);
parser.addParamValue('MaxRatio', defaults.MaxRatio, ...
    @checkMaxRatioThreshold);
parser.addParamValue('Metric', defaults.Metric, @checkMetric);
parser.addParamValue('Prenormalized', defaults.Prenormalized, ...
    @checkPrenormalized);

% Parse input
parser.parse(varargin{:});

% 'MaxRatio' only has meaning if the 'Method' is 'NearestNeighborRatio'
if ~strcmpi(parser.Results.Method, 'NearestNeighborRatio') && ...
        isParameterSetByCaller('MaxRatio', parser)
    warning(message('vision:matchFeatures:maxRatioUnused'));
end

if isBinaryFeature
    if isParameterSetByCaller('Prenormalized', parser)
        warning(message('vision:matchFeatures:binParamUnused', 'Prenormalized'));
    end
    
    if isParameterSetByCaller('Metric', parser)
        warning(message('vision:matchFeatures:binParamUnused', 'Metric'));
    end
end

f1 = parser.Results.features1;
f2 = parser.Results.features2;
metric = parser.Results.Metric;
[features1, features2, metric] = assignFeaturesAndMetric(f1, f2, metric);

match_thresh = parser.Results.MatchThreshold;
method = tolower(parser.Results.Method);
maxRatioThreshold = parser.Results.MaxRatio;
isPrenormalized = logical(parser.Results.Prenormalized);

%==========================================================================
function [features1, features2, metric] = assignFeaturesAndMetric(f1, f2,...
    temp_metric)

% Handle the case when features are of class binaryFeatures

coder.internal.errorIf(~isequal(class(f1), class(f2)),...
     'vision:matchFeatures:featuresNotSameClass');

% Assign outputs
if isa(f1, 'binaryFeatures')
    features1 = f1.Features;
    features2 = f2.Features;
    metric = 'hamming'; % assume default metric for the binary features
else
    features1 = f1;
    features2 = f2;
    metric = tolower(temp_metric);
end

%==========================================================================
function tf = isParameterSetByCaller(param, parser)
tf = strcmp(param, parser.UsingDefaults);
tf = ~any(tf);

%==========================================================================
function tf = checkFeatures(features)
validateattributes(features, {'logical', 'int8', 'uint8', 'int16', ...
    'uint16', 'int32', 'uint32', 'single', 'double', 'binaryFeatures'}, ...
    {'2d', 'nonsparse', 'real'}, 'matchFeatures', 'FEATURES');
tf = true;

%========================================================================== 
function tf = checkMetric(value)
list = {'ssd', 'normxcorr', 'sad'};
validateattributes(value, {'char'}, {'nonempty'}, 'matchFeatures', ...
    'Metric');
value = tolower(value);
matchedValue = validatestring(value, list, 'matchFeatures', 'Metric');   
coder.internal.errorIf(~strcmp(value, matchedValue), ...
     'vision:validateString:unrecognizedStringChoice', value);
tf = true;

%========================================================================== 
function tf = checkMatchMethod(value)
list = {'NearestNeighborRatio', 'Threshold', 'NearestNeighborSymmetric',...
    'NearestNeighbor_old'};
validateattributes(value, {'char'}, {'nonempty'}, 'matchFeatures', ...
    'Method');
matchedValue = validatestring(value, list, 'matchFeatures', 'Method');

coder.internal.errorIf(~strcmp(tolower(value), tolower(matchedValue)), ...
     'vision:validateString:unrecognizedStringChoice', value);
tf = true;

%==========================================================================
function tf = checkMatchThreshold(threshold)
validateattributes(threshold, {'numeric'}, {'nonempty', 'nonnan', ...
    'finite', 'nonsparse', 'real', 'positive', 'scalar', '<=', 100}, ...
    'matchFeatures', 'MatchThreshold');
tf = true;

%==========================================================================
function tf = checkMaxRatioThreshold(threshold)
validateattributes(threshold, {'numeric'}, {'nonempty', 'nonnan', ...
    'finite', 'nonsparse', 'real', 'positive', 'scalar', '<=', 1.0}, ...
    'matchFeatures', 'MaxRatioThreshold');
tf = true;

%==========================================================================
function tf = checkPrenormalized(isPrenormalized)
validateattributes(isPrenormalized, {'logical','numeric'}, ...
    {'nonempty', 'scalar', 'real', 'nonnan', 'nonsparse'}, ...
    'matchFeatures', 'Prenormalized');
tf = true;

%==========================================================================
function defaults = getDefaultParameters(isBinaryFeature)

if isBinaryFeature
    thresh = 10.0;
else
    thresh = 1.0;
end

defaults = struct(...
    'Metric', 'ssd', ...
    'MatchThreshold',  thresh, ...
    'Method', tolower('NearestNeighborRatio'),...
    'MaxRatio', 0.6,...
    'Prenormalized', false);

%==========================================================================
function str = tolower(str)
% convert a string to lower case 
% works in both matlab and codegen
if ~isempty(eml.target)
    str = eml_tolower(str);
else
    str = lower(str);
end
