# Nomadic Terrain

A Flask application that has useful features for campers, on-the-go
travelers. To start simply run `main.py`.

### Installation

- Install all packages under `requirements.txt`

- Need a `.owm` file which has your Open Weather Map API key in it
  (simple string without newline).  It is easy to get, register to OWM
  for free, and API key will be generated.

- For the recoll section to work, place a symlink from `static` to the
  top folder of your books. This folder should be set for `book_dir` in
  the config. Also install recoll's python extensions (download recoll sources,
  go to `python` folder, run setup there).

An example `nomterr.conf` file looks like

```
{
  "guide_detail_dir": "/home/burak/Documents/kod/guide/doc/details",
  "spiller_pdf": "/home/burak/Documents/kod/nomadicterrain/static/spiller.json",
  "weatherapi": "[API]",
  "btype": "/home/burak/Documents/kod/guide/data",
  "hay": "/home/burak/Documents/kod/guide/doc",
  "quandl": "[API]",
  "celeb": "/home/burak/Documents/kod/guide/data/famousbday.txt",
  "book_dir": "file:///home/burak/Documents"
}
```

