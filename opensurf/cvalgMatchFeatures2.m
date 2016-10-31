function [index_pairs, match_metric] = cvalgMatchFeatures2(features1in, ...
                                       features2in, metric, ...
                                       match_percentage, method, ...
                                       maxRatioThreshold, isPrenormalized)

output_class = 'single';

% Determine number of feature vectors in each set
N1 = cast(size(features1in, 2), 'uint32');
N2 = cast(size(features2in, 2), 'uint32');
if (N1 == 0 || N2 == 0)
    % Create 2x0 empty matrix to provide equivalence between MATLAB and
    % MATLAB Coder
    index_pairs = zeros(2, 0, 'uint32');
    match_metric = zeros(0, 0, output_class);
    return
end

% For the Hamming distance, features themselves must remain uint8 and
% normalization cannot be used.
features1 = cast(features1in, output_class);
features2 = cast(features2in, output_class);

if ~isPrenormalized
  [features1, features2] = normalizeFeatures(features1, features2, ...
					     method, metric);
end

% Generate correspondence metric matrix
% Generate correspondence matrix using Sum of Squared Differences
scores = metricSSD(features1, features2, N1, N2, output_class);

% Convert match threshold percent to a numeric threshold
match_threshold = percentToLevel(match_percentage, size(features1, 1), ...
    metric, output_class);

% Find matches
[index_pairs, match_metric] = findMatches(scores, N1, N2, metric, ...
    method, maxRatioThreshold, match_threshold);

%==========================================================================
% Normalize features to be unit vectors
%==========================================================================
function [features1, features2] = normalizeFeatures(features1, features2, ...
    method, metric)

% Convert feature vectors to unit vectors
features1 = normalizeX(features1);
features2 = normalizeX(features2);

%==========================================================================
% Convert match threshold percent to an numeric threshold
%==========================================================================
function match_threshold = percentToLevel(match_percentage, ...
    vector_length, metric, output_class)

  match_percentage = cast(match_percentage, output_class);
  vector_length = cast(vector_length, output_class);

  disp(metric);
  disp('again');

  max_val = cast(4, output_class);
  
  match_threshold = (match_percentage*cast(0.01, output_class))*max_val;
  

        
%==========================================================================
% Find corresponding features
%==========================================================================
function [index_pairs, match_metric] = findMatches(scores, N1, N2,...
    metric, method, maxRatioThreshold, match_threshold)
% SCORES is an N1-by-N2 correspondence metric matrix where the rows
% correspond to the feature vectors in FEATURES1, and the columns
% correspond to the feature vectors in FEATURES2.

  if size(scores, 2) > 1
    [index_pairs, match_metric] = findMatchesNNRatio(scores,...
						     metric, maxRatioThreshold);
    [index_pairs, match_metric] = removeWeakMatches(index_pairs,...
						    match_metric, match_threshold, metric);
  else
    % If FEATURES2 contains only 1 feature, we cannot use ratio.
    % Use NearestNeighborSymmetric instead, resulting in a single
    % match
    [index_pairs, match_metric] = findMatchesNN(scores, ...
						metric, match_threshold);
  end

%==========================================================================
% Find matches using Nearest-Neighbor strategy
%==========================================================================
function [index_pairs, match_metric] = findMatchesNN(scores, ...
    metric, match_threshold)
% Find the maximum(minimum) entry in scores.
% Make it a match.
% Eliminate the corresponding row and the column.
% Repeat.

nRows = size(scores, 1);
nCols = size(scores, 2);
nMatches = min(nRows, nCols);
index_pairs = zeros([2, nMatches], 'uint32');
match_metric = zeros(1, nMatches, 'like', scores);

useMax = strcmp(metric, 'normxcorr');

for i = 1:nMatches
    if useMax
        [match_metric(i), ind] = max(scores(:));
        [r, c] = ind2sub(size(scores), ind);
    else
        [match_metric(i), ind] = min(scores(:));
        [r, c] = ind2sub(size(scores), ind);
    end
    
    index_pairs(:, i) = [r, c];
    if useMax
        scores(r, :) = -inf(class(scores));
        scores(:, c) = -inf(class(scores));
    else
        scores(r, :) = inf(class(scores));
        scores(:, c) = inf(class(scores));
    end
end

[index_pairs, match_metric] = removeWeakMatches(index_pairs, ...
    match_metric, match_threshold, metric);

%==========================================================================
% Find matches using David Lowe's disambiguation strategy
%==========================================================================
function [index_pairs, match_metric] = findMatchesNNRatio(scores, ...
    metric, maxRatioThreshold)

if strcmp(metric, 'normxcorr')
    [values, col_indices] = partialSort(scores, 2, 'descend');
    
    % If the metric is 'normxcorr', then the scores are cosines
    % of the angles between the feature vectors. 
    % The ratio of the angles is an approximation of the ratio of
    % euclidean distances.  See David Lowe's demo code.
    
    values(values > 1) = 1; % prevent complex angles
    rvalues = acos(values);
else
    [values, col_indices] = partialSort(scores, 2, 'ascend');
    rvalues = values;
end

% handle division by effective zero
zeroInds = rvalues(2, :) < 1e-6;
rvalues(:, zeroInds) = 1;
ratios = rvalues(1, :) ./ rvalues(2, :);

unambiguousIndices = ratios <= maxRatioThreshold;
match_metric = values(1, unambiguousIndices);
col_indices = col_indices(1, unambiguousIndices);
row_indices = 1:size(scores, 1);
index_pairs = [row_indices(1, unambiguousIndices); col_indices];
        
%==========================================================================
% returns sorted top n elements of each row of a matrix 
%==========================================================================
function [values, indices] = partialSort(x, n, mode)
% X is the matrix
% N is the number of top elements to return
% MODE can be 'ascend' or 'descend', default 'ascend'

if n > size(x, 2), n = size(x, 2); end;
if nargin < 3, mode = 'ascend'; end;

values = zeros(n, size(x, 1), 'like', x);
indices = values;

if strcmp(mode, 'ascend')
    for i = 1:n
        [values(i, :), indices(i, :)] = min(x, [], 2);
        inds = sub2ind(size(x), 1:size(x, 1), indices(i, :));
        x(inds) = inf;
    end
else
    for i = 1:n
        [values(i, :), indices(i, :)] = max(x, [], 2);
        inds = sub2ind(size(x), 1:size(x, 1), indices(i, :));
        x(inds) = -inf;
    end
end
indices = cast(indices, 'uint32');


%==========================================================================
% Generate correspondence metric matrix using Sum of Squared Differences
%==========================================================================
function scores = metricSSD(features1, features2, N1, N2, output_class)

% Need to obtain feature vector length to perform explicit row indexing 
% needed for code generation of variable sized inputs

% call optimized builtin function
scores = zeros(N1, N2, output_class);
scores(:,:) = visionSSDMetric(features1,features2);


%==========================================================================
% Remove weak matches
%==========================================================================
function [indices, match_metric] = removeWeakMatches(indices, ...
    match_metric, match_threshold, metric)

inds = match_metric <= match_threshold;

indices = indices(:, inds);

if isempty(indices)
    indices = zeros(2, 0, 'uint32');
    match_metric = zeros(0, 0, 'like', match_metric);
else
    match_metric = match_metric(:, inds);    
end

%==========================================================================
% Normalize the columns in X to have unit norm.
%==========================================================================
function X = normalizeX(X)
Xnorm = sqrt(sum(X.^2, 1));
X = bsxfun(@rdivide, X, Xnorm);

% Set effective zero length vectors to zero
X(:, (Xnorm <= eps(single(1))) ) = 0;

