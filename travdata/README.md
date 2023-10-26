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
    "campgrounds": {
	"camping sandaya": [48.94080318, 2.14573]
    },
    "maps": [
	"dcrvbb-17-paris-champs-elysees-la-defense.gpx",
	"paris-secrets.gpx"
     ]	
}
```

The `center` is where the map will be centered, points are interesting
points for your journey. Restaurants and campgrounds are
self-explanatory, the seperate categories will be marked with
different colors

The file can make references to outside GPX maps for paths, these
files should reside in the same directory as the main file. The GPX
format is a standard format, many travel sites such as Wikiloc offers
downloading their free paths shared by other users in this format. 

