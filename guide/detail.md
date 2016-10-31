##Statistical Analysis

If there were enough information, maybe it would be possible to
predict a person's MBTI type given their base astrological
information. We could use celebrity birthdates and MBTI types (since
both are known for some celebrities) to train the model, and then
using simple birthdate, we would attempt to predict an MBTI
output. Good resuls obtained here could prove that a pseudoscience -
science connection exists, and that could mean pseudoscience might not
be so pseudo after all.

Unfortunately we do not have enough data for this. We do have an idea
how to go about modeling this data however:

In order to identify an MBTI type, top two functions are sufficient,
for example NTP can be predicted if we know Ne and Ti are top two
functions. A simple binary classifier could be trained for both of
these either / or functions. The only remaining task then would be
predicting introversion or extroversion which only _changes_ the order
of the top two functions -- ENTP has Ne and Ti whereas INTP is Ti and
Ne. That could be another binary classifier perhaps. But even without
it, the top two function identification would be useful.

I ran a simple logistic regression on Ti being 1/0 for example against
spiller, chinese, and millman, almost all variables were
insignificant. Adjusted R^2 was around 10%, but we were not satisfied
with this.

## Data

All data files required for ML are under 'data' folder. If one wants
to recreate the main file for training on celebrities, rerunning
`mineprep.py` is enough.

Script `scrape_mbti.py` will take celebrity mbti types from a known Web
site, and write its output under /tmp. I already ran this once, copied
its output under data. This is the main data I used to train the
regressor. The file is `data/myer-briggs.txt`, then once mineprep.py
ran it creates the necessary file.

Any manual additions to the celebrity MBTI data should go in
`data/myer-briggs-app.txt` - everything in this file will be appended
to the original file before training file is created by `mineprep.py`.


