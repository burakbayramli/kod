I1 = imread('/home/burak/Downloads/pcv_data/data/alcatraz1.pgm');
I2 = imread('/home/burak/Downloads/pcv_data/data/alcatraz2.pgm');

points1 = detectSURFFeatures(I1);
points2 = detectSURFFeatures(I2);

[f1, vpts1] = extractFeatures(I1, points1);
[f2, vpts2] = extractFeatures(I2, points2);

indexPairs = matchFeatures2(f1, f2) ;
matchedPoints1 = vpts1(indexPairs(:, 1));
matchedPoints2 = vpts2(indexPairs(:, 2));

A = matchedPoints1.Location;
save('mp1','A');
A = matchedPoints2.Location;
save('mp2','A');
A = indexPairs(:, 1);
save('ip11','A');
A = indexPairs(:, 2);
save('ip12','A');

f=figure;
showMatchedFeatures2(I1,I2,matchedPoints1,matchedPoints2);
saveas(f, '/tmp/out.png')
