### Travel Planner

The planner can view travel plans all recorded in a JSON file. The format
of the file looks like this

```
{
    "center": [48.857, 2.33],
    "points": {
	"notre-dame": [48.8539424, 2.348357]
    },
    "restaurants": {
	"Jaja Rueil-Malmaison": [48.86500019, 2.20633412]
    },
    "cafes": {
        "Starbucks 1": [48.2221,2.211881]
    },
    "campgrounds": {
	"camping sandaya": [48.94080318, 2.14573]
    },
    "maps": [
	"dcrvbb-17-paris-champs-elysees-la-defense.gpx",
	"paris-secrets.gpx"
     ]	
}
```

The user has to upload a file, like the one above, to a server
accessible from outside (which will not cause the infamous CORS
error). Github allows such access. Let's say you are user `robjohn` on
Github, and you have a repo called `myrepo3`. A travel json you check
in to that repo under `canada` subfolder would be accessible as
`https://raw.githubusercontent.com/robjohn/myrepo3/master/canada/index.json`

The `center` is where the map will be centered, points are interesting
points for your journey. Restaurants and campgrounds are
self-explanatory, the seperate categories will be marked with
different colors.

The main file can make references to outside GPX maps for travel
paths, if a bare file name is specified like above, it will be assumed
the file resides in the same directory as the main file. If not the code
attempts to connect to the URL as-is.

The GPX format used is pretty standard, many travel sites such as
Wikiloc offers GPX files in this format. 

